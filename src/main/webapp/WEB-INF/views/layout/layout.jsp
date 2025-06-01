<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="mg.hrms.models.Breadcrumb" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="partials/head.jsp" %>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">

    <!-- Preloader -->
    <div class="preloader flex-column justify-content-center align-items-center">
        <img class="animation__shake" src="https://adminlte.io/themes/v3/dist/img/AdminLTELogo.png" alt="AdminLTELogo" height="60" width="60">
    </div>

    <%@ include file="partials/sidebar.jsp" %>

    <!-- Content Wrapper -->
    <div class="content-wrapper">
        <!-- Content Header -->
        <div class="content-header">
            <div class="container-fluid">
                <div class="row mb-2">
                    <div class="col-sm-6">
                        <h1 class="m-0"><%= request.getAttribute("pageTitle") %></h1>
                    </div>
                    <div class="col-sm-6">
                        <ol class="breadcrumb float-sm-right">
                            <% 
                            // Using Map instead of Breadcrumb class for simplicity
                            List<Breadcrumb> breadcrumbs = 
                                (List<Breadcrumb>) request.getAttribute("breadcrumbs");
                            if (breadcrumbs != null) {
                                for (Breadcrumb breadcrumb : breadcrumbs) {
                                    String url = breadcrumb.getUrl();
                                    String name = breadcrumb.getName();
                                    if (url != null && !url.isEmpty()) { %>
                                        <li class="breadcrumb-item"><a href="<%= url %>"><%= name %></a></li>
                                    <% } else { %>
                                        <li class="breadcrumb-item active"><%= name %></li>
                                    <% }
                                }
                            } else { %>
                                <li class="breadcrumb-item"><a href="<%= request.getContextPath() %>/">Home</a></li>
                                <li class="breadcrumb-item active"><%= request.getAttribute("pageTitle") %></li>
                            <% } %>
                        </ol>
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
                
                <%-- Similar blocks for other message types --%>
                
                <!-- Page Content -->
                <% String contentPage = (String) request.getAttribute("contentPage"); %>
                <jsp:include page="<%= contentPage %>" />
            </div>
        </section>
    </div>
    
    <%@ include file="partials/footer.jsp" %>
</div>

<%@ include file="partials/scripts.jsp" %>
</body>
</html>