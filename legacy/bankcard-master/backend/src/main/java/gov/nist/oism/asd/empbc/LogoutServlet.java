package gov.nist.oism.asd.empbc;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();
        session.invalidate();
        response.setContentType(MediaType.APPLICATION_JSON);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(String.format("{ \"sessionInvalidated\": \"%s\" }", sessionId));
        out.flush();
    }
}
