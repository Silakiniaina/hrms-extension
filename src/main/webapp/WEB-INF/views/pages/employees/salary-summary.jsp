<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.SalarySummary" %>
<%
    List<SalarySummary> summaries = (List<SalarySummary>) request.getAttribute("summaries");
    String selectedMonth = (String) request.getAttribute("selectedMonth");
    String selectedYear = (String) request.getAttribute("selectedYear");
%>
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Monthly Salary Summary</h3>
            </div>
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/salary-summary">
                    <div class="row mb-3">
                        <div class="col-md-3">
                            <select name="month" class="form-control">
                                <option value="">All Months</option>
                                <option value="01" <%= "01".equals(selectedMonth) ? "selected" : "" %>>January</option>
                                <option value="02" <%= "02".equals(selectedMonth) ? "selected" : "" %>>February</option>
                                <option value="03" <%= "03".equals(selectedMonth) ? "selected" : "" %>>March</option>
                                <option value="04" <%= "04".equals(selectedMonth) ? "selected" : "" %>>April</option>
                                <option value="05" <%= "05".equals(selectedMonth) ? "selected" : "" %>>May</option>
                                <option value="06" <%= "06".equals(selectedMonth) ? "selected" : "" %>>June</option>
                                <option value="07" <%= "07".equals(selectedMonth) ? "selected" : "" %>>July</option>
                                <option value="08" <%= "08".equals(selectedMonth) ? "selected" : "" %>>August</option>
                                <option value="09" <%= "09".equals(selectedMonth) ? "selected" : "" %>>September</option>
                                <option value="10" <%= "10".equals(selectedMonth) ? "selected" : "" %>>October</option>
                                <option value="11" <%= "11".equals(selectedMonth) ? "selected" : "" %>>November</option>
                                <option value="12" <%= "12".equals(selectedMonth) ? "selected" : "" %>>December</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <input type="text" name="year" class="form-control" placeholder="Year" value="<%= selectedYear != null ? selectedYear : "" %>">
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary">Filter</button>
                            <a href="${pageContext.request.contextPath}/salary-summary" class="btn btn-secondary">Reset</a>
                        </div>
                    </div>
                </form>
                <table id="summaryTable" class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <th>Year</th>
                            <th>Month</th>
                            <th>Employee</th>
                            <th>Gross Pay</th>
                            <th>Total Deduction</th>
                            <th>Net Pay</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (summaries != null && !summaries.isEmpty()) {
                            for (SalarySummary summary : summaries) { %>
                                <tr>
                                    <td><%= summary.getYear() != null ? summary.getYear() : "N/A" %></td>
                                    <td><%= summary.getMonth() != null ? summary.getMonthName() : "N/A" %></td>
                                    <td><%= summary.getEmployee() != null ? summary.getEmployee() : "N/A" %></td>
                                    <td class="text-right"><%= String.format("%,.2f", summary.getGrossPay()) %></td>
                                    <td class="text-right"><%= String.format("%,.2f", summary.getTotalDeduction()) %></td>
                                    <td class="text-right"><%= String.format("%,.2f", summary.getNetPay()) %></td>
                                    <td><%= summary.getStatus() != null ? summary.getStatus() : "N/A" %></td>
                                    <%--<td>
                                        <button class="btn btn-sm btn-info view-details"
                                            data-employee-id="<%= summary.getEmployee() != null ? summary.getEmployee().getEmployeeId() : "" %>"
                                            data-employee-name="<%= summary.getEmployee() != null ? (summary.getEmployee().getFirstName() + " " + summary.getEmployee().getLastName()) : "N/A" %>"
                                            data-gross-pay="<%= String.format("%,.2f", summary.getGrossPay()) %>"
                                            data-total-deduction="<%= String.format("%,.2f", summary.getTotalDeduction()) %>"
                                            data-net-pay="<%= String.format("%,.2f", summary.getNetPay()) %>"
                                            data-posting-date="<%= summary.getPostingDate() != null ? summary.getPostingDate() : "N/A" %>"
                                            data-status="<%= summary.getStatus() != null ? summary.getStatus() : "N/A" %>">
                                            <i class="fas fa-eye"></i> View
                                        </button>
                                    </td>--%>
                                </tr>
                        <% }} else { %>
                            <tr>
                                <td colspan="7" class="text-center">No data available</td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="detailsModal" tabindex="-1" role="dialog" aria-labelledby="detailsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="detailsModalLabel">Employee Salary Details</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <table class="table table-bordered">
                    <tbody>
                        <tr>
                            <th>Employee ID</th>
                            <td id="modalEmployeeId"></td>
                        </tr>
                        <tr>
                            <th>Employee Name</th>
                            <td id="modalEmployeeName"></td>
                        </tr>
                        <tr>
                            <th>Gross Pay</th>
                            <td id="modalGrossPay"></td>
                        </tr>
                        <tr>
                            <th>Total Deduction</th>
                            <td id="modalTotalDeduction"></td>
                        </tr>
                        <tr>
                            <th>Net Pay</th>
                            <td id="modalNetPay"></td>
                        </tr>
                        <tr>
                            <th>Posting Date</th>
                            <td id="modalPostingDate"></td>
                        </tr>
                        <tr>
                            <th>Status</th>
                            <td id="modalStatus"></td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<%!
    private String getMonthName(String monthNumber) {
        if (monthNumber == null) return "N/A";
        switch (monthNumber) {
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
            default: return monthNumber;
        }
    }
%>
<script>
$(document).ready(function() {
    $('#summaryTable').DataTable({
        "responsive": true,
        "lengthChange": true,
        "autoWidth": false,
        "buttons": ["copy", "csv", "excel", "pdf", "print"]
    }).buttons().container().appendTo('#summaryTable_wrapper .col-md-6:eq(0)');
    $('.view-details').click(function() {
        $('#modalEmployeeId').text($(this).data('employee-id'));
        $('#modalEmployeeName').text($(this).data('employee-name'));
        $('#modalGrossPay').text($(this).data('gross-pay'));
        $('#modalTotalDeduction').text($(this).data('total-deduction'));
        $('#modalNetPay').text($(this).data('net-pay'));
        $('#modalPostingDate').text($(this).data('posting-date'));
        $('#modalStatus').text($(this).data('status'));
        $('#detailsModalLabel').text('Employee Salary Details - ' + $(this).data('employee-name'));
        $('#detailsModal').modal('show');
    });
});
</script>
<style>
    .card, .modal-content {
        border-radius: 10px;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
    .table th, .table td {
        padding: 10px;
    }
    .btn {
        border-radius: 5px;
    }
</style>
