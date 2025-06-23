package mg.hrms.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;


import mg.hrms.models.User;
import mg.hrms.utils.OperationUtils;

public class FrappeService {

    private static final Logger logger = LoggerFactory.getLogger(FrappeService.class);
    private final RestApiService restApiService;

    public FrappeService(RestApiService restApiService){
        this.restApiService = restApiService; 
    }

    @SuppressWarnings({ "null", "unchecked", "static-access" })
    public Map<String, Object> getFrappeDocument(String doctype, String name, User user) {
        try {
            String url = restApiService.buildResourceUrl(doctype, name, null);
            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    url,
                    HttpMethod.GET,
                    null,
                    user,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {

                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return data;
            }
            return null;
        } catch (Exception e) {
            OperationUtils.logStep("Failed to get document " + doctype + "/" + name + ": " + e.getMessage(), this.logger);
            return null;
        }
    }
}
