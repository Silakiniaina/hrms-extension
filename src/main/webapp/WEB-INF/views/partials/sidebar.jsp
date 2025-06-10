<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="mg.hrms.models.User" %>

<!-- Main Sidebar Container -->
<aside class="main-sidebar sidebar-dark-primary elevation-4">
    <!-- Brand Logo -->
    <a href="<%= request.getContextPath() %>/" class="brand-link">
        <img src="<%= request.getContextPath() %>/adminlte/dist/img/AdminLTELogo.png" alt="AdminLTE Logo" class="brand-image img-circle elevation-3" style="opacity: .8">
        <span class="brand-text font-weight-light">HRMS Extension</span>
    </a>

    <!-- Sidebar -->
    <div class="sidebar">
        <!-- Sidebar user panel (optional) --> 
        <div class="user-panel mt-3 pb-3 mb-3 d-flex">
            <div class="image">
                <img src="<%= request.getContextPath() %>/adminlte/dist/img/user2-160x160.jpg" class="img-circle elevation-2" alt="User Image">
            </div>
            <div class="info">
                <a href="#" class="d-block">
                    <% if (session.getAttribute("user") != null) { %>
                        <%= ((User)session.getAttribute("user")).getFullName() %>
                    <% } else { %>
                        Admin
                    <% } %>
                </a>
            </div>
        </div>

        <!-- Sidebar Menu -->
        <nav class="mt-2">
            <ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu" data-accordion="false">
                <!-- Add icons to the links using the .nav-icon class with font-awesome or any other icon font library -->
                
                <li class="nav-item">
                    <a href="#" class="nav-link">
                        <i class="nav-icon fas fa-tachometer-alt"></i>
                        <p>
                            Dashboard
                            <i class="fas fa-angle-left right"></i>
                        </p>
                    </a>
                    <ul class="nav nav-treeview">
                        <li class="nav-item">
                            <a href="<%= request.getContextPath() %>/salary-stats" class="nav-link <%= request.getRequestURI().contains("/users") ? "active" : "" %>">
                                <i class="far fa-circle nav-icon"></i>
                                <p>Salary Stats</p>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/salary-charts" class="nav-link">
                                <i class="nav-icon fas fa-file-invoice-dollar"></i>
                                <p>Salary Evolution</p>
                            </a>
                        </li>
                    </ul>
                </li>
                
                <li class="nav-item">
                    <a href="#" class="nav-link">
                        <i class="nav-icon fas fa-users"></i>
                        <p>
                            Employee
                            <i class="fas fa-angle-left right"></i>
                        </p>
                    </a>
                    <ul class="nav nav-treeview">
                        <li class="nav-item">
                            <a href="<%= request.getContextPath() %>/employees" class="nav-link <%= request.getRequestURI().contains("/users") ? "active" : "" %>">
                                <i class="far fa-circle nav-icon"></i>
                                <p>All Employee</p>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/salary-summary" class="nav-link">
                                <i class="nav-icon fas fa-file-invoice-dollar"></i>
                                <p>Salary Summary</p>
                            </a>
                        </li>
                    </ul>
                </li>

                <li class="nav-item">
                    <a href="#" class="nav-link">
                        <i class="nav-icon fas fa-database"></i>
                        <p>
                            Data
                            <i class="fas fa-angle-left right"></i>
                        </p>
                    </a>
                    <ul class="nav nav-treeview">
                        <li class="nav-item">
                            <a href="<%= request.getContextPath() %>/import" class="nav-link <%= request.getRequestURI().contains("/users") ? "active" : "" %>">
                                <i class="far fa-circle nav-icon"></i>
                                <p>Import</p>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="<%= request.getContextPath() %>/reset" class="nav-link <%= request.getRequestURI().contains("/users") ? "active" : "" %>">
                                <i class="far fa-trash nav-icon"></i>
                                <p>Reset</p>
                            </a>
                        </li>
                    </ul>
                </li>

            </ul>
        </nav>
        <!-- /.sidebar-menu -->
    </div>
    <!-- /.sidebar -->
</aside>
