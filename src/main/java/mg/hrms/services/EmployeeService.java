package mg.hrms.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.Employee;
import mg.hrms.models.User;
import mg.hrms.models.args.EmployeeFilterArgs;
import mg.hrms.utils.ApiUtils;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    /* -------------------------------------------------------------------------- */
    /*                                 Constructor                                */
    /* -------------------------------------------------------------------------- */
    public EmployeeService(RestApiService restApiService, ObjectMapper objectMapper){
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /*                             Fetch All employee                             */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "null" })
    public List<Employee> getAll(User user, EmployeeFilterArgs filter) throws Exception {
        String[] fields = {"name", "last_name","first_name","gender","date_of_birth","date_of_joining","company","status"};

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

        String apiUrl = ApiUtils.buildUrl(
            restApiService.getServerHost() + "/api/resource/Employee",
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

        List<Employee> employees = objectMapper.convertValue(
            response.getBody().get("data"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Employee.class)
        );

        if (employees == null) {
            logger.error("Employees data not found");
            throw new Exception("Employees data not found");
        }

        logger.info("Retrieved {} employees successfully", employees.size());

        return employees;
    }
}
