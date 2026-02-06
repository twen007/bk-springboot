package gov.nist.oism.asd.empbc.util;

import com.google.gson.Gson;
import gov.nist.oism.asd.empbc.config.PropertyLoader;
import gov.nist.oism.asd.empbc.model.IbbrChemicalItem;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpPost;
import gov.nist.oism.asd.empbc.v1.SsoService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

public class IbbrWSCalls {

    private static final Logger LOG = Logger.getLogger(IbbrWSCalls.class.getSimpleName());

    public static SsoService.Error createIbbrChemicalItem(HttpServletRequest servletRequest, IbbrChemicalItem ibbrChemicalItem) {
        String jsonString = chemicalItemToJsonString(ibbrChemicalItem);
        LOG.info(String.format("Making the IBBR call with: %s", jsonString));
        return postIbbrRecord(servletRequest, jsonString, true);
    }

    // the WsCallFailedRecord is already in the database, the admin user resubmit the request
    public static SsoService.Error resyncIbbrRecord(HttpServletRequest servletRequest, String jsonString) {
        return postIbbrRecord(servletRequest, jsonString, false);
    }

    private static SsoService.Error postIbbrRecord(HttpServletRequest servletRequest, String jsonString, boolean retry) {
        String wsUrl = PropertyLoader.getProperty("ibbr.create.chemical.item.url");
        HttpPost httpPost = new HttpPost(wsUrl);
        LOG.info(String.format("wsUrl %s", wsUrl));

        addHeadersToHttpPost(httpPost, PropertyLoader.getProperty("ibbr.username.and.password"));

        try {
            LOG.info(String.format("Making the IBBR call with: %s", jsonString));

            httpPost.setEntity(new StringEntity(jsonString));
            CloseableHttpClient httpClient = null;
            if (retry) {
                //add custom retry handler and wait time to retry when ssl exception happens
                int maxRetries = 6;
                int waitPeriod = 1000; // milliseconds 

                httpClient = HttpClientBuilder.create()
                        .setRetryHandler((IOException exception, int execCount, HttpContext context) -> {
                            if (execCount > maxRetries) {
                                return false;
                            } else {
                                try {
                                    Thread.sleep(waitPeriod);
                                } catch (InterruptedException ex) {
                                    //ignore
                                }
                                return true;
                            }
                        })
                        .build();
            } else {
                httpClient = HttpClientBuilder.create().build();
            }

            HttpResponse httpResponse = httpClient.execute(httpPost);
            int rspCode = httpResponse.getStatusLine().getStatusCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            LOG.info(String.format("Response status code : %d", rspCode));
            LOG.info(String.format("Response string: %s", response.toString()));

            if (rspCode != Response.Status.OK.getStatusCode()) {
                SsoService.Error error = new SsoService.Error();
                error.setCode(rspCode);
                error.setDescription(response.toString());
                return error;
            }

        } catch (IOException | UnsupportedOperationException caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            SsoService.Error error = new SsoService.Error();
            error.setCode(StatusCode.IbbrPostFailed.getCode());
            error.setDescription(caught.getMessage());
            return error;

        } finally {
            httpPost.releaseConnection();
        }

        return null;
    }

    private static void addHeadersToHttpPost(HttpPost httpPost, String httpBasicCredential) {
        byte[] encodedAuth = Base64.encodeBase64(httpBasicCredential.getBytes(Charset.forName("US-ASCII")));
        httpPost.setHeader("AUTHORIZATION", "Basic " + new String(encodedAuth));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("X-Stream", "true");
    }

    // the json string to call the IBBR web service shall not include item_id 
    public static String chemicalItemToJsonString(IbbrChemicalItem ibbrChemicalItem) {
        //return gson.toJsonTree(this).getAsJsonObject().remove("itemId").toString();

        Gson gson = new Gson();
        return gson.toJson(ibbrChemicalItem);
    }
}
