package mg.hrms.services;

import mg.hrms.models.User;
import mg.hrms.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Service
public class RestApiService {

    private static final Logger logger = LoggerFactory.getLogger(RestApiService.class);
    private final RestTemplate restTemplate;
    @Value("${erpnext.server.url}")
    private String serverHost;

    public RestApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getServerHost() {
        return serverHost;
    }

    @SuppressWarnings("deprecation")
    public <T, R> ResponseEntity<R> executeApiCall(String url, HttpMethod method, T requestBody,
                                                   User user, ParameterizedTypeReference<R> responseType) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            ApiUtils.setUserCookie(user, headers);

            HttpEntity<?> entity = (requestBody == null) ? new HttpEntity<>(headers) : new HttpEntity<>(requestBody, headers);
            logger.debug("Executing API call: {} {}", method, url);

            ResponseEntity<R> response = restTemplate.exchange(
                    UriComponentsBuilder.fromHttpUrl(url).build(true).toUri(),
                    method, entity, responseType);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("API call successful: {} {}", method, url);
                return response;
            }
            logger.error("API call failed: {} {} with status: {}", method, url, response.getStatusCodeValue());
            throw new Exception("HTTP " + response.getStatusCode().value());
        } catch (HttpClientErrorException e) {
            logger.error("API call failed: {} {} with status: {}", method, url, e.getStatusCode());
            throw new Exception(buildErrorMessage(url, e), e);
        } catch (Exception e) {
            logger.error("API call failed: {} {} with error: {}", method, url, e.getMessage());
            throw new Exception("API request failed: " + e.getMessage(), e);
        }
    }

    public String buildResourceUrl(String doctype, String resourceId, String[] fields) {
        return ApiUtils.buildResourceUrl(serverHost, doctype, resourceId, fields);
    }

    public String buildUrl(String doctype, String[] fields, List<String[]> filters) {
        return ApiUtils.buildUrl(serverHost, doctype, fields, filters);
    }

    public List<String[]> buildDobFilters(int minAge, int maxAge) {
        return ApiUtils.buildDobFilters(minAge, maxAge);
    }

    private String buildErrorMessage(String url, HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return "Unauthorized access to API: Invalid credentials or session";
        } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
            return "Permission denied: User lacks access to resource " + url + ". Response: " + e.getResponseBodyAsString();
        }
        return "API request failed: HTTP " + e.getStatusCode() + ". Response: " + e.getResponseBodyAsString();
    }
}