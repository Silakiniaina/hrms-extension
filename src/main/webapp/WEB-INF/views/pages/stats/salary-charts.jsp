<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mg.hrms.models.SalaryStats" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    List<SalaryStats> stats = (List<SalaryStats>) request.getAttribute("stats");
    List<String> availableYears = (List<String>) request.getAttribute("availableYears");
    String startYear = (String) request.getAttribute("startYear");
    String endYear = (String) request.getAttribute("endYear");
    
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("fr", "FR"));
    currencyFormat.setMaximumFractionDigits(0);
%>

<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">Salary Evolution Charts</h3>
                <div class="card-tools">
                    <form method="get" action="${pageContext.request.contextPath}/salary-charts" class="form-inline">
                        <div class="input-group input-group-sm mr-2">
                            <div class="input-group-prepend">
                                <span class="input-group-text">From</span>
                            </div>
                            <select name="startYear" class="form-control">
                                <% if (availableYears != null) {
                                    for (String year : availableYears) {
                                        String selected = year.equals(startYear) ? "selected" : "";
                                %>
                                    <option value="<%= year %>" <%= selected %>><%= year %></option>
                                <% }} %>
                            </select>
                        </div>
                        <div class="input-group input-group-sm mr-2">
                            <div class="input-group-prepend">
                                <span class="input-group-text">To</span>
                            </div>
                            <select name="endYear" class="form-control">
                                <% if (availableYears != null) {
                                    for (String year : availableYears) {
                                        String selected = year.equals(endYear) ? "selected" : "";
                                %>
                                    <option value="<%= year %>" <%= selected %>><%= year %></option>
                                <% }} %>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-primary btn-sm">
                            <i class="fas fa-search"></i> Update
                        </button>
                    </form>
                </div>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="card-body">
                    <div class="alert alert-danger">
                        <%= request.getAttribute("error") %>
                    </div>
                </div>
            <% } else { %>

                <div class="card-body">
                    <!-- Chart Tabs -->
                    <ul class="nav nav-tabs" id="chartTabs" role="tablist">
                        <li class="nav-item">
                            <a class="nav-link active" id="overview-tab" data-toggle="tab" href="#overview" role="tab">
                                <i class="fas fa-chart-line"></i> Salary Overview
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="components-tab" data-toggle="tab" href="#components" role="tab">
                                <i class="fas fa-chart-area"></i> Salary Components
                            </a>
                        </li>
                    </ul>

                    <div class="tab-content mt-3" id="chartTabsContent">
                        <!-- Overview Chart Tab -->
                        <div class="tab-pane fade show active" id="overview" role="tabpanel">
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="card">
                                        <div class="card-header">
                                            <h4 class="card-title">
                                                <i class="fas fa-chart-line text-primary"></i>
                                                Salary Evolution (<%= startYear %> - <%= endYear %>)
                                            </h4>
                                        </div>
                                        <div class="card-body">
                                            <div style="position: relative; height: 400px; width: 100%;">
                                                <canvas id="salaryOverviewChart"></canvas>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Summary Statistics -->
                            <% if (stats != null && !stats.isEmpty()) { %>
                            <div class="row mt-3">
                                <div class="col-md-3">
                                    <div class="info-box">
                                        <span class="info-box-icon bg-info"><i class="fas fa-chart-line"></i></span>
                                        <div class="info-box-content">
                                            <span class="info-box-text">Total Months</span>
                                            <span class="info-box-number"><%= stats.size() %></span>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="info-box">
                                        <span class="info-box-icon bg-success"><i class="fas fa-money-bill-wave"></i></span>
                                        <div class="info-box-content">
                                            <span class="info-box-text">Avg Gross Pay</span>
                                            <span class="info-box-number">
                                                <%= currencyFormat.format(stats.stream().mapToDouble(s -> s.getTotalGrossPay() != null ? s.getTotalGrossPay() : 0.0).average().orElse(0.0)) %>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="info-box">
                                        <span class="info-box-icon bg-primary"><i class="fas fa-wallet"></i></span>
                                        <div class="info-box-content">
                                            <span class="info-box-text">Avg Net Pay</span>
                                            <span class="info-box-number">
                                                <%= currencyFormat.format(stats.stream().mapToDouble(s -> s.getTotalNetPay() != null ? s.getTotalNetPay() : 0.0).average().orElse(0.0)) %>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="info-box">
                                        <span class="info-box-icon bg-warning"><i class="fas fa-minus-circle"></i></span>
                                        <div class="info-box-content">
                                            <span class="info-box-text">Avg Deductions</span>
                                            <span class="info-box-number">
                                                <%= currencyFormat.format(stats.stream().mapToDouble(s -> s.getTotalDeductions() != null ? s.getTotalDeductions() : 0.0).average().orElse(0.0)) %>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <% } %>
                        </div>

                        <!-- Components Chart Tab -->
                        <div class="tab-pane fade" id="components" role="tabpanel">
                            <div class="row">
                                <div class="col-md-12">
                                    <div class="card">
                                        <div class="card-header">
                                            <h4 class="card-title">
                                                <i class="fas fa-chart-area text-success"></i>
                                                Salary Components Evolution (<%= startYear %> - <%= endYear %>)
                                            </h4>
                                            <div class="card-tools">
                                                <button type="button" class="btn btn-tool" data-card-widget="collapse">
                                                    <i class="fas fa-minus"></i>
                                                </button>
                                            </div>
                                        </div>
                                       <div class="card-body">
                                            <div class="row mb-3">
                                                <div class="col-md-6">
                                                    <div class="form-group">
                                                        <label for="chartType">Chart Type:</label>
                                                        <select id="chartType" class="form-control form-control-sm">
                                                            <option value="line">Line Chart</option>
                                                            <option value="bar">Bar Chart</option>
                                                            <option value="area">Area Chart</option>
                                                        </select>
                                                    </div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="form-group">
                                                        <label for="componentFilter">Show Components:</label>
                                                        <select id="componentFilter" class="form-control form-control-sm">
                                                            <option value="all">All Components</option>
                                                            <option value="earnings">Earnings Only</option>
                                                            <option value="deductions">Deductions Only</option>
                                                        </select>
                                                    </div>
                                                </div>
                                            </div>
                                            <div style="position: relative; height: 500px; width: 100%; max-height: 500px; overflow: hidden;">
                                                <canvas id="salaryComponentsChart"></canvas>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Data Table -->
                <% if (stats != null && !stats.isEmpty()) { %>
                <div class="card mt-3">
                    <div class="card-header">
                        <h4 class="card-title">
                            <i class="fas fa-table text-info"></i>
                            Detailed Monthly Data
                        </h4>
                        <div class="card-tools">
                            <button type="button" class="btn btn-tool" data-card-widget="collapse">
                                <i class="fas fa-minus"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body table-responsive">
                        <table class="table table-bordered table-striped" id="salaryDataTable">
                            <thead>
                                <tr>
                                    <th>Period</th>
                                    <th>Gross Pay</th>
                                    <th>Net Pay</th>
                                    <th>Total Deductions</th>
                                    <th>Growth Rate</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% 
                                double previousGrossPay = 0;
                                for (int i = 0; i < stats.size(); i++) {
                                    SalaryStats stat = stats.get(i);
                                    double currentGrossPay = stat.getTotalGrossPay() != null ? stat.getTotalGrossPay() : 0.0;
                                    double growthRate = 0;
                                    if (i > 0 && previousGrossPay > 0) {
                                        growthRate = ((currentGrossPay - previousGrossPay) / previousGrossPay) * 100;
                                    }
                                    String growthClass = growthRate > 0 ? "text-success" : (growthRate < 0 ? "text-danger" : "text-muted");
                                    String growthIcon = growthRate > 0 ? "fa-arrow-up" : (growthRate < 0 ? "fa-arrow-down" : "fa-minus");
                                %>
                                <tr>
                                    <td><%= stat.getMonthName() %> <%= stat.getYear() %></td>
                                    <td><%= currencyFormat.format(currentGrossPay) %></td>
                                    <td><%= currencyFormat.format(stat.getTotalNetPay() != null ? stat.getTotalNetPay() : 0.0) %></td>
                                    <td><%= currencyFormat.format(stat.getTotalDeductions() != null ? stat.getTotalDeductions() : 0.0) %></td>
                                    <td class="<%= growthClass %>">
                                        <% if (i > 0) { %>
                                            <i class="fas <%= growthIcon %>"></i>
                                            <%= String.format("%.1f%%", Math.abs(growthRate)) %>
                                        <% } else { %>
                                            <span class="text-muted">-</span>
                                        <% } %>
                                    </td>
                                </tr>
                                <%
                                    previousGrossPay = currentGrossPay;
                                } %>
                            </tbody>
                        </table>
                    </div>
                </div>
                <% } %>

            <% } %>
        </div>
    </div>
</div>

<!-- Chart.js Scripts -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script src="<%= request.getContextPath() %>/adminlte/plugins/jquery/jquery.min.js"></script>
<script>
// Remplacez la section script dans salary-charts.jsp par ceci :

$(document).ready(function() {
    // Initialize DataTable
    $('#salaryDataTable').DataTable({
        "responsive": true,
        "lengthChange": false,
        "autoWidth": false,
        "pageLength": 12,
        "order": [[0, "desc"]],
        "columnDefs": [
            { "orderable": false, "targets": [4] }
        ]
    });

    // Chart variables
    let overviewChart = null;
    let componentsChart = null;

    // Load Overview Chart
    function loadOverviewChart() {
        $.ajax({
            url: '${pageContext.request.contextPath}/salary-charts/data',
            method: 'GET',
            data: {
                startYear: '<%= startYear %>',
                endYear: '<%= endYear %>'
            },
            success: function(data) {
                console.log('Overview Chart Data:', data); // Debug
                renderOverviewChart(data);
            },
            error: function(xhr, status, error) {
                console.error('Failed to load overview chart data:', error);
                console.error('Response:', xhr.responseText);
            }
        });
    }

    // Load Components Chart
    function loadComponentsChart() {
        $.ajax({
            url: '${pageContext.request.contextPath}/salary-charts/components-data',
            method: 'GET',
            data: {
                startYear: '<%= startYear %>',
                endYear: '<%= endYear %>'
            },
            success: function(data) {
                console.log('Components Chart Data:', data); // Debug
                renderComponentsChart(data);
            },
            error: function(xhr, status, error) {
                console.error('Failed to load components chart data:', error);
                console.error('Response:', xhr.responseText);
            }
        });
    }

    // Render Overview Chart
    function renderOverviewChart(data) {
        const ctx = document.getElementById('salaryOverviewChart').getContext('2d');

        if (overviewChart) {
            overviewChart.destroy();
        }

        // Vérifier si les données existent
        if (!data || !data.labels || !data.datasets || data.labels.length === 0) {
            console.warn('No data available for overview chart');
            ctx.font = '16px Arial';
            ctx.fillStyle = '#666';
            ctx.textAlign = 'center';
            ctx.fillText('No data available for the selected period', ctx.canvas.width / 2, ctx.canvas.height / 2);
            return;
        }

        overviewChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: data.datasets.map(dataset => ({
                    ...dataset,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    borderWidth: 2,
                    tension: 0.4
                }))
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Salary Evolution Over Time',
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            padding: 20
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: false, // Changé de true à false pour mieux voir les variations
                        ticks: {
                            callback: function(value, index, values) {
                                return new Intl.NumberFormat('fr-FR', {
                                    style: 'currency',
                                    currency: 'EUR',
                                    minimumFractionDigits: 0,
                                    maximumFractionDigits: 0
                                }).format(value);
                            }
                        }
                    },
                    x: {
                        ticks: {
                            maxRotation: 45,
                            minRotation: 0
                        }
                    }
                },
                interaction: {
                    intersect: false,
                    mode: 'index'
                },
                elements: {
                    line: {
                        tension: 0.4
                    }
                }
            }
        });
    }

    // Render Components Chart avec limitation de hauteur
    function renderComponentsChart(data) {
        const ctx = document.getElementById('salaryComponentsChart').getContext('2d');

        if (componentsChart) {
            componentsChart.destroy();
        }

        if (!data || !data.labels || !data.datasets || data.labels.length === 0) {
            console.warn('No data available for components chart');
            ctx.font = '16px Arial';
            ctx.fillStyle = '#666';
            ctx.textAlign = 'center';
            ctx.fillText('No data available for the selected period', ctx.canvas.width / 2, ctx.canvas.height / 2);
            return;
        }

        let chartType = $('#chartType').val() || 'line';
        let componentFilter = $('#componentFilter').val() || 'all';

        // Filter datasets based on component filter
        let filteredDatasets = data.datasets;
        if (componentFilter === 'earnings') {
            filteredDatasets = data.datasets.filter(d => d.label.includes('(Earning)'));
        } else if (componentFilter === 'deductions') {
            filteredDatasets = data.datasets.filter(d => d.label.includes('(Deduction)'));
        }

        // Limiter le nombre de datasets pour éviter le crash
        if (filteredDatasets.length > 10) {
            console.warn('Too many datasets, limiting to 10 most significant ones');
            // Trier par la somme totale des valeurs et prendre les 10 premiers
            filteredDatasets = filteredDatasets
                .map(dataset => ({
                    ...dataset,
                    total: dataset.data.reduce((sum, val) => sum + (val || 0), 0)
                }))
                .sort((a, b) => b.total - a.total)
                .slice(0, 10);
        }

        // Set chart options based on type
        let chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: 'Salary Components Evolution',
                    font: {
                        size: 16
                    }
                },
                legend: {
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 15,
                        boxWidth: 12
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    stacked: chartType === 'area',
                    ticks: {
                        callback: function(value, index, values) {
                            return new Intl.NumberFormat('fr-FR', {
                                style: 'currency',
                                currency: 'EUR',
                                minimumFractionDigits: 0,
                                maximumFractionDigits: 0
                            }).format(value);
                        }
                    }
                },
                x: {
                    stacked: chartType === 'area',
                    ticks: {
                        maxRotation: 45,
                        minRotation: 0
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
            },
            animation: false // Désactiver l'animation pour éviter les problèmes de performance
        };

        // Adjust dataset fill for area chart
        if (chartType === 'area') {
            filteredDatasets = filteredDatasets.map(dataset => ({
                ...dataset,
                fill: true
            }));
        }

        componentsChart = new Chart(ctx, {
            type: chartType === 'area' ? 'line' : chartType,
            data: {
                labels: data.labels,
                datasets: filteredDatasets
            },
            options: chartOptions
        });
    }

    // Event handlers for chart controls
    $('#chartType, #componentFilter').change(function() {
        loadComponentsChart();
    });

    // Load charts when tabs are shown
    $('#overview-tab').on('shown.bs.tab', function() {
        setTimeout(loadOverviewChart, 100); // Petit délai pour s'assurer que le canvas est visible
    });

    $('#components-tab').on('shown.bs.tab', function() {
        setTimeout(loadComponentsChart, 100);
    });

    // Load initial chart
    setTimeout(loadOverviewChart, 500);
});
</script>
