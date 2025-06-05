package mg.hrms.payload;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ImportResult {
    private boolean success;
    private String message;
    private Map<String, List<String>> errors;
    private Map<String, Object> counts;

    public ImportResult() {
        this.errors = new HashMap<>();
        this.counts = new HashMap<>();
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors != null ? errors : new HashMap<>();
    }

    public Map<String, Object> getCounts() {
        return counts;
    }

    public void setCounts(Map<String, Object> counts) {
        this.counts = counts != null ? counts : new HashMap<>();
    }

    // Utility methods
    public boolean hasErrors() {
        return errors != null && errors.values().stream()
            .anyMatch(errorList -> errorList != null && !errorList.isEmpty());
    }

    public int getTotalRecordsCreated() {
        if (counts == null) return 0;

        return counts.values().stream()
            .mapToInt(value -> value instanceof Number ? ((Number) value).intValue() : 0)
            .sum();
    }

    public void addError(String category, String error) {
        if (errors == null) {
            errors = new HashMap<>();
        }
        errors.computeIfAbsent(category, k -> new ArrayList<>()).add(error);
    }

    @Override
    public String toString() {
        return "ImportResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", totalRecords=" + getTotalRecordsCreated() +
                ", hasErrors=" + hasErrors() +
                '}';
    }
}
