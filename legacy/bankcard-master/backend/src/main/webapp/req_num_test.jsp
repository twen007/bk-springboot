<%-- 
    Document   : req_num_test
    Created on : Jul 15, 2019, 2:59:49 PM
    Author     : jtp1
--%>

<%@page import="javax.net.ssl.HttpsURLConnection"%>
<%@page import="com.google.gson.JsonSyntaxException"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.util.stream.Collectors"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.BufferedReader"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Requisition Number</title>
    </head>
    <body>
        <form method="get">
            URL:<br/>
            <input type="text" name="url" size="50" value="https://emp.nist.gov/nap/controllers/v1/divReqNum/generate"/><br/>
            Token:<br/>
            <input type="text" name="token" size="50"/><br/>
            Division Code:<br/>
            <input type="text" name="division_code" value="183"/><br/>
            Group Code:<br/>
            <input type="text" name="group_code" value="00"/><br/>
            Bankcard initials:<br/>
            <input type="text" name="bankcard_ini"/><br/><br/>
            <input type="submit"/>
        </form>

        <%
            String url = request.getParameter("url");
            String token = request.getParameter("token");
            String divisionCode = request.getParameter("division_code");
            String groupCode = request.getParameter("group_code");
            String bankCardIni = request.getParameter("bankcard_ini");
            String error = null;
            String serializedJson = null;
            if (url != null && url.length() > 0 && token != null && token.length() > 0 && divisionCode != null && divisionCode.length() > 0 && groupCode != null && groupCode.length() > 0) {
                BufferedReader reader = null;
                try {
                    StringBuilder urlBuilder = new StringBuilder(url + "?");
                    urlBuilder.append("code=").append(token).append("&divCd=").append(divisionCode).append("&grpCd=").append(groupCode);
                    if (bankCardIni != null && !bankCardIni.isEmpty()) {
                        urlBuilder.append("&bankCardIni=").append(bankCardIni);
                    }
                    URL endpointUrl = new URL(urlBuilder.toString());
                    HttpsURLConnection connection = (HttpsURLConnection) endpointUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/json");
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    serializedJson = reader.lines().collect(Collectors.joining());
                }
                catch (JsonSyntaxException | IOException caught) {
                    error = caught.getMessage();
                }
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException ignore) {
                        }
                    }
                }
                String output = serializedJson;
                if (error != null) {
                    output = error;
                }
        %>
        <br/><br/>
        You output is <%= output%>
        
        <form action="/empbc/req_num_test.jsp">
            <input type="submit" value="Clear"/>
        </form>
        <%
            }
        %>
    </body>
</html>
