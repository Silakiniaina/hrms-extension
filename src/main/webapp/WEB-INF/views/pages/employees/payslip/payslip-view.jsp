<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.Employee" %>
<%@ page import="mg.hrms.models.SalarySlip" %>
<%@ page import="mg.hrms.models.SalaryComponent" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %> <%-- Import LocalDate --%>

<%
    Employee employee = (Employee) request.getAttribute("employee");
    SalarySlip payslip = (SalarySlip) request.getAttribute("payslip");
    // Changed date format to MMMM yyyy as per your original code
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMMM yyyy");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
%>
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Payslip -
                    <% if (payslip.getPostingDate() != null) { %>
                        <%= payslip.getPostingDate().toLocalDate().format(dateFormat) %>
                    <% } else { %>
                        N/A
                    <% } %>
                </h3>
                <div class="card-tools">
                    <a href="${pageContext.request.contextPath}/employees/payslip/export?employeeId=<%= employee.getEmployeeId() != null ? employee.getEmployeeId() : "" %>&payslipId=<%= payslip.getSlipId() != null ? payslip.getSlipId() : "" %>"
                       class="btn btn-danger btn-sm">
                        <i class="fas fa-file-pdf"></i> Export PDF
                    </a>
                    <a href="${pageContext.request.contextPath}/employees/payslip?employeeId=<%= employee.getEmployeeId() != null ? employee.getEmployeeId() : "" %>"
                       class="btn btn-primary btn-sm">
                        <i class="fas fa-arrow-left"></i> Back
                    </a>
                </div>
            </div>
            <div class="card-body">
                <div class="payslip-container" style="background-color: white; padding: 20px; border-radius: 10px;">
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <h4><%= employee.getCompany() != null ? employee.getCompany().getName() : "N/A" %></h4>
                            <p>Employee: <%= (employee.getFirstName() != null ? employee.getFirstName() : "") + " " +
                                             (employee.getLastName() != null ? employee.getLastName() : "") %></p>
                            <p>Employee ID: <%= employee.getEmployeeId() != null ? employee.getEmployeeId() : "N/A" %></p>
                        </div>
                        <div class="col-md-6 text-right">
                            <h4>Payslip</h4>
                            <p>Period:
                                <% if (payslip.getPostingDate() != null) { %>
                                    <%= payslip.getPostingDate().toLocalDate().format(dateFormat) %>
                                <% } else { %>
                                    N/A
                                <% } %>
                            </p>
                            <p>Status: <%= payslip.getStatus() != null ? payslip.getStatus() : "N/A" %></p>
                        </div>
                    </div>
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
                                    <% if (payslip.getEarnings() != null && !payslip.getEarnings().isEmpty()) {
                                        for (SalaryComponent earning : payslip.getEarnings()) { %>
                                            <tr>
                                                <td><%= earning.getName() != null ? earning.getName() : "N/A" %></td>
                                                <td class="text-right"><%= currencyFormat.format(earning.getAmount()) %></td>
                                            </tr>
                                        <% }} %>
                                    <tr class="table-active">
                                        <td><strong>Total Earnings</strong></td>
                                        <td class="text-right"><strong><%= payslip.getGrossPay() != null ? currencyFormat.format(payslip.getGrossPay()) : "0.00" %></strong></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
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
                                    <% if (payslip.getDeductions() != null && !payslip.getDeductions().isEmpty()) {
                                        for (SalaryComponent deduction : payslip.getDeductions()) { %>
                                            <tr>
                                                <td><%= deduction.getName() != null ? deduction.getName() : "N/A" %></td>
                                                <td class="text-right"><%= currencyFormat.format(deduction.getAmount()) %></td>
                                            </tr>
                                        <% }} %>
                                    <tr class="table-active">
                                        <td><strong>Total Deductions</strong></td>
                                        <td class="text-right"><strong><%= (payslip.getGrossPay() != null && payslip.getNetPay() != null) ?
                                            currencyFormat.format(payslip.getGrossPay() - payslip.getNetPay()) : "0.00" %></strong></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 offset-md-6">
                            <table class="table table-bordered">
                                <tr class="table-success">
                                    <td><strong>Net Pay</strong></td>
                                    <td class="text-right"><strong><%= payslip.getNetPay() != null ? currencyFormat.format(payslip.getNetPay()) : "0.00" %></strong></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <% if (payslip.getBankName() != null && !payslip.getBankName().isBlank()) { %>
                        <div class="row mt-4">
                            <div class="col-12">
                                <h5>Bank Details</h5>
                                <p>Bank: <%= payslip.getBankName() %></p>
                                <p>Account Number: <%= payslip.getBankAccountNo() != null ? payslip.getBankAccountNo() : "N/A" %></p>
                            </div>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</div>
<style>
    .payslip-container {
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
    .table th, .table td {
        padding: 10px;
    }
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
        .card-tools {
            display: none;
        }
    }
</style>
