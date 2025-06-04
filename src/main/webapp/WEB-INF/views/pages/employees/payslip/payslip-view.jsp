<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.Employee" %>
<%@ page import="mg.hrms.models.SalarySlip" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
    Employee employee = (Employee) request.getAttribute("employee");
    SalarySlip payslip = (SalarySlip) request.getAttribute("payslip");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
%>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Payslip - <%= dateFormat.format(payslip.getPostingDate()) %></h3>
                <div class="card-tools">
                    <button onclick="window.print()" class="btn btn-secondary btn-sm">
                        <i class="fas fa-print"></i> Print
                    </button>
                    <a href="${pageContext.request.contextPath}/employees/payslip?employeeId=<%= employee.getEmployeeId() %>"
                       class="btn btn-primary btn-sm">
                        <i class="fas fa-arrow-left"></i> Back
                    </a>
                </div>
            </div>
            <div class="card-body">
                <div class="payslip-container" style="background-color: white; padding: 20px;">
                    <!-- Payslip Header -->
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <h4><%= employee.getCompany() %></h4>
                            <p>Employee: <%= employee.getFirstName() %> <%= employee.getLastName() %></p>
                            <p>Employee ID: <%= employee.getEmployeeId() %></p>
                        </div>
                        <div class="col-md-6 text-right">
                            <h4>Payslip</h4>
                            <p>Period: <%= dateFormat.format(payslip.getPostingDate()) %></p>
                            <p>Status: <%= payslip.getStatus() %></p>
                        </div>
                    </div>

                    <!-- Earnings Section -->
                    <div class="row mb-4">
                        <div class="col-12">
                            <h5>Earnings</h5>
                            <table class="table table-bordered">
                                <thead>
                                    <tr>
                                        <th>Description</th>
                                        <th>Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Map<String, Object> earning : payslip.getEarnings()) { %>
                                        <tr>
                                            <td><%= earning.get("salary_component") %></td>
                                            <td class="text-right"><%= currencyFormat.format(earning.get("amount")) %></td>
                                        </tr>
                                    <% } %>
                                    <tr class="table-active">
                                        <td><strong>Total Earnings</strong></td>
                                        <td class="text-right"><strong><%= currencyFormat.format(payslip.getGrossPay()) %></strong></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Deductions Section -->
                    <div class="row mb-4">
                        <div class="col-12">
                            <h5>Deductions</h5>
                            <table class="table table-bordered">
                                <thead>
                                    <tr>
                                        <th>Description</th>
                                        <th>Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Map<String, Object> deduction : payslip.getDeductions()) { %>
                                        <tr>
                                            <td><%= deduction.get("salary_component") %></td>
                                            <td class="text-right"><%= currencyFormat.format(deduction.get("amount")) %></td>
                                        </tr>
                                    <% } %>
                                    <tr class="table-active">
                                        <td><strong>Total Deductions</strong></td>
                                        <td class="text-right"><strong><%= currencyFormat.format(payslip.getGrossPay() - payslip.getNetPay()) %></strong></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Net Pay Section -->
                    <div class="row">
                        <div class="col-md-6 offset-md-6">
                            <table class="table table-bordered">
                                <tr class="table-success">
                                    <td><strong>Net Pay</strong></td>
                                    <td class="text-right"><strong><%= currencyFormat.format(payslip.getNetPay()) %></strong></td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <!-- Bank Details -->
                    <% if (payslip.getBankName() != null && !payslip.getBankName().isEmpty()) { %>
                        <div class="row mt-4">
                            <div class="col-12">
                                <h5>Bank Details</h5>
                                <p>Bank: <%= payslip.getBankName() %></p>
                                <p>Account Number: <%= payslip.getBankAccountNo() %></p>
                            </div>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
@media print {
    body * {
        visibility: hidden;
    }
    .payslip-container, .payslip-container * {
        visibility: visible;
    }
    .payslip-container {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
    }
    .no-print {
        display: none !important;
    }
}
</style>
