<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.Employee" %>
<%@ page import="mg.hrms.models.Gender" %>
<%@ page import="mg.hrms.models.Company" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    List<Employee> employees = (List<Employee>) request.getAttribute("employees");
    List<Gender> genders = (List<Gender>) request.getAttribute("genders");
    List<Company> companies = (List<Company>) request.getAttribute("companies");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
%>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Employee List</h3>

                <!-- Filter Form -->
                <div class="card-tools">
                    <button type="button" class="btn btn-tool" data-card-widget="collapse">
                        <i class="fas fa-filter"></i> Filters
                    </button>
                </div>
            </div>

            <div class="card-body">
                <form id="filterForm" method="post" action="${pageContext.request.contextPath}/employees/filter">
                    <div class="row mb-3">
                        <div class="col-md-2">
                            <input type="text" name="name" class="form-control" placeholder="Employee ID"
                                   value="${param.name}">
                        </div>
                        <div class="col-md-2">
                            <select name="gender" class="form-control">
                                <option value="">All Genders</option>
                                <%
                                    if (genders != null) {
                                        for (Gender gender : genders) {
                                            String selected = gender.getName() != null && gender.getName().equals(request.getParameter("gender")) ? "selected" : "";
                                %>
                                    <option value="<%= gender.getName() %>" <%= selected %>><%= gender.getName() %></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <select name="company" class="form-control">
                                <option value="">All Companies</option>
                                <%
                                    if (companies != null) {
                                        for (Company company : companies) {
                                            String selected = company.getName().equals(request.getParameter("company")) ? "selected" : "";
                                %>
                                    <option value="<%= company.getName() %>" <%= selected %>><%= company.getName() %></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <input type="number" name="minAge" class="form-control" placeholder="Min Age"
                                   value="${param.minAge}">
                        </div>
                        <div class="col-md-2">
                            <input type="number" name="maxAge" class="form-control" placeholder="Max Age"
                                   value="${param.maxAge}">
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary">Filter</button>
                            <button type="button" onclick="resetFilters()" class="btn btn-secondary">Reset</button>
                        </div>
                    </div>
                </form>

                <table id="employeeTable" class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <th>Employee ID</th>
                            <th>Full Name</th>
                            <th>Gender</th>
                            <th>Date of Birth</th>
                            <th>Date of Joining</th>
                            <th>Company</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if(employees != null && !employees.isEmpty()){
                                for (Employee employee : employees) { %>
                                    <tr>
                                        <td><%= employee.getEmployeeId() %></td>
                                        <td><%= employee.getFirstName() + " " + employee.getLastName() %></td>
                                        <td><%= employee.getGender() %></td>
                                        <td><%= employee.getDateOfBirth() != null ? dateFormat.format(employee.getDateOfBirth()) : "" %></td>
                                        <td><%= employee.getDateOfJoining() != null ? dateFormat.format(employee.getDateOfJoining()) : "" %></td>
                                        <td><%= employee.getCompany() != null ? employee.getCompany() : "" %></td>
                                        <td><%= employee.getStatus() != null ? employee.getStatus() : "Not defined" %></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/employees/view?employeeId=<%= employee.getEmployeeId() %>" class="btn btn-info btn-sm">
                                                <i class="fas fa-eye"></i> View
                                            </a>
                                        </td>
                                    </tr>
                                <% } %>
                        <%  }else{ %>
                            <tr>
                                <td colspan="8" class="text-center">No employees found</td>
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
    $("#employeeTable").DataTable({
        "responsive": true,
        "lengthChange": true,
        "autoWidth": false,
        "buttons": ["copy", "csv", "excel", "pdf", "print"],
        "columnDefs": [
            { "orderable": false, "targets": [7] }
        ]
    }).buttons().container().appendTo('#employeeTable_wrapper .col-md-6:eq(0)');

    // Preserve filters when sorting/paging
    $("#employeeTable").on('preXhr.dt', function (e, settings, data) {
        $('form#filterForm').find('input, select').each(function() {
            data[this.name] = this.value;
        });
    });
});

function resetFilters() {
    document.getElementById("filterForm").reset();
    document.getElementById("filterForm").submit();
}
</script>
