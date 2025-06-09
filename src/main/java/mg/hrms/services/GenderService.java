package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.Gender;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GenderService {

    private static final Logger logger = LoggerFactory.getLogger(GenderService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public GenderService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    public List<Gender> getAll(User user) throws Exception {
        logger.info("Fetching all genders for user: {}", user.getFullName());
        String[] fields = {"name"};
        String apiUrl = restApiService.buildUrl("Gender", fields, null);

        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Genders data not found for user: {}", user.getFullName());
            throw new Exception("Genders data not found");
        }

        List<Gender> genders = objectMapper.convertValue(
                response.getBody().get("data"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Gender.class)
        );
        logger.info("Retrieved {} genders for user: {}", genders.size(), user.getFullName());
        return genders;
    }
}