<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.Employee" %>
<%
    List<Employee> employees = (List<Employee>)request.getAttribute("employees");
    String successMessage = (String) request.getAttribute("success");
    String errorMessage = (String) request.getAttribute("error");
%>
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-10">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white">
                    <h3 class="card-title mb-0">Generate Salary</h3>
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
                            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                    <% } %>
                    <form method="post" action="/salary/generate" id="salaryGenerationForm">
                        <div class="form-group">
                            <label for="employee">Employee <span class="text-danger">*</span></label>
                            <select name="employee" class="form-control" id="employee" required>
                                <option value="">-- Select Employee --</option>
                                <% if (employees != null) {
                                    for (Employee employee : employees) {
                                        String selected = (employee.getEmployeeId() != null && employee.getEmployeeId().equals(request.getParameter("employee"))) ? "selected" : "";
                                %>
                                    <option value="<%= employee.getEmployeeId() %>" <%= selected %>><%= employee.getFullName() %> (<%= employee.getEmployeeId() %>)</option>
                                <% }} %>
                            </select>
                            <small class="form-text text-muted">Select the employee for whom to generate salary slips</small>
                        </div>
                        <div class="form-group">
                            <label for="startMonth">Start Month <span class="text-danger">*</span></label>
                            <input type="month" name="startMonth" id="startMonth" class="form-control"
                                   placeholder="Start month" required
                                   value="<%= request.getParameter("startMonth") != null ? request.getParameter("startMonth") : "" %>">
                            <small class="form-text text-muted">Select the first month of the salary generation period</small>
                        </div>
                        <div class="form-group">
                            <label for="endMonth">End Month <span class="text-danger">*</span></label>
                            <input type="month" name="endMonth" id="endMonth" class="form-control"
                                   placeholder="End month" required
                                   value="<%= request.getParameter("endMonth") != null ? request.getParameter("endMonth") : "" %>">
                            <small class="form-text text-muted">Select the last month of the salary generation period</small>
                        </div>
                        <div class="form-group">
                            <label for="amount">Amount (Optional)</label>
                            <input type="number" name="amount" id="amount" class="form-control"
                                   placeholder="Enter salary amount" step="0.01" min="0"
                                   value="<%= request.getParameter("amount") != null ? request.getParameter("amount") : "" %>">
                            <small class="form-text text-muted">Leave empty to use the last salary amount from previous salary slip</small>
                        </div>
                        <div class="form-group text-center">
                            <button type="submit" class="btn btn-primary btn-lg" id="generateBtn">
                                <i class="fas fa-calculator mr-2"></i>Generate Salary Slips
                            </button>
                            <button type="reset" class="btn btn-secondary btn-lg ml-2">
                                <i class="fas fa-undo mr-2"></i>Reset
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            
            <!-- Information Card -->
            <div class="card shadow-sm mt-4">
                <div class="card-header bg-info text-white">
                    <h5 class="card-title mb-0"><i class="fas fa-info-circle mr-2"></i>How it works</h5>
                </div>
                <div class="card-body">
                    <ul class="mb-0">
                        <li><strong>Period Generation:</strong> Salary slips will be generated for each month between start and end dates</li>
                        <li><strong>Skip Existing:</strong> If a salary slip already exists for a month, it will be skipped</li>
                        <li><strong>Amount Logic:</strong> If amount is provided, it will be used. Otherwise, the system will find the last salary slip before the start date and use its amount</li>
                        <li><strong>Validation:</strong> An error will occur if no amount is provided and no previous salary slip exists</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
// Form validation
document.getElementById('salaryGenerationForm').addEventListener('submit', function(e) {
    const startMonth = document.getElementById('startMonth').value;
    const endMonth = document.getElementById('endMonth').value;
    const generateBtn = document.getElementById('generateBtn');
    
    if (startMonth && endMonth) {
        const startDate = new Date(startMonth + '-01');
        const endDate = new Date(endMonth + '-01');
        
        if (startDate > endDate) {
            e.preventDefault();
            alert('Start month cannot be after end month');
            return false;
        }
    }
    
    // Disable button and show loading state
    generateBtn.disabled = true;
    generateBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Generating...';
    
    // Re-enable button after a delay (in case of quick return)
    setTimeout(function() {
        generateBtn.disabled = false;
        generateBtn.innerHTML = '<i class="fas fa-calculator mr-2"></i>Generate Salary Slips';
    }, 10000);
});

// Auto-set end month when start month is selected
document.getElementById('startMonth').addEventListener('change', function() {
    const endMonthField = document.getElementById('endMonth');
    if (!endMonthField.value && this.value) {
        endMonthField.value = this.value;
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
    .text-danger {
        color: #dc3545 !important;
    }
    .form-group label {
        font-weight: 600;
        margin-bottom: 8px;
    }
    .btn:disabled {
        opacity: 0.65;
        cursor: not-allowed;
    }
</style>