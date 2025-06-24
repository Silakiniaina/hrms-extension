<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.SalaryComponent" %>
<%
    List<SalaryComponent> salaryComponents = (List<SalaryComponent>)request.getAttribute("salaryComponents");
    String successMessage = (String) request.getAttribute("success");
    String errorMessage = (String) request.getAttribute("error");
%>
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h3 class="card-title mb-0">Import HRMS Data</h3>
                </div>
                <div class="card-body">
                    <% if (successMessage != null) { %>
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <%= successMessage %>
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    <% } %>
                    <% if (errorMessage != null) { %>
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <%= errorMessage %>
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    <% } %>
                    <form method="post" action="/salary/update" id="salaryUpdateForm">
                        <div class="form-group">
                            <label for="salaryComponent">Salary Component <span class="text-danger">*</span></label>
                            <select name="salaryComponent" class="form-control" id="salaryComponent" required>
                                <option value="">-- Select Salary Component --</option>
                                <% if (salaryComponents != null) {
                                    for (SalaryComponent salaryComponent : salaryComponents) {
                                        String selected = (salaryComponent.getId() != null && salaryComponent.getId().equals(request.getParameter("salaryComponent"))) ? "selected" : "";
                                %>
                                    <option value="<%= salaryComponent.getId() %>" <%= selected %>>
                                        <%= salaryComponent.getId() %> (<%= salaryComponent.getType() %>)
                                    </option>
                                <% }} %>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label for="operator">Operator <span class="text-danger">*</span></label>
                            <select name="operator" class="form-control" id="operator" required>
                                <option value=">">Greater Than (>)</option>
                                <option value="<">Less Than (<)</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label for="amount">Amount <span class="text-danger">*</span></label>
                            <input type="number" name="amount" id="amount" class="form-control"
                                   placeholder="Enter the amount" step="0.01" min="0" required
                                   value="<%= request.getParameter("amount") != null ? request.getParameter("amount") : "" %>">
                        </div>   
                        
                        <div class="form-group">
                            <label for="action">Action <span class="text-danger">*</span></label>
                            <select name="action" class="form-control" id="action" required>
                                <option value="Add">Add (+)</option>
                                <option value="Reduce">Reduce (-)</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label for="percentage">Percentage <span class="text-danger">*</span></label>
                            <input type="number" name="percentage" id="percentage" class="form-control"
                                   placeholder="Enter the percentage" step="0.01" min="0.01" max="100" required
                                   value="<%= request.getParameter("percentage") != null ? request.getParameter("percentage") : "" %>">
                            <small class="form-text text-muted">Percentage to add/reduce from base salary (0-100)</small>
                        </div>  
                        
                        <div class="form-group text-center">
                            <button type="submit" class="btn btn-primary btn-lg">
                                <i class="fas fa-sync-alt mr-2"></i>Update Salaries
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<style>
    .card {
        border-radius: 10px;
        margin-top: 20px;
    }
    .card-header {
        border-radius: 10px 10px 0 0;
    }
    .form-control-file {
        padding: 8px;
    }
    .btn {
        border-radius: 5px;
        padding: 10px 20px;
    }
    .alert {
        border-radius: 5px;
        margin-bottom: 20px;
    }
    .alert ul {
        margin-bottom: 0;
    }
    small.form-text {
        margin-top: 5px;
    }
</style>
