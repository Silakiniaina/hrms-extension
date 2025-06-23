package mg.hrms.models.dataImport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class SalaryRecordImport {
    @CsvBindByName(column = "Mois")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private String month;
    @CsvBindByName(column = "Ref Employe")
    private String employeeRef;
    @CsvBindByName(column = "Salaire Base")
    private Double baseSalary;
    @CsvBindByName(column = "Salaire")
    private String salaryStructure;
}