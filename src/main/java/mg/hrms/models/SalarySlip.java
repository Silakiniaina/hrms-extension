package mg.hrms.models;

import java.sql.Date;
import java.util.List;
import java.util.Map;

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
public class SalarySlip {
    @JsonProperty("name")
    private String slipId;

    @JsonProperty("employee")
    private String employeeId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("posting_date")
    private Date postingDate;

    @JsonProperty("gross_pay")
    private Double grossPay;

    @JsonProperty("net_pay")
    private Double netPay;

    @JsonProperty("status")
    private String status;

    @JsonProperty("earnings")
    private List<Map<String, Object>> earnings;

    @JsonProperty("deductions")
    private List<Map<String, Object>> deductions;

    @JsonProperty("company")
    private String company;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("bank_account_no")
    private String bankAccountNo;
}
