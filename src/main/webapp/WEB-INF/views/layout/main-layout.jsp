<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/partials/head.jsp" %>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">

    <!-- Preloader -->
    <div class="preloader flex-column justify-content-center align-items-center">
        <img class="animation__shake" src="<%= request.getContextPath() %>/adminlte/dist/img/AdminLTELogo.png" alt="AdminLTELogo" height="60" width="60">
    </div>

    <%@ include file="/WEB-INF/views/partials/sidebar.jsp" %>

    <!-- Content Wrapper -->
    <div class="content-wrapper">
        <!-- Content Header -->
        <div class="content-header">
            <div class="container-fluid">
                <div class="row mb-2">
                    <div class="col-sm-12">
                        <h1 class="m-0"><%= request.getAttribute("pageTitle") %></h1>
                    </div>
                </div>
            </div>
        </div>        

        <!-- Main content -->
        <section class="content">
            <div class="container-fluid">
                <% if (request.getAttribute("successMessage") != null) { %>
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <%= request.getAttribute("successMessage") %>
                        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                <% } %>
                
                <!-- Page Content -->
                <% 
                String contentPage = (String) request.getAttribute("contentPage"); 
                if (contentPage == null || contentPage.isEmpty()) { 
                %>
                    <p>No content available.</p>
                <% } else { 
                    String fullPath = "/WEB-INF/views/" + contentPage;
                %>
                    <jsp:include page="<%= fullPath %>" />
                <% } %>
            </div>
        </section>
    </div>
    
    <%@ include file="/WEB-INF/views/partials/footer.jsp" %>
</div>

<%@ include file="/WEB-INF/views/partials/scripts.jsp" %>
</body>
</html>