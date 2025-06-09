package mg.hrms.models;

import java.sql.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    @JsonProperty("name")
    private String employeeId;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("gender")
    private Gender gender; 
    @JsonProperty("date_of_birth")
    private Date dateOfBirth;
    @JsonProperty("date_of_joining")
    private Date dateOfJoining;
    @JsonProperty("company")
    private Company company; 
    @JsonProperty("status")
    private String status;

    private List<SalaryStructureAssignment> salaryAssignments; 

    public String getFullName(){
        return ""+this.getLastName()+" "+this.getFirstName();
    }
}
