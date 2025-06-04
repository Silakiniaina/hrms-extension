package mg.hrms.services;

import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.Company;
import mg.hrms.models.Gender;
import mg.hrms.models.User;
import mg.hrms.utils.ApiUtils;

@Service
public class GenderService {
    
    Logger logger = LoggerFactory.getLogger(GenderService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    /* -------------------------------------------------------------------------- */
    /*                                 Constructor                                */
    /* -------------------------------------------------------------------------- */
    public GenderService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /*                              Fetch all genders                             */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("null")
    public List<Gender> getAll(User user) throws Exception {
        logger.info("Fetching all genders for user: " + user.getFullName());
        String[] fields = {"name"};
        String apiUrl = ApiUtils.buildUrl(restApiService.getServerHost() + "/api/resource/Gender", fields, null);
        var response = restApiService.executeApiCall(
            apiUrl,
            HttpMethod.GET,
            null,
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        logger.info("Response received: {}", response);
        List<Gender> genders = objectMapper.convertValue(
            response.getBody().get("data"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Gender.class)
        );

        if(genders == null || genders.isEmpty()) {
            logger.error("Genders data not found");
            throw new Exception("Genders data not found");
        }

        return genders;
    }
}
