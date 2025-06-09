<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.SalaryStats" %>
<%@ page import="mg.hrms.models.SalaryComponent" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    List<SalaryStats> stats = (List<SalaryStats>) request.getAttribute("stats");
    List<String> availableYears = (List<String>) request.getAttribute("availableYears");
    String selectedYear = (String) request.getAttribute("selectedYear");
    Double yearlyGrossPay = (Double) request.getAttribute("yearlyGrossPay");
    Double yearlyNetPay = (Double) request.getAttribute("yearlyNetPay");
    Double yearlyDeductions = (Double) request.getAttribute("yearlyDeductions");

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("fr", "FR"));
    currencyFormat.setMaximumFractionDigits(0);
%>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Salary Statistics</h3>
                <div class="card-tools">
                    <form method="get" action="${pageContext.request.contextPath}/salary-stats" class="form-inline">
                        <div class="input-group input-group-sm">
                            <select name="year" class="form-control" onchange="this.form.submit()">
                                <option value="">Select Year</option>
                                <% if (availableYears != null) {
                                    for (String year : availableYears) {
                                        String selected = year.equals(selectedYear) ? "selected" : "";
                                %>
                                    <option value="<%= year %>" <%= selected %>><%= year %></option>
                                <% }} %>
                            </select>
                        </div>
                    </form>
                </div>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="card-body">
                    <div class="alert alert-danger">
                        <%= request.getAttribute("error") %>
                    </div>
                </div>
            <% } else { %>

                <!-- Yearly Summary Cards -->
                <%--<div class="card-body">
                    <div class="row">
                        <div class="col-lg-4 col-6">
                            <div class="small-box bg-success">
                                <div class="inner">
                                    <h3><%= yearlyGrossPay != null ? currencyFormat.format(yearlyGrossPay) : "0 €" %></h3>
                                    <p>Total Gross Pay (<%= selectedYear != null ? selectedYear : "All Years" %>)</p>
                                </div>
                                <div class="icon">
                                    <i class="fas fa-money-bill-wave"></i>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-4 col-6">
                            <div class="small-box bg-info">
                                <div class="inner">
                                    <h3><%= yearlyNetPay != null ? currencyFormat.format(yearlyNetPay) : "0 €" %></h3>
                                    <p>Total Net Pay (<%= selectedYear != null ? selectedYear : "All Years" %>)</p>
                                </div>
                                <div class="icon">
                                    <i class="fas fa-hand-holding-usd"></i>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-4 col-6">
                            <div class="small-box bg-warning">
                                <div class="inner">
                                    <h3><%= yearlyDeductions != null ? currencyFormat.format(yearlyDeductions) : "0 €" %></h3>
                                    <p>Total Deductions (<%= selectedYear != null ? selectedYear : "All Years" %>)</p>
                                </div>
                                <div class="icon">
                                    <i class="fas fa-minus-circle"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>--%>

                <!-- Monthly Statistics Table -->
                <div class="card-body">
                    <table id="salaryStatsTable" class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>Month</th>
                                <th>Gross Pay</th>
                                <th>Deductions</th>
                                <th>Net Pay</th>
                                <th>Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (stats != null && !stats.isEmpty()) {
                                for (SalaryStats stat : stats) { %>
                                    <tr>
                                        <td><%= stat.getMonthName() %> <%= stat.getYear() %></td>
                                        <td class="text-right">
                                            <%= stat.getTotalGrossPay() != null ? currencyFormat.format(stat.getTotalGrossPay()) : "0 €" %>
                                        </td>
                                        <td class="text-right">
                                            <%= stat.getTotalDeductions() != null ? currencyFormat.format(stat.getTotalDeductions()) : "0 €" %>
                                        </td>
                                        <td class="text-right">
                                            <%= stat.getTotalNetPay() != null ? currencyFormat.format(stat.getTotalNetPay()) : "0 €" %>
                                        </td>
                                        <td>
                                            <button type="button" class="btn btn-primary btn-sm"
                                                data-toggle="modal"
                                                data-target="#detailsModal<%= stat.getYear() %><%= stat.getMonth() %>">
                                                <i class="fas fa-eye"></i> Details
                                            </button>
                                        </td>
                                    </tr>
                            <% }} else { %>
                                <tr>
                                    <td colspan="5" class="text-center">No salary data found for the selected year</td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </div>
    </div>
</div>

<!-- Details Modals -->
<% if (stats != null) {
    for (SalaryStats stat : stats) { %>
        <div class="modal fade" id="detailsModal<%= stat.getYear() %><%= stat.getMonth() %>" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title">Salary Details - <%= stat.getMonthName() %> <%= stat.getYear() %></h4>
                        <button type="button" class="close" data-dismiss="modal">
                            <span>&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <!-- Earnings Details -->
                            <div class="col-md-6">
                                <h5 class="text-success">Earnings</h5>
                                <table class="table table-sm">
                                    <thead>
                                        <tr>
                                            <th>Component</th>
                                            <th class="text-right">Amount</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (stat.getEarningsDetails() != null && !stat.getEarningsDetails().isEmpty()) {
                                            for (SalaryComponent earning : stat.getEarningsDetails()) { %>
                                                <tr>
                                                    <td><%= earning.getName() %></td>
                                                    <td class="text-right"><%= currencyFormat.format(earning.getAmount()) %></td>
                                                </tr>
                                        <% }} else { %>
                                            <tr>
                                                <td colspan="2" class="text-center text-muted">No earnings data</td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                    <tfoot>
                                        <tr class="font-weight-bold">
                                            <td>Total Gross Pay</td>
                                            <td class="text-right text-success">
                                                <%= stat.getTotalGrossPay() != null ? currencyFormat.format(stat.getTotalGrossPay()) : "0 €" %>
                                            </td>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>

                            <!-- Deductions Details -->
                            <div class="col-md-6">
                                <h5 class="text-warning">Deductions</h5>
                                <table class="table table-sm">
                                    <thead>
                                        <tr>
                                            <th>Component</th>
                                            <th class="text-right">Amount</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (stat.getDeductionsDetails() != null && !stat.getDeductionsDetails().isEmpty()) {
                                            for (SalaryComponent deduction : stat.getDeductionsDetails()) { %>
                                                <tr>
                                                    <td><%= deduction.getName() %></td>
                                                    <td class="text-right"><%= currencyFormat.format(deduction.getAmount()) %></td>
                                                </tr>
                                        <% }} else { %>
                                            <tr>
                                                <td colspan="2" class="text-center text-muted">No deductions data</td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                    <tfoot>
                                        <tr class="font-weight-bold">
                                            <td>Total Deductions</td>
                                            <td class="text-right text-warning">
                                                <%= stat.getTotalDeductions() != null ? currencyFormat.format(stat.getTotalDeductions()) : "0 €" %>
                                            </td>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>

                        <hr>
                        <div class="row">
                            <div class="col-12">
                                <h4 class="text-center">
                                    Net Pay:
                                    <span class="badge badge-info badge-lg">
                                        <%= stat.getTotalNetPay() != null ? currencyFormat.format(stat.getTotalNetPay()) : "0 €" %>
                                    </span>
                                </h4>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
<% }} %>

<script>
$(function () {
    $("#salaryStatsTable").DataTable({
        "responsive": true,
        "lengthChange": true,
        "autoWidth": false,
        "buttons": ["copy", "csv", "excel", "pdf", "print"],
        "order": [[ 0, "desc" ]], // Sort by month descending
        "columnDefs": [
            { "orderable": false, "targets": [4] }, // Disable sorting on Details column
            { "type": "date", "targets": [0] } // Treat first column as date for proper sorting
        ]
    }).buttons().container().appendTo('#salaryStatsTable_wrapper .col-md-6:eq(0)');
});
</script>
