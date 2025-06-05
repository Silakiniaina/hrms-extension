// SalaryRecordImport.java - Model for salary record import data
package mg.hrms.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class SalaryRecordImport {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private String month;

    private String employeeRef;
    private Double baseSalary;
    private String salaryStructure;
}
