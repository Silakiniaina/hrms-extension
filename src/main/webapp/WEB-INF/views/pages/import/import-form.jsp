<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.User" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    User user = (User) session.getAttribute("user");
    String message = (String) request.getAttribute("message");
%>

<div class="row">
    <div class="col-md-8 offset-md-2">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Import HRMS Data</h3>
            </div>
            <div class="card-body">
                <% if (message != null) { %>
                    <div class="alert alert-info">
                        <%= message %>
                    </div>
                <% } %>

                <form method="post" action="${pageContext.request.contextPath}/import" enctype="multipart/form-data">
                    <div class="form-group">
                        <label for="employeesFile">Employees File (CSV):</label>
                        <input type="file" name="employeesFile" id="employeesFile" class="form-control-file" accept=".csv" required>
                        <small class="form-text text-muted">Upload employee data CSV file (format: Ref,Nom,Prenom,genre,Date embauche,date naissance,company)</small>
                    </div>

                    <div class="form-group">
                        <label for="structuresFile">Salary Structures File (CSV):</label>
                        <input type="file" name="structuresFile" id="structuresFile" class="form-control-file" accept=".csv" required>
                        <small class="form-text text-muted">Upload salary structure CSV file (format: salary structure,name,Abbr,type,valeur,company)</small>
                    </div>

                    <div class="form-group">
                        <label for="recordsFile">Salary Records File (CSV):</label>
                        <input type="file" name="recordsFile" id="recordsFile" class="form-control-file" accept=".csv" required>
                        <small class="form-text text-muted">Upload salary records CSV file (format: Mois,Ref Employe,Salaire Base,Salaire)</small>
                    </div>

                    <div class="form-group text-center">
                        <button type="submit" class="btn btn-primary">Import Data</button>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
// Client-side validation for file types
document.querySelector('form').addEventListener('submit', function(e) {
    const employeesFile = document.getElementById('employeesFile');
    const structuresFile = document.getElementById('structuresFile');
    const recordsFile = document.getElementById('recordsFile');

    if (!employeesFile.value.endsWith('.csv') ||
        !structuresFile.value.endsWith('.csv') ||
        !recordsFile.value.endsWith('.csv')) {
        alert('Please upload only CSV files');
        e.preventDefault();
    }
});
</script>
