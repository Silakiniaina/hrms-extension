package mg.hrms.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import mg.hrms.models.User;
import mg.hrms.utils.ApiUtils;

@Service
public class RestApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${erpnext.server.url}")
    private String serverHost;

    /* -------------------------------------------------------------------------- */
    /*                                   Getter                                   */
    /* -------------------------------------------------------------------------- */
    public String getServerHost(){
        return this.serverHost;
    }

    /* -------------------------------------------------------------------------- */
    /*                                 Constructor                                */
    /* -------------------------------------------------------------------------- */
    public RestApiService(RestTemplate r){
        this.restTemplate = r;
    }

    /* -------------------------------------------------------------------------- */
    /*                          API Call for ErpNEXT API                          */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings("deprecation")
    public <T, R> ResponseEntity<R> executeApiCall(String url, HttpMethod method, T requestBody,
            User user, ParameterizedTypeReference<R> responseType) throws Exception {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            ApiUtils.setUserCookie(user, headers);

            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<R> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    responseType);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response;
            }

            throw new Exception("HTTP " + response.getStatusCodeValue());

        } catch (HttpClientErrorException e) {
            String errorMessage;
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                errorMessage = "Unauthorized access to API: Invalid credentials or session";
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                errorMessage = "Permission denied: User lacks access to resource " + url + ". Response: "
                        + e.getResponseBodyAsString();
            } else {
                errorMessage = "API request failed: HTTP " + e.getStatusCode() + ". Response: "
                        + e.getResponseBodyAsString();
            }
            throw new Exception(errorMessage, e);
        } catch (Exception e) {
            throw new Exception("API request failed: " + e.getMessage(), e);
        }
    }
}
