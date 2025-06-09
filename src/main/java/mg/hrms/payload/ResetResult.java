package mg.hrms.payload;

import lombok.Data;

@Data
public class ResetResult {
    private boolean success;
    private String message;
}
