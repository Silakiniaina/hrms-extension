// ImportData.java - Main container for all import data
package mg.hrms.models;

import java.util.List;

import lombok.Data;

@Data
public class ImportData {
    private List<EmployeeImport> employees;
    private List<SalaryStructureImport> salaryStructures;
    private List<SalaryRecordImport> salaryRecords;
}
