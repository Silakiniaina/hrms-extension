package mg.hrms.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.ParameterizedTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.User;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.SalarySummary;
import mg.hrms.utils.ApiUtils;

@Service
public class SalarySummaryService {

    private static final Logger logger = LoggerFactory.getLogger(SalarySummaryService.class);

    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private final RestApiService restApiService;

    @Autowired
    public SalarySummaryService(EmployeeService employeeService, ObjectMapper objectMapper, RestApiService restApiService) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
        this.restApiService = restApiService;
    }

    public List<SalarySummary> getMonthlySalarySummary(User user, String month, String year) throws Exception {
        // Fields we need from Salary Slip
        String[] fields = {
            "name", "employee", "employee_name", "posting_date",
            "gross_pay", "net_pay", "status"
        };

        List<String[]> filters = new ArrayList<>();
        filters.add(new String[]{"docstatus", "=", "1"}); // Only submitted salary slips

        // Date range filtering
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (year != null && !year.isEmpty()) {
                if (month != null && !month.isEmpty()) {
                    // Case 1: Both month and year selected - filter specific month/year
                    YearMonth yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
                    LocalDate startDate = yearMonth.atDay(1);
                    LocalDate endDate = yearMonth.atEndOfMonth();
                    filters.add(new String[]{"posting_date", ">=", startDate.format(dateFormatter)});
                    filters.add(new String[]{"posting_date", "<=", endDate.format(dateFormatter)});
                } else {
                    // Case 2: Only year selected - filter entire year
                    LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
                    LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);
                    filters.add(new String[]{"posting_date", ">=", startDate.format(dateFormatter)});
                    filters.add(new String[]{"posting_date", "<=", endDate.format(dateFormatter)});
                }
            } else if (month != null && !month.isEmpty()) {
                // Case 3: Only month selected - we'll filter after getting all records
                // No date filters added here - we'll get all records and filter in memory
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            logger.warn("Invalid month or year provided: month={}, year={}", month, year);
            throw new Exception("Invalid month or year format");
        }

        String apiUrl = ApiUtils.buildUrl(
            restApiService.getServerHost(),
            "Salary Slip",
            fields,
            filters
        );

        logger.info("Fetching salary slips for summary with URL: {}", apiUrl);

        var response = restApiService.executeApiCall(
            apiUrl,
            HttpMethod.GET,
            null,
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.warn("No salary slip data found");
            return new ArrayList<>();
        }

        // Convert to List of SalarySlip objects
        List<SalarySlip> salarySlips = objectMapper.convertValue(
            response.getBody().get("data"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, SalarySlip.class)
        );

        // Apply month filter in memory if only month was specified
        if (month != null && !month.isEmpty() && (year == null || year.isEmpty())) {
            salarySlips = salarySlips.stream()
                .filter(slip -> slip.getPostingDate() != null)
                .filter(slip -> {
                    LocalDate postingDate = slip.getPostingDate().toLocalDate();
                    return String.format("%02d", postingDate.getMonthValue()).equals(month);
                })
                .collect(Collectors.toList());
        }

        // Transform data for per-employee records
        return transformSalaryData(salarySlips);
    }

    private List<SalarySummary> transformSalaryData(List<SalarySlip> salarySlips) {
        List<SalarySummary> summaries = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (SalarySlip slip : salarySlips) {
            if (slip.getPostingDate() == null) continue;

            LocalDate postingDate = slip.getPostingDate().toLocalDate();
            SalarySummary summary = new SalarySummary();

            // Set basic fields
            summary.setMonth(postingDate.format(monthFormatter));
            summary.setYear(postingDate.format(yearFormatter));
            summary.setEmployeeName(slip.getEmployeeName());
            summary.setGrossPay(slip.getGrossPay() != null ? slip.getGrossPay() : 0.0);
            summary.setNetPay(slip.getNetPay() != null ? slip.getNetPay() : 0.0);

            // Calculate total deduction
            double gross = slip.getGrossPay() != null ? slip.getGrossPay() : 0.0;
            double net = slip.getNetPay() != null ? slip.getNetPay() : 0.0;
            summary.setTotalDeduction(gross - net);

            // Additional details for modal
            summary.setEmployeeId(slip.getEmployeeId());
            summary.setPostingDate(postingDate.format(dateFormatter));
            summary.setStatus(slip.getStatus());

            summaries.add(summary);
        }

        return summaries;
    }
}
