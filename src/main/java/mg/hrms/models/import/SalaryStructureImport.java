// SalaryStructureImport.java - Model for salary structure import data
package mg.hrms.models;

import lombok.Data;

@Data
public class SalaryStructureImport {
    private String salaryStructure;
    private String name;
    private String abbreviation;
    private String type;
    private String formula;
    private String company;
}
