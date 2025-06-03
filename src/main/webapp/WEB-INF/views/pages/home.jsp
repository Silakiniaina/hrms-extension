<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- ----------------------------- Error card ------------------------------ -->
<% if (request.getAttribute("success") != null) { %>
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <%= request.getAttribute("success") %>
        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
    </div>
<% } %>

<div class="row d-flex align-center">
    <h1>Welcome to HRMS - Extension Application</h1>
</div>
<!-- ---------------------------- End home row ----------------------------- -->