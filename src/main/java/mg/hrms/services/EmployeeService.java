package mg.hrms.services;

import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.Employee;
import mg.hrms.models.User;
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
    public List<Employee> getAll(User user) throws Exception {
        String[] fields = {"name", "last_name","first_name","gender","date_of_birth","date_of_joining","company","status"};
        String apiUrl = ApiUtils.buildUrl(restApiService.getServerHost() + "/api/resource/Employee", fields);

        var response = restApiService.executeApiCall(
            apiUrl,
            HttpMethod.GET,
            null,
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        logger.info("response : "+response);

        List<Employee> employees = objectMapper.convertValue(
            response.getBody().get("data"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Employee.class)
        );

        if (employees == null) {
            logger.error("Employees data not found");
            throw new Exception("Employees data not found");
        }

        logger.info("Employees data retrieved successfully");
        logger.debug("Successfully fetched Employee data: {}", employees);
        
        return employees;
    }
}
