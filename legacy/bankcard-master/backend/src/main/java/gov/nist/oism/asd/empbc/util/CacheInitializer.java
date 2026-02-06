/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.util;

/**
 *
 * @author xinweiw
 */
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class CacheInitializer implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(CacheInitializer.class.getSimpleName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.log(Level.INFO, "Initializing CacheManager via ServletContextListener...");
        try {
            // Initialize the CacheManager. This will trigger the cache loading process.
            //CacheManager.getInstance();
            //LOG.log(Level.INFO, "CacheManager initialized successfully.");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error initializing CacheManager", e);
            // Handle the exception appropriately. Consider throwing a RuntimeException to halt application startup.
            // throw new RuntimeException("Failed to initialize CacheManager", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No need to explicitly shut down the CacheManager in this case.
        // Guava caches are automatically garbage collected.
        // You might log something here if needed.
        //LOG.log(Level.INFO, "CacheManager context destroyed.");
    }
}