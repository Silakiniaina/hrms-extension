package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.SalaryStats;
import mg.hrms.models.EmployeeSalaryDetail;
import mg.hrms.models.SalaryComponent;
import mg.hrms.models.User;
import mg.hrms.models.SalarySlip;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryStatsService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStatsService.class);
    private static final int PAGE_SIZE = 200;
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;
    private final SalarySlipService salarySlipService;

    public SalaryStatsService(RestApiService restApiService, ObjectMapper objectMapper, SalarySlipService salarySlipService) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
        this.salarySlipService = salarySlipService;
    }

    public List<SalaryStats> getMonthlySalaryStats(User user, String year) throws Exception {
        logger.info("Fetching monthly salary stats for year: {}", year);

        String[] fields = {"name", "posting_date", "gross_pay", "net_pay"};
        List<String[]> filters = new ArrayList<>();

        // Filter by year if provided
        if (year != null && !year.isEmpty()) {
            filters.add(new String[]{"posting_date", ">=", year + "-01-01"});
            filters.add(new String[]{"posting_date", "<=", year + "-12-31"});
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
                logger.warn("No salary data found for year: {}", year);
                break;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> salaryData = (List<Map<String, Object>>) response.getBody().get("data");
            allSalaryData.addAll(salaryData);

            // Check if more data exists
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

            // Fetch detailed salary slip using SalarySlipService.getById
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

            // Process earnings and deductions from detailed slip
            processComponents(detailedSlip, "earnings", stats, true);
            processComponents(detailedSlip, "deductions", stats, false);

            monthlyStatsMap.put(monthKey, stats);
        }

        // Convert to list and sort by year-month
        List<SalaryStats> result = new ArrayList<>(monthlyStatsMap.values());
        result.sort((a, b) -> {
            String keyA = a.getYear() + "-" + a.getMonth();
            String keyB = b.getYear() + "-" + b.getMonth();
            return keyA.compareTo(keyB);
        });

        logger.info("Retrieved {} monthly salary stats for year: {}", result.size(), year);
        return result;
    }

    private void processComponents(SalarySlip slip, String componentType, SalaryStats stats, boolean isEarning) {
        List<SalaryComponent> components = componentType.equals("earnings") ? slip.getEarnings() : slip.getDeductions();

        if (components == null) return;

        List<SalaryComponent> existingComponents = isEarning ? stats.getEarningsDetails() : stats.getDeductionsDetails();
        if (existingComponents == null) {
            existingComponents = new ArrayList<>();
            if (isEarning) {
                stats.setEarningsDetails(existingComponents);
            } else {
                stats.setDeductionsDetails(existingComponents);
            }
        }

        for (SalaryComponent comp : components) {
            String name = comp.getName();
            Double amount = getDoubleValue(comp.getAmount());

            if (name != null && amount != null && amount > 0) {
                // Find existing component or create new one
                SalaryComponent existingComp = existingComponents.stream()
                    .filter(c -> name.equals(c.getName()))
                    .findFirst()
                    .orElse(null);

                if (existingComp != null) {
                    existingComp.setAmount(existingComp.getAmount() + amount);
                } else {
                    SalaryComponent newComp = new SalaryComponent();
                    newComp.setName(name);
                    newComp.setType(isEarning ? "Earning" : "Deduction");
                    newComp.setAmount(amount);
                    existingComponents.add(newComp);
                }

                // Update total deductions
                if (!isEarning) {
                    stats.setTotalDeductions(stats.getTotalDeductions() + amount);
                }
            }
        }
    }

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

    public List<String> getAvailableYears(User user) throws Exception {
        logger.info("Fetching available years for salary statistics");

        String[] fields = {"posting_date"};
        List<String[]> filters = new ArrayList<>();
        filters.add(new String[]{"docstatus", "=", "1"});

        // Initialize variables for pagination
        int start = 0;
        Set<String> years = new HashSet<>();
        boolean hasMoreData = true;

        // Fetch data in pages until no more data is available
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

            // Check if more data exists
            if (salaryData.size() < PAGE_SIZE) {
                hasMoreData = false;
            } else {
                start += PAGE_SIZE;
            }
        }

        List<String> sortedYears = new ArrayList<>(years);
        Collections.sort(sortedYears, Collections.reverseOrder());

        logger.info("Found {} available years for salary statistics", sortedYears.size());
        return sortedYears;
    }

    // public List<EmployeeSalaryDetail> getEmployeeSalaryDetails(User user, String year, String month) throws Exception {
    //     logger.info("Fetching employee salary details for {}-{}", year, month);
    //
    //     String[] fields = {"name", "employee", "employee_name", "posting_date", "gross_pay", "net_pay"};
    //     List<String[]> filters = new ArrayList<>();
    //
    //     // Filter by year-month
    //     if (year != null && month != null) {
    //         String startDate = year + "-" + String.format("%02d", Integer.parseInt(month)) + "01-01";
    //         String endDate = year + "-" + String.format("%02d", Integer.parseInt(month)) + "12-31";
    //         filters.add(new String[]{"posting_date", ">=", startDate});
    //         filters.add(new String[]{"posting_date", "<=", endDate});
    //     }
    //
    //     // Only include submitted salary slips
    //     filters.add(new String[]{"docstatus", "=", "1"});
    //
    //     // Initialize variables for pagination
    //     int start = 0;
    //     List<Map<String, Object>> allSalaryData = new ArrayList<>();
    //     boolean hasMoreData = true;
    //
    //     // Fetch data in pages until no more data is available
    //     while (hasMoreData) {
    //         String apiUrl = restApiService.buildUrl("Salary Slip", fields, filters)
    //                 + "&limit_start=" + start + "&limit_page_length=" + PAGE_SIZE;
    //
    //         var response = restApiService.executeApiCall(
    //                 apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});
    //
    //         if (response.getBody() == null || response.getBody().get("data") == null) {
    //             logger.warn("No salary data found for {}-{}", year, month);
    //             break;
    //         }
    //
    //         @SuppressWarnings("unchecked")
    //         List<Map<String, Object>> salaryData = (List<Map<String, Object>>) response.getBody().get("data");
    //         allSalaryData.addAll(salaryData);
    //
    //         // Check if more data exists
    //         if (salaryData.size() < PAGE_SIZE) {
    //             hasMoreData = false;
    //         } else {
    //             start += PAGE_SIZE;
    //         }
    //     }
    //
    //     // Group by employee and calculate details
    //     Map<String, EmployeeSalaryDetail> employeeDetailsMap = new HashMap<>();
    //
    //     for (Map<String, Object> slip : allSalaryData) {
    //         String slipId = (String) slip.get("name");
    //         String employeeId = (String) slip.get("employee");
    //         String employeeName = (String) slip.get("employee_name");
    //
    //         if (slipId == null || employeeId == null) continue;
    //
    //         // Fetch detailed salary slip
    //         SalarySlip detailedSlip = salarySlipService.getById(user, slipId);
    //         if (detailedSlip == null) {
    //             logger.warn("Failed to fetch detailed salary slip for ID: {}", slipId);
    //             continue;
    //         }
    //
    //         EmployeeSalaryDetail detail = employeeDetailsMap.getOrDefault(employeeId, new EmployeeSalaryDetail());
    //         detail.setEmployeeId(employeeId);
    //         detail.setEmployeeName(employeeName);
    //         detail.setYear(year);
    //         detail.setMonth(month);
    //
    //         // Initialize totals if null
    //         if (detail.getTotalGrossPay() == null) detail.setTotalGrossPay(0.0);
    //         if (detail.getTotalNetPay() == null) detail.setTotalNetPay(0.0);
    //         if (detail.getTotalDeductions() == null) detail.setTotalDeductions(0.0);
    //
    //         // Add values
    //         Double grossPay = getDoubleValue(detailedSlip.getGrossPay());
    //         Double netPay = getDoubleValue(detailedSlip.getNetPay());
    //
    //         detail.setTotalGrossPay(detail.getTotalGrossPay() + grossPay);
    //         detail.setTotalNetPay(detail.getTotalNetPay() + netPay);
    //
    //         // Process earnings and deductions
    //         processEmployeeComponents(detailedSlip, "earnings", detail, true);
    //         processEmployeeComponents(detailedSlip, "deductions", detail, false);
    //
    //         // Add salary slip reference
    //         if (detail.getSalarySlips() == null) {
    //             detail.setSalarySlips(new ArrayList<>());
    //         }
    //         detail.getSalarySlips().add(detailedSlip);
    //
    //         employeeDetailsMap.put(employeeId, detail);
    //     }
    //
    //     // Convert to list and sort by employee name
    //     List<EmployeeSalaryDetail> result = new ArrayList<>(employeeDetailsMap.values());
    //     result.sort((a, b) -> {
    //         if (a.getEmployeeName() == null) return 1;
    //         if (b.getEmployeeName() == null) return -1;
    //         return a.getEmployeeName().compareTo(b.getEmployeeName());
    //     });
    //
    //     logger.info("Retrieved {} employee salary details for {}-{}", result.size(), year, month);
    //     return result;
    // }

    public List<EmployeeSalaryDetail> getEmployeeSalaryDetails(User user, String year, String month) throws Exception {
        logger.info("Fetching employee salary details for {}-{}", year, month);

        String[] fields = {"name", "employee", "employee_name", "posting_date", "gross_pay", "net_pay"};
        List<String[]> filters = new ArrayList<>();

        // Filter by year-month with proper date range
        if (year != null && month != null) {
            try {
                int yearInt = Integer.parseInt(year);
                int monthInt = Integer.parseInt(month);

                // Use YearMonth to calculate valid start and end dates
                YearMonth yearMonth = YearMonth.of(yearInt, monthInt);
                String startDate = yearMonth.atDay(1).toString(); // First day of the month
                String endDate = yearMonth.atEndOfMonth().toString(); // Last day of the month

                filters.add(new String[]{"posting_date", ">=", startDate});
                filters.add(new String[]{"posting_date", "<=", endDate});
            } catch (NumberFormatException e) {
                logger.error("Invalid year or month format: year={}, month={}", year, month, e);
                throw new Exception("Invalid year or month format", e);
            } catch (Exception e) {
                logger.error("Error calculating date range for year={}, month={}", year, month, e);
                throw new Exception("Error calculating date range", e);
            }
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
                logger.warn("No salary data found for {}-{}", year, month);
                break;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> salaryData = (List<Map<String, Object>>) response.getBody().get("data");
            allSalaryData.addAll(salaryData);

            // Check if more data exists
            if (salaryData.size() < PAGE_SIZE) {
                hasMoreData = false;
            } else {
                start += PAGE_SIZE;
            }
        }

        // Group by employee and calculate details
        Map<String, EmployeeSalaryDetail> employeeDetailsMap = new HashMap<>();

        for (Map<String, Object> slip : allSalaryData) {
            String slipId = (String) slip.get("name");
            String employeeId = (String) slip.get("employee");
            String employeeName = (String) slip.get("employee_name");

            if (slipId == null || employeeId == null) continue;

            // Fetch detailed salary slip
            SalarySlip detailedSlip = salarySlipService.getById(user, slipId);
            if (detailedSlip == null) {
                logger.warn("Failed to fetch detailed salary slip for ID: {}", slipId);
                continue;
            }

            EmployeeSalaryDetail detail = employeeDetailsMap.getOrDefault(employeeId, new EmployeeSalaryDetail());
            detail.setEmployeeId(employeeId);
            detail.setEmployeeName(employeeName);
            detail.setYear(year);
            detail.setMonth(month);

            // Initialize totals if null
            if (detail.getTotalGrossPay() == null) detail.setTotalGrossPay(0.0);
            if (detail.getTotalNetPay() == null) detail.setTotalNetPay(0.0);
            if (detail.getTotalDeductions() == null) detail.setTotalDeductions(0.0);

            // Add values
            Double grossPay = getDoubleValue(detailedSlip.getGrossPay());
            Double netPay = getDoubleValue(detailedSlip.getNetPay());

            detail.setTotalGrossPay(detail.getTotalGrossPay() + grossPay);
            detail.setTotalNetPay(detail.getTotalNetPay() + netPay);

            // Process earnings and deductions
            processEmployeeComponents(detailedSlip, "earnings", detail, true);
            processEmployeeComponents(detailedSlip, "deductions", detail, false);

            // Add salary slip reference
            if (detail.getSalarySlips() == null) {
                detail.setSalarySlips(new ArrayList<>());
            }
            detail.getSalarySlips().add(detailedSlip);

            employeeDetailsMap.put(employeeId, detail);
        }

        // Convert to list and sort by employee name
        List<EmployeeSalaryDetail> result = new ArrayList<>(employeeDetailsMap.values());
        result.sort((a, b) -> {
            if (a.getEmployeeName() == null) return 1;
            if (b.getEmployeeName() == null) return -1;
            return a.getEmployeeName().compareTo(b.getEmployeeName());
        });

        logger.info("Retrieved {} employee salary details for {}-{}", result.size(), year, month);
        return result;
    }

    private void processEmployeeComponents(SalarySlip slip, String componentType, EmployeeSalaryDetail detail, boolean isEarning) {
        List<SalaryComponent> components = componentType.equals("earnings") ? slip.getEarnings() : slip.getDeductions();

        if (components == null) return;

        List<SalaryComponent> existingComponents = isEarning ? detail.getEarningsDetails() : detail.getDeductionsDetails();
        if (existingComponents == null) {
            existingComponents = new ArrayList<>();
            if (isEarning) {
                detail.setEarningsDetails(existingComponents);
            } else {
                detail.setDeductionsDetails(existingComponents);
            }
        }

        for (SalaryComponent comp : components) {
            String name = comp.getName();
            Double amount = getDoubleValue(comp.getAmount());

            if (name != null && amount != null && amount > 0) {
                // Find existing component or create new one
                SalaryComponent existingComp = existingComponents.stream()
                    .filter(c -> name.equals(c.getName()))
                    .findFirst()
                    .orElse(null);

                if (existingComp != null) {
                    existingComp.setAmount(existingComp.getAmount() + amount);
                } else {
                    SalaryComponent newComp = new SalaryComponent();
                    newComp.setName(name);
                    newComp.setType(isEarning ? "Earning" : "Deduction");
                    newComp.setAmount(amount);
                    existingComponents.add(newComp);
                }

                if (!isEarning) {
                    detail.setTotalDeductions(detail.getTotalDeductions() + amount);
                }
            }
        }
    }
}
