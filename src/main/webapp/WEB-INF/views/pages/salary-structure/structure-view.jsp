<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.SalaryStructure" %>
<%@ page import="mg.hrms.models.SalaryComponent" %>
<%@ page import="java.util.List" %>
<%
    SalaryStructure structure = (SalaryStructure) request.getAttribute("structure");
%>
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Salary Structure - <%= structure.getName() != null ? structure.getName() : "N/A" %></h3>
                <div class="card-tools">
                    <a href="${pageContext.request.contextPath}/salary-structures" class="btn btn-secondary btn-sm">
                        <i class="fas fa-arrow-left"></i> Back to List
                    </a>
                </div>
            </div>
            <div class="card-body">
                <div class="row mb-4">
                    <div class="col-md-6">
                        <h4>Details</h4>
                        <table class="table table-bordered">
                            <tr>
                                <th>Name</th>
                                <td><%= structure.getName() != null ? structure.getName() : "N/A" %></td>
                            </tr>
                            <tr>
                                <th>Company</th>
                                <td><%= structure.getCompany() != null ? structure.getCompany().getName() : "N/A" %></td>
                            </tr>
                        </table>
                    </div>
                </div>
                <h4>Components</h4>
                <table id="componentTable" class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Type</th>
                            <th>Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (structure.getComponents() != null && !structure.getComponents().isEmpty()) {
                            for (SalaryComponent component : structure.getComponents()) { %>
                                <tr>
                                    <td><%= component.getName() != null ? component.getName() : "N/A" %></td>
                                    <td><%= component.getType() != null ? component.getType() : "N/A" %></td>
                                    <td class="text-right"><%= component.getAmount() != null ? String.format("%,.2f", component.getAmount()) : "0.00" %></td>
                                </tr>
                        <% }} else { %>
                            <tr>
                                <td colspan="3" class="text-center">No components defined</td>
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
    $("#componentTable").DataTable({
        "responsive": true,
        "lengthChange": true,
        "autoWidth": false,
        "buttons": ["copy", "csv", "excel", "pdf", "print"]
    }).buttons().container().appendTo('#componentTable_wrapper .col-md-6:eq(0)');
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