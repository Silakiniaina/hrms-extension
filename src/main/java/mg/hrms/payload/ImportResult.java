package mg.hrms.payload;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ImportResult {
    private boolean success;
    private String message;
    private Map<String, List<String>> errors = new HashMap<>();
    private Map<String, Object> counts = new HashMap<>();
    private Map<String, List<String>> warnings = new HashMap<>();

    public boolean hasErrors() {
        return errors.values().stream().anyMatch(list -> list != null && !list.isEmpty());
    }

    public boolean hasWarnings() {
        return warnings.values().stream().anyMatch(list -> list != null && !list.isEmpty());
    }

    public int getTotalRecordsCreated() {
        return counts.values().stream()
                .mapToInt(value -> value instanceof Number ? ((Number) value).intValue() : 0)
                .sum();
    }

    public void addError(String category, String error) {
        errors.computeIfAbsent(category, k -> new ArrayList<>()).add(error);
    }

    public void addWarning(String category, String warning) {
        warnings.computeIfAbsent(category, k -> new ArrayList<>()).add(warning);
    }
}
