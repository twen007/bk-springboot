<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.springframework.core.env.Environment" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%
    Environment env = WebApplicationContextUtils
        .getRequiredWebApplicationContext(application)
        .getBean(Environment.class);
    String appEnv = env.getProperty("app.environment", "not set");
    String version = env.getProperty("version", "not set");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bankcard App Info</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f6fa; margin: 0; padding: 0; }
        .container { max-width: 600px; margin: 100px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); padding: 40px; text-align: center; }
        h1 { color: #2d3a4b; }
        p { color: #4a5a6a; }
        .info { font-size: 1.2em; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Bankcard App Info</h1>
        <div class="info">
            <strong>Environment:</strong> <%= appEnv %><br>
            <strong>Version:</strong> <%= version %>
        </div>
        <p>This page shows which properties file is loaded.</p>
    </div>
</body>
</html>
