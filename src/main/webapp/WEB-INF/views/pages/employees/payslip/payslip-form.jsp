<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.Employee" %>
<%@ page import="mg.hrms.models.SalarySlip" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.List" %>

<%
    Employee employee = (Employee) request.getAttribute("employee");
    List<SalarySlip> salaries = (List<SalarySlip>) request.getAttribute("salaries");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
%>

<div class="row">
    <div class="col-md-6 offset-md-3">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Select Payslip</h3>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/employees/payslip">
                    <input type="hidden" name="employeeId" value="<%= employee.getEmployeeId() %>">

                    <div class="form-group">
                        <label>Select Month:</label>
                        <select name="payslipId" class="form-control" required>
                            <option value="">-- Select a month --</option>
                            <% for (SalarySlip salary : salaries) { %>
                                <option value="<%= salary.getSlipId() %>">
                                    <%= dateFormat.format(salary.getPostingDate()) %>
                                </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group text-center">
                        <button type="submit" class="btn btn-primary">View Payslip</button>
                        <a href="${pageContext.request.contextPath}/employees/view?employeeId=<%= employee.getEmployeeId() %>"
                           class="btn btn-secondary">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
