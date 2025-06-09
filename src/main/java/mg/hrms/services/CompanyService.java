package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.Company;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public CompanyService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    public List<Company> getAll(User user) throws Exception {
        logger.info("Fetching all companies for user: {}", user.getFullName());
        String[] fields = {"name"};
        String apiUrl = restApiService.buildUrl("Company", fields, null);

        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Companies data not found for user: {}", user.getFullName());
            throw new Exception("Companies data not found");
        }

        List<Company> companies = objectMapper.convertValue(
                response.getBody().get("data"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Company.class)
        );
        logger.info("Retrieved {} companies for user: {}", companies.size(), user.getFullName());
        return companies;
    }
}