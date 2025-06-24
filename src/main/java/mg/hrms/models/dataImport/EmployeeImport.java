package mg.hrms.models.dataImport;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

import java.sql.Date;

@Data
public class EmployeeImport {
    @CsvBindByName(column = "Ref")
    private String employeeRef;
    
    @CsvBindByName(column = "Nom")
    private String lastName;

    @CsvBindByName(column = "Prenom")
    private String firstName;

    @CsvBindByName(column = "genre")
    private String gender;

    @CsvBindByName(column = "company")
    private String company;

    @CsvBindByName(column = "Date embauche")
    private String hireDate;

    @CsvBindByName(column = "date naissance")
    private String birthDate;

    private Date birthDateValue;
    private Date hireDateValue;
}