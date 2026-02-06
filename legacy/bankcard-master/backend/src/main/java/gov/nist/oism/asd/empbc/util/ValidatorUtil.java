package gov.nist.oism.asd.empbc.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.ws.rs.BadRequestException;
/**
 * validate query params, throw BadRequestException if values are not expected
 * @author xinweiw
 */
public class ValidatorUtil {
    private static final Logger LOG = Logger.getLogger(ValidatorUtil.class.getName());
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static Date parseDate(String dateString, String parameterName) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null; // Or throw an exception if empty strings are not allowed
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setLenient(false); // Disallow lenient parsing (e.g., 2023-02-30)

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            LOG.warning(String.format("Error parsing %s for parameter %s: %s", dateString, parameterName, e.getMessage()));
            throw new BadRequestException(String.format("Invalid date format for parameter %s. Expected format: %s", parameterName, DATE_FORMAT));
        }
    }
    
}
