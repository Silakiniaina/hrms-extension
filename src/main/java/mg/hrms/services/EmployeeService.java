package mg.hrms.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import mg.hrms.models.Employee;
import mg.hrms.models.User;
import mg.hrms.models.SalarySummary;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.args.EmployeeFilterArgs;
import mg.hrms.utils.ApiUtils;


@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    /* -------------------------------------------------------------------------- */
    /* Constructor                                */
    /* -------------------------------------------------------------------------- */
    public EmployeeService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /* -------------------------------------------------------------------------- */
    /* Fetch employee by ID                           */
    /* -------------------------------------------------------------------------- */
    public Employee getById(User user, String employeeId) throws Exception {
        String[] fields = {"name", "last_name", "first_name", "gender", "date_of_birth", "date_of_joining", "company", "status"};

        // Already using buildResourceUrl, which is good.
        String apiUrl = ApiUtils.buildResourceUrl(
            restApiService.getServerHost(),
            "Employee",
            employeeId,
            fields
        );

        logger.info("Fetching employee by ID: {}", apiUrl);

        var response = restApiService.executeApiCall(
            apiUrl,
            HttpMethod.GET,
            null,
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Employee not found");
            throw new Exception("Employee not found");
        }

        Employee employee = objectMapper.convertValue(
            response.getBody().get("data"),
            Employee.class
        );

        return employee;
    }

    /* -------------------------------------------------------------------------- */
    /* Fetch All employee                             */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "null" })
    public List<Employee> getAll(User user, EmployeeFilterArgs filter) throws Exception {
        String[] fields = {"name", "last_name", "first_name", "gender", "date_of_birth", "date_of_joining", "company", "status"};

        // Prepare filters as List of String arrays for ERPNext format: [["field","operator","value"],...]
        List<String[]> filters = new ArrayList<>();

        if (filter != null) {
            logger.info("Applying filters: {}", filter);

            if (filter.getName() != null && !filter.getName().isEmpty()) {
                filters.add(new String[]{"name", "like", "%" + filter.getName() + "%"});
                logger.info("Added name filter: {}", filter.getName());
            }
            if (filter.getGender() != null && !filter.getGender().isEmpty()) {
                filters.add(new String[]{"gender", "=", filter.getGender()});
                logger.info("Added gender filter: {}", filter.getGender());
            }
            if (filter.getCompany() != null && !filter.getCompany().isEmpty()) {
                filters.add(new String[]{"company", "=", filter.getCompany()});
                logger.info("Added company filter: {}", filter.getCompany());
            }
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                filters.add(new String[]{"status", "=", filter.getStatus()});
                logger.info("Added status filter: {}", filter.getStatus());
            }

            // Handle age filtering with separate date conditions
            if (filter.getMinAge() > 0 || filter.getMaxAge() > 0) {
                List<String[]> dobFilters = ApiUtils.buildDobFilters(filter.getMinAge(), filter.getMaxAge());
                filters.addAll(dobFilters);
                logger.info("Added {} date_of_birth filters for age range", dobFilters.size());
                for (String[] dobFilter : dobFilters) {
                    logger.info("DOB filter: {} {} {}", dobFilter[0], dobFilter[1], dobFilter[2]);
                }
            }
        }

        // Updated buildUrl call to pass doctype separately
        String apiUrl = ApiUtils.buildUrl(
            restApiService.getServerHost(),
            "Employee", // Doctype moved to a separate argument
            fields,
            filters
        );

        logger.info("Final API URL: {}", apiUrl);

        var response = restApiService.executeApiCall(
            apiUrl,
            HttpMethod.GET,
            null,
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        logger.info("API response received successfully");

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Employees data not found");
            throw new Exception("Employees data not found");
        }

        List<Employee> employees = objectMapper.convertValue(
            response.getBody().get("data"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Employee.class)
        );

        logger.info("Retrieved {} employees successfully", employees.size());

        return employees;
    }

    /* -------------------------------------------------------------------------- */
    /* Fetch Employee Salaries                           */
    /* -------------------------------------------------------------------------- */
    public List<SalarySlip> getEmployeeSalaries(User user, String employeeId) throws Exception {
        String[] fields = {"name", "employee", "employee_name", "posting_date", "gross_pay", "net_pay", "status"};
        List<String[]> filters = new ArrayList<>();
        filters.add(new String[]{"employee", "=", employeeId});
        filters.add(new String[]{"docstatus", "=", "1"});

        // Updated buildUrl call to pass doctype separately
        String apiUrl = ApiUtils.buildUrl(
            restApiService.getServerHost(),
            "Salary Slip",
            fields,
            filters
        );

        logger.info("Fetching employee salaries with URL: {}", apiUrl);

        try {
            var response = restApiService.executeApiCall(
                apiUrl,
                HttpMethod.GET,
                null,
                user,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() == null || response.getBody().get("data") == null) {
                logger.warn("No salary data found for employeeId: {}", employeeId);
                return new ArrayList<>();
            }

            // Configure ObjectMapper to ignore unknown properties
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<SalarySlip> salaries = mapper.convertValue(
                response.getBody().get("data"),
                mapper.getTypeFactory().constructCollectionType(List.class, SalarySlip.class)
            );

            logger.info("Retrieved {} salary slips for employeeId: {}", salaries.size(), employeeId);
            return salaries;
        } catch (Exception e) {
            logger.error("Failed to fetch salaries for employeeId: {}. Error: {}", employeeId, e.getMessage());
            throw new Exception("Failed to fetch employee salaries: " + e.getMessage(), e);
        }
    }

    public SalarySlip getPayslipById(User user, String payslipId) throws Exception {
        String[] fields = {"name", "employee", "employee_name", "posting_date",
                        "gross_pay", "net_pay", "status", "earnings", "deductions",
                        "company", "bank_name", "bank_account_no"};

        // Use buildResourceUrl for individual resource requests, it handles doctype with spaces and fields properly
        String apiUrl = ApiUtils.buildResourceUrl(
            restApiService.getServerHost(),
            "Salary Slip", // doctype
            payslipId,   // resourceId
            fields       // fields
        );

        logger.info("Fetching payslip by ID: {}", apiUrl);

        var response = restApiService.executeApiCall(
            apiUrl,
            HttpMethod.GET,
            null,
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Payslip not found with ID: {}", payslipId);
            throw new Exception("Payslip not found");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.convertValue(
            response.getBody().get("data"),
            SalarySlip.class
        );
    }
}
