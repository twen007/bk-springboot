package gov.nist.oism.asd.empbc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyLoader {

    private static final Map<String, String> properties = loadProperties();

    public static String getProperty(String key) {
        String val = properties.get(key);
        if (val == null) {
            System.err.println("Error loading property " + key);
        }
        return val;
    }

    private static Map<String, String> loadProperties() {
        Map<String, String> props = new HashMap<>(); 
        String env = System.getenv("ENV"); // Get the environment variable
        if (env == null || env.trim().isEmpty()) {
            env = "default"; // Default environment if not set
        }
        System.out.println("app environment: " + env);
        String propertyFileName = "application-" + env + ".properties";
        InputStream inputStream = PropertyLoader.class.getClassLoader().getResourceAsStream(propertyFileName);

        if (inputStream == null) {
            // Try to load default properties
            propertyFileName = "application.properties";
            inputStream = PropertyLoader.class.getClassLoader().getResourceAsStream(propertyFileName);
        }

        if (inputStream != null) {
            try {
                Properties p = new Properties();
                p.load(inputStream);
                for (final String name : p.stringPropertyNames()) {
                    props.put(name, p.getProperty(name));
                }
            } catch (IOException e) {
                System.err.println("Error loading properties from " + propertyFileName + ": " + e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("Error closing input stream: " + e.getMessage());
                }
            }
        } else {
            System.err.println("Could not find properties file: " + propertyFileName);
        }
        return props;
    }
}
