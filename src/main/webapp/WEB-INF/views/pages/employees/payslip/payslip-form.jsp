<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.Employee" %>
<%@ page import="mg.hrms.models.SalarySlip" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %> <%-- Import LocalDate --%>

<%
    Employee employee = (Employee) request.getAttribute("employee");
    List<SalarySlip> salaries = (List<SalarySlip>) request.getAttribute("salaries");
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMMM yyyy"); // Corrected pattern for month and year
%>
<div class="row">
    <div class="col-md-6 offset-md-3">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Select Payslip</h3>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/employees/payslip">
                    <input type="hidden" name="employeeId" value="<%= employee.getEmployeeId() != null ? employee.getEmployeeId() : "" %>">
                    <div class="form-group">
                        <label for="payslipId">Select Period:</label>
                        <select name="payslipId" id="payslipId" class="form-control" required>
                            <option value="">-- Select a period --</option>
                            <% if (salaries != null && !salaries.isEmpty()) {
                                for (SalarySlip salary : salaries) { %>
                                    <option value="<%= salary.getSlipId() != null ? salary.getSlipId() : "" %>">
                                        <% if (salary.getPostingDate() != null) { %>
                                            <%= salary.getPostingDate().toLocalDate().format(dateFormat) %>
                                        <% } else { %>
                                            N/A
                                        <% } %>
                                    </option>
                                <% }} %>
                        </select>
                    </div>
                    <div class="form-group text-center">
                        <button type="submit" class="btn btn-primary">View Payslip</button>
                        <a href="${pageContext.request.contextPath}/employees/view?employeeId=<%= employee.getEmployeeId() != null ? employee.getEmployeeId() : "" %>"
                           class="btn btn-secondary">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
