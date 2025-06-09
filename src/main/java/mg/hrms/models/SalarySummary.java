package mg.hrms.models;

import java.sql.Date;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalarySummary {
    @JsonProperty("employee_name")
    private String employee;
    @JsonProperty("month")
    private String month;
    @JsonProperty("year")
    private String year;
    @JsonProperty("gross_pay")
    private double grossPay;
    @JsonProperty("total_deduction")
    private double totalDeduction;
    @JsonProperty("net_pay")
    private double netPay;
    @JsonProperty("posting_date")
    private Date postingDate;
    @JsonProperty("status")
    private String status;

    /* -------------------------------------------------------------------------- */
    /*              Get the month name value according to its number              */
    /* -------------------------------------------------------------------------- */
    public String getMonthName() {
        switch(month) {
            case "01": return "January";
            case "02": return "February";
            case "03": return "March";
            case "04": return "April";
            case "05": return "May";
            case "06": return "June";
            case "07": return "July";
            case "08": return "August";
            case "09": return "September";
            case "10": return "October";
            case "11": return "November";
            case "12": return "December";
            default: return month;
        }
    }
}
