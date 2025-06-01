<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Users List</h3>
                <div class="card-tools">
                    <a href="<%= request.getContextPath() %>/users/create" class="btn btn-primary btn-sm">
                        <i class="fas fa-plus"></i> Add New User
                    </a>
                </div>
            </div>
            <!-- /.card-header -->
            <div class="card-body">
                <table id="usersTable" class="table table-bordered table-striped data-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Created At</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>1</td>
                        <td>John Doe</td>
                        <td>john@example.com</td>
                        <td><span class="badge badge-primary">Admin</span></td>
                        <td><span class="badge badge-success">Active</span></td>
                        <td>2024-01-15</td>
                        <td>
                            <div class="btn-group" role="group">
                                <a href="<%= request.getContextPath() %>/users/1" class="btn btn-info btn-sm" title="View">
                                    <i class="fas fa-eye"></i>
                                </a>
                                <a href="<%= request.getContextPath() %>/users/1/edit" class="btn btn-warning btn-sm" title="Edit">
                                    <i class="fas fa-edit"></i>
                                </a>
                                <button type="button" class="btn btn-danger btn-sm" title="Delete" 
                                        onclick="confirmDelete('<%= request.getContextPath() %>/users/1/delete', 'Are you sure you want to delete this user?')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>2</td>
                        <td>Jane Smith</td>
                        <td>jane@example.com</td>
                        <td><span class="badge badge-secondary">User</span></td>
                        <td><span class="badge badge-success">Active</span></td>
                        <td>2024-01-20</td>
                        <td>
                            <div class="btn-group" role="group">
                                <a href="<%= request.getContextPath() %>/users/2" class="btn btn-info btn-sm" title="View">
                                    <i class="fas fa-eye"></i>
                                </a>
                                <a href="<%= request.getContextPath() %>/users/2/edit" class="btn btn-warning btn-sm" title="Edit">
                                    <i class="fas fa-edit"></i>
                                </a>
                                <button type="button" class="btn btn-danger btn-sm" title="Delete" 
                                        onclick="confirmDelete('<%= request.getContextPath() %>/users/2/delete', 'Are you sure you want to delete this user?')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>3</td>
                        <td>Bob Wilson</td>
                        <td>bob@example.com</td>
                        <td><span class="badge badge-secondary">User</span></td>
                        <td><span class="badge badge-warning">Inactive</span></td>
                        <td>2024-02-01</td>
                        <td>
                            <div class="btn-group" role="group">
                                <a href="<%= request.getContextPath() %>/users/3" class="btn btn-info btn-sm" title="View">
                                    <i class="fas fa-eye"></i>
                                </a>
                                <a href="<%= request.getContextPath() %>/users/3/edit" class="btn btn-warning btn-sm" title="Edit">
                                    <i class="fas fa-edit"></i>
                                </a>
                                <button type="button" class="btn btn-danger btn-sm" title="Delete" 
                                        onclick="confirmDelete('<%= request.getContextPath() %>/users/3/delete', 'Are you sure you want to delete this user?')">
                                    <i class="fas fa-trash"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                    <tfoot>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Created At</th>
                        <th>Actions</th>
                    </tr>
                    </tfoot>
                </table>
            </div>
            <!-- /.card-body -->
        </div>
        <!-- /.card -->
    </div>
    <!-- /.col -->
</div>
<!-- /.row -->