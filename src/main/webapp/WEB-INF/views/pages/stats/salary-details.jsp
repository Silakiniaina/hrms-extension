<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.EmployeeSalaryDetail" %>
<%@ page import="mg.hrms.models.SalaryComponent" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    List<EmployeeSalaryDetail> employeeDetails = (List<EmployeeSalaryDetail>) request.getAttribute("employeeDetails");
    String selectedYear = (String) request.getAttribute("selectedYear");
    String selectedMonth = (String) request.getAttribute("selectedMonth");
    String monthName = (String) request.getAttribute("monthName");
    Double monthlyTotalGrossPay = (Double) request.getAttribute("monthlyTotalGrossPay");
    Double monthlyTotalNetPay = (Double) request.getAttribute("monthlyTotalNetPay");
    Double monthlyTotalDeductions = (Double) request.getAttribute("monthlyTotalDeductions");

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("fr", "FR"));
    currencyFormat.setMaximumFractionDigits(0);
%>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Employee Salary Details - <%= monthName %> <%= selectedYear %></h3>
                <div class="card-tools">
                    <a href="${pageContext.request.contextPath}/salary-stats?year=<%= selectedYear %>"
                       class="btn btn-secondary btn-sm">
                        <i class="fas fa-arrow-left"></i> Back to Statistics
                    </a>
                </div>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="card-body">
                    <div class="alert alert-danger">
                        <%= request.getAttribute("error") %>
                    </div>
                </div>
            <% } else { %>

                <!-- Monthly Summary Cards
                <div class="card-body">
                    <div class="row">
                        <div class="col-lg-4 col-6">
                            <div class="small-box bg-success">
                                <div class="inner">
                                    <h3><%= monthlyTotalGrossPay != null ? currencyFormat.format(monthlyTotalGrossPay) : "0 €" %></h3>
                                    <p>Total Gross Pay - <%= monthName %></p>
                                </div>
                                <div class="icon">
                                    <i class="fas fa-money-bill-wave"></i>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-4 col-6">
                            <div class="small-box bg-warning">
                                <div class="inner">
                                    <h3><%= monthlyTotalDeductions != null ? currencyFormat.format(monthlyTotalDeductions) : "0 €" %></h3>
                                    <p>Total Deductions - <%= monthName %></p>
                                </div>
                                <div class="icon">
                                    <i class="fas fa-minus-circle"></i>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-4 col-6">
                            <div class="small-box bg-info">
                                <div class="inner">
                                    <h3><%= monthlyTotalNetPay != null ? currencyFormat.format(monthlyTotalNetPay) : "0 €" %></h3>
                                    <p>Total Net Pay - <%= monthName %></p>
                                </div>
                                <div class="icon">
                                    <i class="fas fa-hand-holding-usd"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div> -->

                <!-- Employee Details Table -->
                <div class="card-body">
                    <table id="employeeDetailsTable" class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>Employee</th>
                                <th>Gross Pay</th>
                                <th>Deductions</th>
                                <th>Net Pay</th>
                                <th>Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (employeeDetails != null && !employeeDetails.isEmpty()) {
                                for (EmployeeSalaryDetail detail : employeeDetails) { %>
                                    <tr>
                                        <td>
                                            <strong><%= detail.getEmployeeName() != null ? detail.getEmployeeName() : "N/A" %></strong>
                                            <br>
                                            <small class="text-muted">ID: <%= detail.getEmployeeId() %></small>
                                        </td>
                                        <td class="text-right">
                                            <%= detail.getTotalGrossPay() != null ? currencyFormat.format(detail.getTotalGrossPay()) : "0 €" %>
                                        </td>
                                        <td class="text-right">
                                            <%= detail.getTotalDeductions() != null ? currencyFormat.format(detail.getTotalDeductions()) : "0 €" %>
                                        </td>
                                        <td class="text-right">
                                            <strong><%= detail.getTotalNetPay() != null ? currencyFormat.format(detail.getTotalNetPay()) : "0 €" %></strong>
                                        </td>
                                        <td>
                                            <button type="button" class="btn btn-primary btn-sm"
                                                data-toggle="modal"
                                                data-target="#employeeModal<%= detail.getEmployeeId().replaceAll("[^a-zA-Z0-9]", "") %>">
                                                <i class="fas fa-eye"></i> View Details
                                            </button>
                                        </td>
                                    </tr>
                            <% }} else { %>
                                <tr>
                                    <td colspan="5" class="text-center">No employee salary data found for <%= monthName %> <%= selectedYear %></td>
                                </tr>
                            <% } %>
                        </tbody>
                        <tfoot>
                            <tr class="font-weight-bold bg-light">
                                <td>TOTAL</td>
                                <td class="text-right"><%= monthlyTotalGrossPay != null ? currencyFormat.format(monthlyTotalGrossPay) : "0 €" %></td>
                                <td class="text-right"><%= monthlyTotalDeductions != null ? currencyFormat.format(monthlyTotalDeductions) : "0 €" %></td>
                                <td class="text-right"><%= monthlyTotalNetPay != null ? currencyFormat.format(monthlyTotalNetPay) : "0 €" %></td>
                                <td></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            <% } %>
        </div>
    </div>
</div>

<!-- Employee Details Modals -->
<% if (employeeDetails != null) {
    for (EmployeeSalaryDetail detail : employeeDetails) {
        String modalId = detail.getEmployeeId().replaceAll("[^a-zA-Z0-9]", "");
%>
        <div class="modal fade" id="employeeModal<%= modalId %>" tabindex="-1" role="dialog">
            <div class="modal-dialog modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title">
                            <%= detail.getEmployeeName() %> - <%= monthName %> <%= selectedYear %>
                        </h4>
                        <button type="button" class="close" data-dismiss="modal">
                            <span>&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <!-- Employee Info -->
                            <div class="col-12 mb-3">
                                <div class="card bg-light">
                                    <div class="card-body">
                                        <h5>Employee Information</h5>
                                        <p><strong>Name:</strong> <%= detail.getEmployeeName() %></p>
                                        <p><strong>Employee ID:</strong> <%= detail.getEmployeeId() %></p>
                                        <p><strong>Period:</strong> <%= monthName %> <%= selectedYear %></p>
                                        <p><strong>Number of Salary Slips:</strong>
                                           <%= detail.getSalarySlips() != null ? detail.getSalarySlips().size() : 0 %>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <!-- Earnings Details -->
                            <div class="col-md-6">
                                <h5 class="text-success">Earnings Breakdown</h5>
                                <table class="table table-sm table-bordered">
                                    <thead class="thead-light">
                                        <tr>
                                            <th>Component</th>
                                            <th class="text-right">Amount</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (detail.getEarningsDetails() != null && !detail.getEarningsDetails().isEmpty()) {
                                            for (SalaryComponent earning : detail.getEarningsDetails()) { %>
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
                                    <tfoot class="thead-dark">
                                        <tr>
                                            <th>Total Gross Pay</th>
                                            <th class="text-right text-success">
                                                <%= detail.getTotalGrossPay() != null ? currencyFormat.format(detail.getTotalGrossPay()) : "0 €" %>
                                            </th>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>

                            <!-- Deductions Details -->
                            <div class="col-md-6">
                                <h5 class="text-warning">Deductions Breakdown</h5>
                                <table class="table table-sm table-bordered">
                                    <thead class="thead-light">
                                        <tr>
                                            <th>Component</th>
                                            <th class="text-right">Amount</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% if (detail.getDeductionsDetails() != null && !detail.getDeductionsDetails().isEmpty()) {
                                            for (SalaryComponent deduction : detail.getDeductionsDetails()) { %>
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
                                    <tfoot class="thead-dark">
                                        <tr>
                                            <th>Total Deductions</th>
                                            <th class="text-right text-warning">
                                                <%= detail.getTotalDeductions() != null ? currencyFormat.format(detail.getTotalDeductions()) : "0 €" %>
                                            </th>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>

                        <hr>
                        <div class="row">
                            <div class="col-12">
                                <div class="text-center">
                                    <h4>
                                        Final Net Pay:
                                        <span class="badge badge-info badge-lg">
                                            <%= detail.getTotalNetPay() != null ? currencyFormat.format(detail.getTotalNetPay()) : "0 €" %>
                                        </span>
                                    </h4>
                                </div>
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
    $("#employeeDetailsTable").DataTable({
        "responsive": true,
        "lengthChange": true,
        "autoWidth": false,
        "buttons": ["copy", "csv", "excel", "pdf", "print"],
        "order": [[ 0, "asc" ]], // Sort by employee name ascending
        "columnDefs": [
            { "orderable": false, "targets": [4] } // Disable sorting on Details column
        ]
    }).buttons().container().appendTo('#employeeDetailsTable_wrapper .col-md-6:eq(0)');
});
</script>
