package mg.hrms.models;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty("name")
    private String employeeId;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("date_of_birth")
    private Date dateOfBirth;

    @JsonProperty("date_of_joining")
    private Date dateOfJoining;

    @JsonProperty("company")
    private String company;

    @JsonProperty("status")
    private String status;
}