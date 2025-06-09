<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.Employee" %>
<%@ page import="mg.hrms.models.SalarySlip" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %> <%-- Import LocalDate --%>

<%
    Employee employee = (Employee) request.getAttribute("employee");
    List<SalarySlip> salaries = (List<SalarySlip>) request.getAttribute("salaries");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
%>
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Employee Details</h3>
                <div class="card-tools">
                    <a href="${pageContext.request.contextPath}/employees" class="btn btn-secondary btn-sm">
                        <i class="fas fa-arrow-left"></i> Back to List
                    </a>
                </div>
            </div>
            <div class="card-body">
                <div class="row mb-4">
                    <div class="col-md-6">
                        <h4>Personal Information</h4>
                        <table class="table table-bordered">
                            <tr>
                                <th>Employee ID</th>
                                <td><%= employee.getEmployeeId() != null ? employee.getEmployeeId() : "N/A" %></td>
                            </tr>
                            <tr>
                                <th>Full Name</th>
                                <td><%= (employee.getFirstName() != null ? employee.getFirstName() : "") + " " +
                                        (employee.getLastName() != null ? employee.getLastName() : "") %></td>
                            </tr>
                            <tr>
                                <th>Gender</th>
                                <td><%= employee.getGender() != null ? employee.getGender() : "N/A" %></td>
                            </tr>
                            <tr>
                                <th>Date of Birth</th>
                                <td>
                                    <% if (employee.getDateOfBirth() != null) {
                                        LocalDate dob = employee.getDateOfBirth().toLocalDate(); %>
                                        <%= dob.format(dateFormat) %>
                                    <% } else { %>
                                        N/A
                                    <% } %>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div class="col-md-6">
                        <h4>Employment Information</h4>
                        <table class="table table-bordered">
                            <tr>
                                <th>Company</th>
                                <td><%= employee.getCompany() != null ? employee.getCompany().getName() : "N/A" %></td>
                            </tr>
                            <tr>
                                <th>Date of Joining</th>
                                 <td>
                                    <% if (employee.getDateOfJoining() != null) {
                                        LocalDate doj = employee.getDateOfJoining().toLocalDate(); %>
                                        <%= doj.format(dateFormat) %>
                                    <% } else { %>
                                        N/A
                                    <% } %>
                                </td>
                            </tr>
                            <tr>
                                <th>Status</th>
                                <td><%= employee.getStatus() != null ? employee.getStatus() : "N/A" %></td>
                            </tr>
                        </table>
                    </div>
                </div>
                <h4>Salary History</h4>
                <table id="salaryTable" class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <th>Salary Slip ID</th>
                            <th>Period</th>
                            <th>Gross Pay</th>
                            <th>Net Pay</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (salaries != null && !salaries.isEmpty()) {
                            for (SalarySlip salary : salaries) { %>
                                <tr>
                                    <td><%= salary.getSlipId() != null ? salary.getSlipId() : "N/A" %></td>
                                    <td><%= salary.getPostingDate() != null ? salary.getPostingDate().toLocalDate().format(dateFormat) : "N/A" %></td>
                                    <td><%= salary.getGrossPay() != null ? String.format("%,.2f", salary.getGrossPay()) : "0.00" %></td>
                                    <td><%= salary.getNetPay() != null ? String.format("%,.2f", salary.getNetPay()) : "0.00" %></td>
                                    <td><%= salary.getStatus() != null ? salary.getStatus() : "N/A" %></td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/employees/payslip" method="POST" style="display: inline;">
                                            <input type="hidden" name="employeeId" value="<%= employee.getEmployeeId() %>">
                                            <input type="hidden" name="payslipId" value="<%= salary.getSlipId() %>">
                                            <button type="submit" class="btn btn-info btn-sm">
                                                <i class="fas fa-eye"></i> View Payslip
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                        <% }} else { %>
                            <tr>
                                <td colspan="6" class="text-center">No salary slips found</td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script>
$(function () {
    $("#salaryTable").DataTable({
        "responsive": true,
        "lengthChange": true,
        "autoWidth": false,
        "order": [[1, "desc"]],
        "buttons": ["copy", "csv", "excel", "pdf", "print"]
    }).buttons().container().appendTo('#salaryTable_wrapper .col-md-6:eq(0)');
});
</script>
