package mg.hrms.models.args;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeFilterArgs {
    private String name; 
    private int minAge;
    private int maxAge;
    private String company;
    private String gender;
    private String status;
}
