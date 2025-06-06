<%-- reset-form.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.User" %>

<%
    User user = (User) session.getAttribute("user");
    String success = (String) request.getAttribute("success");
    String error = (String) request.getAttribute("error");
%>

<div class="row">
    <div class="col-md-8 offset-md-2">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Reset HRMS Data</h3>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/reset">
                    <div class="form-group">
                        <label for="company">Company (optional):</label>
                        <input type="text" name="company" id="company" class="form-control"
                               placeholder="Leave blank to reset all companies">
                        <small class="form-text text-muted">
                            Enter company name to reset only that company's data, or leave blank to reset all companies
                        </small>
                    </div>

                    <div class="alert alert-warning">
                        <strong>Warning!</strong> This action will permanently delete all HRMS data.
                        This cannot be undone. Please proceed with caution.
                    </div>

                    <div class="form-group text-center">
                        <button type="submit" class="btn btn-danger"
                                onclick="return confirm('Are you sure you want to reset HRMS data? This cannot be undone.')">
                            Reset Data
                        </button>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">Cancel</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
