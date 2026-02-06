package gov.nist.oism.asd.empbc;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger; 

@WebFilter(filterName = "FrameSecurityFilter", urlPatterns = {"/app/*"})
public class FrameSecurityFilter implements Filter {
    
     private static final Logger LOG = Logger.getLogger(FrameSecurityFilter.class.getSimpleName());
     
     public static class State {
	private String orgURL;

	public String getOrgURL() {
		return orgURL;
	}

	public void setOrgURL(String orgURL) {
		this.orgURL = orgURL;
	}
}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        
        httpServletResponse.addHeader("X-Content-Type-Options", "nosniff"); 
        httpServletResponse.addHeader("X-Frame-Options", "SAMEORIGIN");
        httpServletResponse.addHeader("X-XSS-Protection", "1; mode=block");
        
        //HttpSession session = httpServletRequest.getSession();
        //LOG.info("doFilter - entered; " + httpServletRequest.getRequestURL().toString());     
        //String sessionState = httpServletRequest.getParameter("sessionstate");
        
        //String orgUrl=(String)session.getAttribute("orgURL");
        //if ( sessionState!= null) {
        //    byte[] decodedBytes = Base64.getDecoder().decode(sessionState);
        //   String decodedString = new String(decodedBytes);
            //State state = JsonUtil.jsonToObject(decodedString, State.class);

            //String orgUrl = state.getOrgURL();
            //LOG.info("session stored org url is: " + orgUrl); 
        //}
        
        
        
        //OKTA change
        //since fragments are client technology and the servlet request do not include them
        //for example, the #requesttracking/83379 part will not exists when okta valve redirect the user
        //after authentication. to make deeping link work, we need to change links used in notification
        //emails from #something to ?subview=something so that okta valve will get query params and include it
        //in the redirect url like this:       
        //http://localhost:8080/empbc/app/index.html?subview=requesttracking/83379
        String hashParam=httpServletRequest.getParameter("subview");
        
        //NOTE: the code below is for convert the parameter style in the url to fragment style. It does work and
        //send the user to the correct view in the app but it causes a full app reload each time since we used redirect
        //so commentted out this and go with the client side solution in the onBeforeRoute handler instead
        
        
        //TODO: on rare case, users may see 500 error due to the 
        //backend complains about sendError method is called after response was committed
        //it has something to do with OKTA valve code. Jason suggest to use
        //httpServletRequest.getServletContext().getRequestDispatcher("/{somepage}").include(request, response);
        //which might fix the issue    
            
        //when we detect the subview param, we will convert it to fragement, add it to the url and do a
        //redirect so ExtJS client app can process it 
        if(hashParam!=null){
            String newUrl = httpServletRequest.getRequestURL().toString().split("\\?")[0] + "#" + hashParam;
            httpServletResponse.sendRedirect(newUrl);
        }else{
            //for urls without the subview parameter, just proceed without mod
            chain.doFilter(request, response);
        }
        //chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {        
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}
