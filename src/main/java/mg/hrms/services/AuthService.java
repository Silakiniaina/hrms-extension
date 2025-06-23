package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${erpnext.server.url}")
    private String serverHost;

    public AuthService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /*                              Method for login                              */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "deprecation", "null" })
    public User login(String username, String password) throws Exception {
        logger.info("Attempting login for user: {}", username);
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(serverHost + "/api/method/login")
                    .build()
                    .toUriString();

            Map<String, String> requestBody = Map.of("usr", username, "pwd", password);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, new org.springframework.core.ParameterizedTypeReference<>() {});

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if ("Logged In".equals(responseBody.get("message"))) {
                    String sid = response.getHeaders().get("Set-Cookie")
                            .stream()
                            .filter(cookie -> cookie.contains("sid="))
                            .map(cookie -> cookie.split("sid=")[1].split(";")[0])
                            .findFirst()
                            .orElse(null);

                    User user = new User(
                            (String) responseBody.get("user_id"),
                            (String) responseBody.get("full_name"),
                            (String) responseBody.getOrDefault("user_lang", "en"),
                            (String) responseBody.getOrDefault("system_user", "yes"),
                            sid
                    );
                    logger.info("Login successful for user: {}", username);
                    return user;
                }
                logger.error("Invalid response from server for user: {}", username);
                throw new Exception("Invalid response from server");
            }
            logger.error("Login failed for user: {} with HTTP status: {}", username, response.getStatusCodeValue());
            throw new Exception("HTTP " + response.getStatusCodeValue());
        } catch (HttpClientErrorException e) {
            logger.error("Login failed for user: {} with HTTP status: {}", username, e.getStatusCode());
            throw new Exception(e.getStatusCode() == HttpStatus.UNAUTHORIZED
                    ? "Incorrect username or password"
                    : "Login failed: HTTP " + e.getStatusCode() + " - " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Login failed for user: {} with error: {}", username, e.getMessage());
            throw new Exception("Login failed: " + e.getMessage(), e);
        }
    }
}