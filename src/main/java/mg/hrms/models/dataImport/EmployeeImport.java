package mg.hrms.models.dataImport;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Date;

@Data
public class EmployeeImport {
    private String employeeRef;
    private String lastName;
    private String firstName;
    private String gender;
    private String company;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date hireDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date birthDate;
}
