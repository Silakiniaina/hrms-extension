<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.User" %>
<%
    User user = (User) session.getAttribute("user");
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow-sm">
                <div class="card-header bg-danger text-white">
                    <h3 class="card-title mb-0">Reset HRMS Data</h3>
                </div>
                <div class="card-body">
                    <% if (success != null) { %>
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <%= success %>
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    <% } %>
                    <% if (error != null) { %>
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <%= error %>
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    <% } %>
                    <form method="post" action="${pageContext.request.contextPath}/reset" id="resetForm">
                        <div class="form-group">
                            <label for="company">Company (optional):</label>
                            <input type="text" name="company" id="company" class="form-control"
                                   placeholder="Enter company name or leave blank to reset all">
                            <small class="form-text text-muted">
                                Specify a company name to reset only its data, or leave blank to reset all companies.
                            </small>
                        </div>
                        <div class="alert alert-warning">
                            <i class="fas fa-exclamation-triangle mr-2"></i>
                            <strong>Warning!</strong> This action will permanently delete all HRMS data and cannot be undone. Proceed with caution.
                        </div>
                        <div class="form-group text-center">
                            <button type="submit" class="btn btn-danger btn-lg">
                                <i class="fas fa-trash-alt mr-2"></i>Reset Data
                            </button>
                            <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary btn-lg ml-2">Cancel</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    document.getElementById('resetForm').addEventListener('submit', function(e) {
        if (!confirm('Are you absolutely sure you want to reset HRMS data? This action is irreversible.')) {
            e.preventDefault();
        }
    });
</script>
<style>
    .card {
        border-radius: 10px;
        margin-top: 20px;
    }
    .card-header {
        border-radius: 10px 10px 0 0;
    }
    .form-control {
        border-radius: 5px;
    }
    .btn {
        border-radius: 5px;
        padding: 10px 20px;
    }
    .alert {
        border-radius: 5px;
        margin-bottom: 20px;
    }
</style>
