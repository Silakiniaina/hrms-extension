package mg.hrms.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.User;
import mg.hrms.payload.ResetResult;
import java.util.Map;
import java.util.List;

@Service
public class ResetService {

    private final Logger logger = LoggerFactory.getLogger(ResetService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public ResetService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    public ResetResult processReset(String company, User user) throws Exception {
        String resetUrl = restApiService.getServerHost() + "/api/method/hrms.api.data_reset.reset_hrms_data";

        // Build request parameters
        String requestParams = "";
        if (company != null && !company.isEmpty()) {
            requestParams = "?company=" + company + "&verbose=true";
        } else {
            requestParams = "?verbose=true";
        }

        ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
            resetUrl + requestParams,
            HttpMethod.POST,
            null, // No request body for POST
            user,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        return processResetResponse(response.getBody());
    }

    private ResetResult processResetResponse(Map<String, Object> responseBody) {
        ResetResult result = new ResetResult();

        try {
            logger.debug("Processing response body: {}", responseBody);

            // ERPNext might wrap the response in a "message" field
            Map<String, Object> actualData = responseBody;

            // Check if response is wrapped (common in ERPNext responses)
            if (responseBody.containsKey("message") && responseBody.get("message") instanceof Map) {
                actualData = (Map<String, Object>) responseBody.get("message");
                logger.debug("Found wrapped response, extracting actual data: {}", actualData);
            }

            // Now process the actual data
            if (actualData.containsKey("success") || actualData.containsKey("message")) {
                Object messageObj = actualData.get("message");
                String message = extractMessage(messageObj);
                Boolean success = extractBoolean(actualData.get("success"));

                result.setSuccess(success != null && success);
                result.setMessage(message != null ? message : (result.isSuccess() ? "Reset completed successfully" : "Reset failed"));

                // If there are log messages, append them to the message
                if (actualData.containsKey("log")) {
                    String logDetails = extractLogDetails(actualData.get("log"));
                    if (logDetails != null && !logDetails.isEmpty()) {
                        result.setMessage(result.getMessage() + "\n\nDetails:\n" + logDetails);
                    }
                }
            } else {
                // Handle unexpected response format
                result.setSuccess(false);
                result.setMessage("Unexpected response format from server: " + responseBody.toString());
            }
        } catch (Exception e) {
            logger.error("Error processing reset response. Response body: {}", responseBody, e);
            result.setSuccess(false);
            result.setMessage("Error processing reset response: " + e.getMessage());
        }

        // Log the result
        if (result.isSuccess()) {
            logger.info("Reset successful: {}", result.getMessage());
        } else {
            logger.warn("Reset failed: {}", result.getMessage());
        }

        return result;
    }

    private String extractMessage(Object messageObj) {
        if (messageObj == null) {
            return null;
        }

        if (messageObj instanceof String) {
            return (String) messageObj;
        } else if (messageObj instanceof Map) {
            // If message is a nested object, try to extract meaningful information
            Map<String, Object> messageMap = (Map<String, Object>) messageObj;

            // Common patterns for nested message objects
            if (messageMap.containsKey("message")) {
                return extractMessage(messageMap.get("message"));
            } else if (messageMap.containsKey("text")) {
                return extractMessage(messageMap.get("text"));
            } else if (messageMap.containsKey("description")) {
                return extractMessage(messageMap.get("description"));
            } else {
                // Convert the entire map to a readable string
                return messageMap.toString();
            }
        } else {
            // For any other type, convert to string
            return messageObj.toString();
        }
    }

    private Boolean extractBoolean(Object booleanObj) {
        if (booleanObj == null) {
            return null;
        }

        if (booleanObj instanceof Boolean) {
            return (Boolean) booleanObj;
        } else if (booleanObj instanceof String) {
            return Boolean.valueOf((String) booleanObj);
        } else if (booleanObj instanceof Number) {
            return ((Number) booleanObj).intValue() != 0;
        }

        return null;
    }

    private String extractLogDetails(Object logObj) {
        if (logObj == null) {
            return null;
        }

        StringBuilder logMessage = new StringBuilder();

        if (logObj instanceof List) {
            List<?> logs = (List<?>) logObj;
            for (Object log : logs) {
                if (log instanceof String) {
                    logMessage.append((String) log).append("\n");
                } else if (log instanceof Map) {
                    // Handle log entries that might be objects
                    Map<String, Object> logMap = (Map<String, Object>) log;
                    if (logMap.containsKey("message")) {
                        logMessage.append(extractMessage(logMap.get("message"))).append("\n");
                    } else {
                        logMessage.append(logMap.toString()).append("\n");
                    }
                } else {
                    logMessage.append(log.toString()).append("\n");
                }
            }
        } else if (logObj instanceof String) {
            logMessage.append((String) logObj);
        } else {
            logMessage.append(logObj.toString());
        }

        return logMessage.toString().trim();
    }
}
