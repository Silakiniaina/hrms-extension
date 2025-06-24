package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.SalarySummary;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalarySummaryService {

    private static final Logger logger = LoggerFactory.getLogger(SalarySummaryService.class);
    private final FrappeService frappeService;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
    private final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");

    public SalarySummaryService(FrappeService frappeService, ObjectMapper objectMapper) {
        this.frappeService = frappeService;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /*                   Get the salary summary for an employee                   */
    /* -------------------------------------------------------------------------- */
    public List<SalarySummary> getMonthlySalarySummary(User user, String month, String year) throws Exception {
        logger.info("Fetching salary summary for month: {}, year: {}", month, year);
        String[] fields = {"name", "employee","employee_name", "posting_date", "gross_pay", "net_pay", "status"};
        List<String[]> filters = buildFilters(month, year);

        List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Salary Slip", fields, filters, user);

        if (response == null) {
            logger.warn("No salary slip data found for month: {}, year: {}", month, year);
            return new ArrayList<>();
        }

        List<SalarySlip> salarySlips = objectMapper.convertValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalarySlip.class)
        );

        if (month != null && !month.isEmpty() && (year == null || year.isEmpty())) {
            salarySlips = salarySlips.stream()
                    .filter(slip -> slip.getPostingDate() != null)
                    .filter(slip -> slip.getPostingDate().toLocalDate().format(monthFormatter).equals(month))
                    .collect(Collectors.toList());
        }

        List<SalarySummary> summaries = transformSalaryData(salarySlips);
        logger.info("Retrieved {} salary summaries for month: {}, year: {}", summaries.size(), month, year);
        return summaries;
    }

    /* -------------------------------------------------------------------------- */
    /*              Build a filter according to the given parameters              */
    /* -------------------------------------------------------------------------- */
    private List<String[]> buildFilters(String month, String year) throws Exception {
        List<String[]> filters = new ArrayList<>();
        filters.add(new String[]{"docstatus", "=", "1"});

        try {
            if (year != null && !year.isEmpty()) {
                if (month != null && !month.isEmpty()) {
                    YearMonth yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
                    filters.add(new String[]{"posting_date", ">=", yearMonth.atDay(1).format(dateFormatter)});
                    filters.add(new String[]{"posting_date", "<=", yearMonth.atEndOfMonth().format(dateFormatter)});
                } else {
                    LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
                    LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);
                    filters.add(new String[]{"posting_date", ">=", startDate.format(dateFormatter)});
                    filters.add(new String[]{"posting_date", "<=", endDate.format(dateFormatter)});
                }
            }
        } catch (Exception e) {
            logger.warn("Invalid month or year provided: month={}, year={}", month, year);
            throw new Exception("Invalid month or year format", e);
        }
        return filters;
    }

    /* -------------------------------------------------------------------------- */
    /*            Transoform a list for salary slip into Salary Summary           */
    /* -------------------------------------------------------------------------- */
    private List<SalarySummary> transformSalaryData(List<SalarySlip> salarySlips) {
        List<SalarySummary> summaries = new ArrayList<>();
        for (SalarySlip slip : salarySlips) {
            if (slip.getPostingDate() == null) continue;

            LocalDate postingDate = slip.getPostingDate().toLocalDate();
            SalarySummary summary = new SalarySummary();
            summary.setEmployee(slip.getEmployeeName());
            summary.setPostingDate(slip.getPostingDate());
            summary.setStatus(slip.getStatus());
            summary.setMonth(postingDate.format(monthFormatter));
            summary.setYear(postingDate.format(yearFormatter));
            summary.setGrossPay(slip.getGrossPay() != null ? slip.getGrossPay() : 0.0);
            summary.setNetPay(slip.getNetPay() != null ? slip.getNetPay() : 0.0);
            summary.setTotalDeduction(slip.getGrossPay() != null && slip.getNetPay() != null
                    ? slip.getGrossPay() - slip.getNetPay() : 0.0);

            summaries.add(summary);
        }
        return summaries;
    }
}
