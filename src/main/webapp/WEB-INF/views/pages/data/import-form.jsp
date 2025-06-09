<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.User" %>
<%@ page import="mg.hrms.payload.ImportResult" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    String successMessage = (String) request.getAttribute("success");
    String errorMessage = (String) request.getAttribute("error");
    Map<String, List<String>> detailedErrors = (Map<String, List<String>>) request.getAttribute("errors");
    Map<String, List<String>> detailedWarnings = (Map<String, List<String>>) request.getAttribute("warnings");
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
                            <% if (detailedErrors != null && !detailedErrors.isEmpty()) { %>
                                <p><strong>Detailed Errors:</strong></p>
                                <ul>
                                    <% for (Map.Entry<String, List<String>> entry : detailedErrors.entrySet()) { %>
                                        <li><strong><%= entry.getKey().replace("_", " ").toUpperCase() %>:</strong>
                                            <ul>
                                                <% for (String error : entry.getValue()) { %>
                                                    <li><%= error %></li>
                                                <% } %>
                                            </ul>
                                        </li>
                                    <% } %>
                                </ul>
                            <% } %>
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    <% } %>
                    <% if (detailedWarnings != null && !detailedWarnings.isEmpty()) { %>
                        <div class="alert alert-warning alert-dismissible fade show" role="alert">
                            <p><strong>Warnings:</strong></p>
                            <ul>
                                <% for (Map.Entry<String, List<String>> entry : detailedWarnings.entrySet()) { %>
                                    <li><strong><%= entry.getKey().replace("_", " ").toUpperCase() %>:</strong>
                                        <ul>
                                            <% for (String warning : entry.getValue()) { %>
                                                <li><%= warning %></li>
                                            <% } %>
                                        </ul>
                                    </li>
                                <% } %>
                            </ul>
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    <% } %>
                    <form method="post" action="${pageContext.request.contextPath}/import" enctype="multipart/form-data" id="importForm">
                        <div class="form-group">
                            <label for="employeesFile">Employees File (CSV):</label>
                            <input type="file" name="employeesFile" id="employeesFile" class="form-control-file" accept=".csv">
                            <small class="form-text text-muted">
                                Upload a CSV file with employee data (format: Ref,Nom,Prenom,genre,Date embauche,date naissance,company).
                                Example: EMP001,Doe,John,Male,01/01/2020,15/05/1990,Acme Corp
                            </small>
                        </div>
                        <div class="form-group">
                            <label for="structuresFile">Salary Structures File (CSV):</label>
                            <input type="file" name="structuresFile" id="structuresFile" class="form-control-file" accept=".csv">
                            <small class="form-text text-muted">
                                Upload a CSV file with salary structure data (format: salary structure,name,Abbr,type,valeur,company).
                                Example: STR001,Basic Salary,BAS,Fixed,50000,Acme Corp
                            </small>
                        </div>
                        <div class="form-group">
                            <label for="recordsFile">Salary Records File (CSV):</label>
                            <input type="file" name="recordsFile" id="recordsFile" class="form-control-file" accept=".csv">
                            <small class="form-text text-muted">
                                Upload a CSV file with salary records (format: Mois,Ref Employe,Salaire Base,Salaire).
                                Example: 01/2025,EMP001,50000,STR001
                            </small>
                        </div>
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle mr-2"></i>
                            At least one file is required. Ensure files are in CSV format with correct headers.
                        </div>
                        <div class="form-group text-center">
                            <button type="submit" class="btn btn-primary btn-lg">
                                <i class="fas fa-upload mr-2"></i>Import Data
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
    document.getElementById('importForm').addEventListener('submit', function(e) {
        const employeesFile = document.getElementById('employeesFile').value;
        const structuresFile = document.getElementById('structuresFile').value;
        const recordsFile = document.getElementById('recordsFile').value;

        // Check if at least one file is selected
        if (!employeesFile && !structuresFile && !recordsFile) {
            alert('Please upload at least one CSV file.');
            e.preventDefault();
            return;
        }

        // Validate file extensions
        const validExtension = file => !file || file.toLowerCase().endsWith('.csv');
        if (!validExtension(employeesFile) || !validExtension(structuresFile) || !validExtension(recordsFile)) {
            alert('Please upload only CSV files.');
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
