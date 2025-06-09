<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="container">
    <% if (request.getAttribute("success") != null) { %>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <%= request.getAttribute("success") %>
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    <% } %>

    <div class="row d-flex justify-content-center align-items-center">
        <div class="col-12 text-center">
            <h1>Welcome to HRMS - Extension Application</h1>
            <p class="lead">Manage employees, salary structures, and payslips efficiently.</p>
        </div>
    </div>
</div>

<style>
    .container {
        padding: 20px;
    }
    .lead {
        font-size: 1.25rem;
        color: #555;
    }
</style>