package mg.hrms.models.dataImport;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class SalaryStructureImport {
    @CsvBindByName(column = "salary structure")
    private String salaryStructure;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "Abbr")
    private String abbreviation;

    @CsvBindByName(column = "type")
    private String type;

    @CsvBindByName(column = "valeur")
    private String valeur; 
    private String formula;
    
    @CsvBindByName(column = "company")
    private String company;
}