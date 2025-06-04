package mg.hrms.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.Company;
import mg.hrms.models.User;
import mg.hrms.utils.ApiUtils;

@Service
public class CompanyService {

    Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    /* -------------------------------------------------------------------------- */
    /*                                 Constructor                                */
    /* -------------------------------------------------------------------------- */
    public CompanyService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }
    
    /* -------------------------------------------------------------------------- */
    /*                                 Additional Methods                         */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("null")
    public List<Company> getAll(User user) throws Exception {
        logger.info("Fetching all companies for user: {}", user.getFullName());
        String[] fields = {"name"};
        String apiUrl = ApiUtils.buildUrl(restApiService.getServerHost() + "/api/resource/Company", fields, null);
        var response = restApiService.executeApiCall(
            apiUrl,
            HttpMethod.GET,
            null,
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        logger.info("Response received: {}", response);
        List<Company> companies = objectMapper.convertValue(
            response.getBody().get("data"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Company.class)
        );

        if(companies == null || companies.isEmpty()) {
            logger.error("Companies data not found");
            throw new Exception("Companies data not found");
        }

        return companies;
    }

}
