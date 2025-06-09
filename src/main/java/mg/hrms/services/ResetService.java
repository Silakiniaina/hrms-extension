package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.User;
import mg.hrms.payload.ResetResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ResetService {

    private static final Logger logger = LoggerFactory.getLogger(ResetService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public ResetService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    public ResetResult processReset(String company, User user) throws Exception {
        logger.info("Processing reset for company: {}", company != null ? company : "All");
        String resetUrl = restApiService.getServerHost() + "/api/method/hrms.api.data_reset.reset_hrms_data";
        String requestParams = company != null && !company.isBlank() ? "?company=" + company + "&verbose=true" : "?verbose=true";

        try {
            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    resetUrl + requestParams,
                    HttpMethod.POST,
                    null,
                    user,
                    new ParameterizedTypeReference<>() {}
            );
            return processResetResponse(response.getBody());
        } catch (Exception e) {
            logger.error("Failed to process reset: {}", e.getMessage(), e);
            throw new Exception("Reset operation failed: " + e.getMessage());
        }
    }

    private ResetResult processResetResponse(Map<String, Object> responseBody) {
        ResetResult result = new ResetResult();
        try {
            logger.debug("Processing reset response: {}", responseBody);
            Map<String, Object> actualData = responseBody.containsKey("message") && responseBody.get("message") instanceof Map
                    ? (Map<String, Object>) responseBody.get("message")
                    : responseBody;

            Boolean success = extractBoolean(actualData.get("success"));
            String message = extractMessage(actualData.get("message"));

            result.setSuccess(success != null && success);
            result.setMessage(message != null ? message : (result.isSuccess() ? "Reset completed successfully" : "Reset failed"));

            if (actualData.containsKey("log")) {
                String logDetails = extractLogDetails(actualData.get("log"));
                if (logDetails != null && !logDetails.isBlank()) {
                    result.setMessage(result.getMessage() + "\n\nDetails:\n" + logDetails);
                }
            }
            logger.info("Reset result: success={}, message={}", result.isSuccess(), result.getMessage());
        } catch (Exception e) {
            logger.error("Error processing reset response: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Error processing reset response: " + e.getMessage());
        }
        return result;
    }

    private String extractMessage(Object messageObj) {
        if (messageObj == null) return null;
        if (messageObj instanceof String) return (String) messageObj;
        if (messageObj instanceof Map) {
            Map<String, Object> messageMap = (Map<String, Object>) messageObj;
            return messageMap.containsKey("message") ? extractMessage(messageMap.get("message"))
                    : messageMap.containsKey("text") ? extractMessage(messageMap.get("text"))
                    : messageMap.containsKey("description") ? extractMessage(messageMap.get("description"))
                    : messageMap.toString();
        }
        return messageObj.toString();
    }

    private Boolean extractBoolean(Object booleanObj) {
        if (booleanObj == null) return null;
        if (booleanObj instanceof Boolean) return (Boolean) booleanObj;
        if (booleanObj instanceof String) return Boolean.parseBoolean((String) booleanObj);
        if (booleanObj instanceof Number) return ((Number) booleanObj).intValue() != 0;
        return null;
    }

    private String extractLogDetails(Object logObj) {
        if (logObj == null) return null;
        StringBuilder logMessage = new StringBuilder();
        if (logObj instanceof List<?> logs) {
            for (Object log : logs) {
                if (log instanceof String) logMessage.append(log).append("\n");
                else if (log instanceof Map<?, ?> logMap && logMap.containsKey("message")) {
                    logMessage.append(extractMessage(logMap.get("message"))).append("\n");
                } else logMessage.append(log).append("\n");
            }
        } else if (logObj instanceof String) logMessage.append(logObj);
        else logMessage.append(logObj.toString());
        return logMessage.toString().trim();
    }
}
