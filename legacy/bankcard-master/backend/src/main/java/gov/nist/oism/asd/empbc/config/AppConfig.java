package gov.nist.oism.asd.empbc.config;

import gov.nist.oism.asd.empbc.filters.AdminOnlyFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author xinweiw
 */
public class AppConfig extends ResourceConfig{
     public AppConfig() {
    	packages("gov.nist.oism.asd.empbc.v1");
    	register(AdminOnlyFilter.class);
    	register(MultiPartFeature.class);

    }
    
    
}
