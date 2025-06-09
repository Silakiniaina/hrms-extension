package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.SalaryStructureAssignment;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SalaryStructureAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStructureAssignmentService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;
    private final SalaryStructureService salaryStructureService;

    public SalaryStructureAssignmentService(RestApiService restApiService, ObjectMapper objectMapper, SalaryStructureService salaryStructureService) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
        this.salaryStructureService = salaryStructureService;
    }

    public List<SalaryStructureAssignment> getAllForEmployee(User user, String employeeId) throws Exception {
        logger.info("Fetching salary structure assignments for employee: {}", employeeId);
        String[] fields = {"employee", "salary_structure", "from_date"};
        List<String[]> filters = new ArrayList<>();
        filters.add(new String[]{"employee", "=", employeeId});

        String apiUrl = restApiService.buildUrl("Salary Structure Assignment", fields, filters);
        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.warn("No salary structure assignments found for employee: {}", employeeId);
            return new ArrayList<>();
        }

        List<SalaryStructureAssignment> assignments = objectMapper.convertValue(
                response.getBody().get("data"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalaryStructureAssignment.class)
        );

        for(SalaryStructureAssignment assignment : assignments){
            assignment.setSalaryStructure(salaryStructureService.getByName(user, assignment.getSalaryStructureStr()));
        }

        logger.info("Retrieved {} salary structure assignments for employee: {}", assignments.size(), employeeId);
        return assignments;
    }
}
