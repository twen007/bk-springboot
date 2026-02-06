<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<%
    final Map<Integer, String> ERROR_MESSAGES = Map.of(
        401, "You are not authorized.",
        403, "You are not authorized to access this particular application.",
        415, "Internal error (content type).",
        500, "An internal error happened.",
        503, "The application seems to be unreachable."
    );

    String msg = request.getParameter("msg");
    if ((msg == null) || (msg.length() == 0)) {
        final int status = response.getStatus();

        msg =  ERROR_MESSAGES.getOrDefault(status, "Unknown error");
    }
%>
<!DOCTYPE HTML>
<html lang="en-US">
<head>
    <title>An Error Occurred</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <style>
        body {
            background: linear-gradient(#BB4949, #E7E7E7) no-repeat fixed;
            font-size: 10pt; 
            font-family: "Verdana", Sans-serif;
            text-align: center;
            margin: 0;
            height: 100%;
        }

        #content {
            position: absolute;
            top: 0;
        	bottom: 0;
        	left: 0;
        	right: 0;
            margin: auto;
            height: 12em;
        }

        #panel {
            margin: auto;
            padding: 16px;
            width: 500px;
            border: 1px solid #af5774;
            border-radius:13px;
            background: rgba(240, 240, 240, 0.2);
            font-size: 12pt;
            font-weight: bold;
        }
        
        #contact {
            font-size: 9pt;
            font-weight: bold;
            margin-top: 1em;
        }
    </style>
    <script>
        function refreshPage() {
            location.reload();
        }
    </script>
</head>

<body>
	<div id="content">
        <h4>Error</h4>

        <div id="panel">
            <%= msg %>
        </div>
                <div id="contact">Please try again by refreshing the page with this <a href="#" onclick="refreshPage()">link</a></div>
                <br>
		<div id="contact">For further assistance, please <a href="mailto:mml.systemshelp@nist.gov">email MML Systems Support</a></div>
	</div>
</body>
</html>

cosigmn