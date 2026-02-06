<%@page import="gov.nist.oism.asd.empbc.config.PropertyLoader"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		 <%-- Clickjack frame burst --%>
		<style id="antiClickjack">body { display:none !important; }</style>
		<script type="text/javascript">
		  if (self === top) {
			  var antiClickjack = document.getElementById("antiClickjack");
			  antiClickjack.parentNode.removeChild(antiClickjack);
		  } else {
			  top.location = self.location;
		  }
		</script>
        <title>Expense Management Program - Bankcard Purchase Request</title>
    </head>
    <body>
        <h1>EMPBC v<%= PropertyLoader.getProperty("version") %></h1>
        <h3>use_sso_proxy: <%= PropertyLoader.getProperty("use.sso.proxy") %></h3>
        <h3>use_gendev_password: <%= PropertyLoader.getProperty("use.gendev.password") %></h3>
        <h3>app_environment: <%= PropertyLoader.getProperty("app.environment") %></h3>
        <h3>are_ibbr_ws_calls_active: <%= PropertyLoader.getProperty("are.ibbr.ws.calls.active") %></h3>
    </body>
</html>
