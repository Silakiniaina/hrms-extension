<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.SalaryStructure" %>
<%
    List<SalaryStructure> structures = (List<SalaryStructure>) request.getAttribute("structures");
%>
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Salary Structures</h3>
            </div>
            <div class="card-body">
                <table id="structureTable" class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Company</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (structures != null && !structures.isEmpty()) {
                            for (SalaryStructure structure : structures) { %>
                                <tr>
                                    <td><%= structure.getName() != null ? structure.getName() : "N/A" %></td>
                                    <td><%= structure.getCompany() != null ? structure.getCompany().getName() : "N/A" %></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/salary-structures/view?name=<%= structure.getName() != null ? structure.getName() : "" %>"
                                           class="btn btn-info btn-sm">
                                            <i class="fas fa-eye"></i> View
                                        </a>
                                    </td>
                                </tr>
                        <% }} else { %>
                            <tr>
                                <td colspan="3" class="text-center">No salary structures found</td>
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
    $("#structureTable").DataTable({
        "responsive": true,
        "lengthChange": true,
        "autoWidth": false,
        "buttons": ["copy", "csv", "excel", "pdf", "print"],
        "columnDefs": [{ "orderable": false, "targets": [2] }]
    }).buttons().container().appendTo('#structureTable_wrapper .col-md-6:eq(0)');
});
</script>
<style>
    .card {
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