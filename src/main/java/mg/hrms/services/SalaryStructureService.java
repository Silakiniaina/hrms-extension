package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.SalaryStructure;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SalaryStructureService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStructureService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public SalaryStructureService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    public SalaryStructure getByName(User user, String name) throws Exception {
        logger.info("Fetching salary structure by name: {}", name);
        String[] fields = {"name", "company", "components"};
        String apiUrl = restApiService.buildResourceUrl("Salary Structure", name, fields);

        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Salary structure not found: {}", name);
            throw new Exception("Salary structure not found");
        }

        SalaryStructure structure = objectMapper.convertValue(response.getBody().get("data"), SalaryStructure.class);
        logger.info("Retrieved salary structure: {}", name);
        return structure;
    }

    public List<SalaryStructure> getAll(User user) throws Exception {
        logger.info("Fetching all salary structures for user: {}", user.getFullName());
        String[] fields = {"name", "company", "components"};
        String apiUrl = restApiService.buildUrl("Salary Structure", fields, null);

        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Salary structures data not found for user: {}", user.getFullName());
            throw new Exception("Salary structures data not found");
        }

        List<SalaryStructure> structures = objectMapper.convertValue(
                response.getBody().get("data"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalaryStructure.class)
        );
        logger.info("Retrieved {} salary structures for user: {}", structures.size(), user.getFullName());
        return structures;
    }
}