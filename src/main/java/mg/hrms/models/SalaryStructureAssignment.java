package mg.hrms.models;

import java.util.Date;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalaryStructureAssignment {
    @JsonProperty("employee")
    private String employeeId;
    @JsonProperty("salary_structure")
    private String salaryStructureStr;
    @JsonProperty("from_date")
    private Date fromDate;

    private SalaryStructure salaryStructure;
}
