package mg.hrms.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.Employee;
import mg.hrms.models.SalaryComponent;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.SalaryStructureAssignment;
import mg.hrms.models.User;
import mg.hrms.utils.OperationUtils;

@Service
public class SalaryService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FrappeService frappeService;
    private final ImportService importService;
    private final EmployeeService employeeService; 
    private final SalarySlipService salarySlipService;
    private final SalaryStructureAssignmentService salaryStructureAssignmentService;
    private final ObjectMapper objectMapper;

    public SalaryService(EmployeeService employeeService, SalarySlipService salarySlipService,SalaryStructureAssignmentService salaryStructureAssignmentService, ImportService importService,
            FrappeService frappeService, ObjectMapper objectMapper) {
        this.frappeService = frappeService;
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
        this.importService = importService;
        this.salarySlipService = salarySlipService;
        this.salaryStructureAssignmentService = salaryStructureAssignmentService;
    }

    /* -------------------------------------------------------------------------- */
    /* Update salary slips by condition */
    /* -------------------------------------------------------------------------- */
    public List<SalarySlip> updateSalarySlipsByCondition(
            String salaryComponentName,
            String operator,
            double amount,
            String action,
            double percentage,
            User user) throws Exception {

        logger.info("Starting salary slip update for component {} {} {} with {}% {}",
                salaryComponentName, operator, amount, percentage, action);

        // Get all salary slips that match the condition
        List<SalarySlip> matchingSlips = findSalarySlipsByComponentCondition(
                salaryComponentName,
                operator,
                amount,
                user);

        List<SalarySlip> updatedSlips = new ArrayList<>();

        // Process each matching slip
        for (SalarySlip slip : matchingSlips) {
            try {
                // Calculate new base salary
                double currentBase = slip.getBaseSalary(); // Using gross pay as base salary
                double newBase = calculateAdjustedSalary(currentBase, action, percentage);

                // Cancel the existing slip
                boolean cancelled = frappeService.cancelFrappeDocument("Salary Slip", slip.getSlipId(), user);
                boolean deleted = frappeService.deleteFrappeDocument("Salary Slip", slip.getSlipId(), user);
                if (!cancelled || !deleted) {
                    logger.warn("Failed to cancel/delete salary slip {}", slip.getSlipId());
                    continue;
                }

                // Get employee details
                Employee employee = employeeService.getById(user, slip.getEmployee());

                // Create new slip with updated base salary
                String postingDate = slip.getPostingDate().toString();
                SalarySlip newSlip = createSalarySlip(employee, postingDate, newBase, true, true, user);

                if (newSlip != null) {
                    updatedSlips.add(newSlip);
                    logger.info("Successfully updated salary slip {} with new base {}",
                            slip.getSlipId(), newBase);
                }
            } catch (Exception e) {
                logger.error("Error updating salary slip {}: {}", slip.getSlipId(), e.getMessage());
            }
        }

        logger.info("Completed salary slip update. Processed {} slips, successfully updated {}",
                matchingSlips.size(), updatedSlips.size());

        return updatedSlips;
    }

    /* -------------------------------------------------------------------------- */
    /* Find salary slips matching component and amount condition */
    /* -------------------------------------------------------------------------- */
    private List<SalarySlip> findSalarySlipsByComponentCondition(
            String componentName,
            String operator,
            double amount,
            User user) throws Exception {

        logger.info("Finding salary slips where component {} {} {}", componentName, operator, amount);

        // Get all salary slips with the specified component
        String[] fields = { "name", "employee", "employee_name", "posting_date",
                "salary_structure", "gross_pay", "net_pay", "status",
                "bank_name", "bank_account_no"};

        List<Map<String, Object>> allSlipsData = frappeService.searchFrappeDocuments(
                "Salary Slip", fields, null, user);

        if (allSlipsData == null || allSlipsData.isEmpty()) {
            return new ArrayList<>();
        }

        // Convert to SalarySlip objects and filter
        List<SalarySlip> matchingSlips = new ArrayList<>();
        for (Map<String, Object> slipData : allSlipsData) {
            try {
                SalarySlip slip = objectMapper.convertValue(slipData, SalarySlip.class);

                // Ensure salary slip got earnings
                slip = salarySlipService.getById(user, slip.getSlipId());

                // Check if slip has the component and matches condition
                if (hasComponentWithCondition(slip, componentName, operator, amount)) {
                    matchingSlips.add(slip);
                }
            } catch (Exception e) {
                logger.error("Error processing salary slip data: {}", e.getMessage());
            }
        }

        return matchingSlips;
    }

    /* -------------------------------------------------------------------------- */
    /* Check if salary slip has component matching condition */
    /* -------------------------------------------------------------------------- */
    private boolean hasComponentWithCondition(
            SalarySlip slip,
            String componentName,
            String operator,
            double amount) {

        // Check earnings
        if (slip.getEarnings() != null) {
            for (SalaryComponent component : slip.getEarnings()) {
                if (componentName.equals(component.getName()) &&
                        matchesCondition(component.getAmount(), operator, amount)) {
                    return true;
                }
            }
        }

        // Check deductions
        if (slip.getDeductions() != null) {
            for (SalaryComponent component : slip.getDeductions()) {
                if (componentName.equals(component.getName()) &&
                        matchesCondition(component.getAmount(), operator, amount)) {
                    return true;
                }
            }
        }

        return false;
    }

    /* -------------------------------------------------------------------------- */
    /* Check if value matches condition */
    /* -------------------------------------------------------------------------- */
    private boolean matchesCondition(double componentAmount, String operator, double amount) {
        switch (operator) {
            case ">":
                return componentAmount > amount;
            case "<":
                return componentAmount < amount;
            default:
                return false;
        }
    }

    /* -------------------------------------------------------------------------- */
    /* Calculate adjusted base salary */
    /* -------------------------------------------------------------------------- */
    private double calculateAdjustedSalary(double currentBase, String action, double percentage) {
        double adjustment = currentBase * (percentage / 100);
        if ("Add".equalsIgnoreCase(action)) {
            return currentBase + adjustment;
        } else {
            return currentBase - adjustment;
        }
    }

    /* -------------------------------------------------------------------------- */
    /* generate salary slips */
    /* -------------------------------------------------------------------------- */
    public List<SalarySlip> generateSalarySlips(Employee emp, String startDate, String endDate, double amount,
            User user) throws Exception {
        logger.info("Starting salary slip generation for employee {} from {} to {}", emp.getEmployeeId(), startDate,
                endDate);
        List<SalarySlip> results = new ArrayList<>();

        // Parse start and end dates
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        // Validate date range
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // Determine base salary amount
        double baseSalary = amount;
        if (amount <= 0) {
            SalarySlip lastSlip = getLastSalarySlip(emp, startDate, user);
            if (lastSlip == null || lastSlip.getGrossPay() <= 0) {
                throw new Exception("No previous salary slip found before " + startDate
                        + " and no amount provided for employee " + emp.getEmployeeId());
            }
            baseSalary = lastSlip.getGrossPay();
            logger.info("Using base salary {} from last salary slip for employee {}", baseSalary, emp.getEmployeeId());
        }

        // Generate salary slips for each month in the period
        LocalDate current = start.withDayOfMonth(1);
        LocalDate endMonth = end.withDayOfMonth(1);

        while (!current.isAfter(endMonth)) {
            String monthDate = current.format(DATE_FORMATTER);

            // Check if salary slip already exists for this month
            if (!salarySlipExistsForMonth(emp, current, user)) {
                logger.info("Generating salary slip for employee {} for month {}", emp.getEmployeeId(),
                        current.getMonth() + " " + current.getYear());

                SalarySlip generatedSlip = createSalarySlip(emp, monthDate, baseSalary, false, false, user);
                if (generatedSlip != null) {
                    results.add(generatedSlip);
                    logger.info("Successfully generated salary slip for employee {} for month {}", emp.getEmployeeId(),
                            current.getMonth() + " " + current.getYear());
                } else {
                    logger.warn("Failed to generate salary slip for employee {} for month {}", emp.getEmployeeId(),
                            current.getMonth() + " " + current.getYear());
                }
            } else {
                logger.info("Salary slip already exists for employee {} for month {}, skipping generation",
                        emp.getEmployeeId(), current.getMonth() + " " + current.getYear());
            }

            // Move to next month
            current = current.plusMonths(1);
        }

        logger.info("Completed salary slip generation for employee {}. Generated {} new salary slips",
                emp.getEmployeeId(), results.size());
        return results;
    }

    /* -------------------------------------------------------------------------- */
    /* Create a salary slip for an employee with base salary on a period */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("null")
    public SalarySlip createSalarySlip(Employee emp, String date, double amount, boolean override, boolean isUpdate, User user) {
        logger.info("Creating salary slip for employee {} on date {} with amount {}", emp.getEmployeeId(), date,
                amount);

        try {
            LocalDate salaryDate = null;
            if(date.contains("-")){
                salaryDate = LocalDate.parse(date,DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }else if(date.contains("/")){
                salaryDate = LocalDate.parse(date, DATE_FORMATTER);
            }else{
                throw new Exception("Unknown date format for date "+date);
            }

            // Check if salary slip already exists and override is false
            if (!override && salarySlipExistsForMonth(emp, salaryDate, user)) {
                logger.warn("Salary slip already exists for employee {} for month {} and override is false",
                        emp.getEmployeeId(), salaryDate.getMonth() + " " + salaryDate.getYear());
                return null;
            }

            List<SalaryStructureAssignment> salaryStructureAss = salaryStructureAssignmentService
                    .getAllForEmployee(user, emp.getEmployeeId());
            if(!isUpdate){
                importService.ensureSalaryStructureAssignment(emp.getEmployeeId(),
                        salaryStructureAss.get(0).getSalaryStructure().getName(), date, amount, emp.getCompany().getName(),
                        user);
            }else{
                ensureSalaryStructureAssignment(emp.getEmployeeId(),
                        salaryStructureAss.get(0).getSalaryStructure().getName(), date, amount, emp.getCompany().getName(),
                        user);
            }

            // Prepare salary slip data for Frappe
            Map<String, Object> salarySlipData = new HashMap<>();
            salarySlipData.put("employee", emp.getEmployeeId());
            salarySlipData.put("posting_date", OperationUtils.formatIntoFrappeDate(date));
            salarySlipData.put("start_date", salaryDate.with(TemporalAdjusters.firstDayOfMonth())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            salarySlipData.put("end_date", salaryDate.with(TemporalAdjusters.lastDayOfMonth())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            salarySlipData.put("base_salary", amount);
            salarySlipData.put("salary_structure", salaryStructureAss.get(0).getSalaryStructure().getName());
            salarySlipData.put("payroll_frequency", "Monthly");
            salarySlipData.put("company", emp.getCompany().getName());

            // Create the salary slip in Frappe
            String createdSlipName = frappeService.createFrappeDocument("Salary Slip", salarySlipData, user, true);

            if (createdSlipName != null) {
                logger.info("Successfully created salary slip {} for employee {}", createdSlipName,
                        emp.getEmployeeId());

                // Fetch the created salary slip to return
                String[] fields = { "name", "employee", "employee_name", "posting_date", "salary_structure",
                        "gross_pay", "net_pay", "status", "bank_name", "bank_account_no" };
                Map<String, Object> createdSlipData = frappeService.getFrappeDocument("Salary Slip", fields,
                        createdSlipName, user);

                if (createdSlipData != null) {
                    return objectMapper.convertValue(createdSlipData, SalarySlip.class);
                }
            }

            logger.error("Failed to create salary slip for employee {} on date {}", emp.getEmployeeId(), date);
            return null;

        } catch (Exception e) {
            logger.error("Error creating salary slip for employee {} on date {}: {}", emp.getEmployeeId(), date,
                    e.getMessage());
            return null;
        }
    }

    public boolean ensureSalaryStructureAssignment(String employee, String salaryStructure,
            String fromDate, Double baseSalary, String company, User user) {
        try {
            String[] fields = {"name", "employee", "salary_structure", "from_date", "docstatus"};

            List<String[]> filters = new ArrayList<>();
            filters.add(new String[]{"employee","=",employee});
            filters.add(new String[]{"salary_structure","=",salaryStructure});
            filters.add(new String[]{"from_date","=",OperationUtils.formatIntoFrappeDate(fromDate)});

            List<Map<String, Object>> assignments = frappeService.searchFrappeDocuments("Salary Structure Assignment", fields, filters, user);

            // Delete existing assignments if any
            if (assignments != null && !assignments.isEmpty()) {
                // logStep("Found " + assignments.size() + " existing salary structure assignment(s), deleting them...");

                for (Map<String, Object> assignment : assignments) {
                    String assignmentName = (String) assignment.get("name");
                    Object docstatusObj = assignment.get("docstatus");
                    int docstatus = docstatusObj != null ? ((Number) docstatusObj).intValue() : 0;

                    try {
                        // If document is submitted (docstatus = 1), cancel it first
                        if (docstatus == 1) {
                            boolean cancelled = frappeService.cancelFrappeDocument("Salary Structure Assignment", assignmentName, user);
                            if (!cancelled) {
                                // logStep("Failed to cancel salary structure assignment: " + assignmentName);
                                continue;
                            }
                            // logStep("Cancelled salary structure assignment: " + assignmentName);
                        }

                        // Delete the assignment
                        boolean deleted = frappeService.deleteFrappeDocument("Salary Structure Assignment", assignmentName, user);
                        if (deleted) {
                            // logStep("Deleted salary structure assignment: " + assignmentName);
                        } else {
                            // logStep("Failed to delete salary structure assignment: " + assignmentName);
                        }

                    } catch (Exception e) {
                        // logStep("Error deleting salary structure assignment " + assignmentName + ": " + e.getMessage());
                    }
                }
            }

            // Create new assignment with updated base salary
            Map<String, Object> assignmentData = new HashMap<>();
            assignmentData.put("employee", employee);
            assignmentData.put("salary_structure", salaryStructure);
            assignmentData.put("from_date", OperationUtils.formatIntoFrappeDate(fromDate));
            assignmentData.put("base", baseSalary);
            assignmentData.put("company", company);

            String docName = frappeService.createFrappeDocument("Salary Structure Assignment", assignmentData, user, true);
            if (docName != null) {
                frappeService.submitFrappeDocument("Salary Structure Assignment", docName, user);
                // logStep("Created new salary structure assignment: " + docName + " with base salary: " + baseSalary);
                return true;
            } else {
                // logStep("Failed to create new salary structure assignment");
                return false;
            }

        } catch (Exception e) {
            // logStep("Failed to ensure salary structure assignment: " + e.getMessage());
            return false;
        }
    }

    /* -------------------------------------------------------------------------- */
    /* Get employee salaries on a month date */
    /* -------------------------------------------------------------------------- */
    public List<SalarySlip> getEmployeeSalaries(Employee emp, String date, User user) throws Exception {
        logger.info("Attempt to fetch salary for employee {} on period {}", emp.getEmployeeId(), date);
        String[] fields = { "name", "employee", "employee_name", "posting_date", "salary_structure", "gross_pay",
                "net_pay", "status", "bank_name", "bank_account_no" };
        List<String[]> filters = new ArrayList<>();
        if (emp != null) {
            filters.add(new String[] { "employee", "=", emp.getEmployeeId() });
        }
        filters.add(new String[] { "posting_date", "<=", OperationUtils.formatIntoFrappeDate(date) });
        filters.add(new String[] { "docstatus", "=", "1" });

        List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Salary Slip", fields, filters, user);

        if (response == null) {
            logger.warn("No salary slips found for employee: {}", emp.getEmployeeId());
            return new ArrayList<>();
        }

        List<SalarySlip> slips = objectMapper.convertValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalarySlip.class));
        logger.info("Retrieved {} salary slips for employee: {} on the period {}", slips.size(), emp.getEmployeeId(),
                date);
        return slips;
    }

    /* -------------------------------------------------------------------------- */
    /* Fetching the last salary slip of an employee before a date */
    /* -------------------------------------------------------------------------- */
    public SalarySlip getLastSalarySlip(Employee emp, String date, User user) {
        logger.info("Fetching last salary slip for employee {} before date {}", emp.getEmployeeId(), date);

        try {
            String[] fields = { "name", "employee", "employee_name", "posting_date", "salary_structure", "gross_pay",
                    "net_pay", "status", "bank_name", "bank_account_no" };
            List<String[]> filters = new ArrayList<>();
            filters.add(new String[] { "employee", "=", emp.getEmployeeId() });
            filters.add(new String[] { "posting_date", "<", OperationUtils.formatIntoFrappeDate(date) });
            filters.add(new String[] { "docstatus", "=", "1" });

            List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Salary Slip", fields, filters,
                    user);

            if (response == null || response.isEmpty()) {
                logger.warn("No salary slips found for employee {} before date {}", emp.getEmployeeId(), date);
                return null;
            }

            // Convert to SalarySlip objects
            List<SalarySlip> slips = objectMapper.convertValue(
                    response,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SalarySlip.class));

            // Find the most recent salary slip (assuming the response is ordered by
            // posting_date desc)
            SalarySlip lastSlip = slips.get(0);
            for (SalarySlip slip : slips) {
                if (slip.getPostingDate() != null && (lastSlip.getPostingDate() == null ||
                        slip.getPostingDate().compareTo(lastSlip.getPostingDate()) > 0)) {
                    lastSlip = slip;
                }
            }

            logger.info("Found last salary slip {} for employee {} with gross pay {} on date {}",
                    lastSlip.getSlipId(), emp.getEmployeeId(), lastSlip.getGrossPay(), lastSlip.getPostingDate());
            return lastSlip;

        } catch (Exception e) {
            logger.error("Error fetching last salary slip for employee {} before date {}: {}", emp.getEmployeeId(),
                    date, e.getMessage());
            return null;
        }
    }

    /* -------------------------------------------------------------------------- */
    /* Helper method to check if salary slip exists for month */
    /* -------------------------------------------------------------------------- */
    private boolean salarySlipExistsForMonth(Employee emp, LocalDate monthDate, User user) {
        try {
            // Get first and last day of the month
            LocalDate startOfMonth = monthDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate endOfMonth = monthDate.with(TemporalAdjusters.lastDayOfMonth());

            String[] fields = { "name" };
            List<String[]> filters = new ArrayList<>();
            filters.add(new String[] { "employee", "=", emp.getEmployeeId() });
            filters.add(new String[] { "posting_date", ">=",
                    startOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) });
            filters.add(new String[] { "posting_date", "<=",
                    endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) });
            filters.add(new String[] { "docstatus", "=", "1" });

            List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Salary Slip", fields, filters,
                    user);

            boolean exists = response != null && !response.isEmpty();
            logger.debug("Salary slip exists for employee {} for month {}: {}", emp.getEmployeeId(),
                    monthDate.getMonth() + " " + monthDate.getYear(), exists);
            return exists;

        } catch (Exception e) {
            logger.error("Error checking if salary slip exists for employee {} for month {}: {}", emp.getEmployeeId(),
                    monthDate, e.getMessage());
            return false;
        }
    }
}
