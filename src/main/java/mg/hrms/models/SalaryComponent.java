package mg.hrms.models;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalaryComponent {
    @JsonProperty("name")
    private String id;
    @JsonProperty("salary_component")
    private String name;
    @JsonProperty("type")
    private String type;
    @JsonProperty("amount")
    private double amount;
}
