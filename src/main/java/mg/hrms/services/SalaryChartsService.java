package mg.hrms.services;

import mg.hrms.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryChartsService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryChartsService.class);
    private static final int PAGE_SIZE = 200;
    private final RestApiService restApiService;
    private final SalarySlipService salarySlipService;

    public SalaryChartsService(RestApiService restApiService, SalarySlipService salarySlipService) {
        this.restApiService = restApiService;
        this.salarySlipService = salarySlipService;
    }

    /* -------------------------------------------------------------------------- */
    /*                       Fetch salary stats for a period                      */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("null")
    public List<SalaryStats> getSalaryStatsForPeriod(User user, String startYear, String endYear) throws Exception {
        logger.info("Fetching salary stats for period: {} to {}", startYear, endYear);

        String[] fields = {"name", "posting_date", "gross_pay", "net_pay"};
        List<String[]> filters = new ArrayList<>();

        // Filter by date range
        if (startYear != null && !startYear.isEmpty()) {
            filters.add(new String[]{"posting_date", ">=", startYear + "-01-01"});
        }
        if (endYear != null && !endYear.isEmpty()) {
            filters.add(new String[]{"posting_date", "<=", endYear + "-12-31"});
        }

        // Only include submitted salary slips
        filters.add(new String[]{"docstatus", "=", "1"});

        // Initialize variables for pagination
        int start = 0;
        List<Map<String, Object>> allSalaryData = new ArrayList<>();
        boolean hasMoreData = true;

        // Fetch data in pages until no more data is available
        while (hasMoreData) {
            String apiUrl = restApiService.buildUrl("Salary Slip", fields, filters)
                    + "&limit_start=" + start + "&limit_page_length=" + PAGE_SIZE;

            var response = restApiService.executeApiCall(
                    apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getBody() == null || response.getBody().get("data") == null) {
                logger.warn("No salary data found for period: {} to {}", startYear, endYear);
                break;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> salaryData = (List<Map<String, Object>>) response.getBody().get("data");
            allSalaryData.addAll(salaryData);

            if (salaryData.size() < PAGE_SIZE) {
                hasMoreData = false;
            } else {
                start += PAGE_SIZE;
            }
        }

        // Group by month and calculate statistics
        Map<String, SalaryStats> monthlyStatsMap = new HashMap<>();

        for (Map<String, Object> slip : allSalaryData) {
            String slipId = (String) slip.get("name");
            if (slipId == null) continue;

            // Fetch detailed salary slip
            SalarySlip detailedSlip = salarySlipService.getById(user, slipId);
            if (detailedSlip == null) {
                logger.warn("Failed to fetch detailed salary slip for ID: {}", slipId);
                continue;
            }

            String postingDate = detailedSlip.getPostingDate().toString();
            if (postingDate == null || postingDate.length() < 7) continue;

            String slipYear = postingDate.substring(0, 4);
            String month = postingDate.substring(5, 7);
            String monthKey = slipYear + "-" + month;

            SalaryStats stats = monthlyStatsMap.getOrDefault(monthKey, new SalaryStats());
            stats.setYear(slipYear);
            stats.setMonth(month);

            // Initialize totals if null
            if (stats.getTotalGrossPay() == null) stats.setTotalGrossPay(0.0);
            if (stats.getTotalNetPay() == null) stats.setTotalNetPay(0.0);
            if (stats.getTotalDeductions() == null) stats.setTotalDeductions(0.0);

            // Add gross pay and net pay
            Double grossPay = getDoubleValue(detailedSlip.getGrossPay());
            Double netPay = getDoubleValue(detailedSlip.getNetPay());

            stats.setTotalGrossPay(stats.getTotalGrossPay() + grossPay);
            stats.setTotalNetPay(stats.getTotalNetPay() + netPay);

            // Process components
            processComponents(detailedSlip, stats);

            monthlyStatsMap.put(monthKey, stats);
        }

        // Convert to list and sort by year-month
        List<SalaryStats> result = new ArrayList<>(monthlyStatsMap.values());
        result.sort((a, b) -> {
            String keyA = a.getYear() + "-" + String.format("%02d", Integer.parseInt(a.getMonth()));
            String keyB = b.getYear() + "-" + String.format("%02d", Integer.parseInt(b.getMonth()));
            return keyA.compareTo(keyB);
        });

        logger.info("Retrieved {} monthly salary stats for period: {} to {}", result.size(), startYear, endYear);
        return result;
    }

    /* -------------------------------------------------------------------------- */
    /*                     Get salary chart data for a period                     */
    /* -------------------------------------------------------------------------- */
    public SalaryChartData getSalaryChartData(User user, String startYear, String endYear) throws Exception {
        logger.info("Generating salary chart data for period: {} to {}", startYear, endYear);

        List<SalaryStats> stats = getSalaryStatsForPeriod(user, startYear, endYear);

        SalaryChartData chartData = new SalaryChartData();

        if (stats == null || stats.isEmpty()) {
            logger.warn("No salary stats found for period: {} to {}", startYear, endYear);
            chartData.setLabels(new ArrayList<>());
            chartData.setDatasets(new ArrayList<>());
            return chartData;
        }

        // Generate labels (Month Year format)
        List<String> labels = stats.stream()
                .map(s -> s.getMonthName() + " " + s.getYear())
                .collect(Collectors.toList());
        chartData.setLabels(labels);

        // Generate datasets
        List<SalaryChartData.ChartDataset> datasets = new ArrayList<>();

        // Gross Pay dataset
        List<Double> grossPayData = stats.stream()
                .map(s -> {
                    Double grossPay = s.getTotalGrossPay();
                    logger.debug("Gross pay for {}: {}", s.getMonthName() + " " + s.getYear(), grossPay);
                    return grossPay != null ? grossPay : 0.0;
                })
                .collect(Collectors.toList());

        // Log the data for debugging
        logger.info("Gross pay data: {}", grossPayData);

        datasets.add(new SalaryChartData.ChartDataset(
                "Gross Pay", grossPayData, "rgb(75, 192, 192)", "rgba(75, 192, 192, 0.2)"
        ));

        // Net Pay dataset
        List<Double> netPayData = stats.stream()
                .map(s -> {
                    Double netPay = s.getTotalNetPay();
                    return netPay != null ? netPay : 0.0;
                })
                .collect(Collectors.toList());

        logger.info("Net pay data: {}", netPayData);

        datasets.add(new SalaryChartData.ChartDataset(
                "Net Pay", netPayData, "rgb(54, 162, 235)", "rgba(54, 162, 235, 0.2)"
        ));

        // Deductions dataset
        List<Double> deductionsData = stats.stream()
                .map(s -> {
                    Double deductions = s.getTotalDeductions();
                    return deductions != null ? deductions : 0.0;
                })
                .collect(Collectors.toList());

        logger.info("Deductions data: {}", deductionsData);

        datasets.add(new SalaryChartData.ChartDataset(
                "Deductions", deductionsData, "rgb(255, 99, 132)", "rgba(255, 99, 132, 0.2)"
        ));

        chartData.setDatasets(datasets);

        logger.info("Generated salary chart data with {} labels and {} datasets",
                labels.size(), datasets.size());
        return chartData;
    }

    /* -------------------------------------------------------------------------- */
    /*                Get salary component chart data for a period.               */
    /* -------------------------------------------------------------------------- */
    public SalaryChartData getSalaryComponentsChartData(User user, String startYear, String endYear) throws Exception {
        logger.info("Generating salary components chart data for period: {} to {}", startYear, endYear);

        List<SalaryStats> stats = getSalaryStatsForPeriod(user, startYear, endYear);

        SalaryChartData chartData = new SalaryChartData();

        if (stats == null || stats.isEmpty()) {
            logger.warn("No salary stats found for components chart for period: {} to {}", startYear, endYear);
            chartData.setLabels(new ArrayList<>());
            chartData.setDatasets(new ArrayList<>());
            return chartData;
        }

        // Generate labels
        List<String> labels = stats.stream()
                .map(s -> s.getMonthName() + " " + s.getYear())
                .collect(Collectors.toList());
        chartData.setLabels(labels);

        // Collect all unique component names
        Set<String> allEarningsComponents = new HashSet<>();
        Set<String> allDeductionsComponents = new HashSet<>();

        for (SalaryStats stat : stats) {
            if (stat.getEarningsDetails() != null) {
                allEarningsComponents.addAll(stat.getEarningsDetails().stream()
                        .map(SalaryComponent::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
            }
            if (stat.getDeductionsDetails() != null) {
                allDeductionsComponents.addAll(stat.getDeductionsDetails().stream()
                        .map(SalaryComponent::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
            }
        }

        // Limiter le nombre de composants pour éviter le crash
        List<String> limitedEarnings = allEarningsComponents.stream()
                .limit(5)
                .collect(Collectors.toList());
        List<String> limitedDeductions = allDeductionsComponents.stream()
                .limit(5)
                .collect(Collectors.toList());

        List<SalaryChartData.ChartDataset> datasets = new ArrayList<>();

        // Define colors for components
        String[] earningsColors = {
                "rgb(75, 192, 192)", "rgb(153, 102, 255)", "rgb(255, 159, 64)",
                "rgb(54, 162, 235)", "rgb(255, 205, 86)"
        };
        String[] deductionsColors = {
                "rgb(255, 99, 132)", "rgb(255, 159, 64)", "rgb(255, 205, 86)",
                "rgb(75, 192, 192)", "rgb(54, 162, 235)"
        };

        // Create datasets for earnings components
        int earningsColorIndex = 0;
        for (String componentName : limitedEarnings) {
            List<Double> componentData = new ArrayList<>();
            for (SalaryStats stat : stats) {
                double amount = 0.0;
                if (stat.getEarningsDetails() != null) {
                    amount = stat.getEarningsDetails().stream()
                            .filter(c -> componentName.equals(c.getName()))
                            .mapToDouble(SalaryComponent::getAmount)
                            .sum();
                }
                componentData.add(amount);
            }

            // Only add dataset if it has non-zero data
            if (componentData.stream().anyMatch(d -> d > 0)) {
                String color = earningsColors[earningsColorIndex % earningsColors.length];
                datasets.add(new SalaryChartData.ChartDataset(
                        componentName + " (Earning)", componentData, color,
                        color.replace("rgb", "rgba").replace(")", ", 0.2)")
                ));
                earningsColorIndex++;
            }
        }

        // Create datasets for deductions components
        int deductionsColorIndex = 0;
        for (String componentName : limitedDeductions) {
            List<Double> componentData = new ArrayList<>();
            for (SalaryStats stat : stats) {
                double amount = 0.0;
                if (stat.getDeductionsDetails() != null) {
                    amount = stat.getDeductionsDetails().stream()
                            .filter(c -> componentName.equals(c.getName()))
                            .mapToDouble(SalaryComponent::getAmount)
                            .sum();
                }
                componentData.add(amount);
            }

            // Only add dataset if it has non-zero data
            if (componentData.stream().anyMatch(d -> d > 0)) {
                String color = deductionsColors[deductionsColorIndex % deductionsColors.length];
                datasets.add(new SalaryChartData.ChartDataset(
                        componentName + " (Deduction)", componentData, color,
                        color.replace("rgb", "rgba").replace(")", ", 0.2)")
                ));
                deductionsColorIndex++;
            }
        }

        chartData.setDatasets(datasets);

        logger.info("Generated salary components chart data with {} earnings and {} deductions components",
                limitedEarnings.size(), limitedDeductions.size());
        return chartData;
    }

    /* -------------------------------------------------------------------------- */
    /*                     Get all available year for filters                     */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("null")
    public List<String> getAvailableYears(User user) throws Exception {
        logger.info("Fetching available years for salary charts");

        String[] fields = {"posting_date"};
        List<String[]> filters = new ArrayList<>();
        filters.add(new String[]{"docstatus", "=", "1"});

        int start = 0;
        Set<String> years = new HashSet<>();
        boolean hasMoreData = true;

        while (hasMoreData) {
            String apiUrl = restApiService.buildUrl("Salary Slip", fields, filters)
                    + "&limit_start=" + start + "&limit_page_length=" + PAGE_SIZE;

            var response = restApiService.executeApiCall(
                    apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getBody() == null || response.getBody().get("data") == null) {
                break;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> salaryData = (List<Map<String, Object>>) response.getBody().get("data");

            years.addAll(salaryData.stream()
                    .map(slip -> (String) slip.get("posting_date"))
                    .filter(Objects::nonNull)
                    .filter(date -> date.length() >= 4)
                    .map(date -> date.substring(0, 4))
                    .collect(Collectors.toSet()));

            if (salaryData.size() < PAGE_SIZE) {
                hasMoreData = false;
            } else {
                start += PAGE_SIZE;
            }
        }

        List<String> sortedYears = new ArrayList<>(years);
        Collections.sort(sortedYears, Collections.reverseOrder());

        logger.info("Found {} available years for salary charts", sortedYears.size());
        return sortedYears;
    }

    /* -------------------------------------------------------------------------- */
    /*                            Processing components                           */
    /* -------------------------------------------------------------------------- */
    private void processComponents(SalarySlip slip, SalaryStats stats) {
        // Process earnings
        if (slip.getEarnings() != null) {
            List<SalaryComponent> existingEarnings = stats.getEarningsDetails();
            if (existingEarnings == null) {
                existingEarnings = new ArrayList<>();
                stats.setEarningsDetails(existingEarnings);
            }
            
            for (SalaryComponent comp : slip.getEarnings()) {
                String name = comp.getName();
                Double amount = getDoubleValue(comp.getAmount());
                
                if (name != null && amount != null && amount > 0) {
                    SalaryComponent existingComp = existingEarnings.stream()
                        .filter(c -> name.equals(c.getName()))
                        .findFirst()
                        .orElse(null);

                    if (existingComp != null) {
                        existingComp.setAmount(existingComp.getAmount() + amount);
                    } else {
                        SalaryComponent newComp = new SalaryComponent();
                        newComp.setName(name);
                        newComp.setType("Earning");
                        newComp.setAmount(amount);
                        existingEarnings.add(newComp);
                    }
                }
            }
        }

        // Process deductions
        if (slip.getDeductions() != null) {
            List<SalaryComponent> existingDeductions = stats.getDeductionsDetails();
            if (existingDeductions == null) {
                existingDeductions = new ArrayList<>();
                stats.setDeductionsDetails(existingDeductions);
            }
            
            for (SalaryComponent comp : slip.getDeductions()) {
                String name = comp.getName();
                Double amount = getDoubleValue(comp.getAmount());
                
                if (name != null && amount != null && amount > 0) {
                    SalaryComponent existingComp = existingDeductions.stream()
                        .filter(c -> name.equals(c.getName()))
                        .findFirst()
                        .orElse(null);

                    if (existingComp != null) {
                        existingComp.setAmount(existingComp.getAmount() + amount);
                    } else {
                        SalaryComponent newComp = new SalaryComponent();
                        newComp.setName(name);
                        newComp.setType("Deduction");
                        newComp.setAmount(amount);
                        existingDeductions.add(newComp);
                    }
                    
                    stats.setTotalDeductions(stats.getTotalDeductions() + amount);
                }
            }
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                        Get double value of an object                       */
    /* -------------------------------------------------------------------------- */
    private Double getDoubleValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
