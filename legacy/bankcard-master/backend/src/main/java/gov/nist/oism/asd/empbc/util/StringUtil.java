package gov.nist.oism.asd.empbc.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtil {

    // https://snyk.io/blog/java-url-encoding-decoding/
    public static String escapeURL(final String url) {
        if (isEmpty(url)) {
            return url;
        }

        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isEmpty(final String str) {
        return (str == null) || str.isEmpty();
    }

    /**
     * @param url original URL
     * @param name Parameter name
     * @param value {@value null} = removes the parameter
     * @param replace {@value true} = replace if exists, otherwise skip
     * @return modified URL
     */
    public static String appendQueryParameter(String url,
            final String name, final String value, boolean replace) {
        if (isEmpty(url)) {
            return url;
        }

        int qmPos = url.indexOf('?');
        if (qmPos < 0) {
            if (value != null) {
                url += "?" + name + "=" + escapeURL(value);
            }
        } else {
            final var nvRE = Pattern.compile("((?:^|&)" + name + "=)([^&]*)");
            final var matcher = nvRE.matcher(url.substring(qmPos + 1));

            if (matcher.find()) {
                if (replace || isEmpty(value)) {
                    url = url.substring(0, qmPos + 1)
                            + matcher.replaceFirst((value == null) ? ""
                                    : "$1" + escapeURL(value));
                }
            } else {
                url += "&" + name + "=" + escapeURL(value);
            }
        }

        return url;
    }

    public static String sanitizeString(String input) {
        if (isEmpty(input)) {
            return input;
        }
        // Normalize the string to decompose combined characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Remove non-ASCII characters
        String asciiOnly = normalized.replaceAll("[^\\p{ASCII}]", "");
        return asciiOnly;
    }

    /* XXX Make into unit test
	public static void main(String args[]) {
		System.out.println(appendQueryParameter(
				"INDEX.JSP?test=it", "one", "o1", false));
		System.out.println(appendQueryParameter(
				"INDEX.JSP?test=it", "test", "hmm", false));		
		System.out.println(appendQueryParameter(
				"INDEX.JSP", "one", "o1", false));		
		System.out.println(appendQueryParameter(
				"INDEX.JSP?test=it", "test", "xzy", true));
		System.out.println(appendQueryParameter(
				"INDEX.JSP?test=it&else=sowhat", "test", "xzy", true));		
		System.out.println(appendQueryParameter(
				"INDEX.JSP?test=it", "test", null, true));
		System.out.println(appendQueryParameter(
				"INDEX.JSP?test=it", "test", null, false));
	}
     */
}
