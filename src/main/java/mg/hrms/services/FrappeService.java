package mg.hrms.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /* -------------------------------------------------------------------------- */
    /*                        Find a document with its name                       */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "null", "unchecked" })
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
            OperationUtils.logStep("Failed to get document " + doctype + "/" + name + ": " + e.getMessage(), logger);
            return null;
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                       Search for object with filters                       */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "unchecked", "null" })
    public List<Map<String, Object>> searchFrappeDocuments(String doctype, Map<String, Object> filters, User user) {
        try {
            // Convertir les filtres Map en List<String[]> pour buildUrl
            List<String[]> filtersList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                filtersList.add(new String[] { entry.getKey(), "=", entry.getValue().toString() });
            }

            String url = restApiService.buildUrl(doctype, null, filtersList);
            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    url,
                    HttpMethod.GET,
                    null,
                    user,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {

                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                return data;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            OperationUtils.logStep("Failed to search documents " + doctype + ": " + e.getMessage(), logger);
            return new ArrayList<>();
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                              Create a document                             */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "unchecked", "null" })
    public String createFrappeDocument(String doctype, Map<String, Object> data, User user, boolean submit) {
        try {

            System.out.println(data);
            String url = restApiService.buildUrl(doctype, null, null);
            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    url,
                    HttpMethod.POST,
                    data,
                    user,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {

                Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
                String resultName = (String) responseData.get("name");
                if (resultName != null && submit) {
                    submitFrappeDocument(doctype, resultName, user);
                }
                return resultName;
            }
            return null;
        } catch (Exception e) {
            OperationUtils.logStep("Failed to create document " + doctype + ": " + e.getMessage(), logger);
            return null;
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                              Submit a document                             */
    /* -------------------------------------------------------------------------- */
    public boolean submitFrappeDocument(String doctype, String name, User user) {
        try {
            String url = restApiService.buildResourceUrl(doctype, name, null);
            Map<String, Object> data = new HashMap<>();
            data.put("docstatus", 1);

            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    url,
                    HttpMethod.PUT,
                    data,
                    user,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            OperationUtils.logStep("Failed to submit document " + doctype + "/" + name + ": " + e.getMessage(), logger);
            return false;
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                              Cancel a document                             */
    /* -------------------------------------------------------------------------- */
    public boolean cancelFrappeDocument(String doctype, String name, User user) {
        try {
            String url = restApiService.buildResourceUrl(doctype, name, null);
            Map<String, Object> data = new HashMap<>();
            data.put("docstatus", 2);

            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    url,
                    HttpMethod.PUT,
                    data,
                    user,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            OperationUtils.logStep("Failed to cancel document " + doctype + "/" + name + ": " + e.getMessage(), logger);
            return false;
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                              Delete a document                             */
    /* -------------------------------------------------------------------------- */
    public boolean deleteFrappeDocument(String doctype, String name, User user) {
        try {
            String url = restApiService.buildResourceUrl(doctype, name, null);
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);

            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    url,
                    HttpMethod.DELETE,
                    data,
                    user,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            OperationUtils.logStep("Failed to delete document " + doctype + "/" + name + ": " + e.getMessage(), logger);
            return false;
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                              Update a document                             */
    /* -------------------------------------------------------------------------- */
    @SuppressWarnings({ "unchecked", "null" })
    public String updateFrappeDocument(String doctype, String name, Map<String, Object> data, User user) {
        try {
            String url = restApiService.buildResourceUrl(doctype, name, null);
            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    url,
                    HttpMethod.PUT,
                    data,
                    user,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {

                Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("data");
                return (String) responseData.get("name");
            }
            return null;
        } catch (Exception e) {
            OperationUtils.logStep("Failed to update document " + doctype + "/" + name + ": " + e.getMessage(), logger);
            return null;
        }
    }
}
