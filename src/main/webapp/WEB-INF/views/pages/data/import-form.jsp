<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.User" %>
<%@ page import="mg.hrms.payload.ImportResult" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>

<%
    User user = (User) session.getAttribute("user");
    // String message = (String) request.getAttribute("message"); // L'ancien message général

    // Récupérer les attributs de flash passés par le contrôleur
    String successMessage = (String) request.getAttribute("success");
    String errorMessage = (String) request.getAttribute("error");
    String warningMessage = (String) request.getAttribute("warning"); // Pour les warnings généraux si pas d'erreurs
    ImportResult importResult = (ImportResult) request.getAttribute("importResult");
    Map<String, List<String>> detailedErrors = (Map<String, List<String>>) request.getAttribute("errors");
    Map<String, List<String>> detailedWarnings = (Map<String, List<String>>) request.getAttribute("warnings"); // NOUVEAU
%>

<div class="row">
    <div class="col-md-8 offset-md-2">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Import HRMS Data</h3>
            </div>
            <div class="card-body">
                <%-- Affichage des messages de succès --%>
                <% if (successMessage != null) { %>
                    <div class="alert alert-success">
                        <%= successMessage %>
                    </div>
                <% } %>

                <%-- Affichage des messages d'erreur --%>
                <% if (errorMessage != null) { %>
                    <div class="alert alert-danger">
                        <%= errorMessage %>
                        <% if (detailedErrors != null && !detailedErrors.isEmpty()) { %>
                            <p><strong>Détails des erreurs :</strong></p>
                            <ul>
                                <% for (Map.Entry<String, List<String>> entry : detailedErrors.entrySet()) { %>
                                    <li><strong><%= entry.getKey().replace("_", " ") %>:</strong>
                                        <ul>
                                            <% for (String error : entry.getValue()) { %>
                                                <li><%= error %></li>
                                            <% } %>
                                        </ul>
                                    </li>
                                <% } %>
                            </ul>
                        <% } %>
                    </div>
                <% } %>

                <%-- Affichage des messages d'avertissement --%>
                <% if (warningMessage != null) { %>
                    <div class="alert alert-warning">
                        <%= warningMessage %>
                    </div>
                <% } %>

                <% if (detailedWarnings != null && !detailedWarnings.isEmpty()) { %>
                    <div class="alert alert-warning">
                        <p><strong>Avertissements :</strong></p>
                        <ul>
                            <% for (Map.Entry<String, List<String>> entry : detailedWarnings.entrySet()) { %>
                                <li><strong><%= entry.getKey().replace("_", " ") %>:</strong>
                                    <ul>
                                        <% for (String warning : entry.getValue()) { %>
                                            <li><%= warning %></li>
                                        <% } %>
                                    </ul>
                                </li>
                            <% } %>
                        </ul>
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

    // Correction: Assurez-vous que les fichiers sont sélectionnés avant de vérifier l'extension
    const isEmployeesCsv = !employeesFile.value || employeesFile.value.endsWith('.csv');
    const isStructuresCsv = !structuresFile.value || structuresFile.value.endsWith('.csv');
    const isRecordsCsv = !recordsFile.value || recordsFile.value.endsWith('.csv');

    if (!isEmployeesCsv || !isStructuresCsv || !isRecordsCsv) {
        alert('Please upload only CSV files');
        e.preventDefault();
    }
});
</script>
