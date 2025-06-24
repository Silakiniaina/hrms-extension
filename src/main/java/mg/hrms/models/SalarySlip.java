package mg.hrms.models;

import java.sql.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalarySlip {
    @JsonProperty("name")
    private String slipId;
    @JsonProperty("employee")
    private String employee;
    @JsonProperty("employee_name")
    private String employeeName;
    @JsonProperty("posting_date")
    private Date postingDate;
    @JsonProperty("salary_structure")
    private String salaryStructure;
    @JsonProperty("gross_pay")
    private Double grossPay;
    @JsonProperty("net_pay")
    private Double netPay;
    @JsonProperty("status")
    private String status;
    @JsonProperty("bank_name")
    private String bankName;
    @JsonProperty("bank_account_no")
    private String bankAccountNo;
    @JsonProperty("earnings")
    private List<SalaryComponent> earnings;
    @JsonProperty("deductions")
    private List<SalaryComponent> deductions;

    private SalaryStructure salaryStructureObj;
    private Employee employeeObj;

    public double getBaseSalary(){
        double result = 0;
        for(SalaryComponent component : earnings){
            System.out.println("Component name : "+component.getName());
            if(component.getName().equalsIgnoreCase("salaire base")){
                result = component.getAmount();
                break;
            }
        }
        return result;
    }
}
