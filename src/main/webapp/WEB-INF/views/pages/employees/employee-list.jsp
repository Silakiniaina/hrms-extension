<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.Employee" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    List<Employee> employees = (List<Employee>) request.getAttribute("employees");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyy");
%>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-body">
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
                            if(employees != null){
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
                                            <a href="#" class="btn btn-info btn-sm">
                                                <i class="fas fa-eye"></i> View
                                            </a>
                                        </td>
                                    </tr>
                                <% } %>
                        <%  }else{ %>
                            <p>No content available.</p>
                        <% } %>
                    </tbody>
                </table>
            </div>
            <!-- /.card-body -->
        </div>
        <!-- /.card -->
    </div>
    <!-- /.col -->
</div>
<!-- /.row -->

<script>
$(function () {
    $("#employeeTable").DataTable({
        "responsive": true, 
        "lengthChange": true, 
        "autoWidth": false,
        "buttons": ["copy", "csv", "excel", "pdf", "print"],
        "columnDefs": [
            { "orderable": false, "targets": [6] } 
        ]
    }).buttons().container().appendTo('#employeeTable_wrapper .col-md-6:eq(0)');
});
</script>