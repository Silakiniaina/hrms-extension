<!DOCTYPE html>
<html lang="en">

<head>
  <%@ include file="/WEB-INF/views/partials/head.jsp" %>
</head>

<body class="hold-transition login-page">
  <!-- ------------------------------ Login box ------------------------------ -->
  <div class="login-box">
    <!-- ----------------------------- Login logo ------------------------------ -->
    <div class="login-logo">
      <a href="/"><b>HRMS</b>-Extension</a>
    </div>
    <!-- --------------------------- End login logo ---------------------------- -->

    <!-- ------------------------------ Form card ------------------------------ -->
    <div class="card">
      <div class="card-body login-card-body">
        <p class="login-box-msg">Sign in to start your session</p>

        <% if (request.getAttribute("error") !=null) { %>
          <!-- ------------------------------ Error box ------------------------------ -->
          <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <%= request.getAttribute("error") %>
              <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
              </button>
          </div>
          <!-- ---------------------------- End error box ---------------------------- -->
          <% } %>
            <!-- -------------------------------- Form --------------------------------- -->
            <form action="/auth" method="post">
              <div class="input-group mb-3">
                <input name="username" type="text" class="form-control" placeholder="Username">
                <div class="input-group-append">
                  <div class="input-group-text">
                    <span class="fas fa-envelope"></span>
                  </div>
                </div>
              </div>
              <div class="input-group mb-3">
                <input name="password" type="password" class="form-control" placeholder="Password">
                <div class="input-group-append">
                  <div class="input-group-text">
                    <span class="fas fa-lock"></span>
                  </div>
                </div>
              </div>
              <div class="row">
                <!-- ----------------------------- Submit col ------------------------------ -->
                <div class="col">
                  <button type="submit" class="btn btn-primary btn-block">Sign In</button>
                </div>
                <!-- --------------------------- End submit col ---------------------------- -->
              </div>
            </form>
            <!-- ------------------------------ End form ------------------------------- -->
      </div>
      <!-- ------------------------- End login card body ------------------------- -->
    </div>
    <!-- --------------------------- End login card ---------------------------- -->
  </div>
<!-- --------------------------- End login box ---------------------------- -->

  <%-- Include scripts --%>
    <%@ include file="/WEB-INF/views/partials/scripts.jsp" %>
</body>

</html>
