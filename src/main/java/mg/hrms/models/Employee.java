package mg.hrms.models;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private String employeeId;
    private String lastName;
    private String firstName;
    private String gender;
    private Date dateOfBirth;
    private Date dateOfJoining;
    private Company company;
}