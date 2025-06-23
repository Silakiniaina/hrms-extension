package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.Employee;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.User;
import mg.hrms.models.args.EmployeeFilterArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;
    private final SalarySlipService salarySlipService;
    private final SalaryStructureAssignmentService salaryStructureAssignmentService;

    public EmployeeService(RestApiService restApiService, ObjectMapper objectMapper, SalarySlipService salarySlipService, SalaryStructureAssignmentService salaryStructureAssignmentService) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
        this.salarySlipService = salarySlipService;
        this.salaryStructureAssignmentService = salaryStructureAssignmentService;
    }

    /* -------------------------------------------------------------------------- */
    /*                       Method to getEmployeeBy his Id                       */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("null")
    public Employee getById(User user, String employeeId) throws Exception {
        logger.info("Fetching employee by ID: {}", employeeId);
        String[] fields = {"name", "last_name", "first_name", "gender", "date_of_birth", "date_of_joining", "company", "status"};
        String apiUrl = restApiService.buildResourceUrl("Employee", employeeId, fields);

        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Employee not found: {}", employeeId);
            throw new Exception("Employee not found");
        }

        Employee employee = objectMapper.convertValue(response.getBody().get("data"), Employee.class);
        logger.info("Retrieved employee: {} {}", employee.getFirstName(), employee.getLastName());

        if(employee != null){
            employee.setSalaryAssignments(salaryStructureAssignmentService.getAllForEmployee(user, employeeId));
        }
        return employee;
    }

    public List<Employee> getAll(User user, EmployeeFilterArgs filter) throws Exception {
        logger.info("Fetching all employees for user: {}", user.getFullName());
        String[] fields = {"name", "last_name", "first_name", "gender", "date_of_birth", "date_of_joining", "company", "status"};
        List<String[]> filters = buildFilters(filter);

        String apiUrl = restApiService.buildUrl("Employee", fields, filters);
        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Employees data not found for user: {}", user.getFullName());
            throw new Exception("Employees data not found");
        }

        List<Employee> employees = objectMapper.convertValue(
                response.getBody().get("data"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Employee.class)
        );

        for(Employee emp : employees){
            emp.setSalaryAssignments(salaryStructureAssignmentService.getAllForEmployee(user, emp.getEmployeeId()));
        }

        logger.info("Retrieved {} employees for user: {}", employees.size(), user.getFullName());
        return employees;
    }

    public List<SalarySlip> getEmployeeSalaries(User user, String employeeId) throws Exception {
        logger.info("Fetching salaries for employee: {}", employeeId);
        return salarySlipService.getAllForEmployee(user, employeeId);
    }

    public SalarySlip getPayslipById(User user, String payslipId) throws Exception {
        logger.info("Fetching payslip by ID: {}", payslipId);
        return salarySlipService.getById(user, payslipId);
    }

    private List<String[]> buildFilters(EmployeeFilterArgs filter) {
        List<String[]> filters = new java.util.ArrayList<>();
        if (filter == null) return filters;

        if (filter.getName() != null && !filter.getName().isEmpty()) {
            filters.add(new String[]{"name", "like", "%" + filter.getName() + "%"});
            logger.info("Applied name filter: {}", filter.getName());
        }
        if (filter.getGender() != null && !filter.getGender().isEmpty()) {
            filters.add(new String[]{"gender", "=", filter.getGender()});
            logger.info("Applied gender filter: {}", filter.getGender());
        }
        if (filter.getCompany() != null && !filter.getCompany().isEmpty()) {
            filters.add(new String[]{"company", "=", filter.getCompany()});
            logger.info("Applied company filter: {}", filter.getCompany());
        }
        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            filters.add(new String[]{"status", "=", filter.getStatus()});
            logger.info("Applied status filter: {}", filter.getStatus());
        }
        if (filter.getMinAge() > 0 || filter.getMaxAge() > 0) {
            filters.addAll(restApiService.buildDobFilters(filter.getMinAge(), filter.getMaxAge()));
            logger.info("Applied age range filter: min={}, max={}", filter.getMinAge(), filter.getMaxAge());
        }
        return filters;
    }
}
