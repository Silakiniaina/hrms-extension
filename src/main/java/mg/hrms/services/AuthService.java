package mg.hrms.services;

import java.util.Map;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.User;

@Service
public class AuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${erpnext.server.url}")
    private String serverHost;

    /* -------------------------------------------------------------------------- */
    /* Constructor                                */
    /* -------------------------------------------------------------------------- */
    public AuthService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /* Function to login                             */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "unchecked", "deprecation", "null" })
    public User login(String username, String password) throws Exception {
        try {
            // This URL does not use fields or filters, so no change needed here.
            String url = UriComponentsBuilder
                    .fromHttpUrl(serverHost + "/api/method/login")
                    .build()
                    .toUriString();

            Map<String, String> requestBody = Map.of(
                    "usr", username,
                    "pwd", password
            );
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                if ("Logged In".equals(responseBody.get("message"))) {
                    String userId = (String) responseBody.get("user_id");
                    String fullName = (String) responseBody.get("full_name");
                    String userLang = (String) responseBody.getOrDefault("user_lang", "en");
                    String systemUser = (String) responseBody.getOrDefault("system_user", "yes");
                    String sid = null;

                    // Extraction du cookie sid
                    if (response.getHeaders().get("Set-Cookie") != null) {
                        for (String cookie : response.getHeaders().get("Set-Cookie")) {
                            if (cookie.contains("sid=")) {
                                sid = cookie.split("sid=")[1].split(";")[0];
                                break;
                            }
                        }
                    }

                    return new User(userId, fullName, userLang, systemUser, sid);
                }
                throw new Exception("Invalid response from server");
            }
            throw new Exception("HTTP " + response.getStatusCodeValue());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new Exception("Incorrect username or password");
            }
            throw new Exception("Login failed: HTTP " + e.getStatusCode() + " - " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Login failed: " + e.getMessage());
        }
    }
}
