package gov.nist.oism.asd.empbc.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author xinweiw
 */
public class CommonUtil {

    private static final Logger LOG = Logger.getLogger(CommonUtil.class.getName());
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static boolean isStringNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Sets a date field in a model object from a date string, handling
     * potential parsing errors.
     *
     * @param dateString The date string to parse (expected format: yyyy-MM-dd).
     * @param setter A functional interface (e.g., lambda) to set the date in
     * the model object.
     * @param fieldName The name of the date field for logging purposes.
     * @throws IllegalArgumentException if the setter is null
     */
    public static void setDateFromString(String dateString, DateSetter setter, String fieldName) {
        if (setter == null) {
            throw new IllegalArgumentException("DateSetter cannot be null");
        }
        if (dateString != null && !dateString.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                Date date = dateFormat.parse(dateString);
                setter.setDate(date);
            } catch (ParseException e) {
                LOG.warning(String.format("Error parsing %s for field %s: %s", dateString, fieldName, e.getMessage()));
            }
        }
    }

    /**
     * Functional interface for setting a Date object.
     */
    @FunctionalInterface
    public interface DateSetter {

        void setDate(Date date);
    }

    /**
     * Gets a date string from a Date object.
     *
     * @param date The Date object to format.
     * @return The formatted date string (yyyy-MM-dd), or null if the input date
     * is null.
     */
    public static String getDateString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(date);
    }
}
