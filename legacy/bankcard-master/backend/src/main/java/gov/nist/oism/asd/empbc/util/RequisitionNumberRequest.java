package gov.nist.oism.asd.empbc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import gov.nist.oism.asd.empbc.util.RequisitionNumberRequest.RequisitionNumberResponse;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
public class RequisitionNumberRequest {

    private static final Logger LOG = Logger.getLogger(RequisitionNumberRequest.class.getSimpleName());

    private static SSLContext mSslContext;

    static {
        try {
            mSslContext = SSLContext.getInstance("TLS");
            mSslContext.init(null, new TrustManager[]{new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

            }}, new java.security.SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException ignore) {
        }
    }

    @Getter // Optional: Usually builder fields aren't accessed directly from outside
    @Setter // Optional: Usually builder fields aren't accessed directly from outside
    @NoArgsConstructor // Generates the default constructor
    @Accessors(fluent = true, chain = true) // Enables fluent builder pattern (e.g., builder.url("...").code("..."))
    public static class Builder {

        private String url;
        private String code;
        private String divCd;
        private String grpCd;
        private String bankCardIni;
        private String fy;

        public RequisitionNumberRequest build() {
            if (StringUtil.isEmpty(url) || StringUtil.isEmpty(code)
                    || StringUtil.isEmpty(divCd) || StringUtil.isEmpty(grpCd)) {
                String errorMessage = String.format(
                        "Cannot build RequisitionNumberRequest. Missing required fields: url=%s, code=%s, divCd=%s, grpCd=%s",
                        url, code, divCd, grpCd);
                LOG.log(Level.SEVERE, errorMessage);
                // Use IllegalArgumentException for invalid parameters during object creation
                throw new IllegalArgumentException(errorMessage);
            }
            // Use the private constructor of the outer class for instantiation
            return new RequisitionNumberRequest(this);
        }
    }

    private String url;
    private String code;
    private String divCd;
    private String grpCd;
    private String bankCardIni;
    private String fy;

    private RequisitionNumberRequest(Builder builder) {
        this.url = builder.url;
        this.code = builder.code;
        this.divCd = builder.divCd;
        this.grpCd = builder.grpCd;
        this.bankCardIni = builder.bankCardIni;
        this.fy = builder.fy;
    }

    public String processRequest() throws StatusCodeException {
        // Check if SSLContext was initialized successfully
        if (mSslContext == null) {
            LOG.severe("SSLContext is not initialized. Cannot proceed with the request.");
        }

        StringBuilder urlBuilder = new StringBuilder(url + "?");
        urlBuilder.append("code=").append(code).append("&divCd=").append(divCd).append("&grpCd=").append(grpCd);
        if (bankCardIni != null && !bankCardIni.isEmpty()) {
            urlBuilder.append("&bankCardIni=").append(bankCardIni);
        }

        if (fy != null && !fy.isEmpty()) {
            urlBuilder.append("&fy=").append(fy);
        }

        String finalUrl = urlBuilder.toString();
        LOG.log(Level.INFO, "Requesting Requisition Number from URL: {0}", finalUrl);

        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        String json = null;
        RequisitionNumberResponse reqResponse = null;

        try ( Response response = client.target(urlBuilder.toString())
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            int statusCode = response.getStatus();
            json = response.readEntity(String.class);

            if (statusCode != Response.Status.OK.getStatusCode()) {
                LOG.log(Level.SEVERE, "Failed to get requisition number. Status: {0}, Response: {1}",
                        new Object[]{statusCode, json});
                // Throw a specific exception based on status code if needed, or a general one
                throw new StatusCodeException(statusCode,
                        "Failed to retrieve requisition number from service. Status: " + statusCode);
            }

            // response.close();
            Gson gson = new GsonBuilder().create();
            reqResponse = gson.fromJson(json, RequisitionNumberResponse.class);
            // Check the success flag within the response payload
            if (reqResponse == null || !reqResponse.isSuccess()) {
                String errorDescription = "Unknown error from requisition service.";
                int errorCode = StatusCode.ServerError.getCode(); // Default error code
                if (reqResponse != null && reqResponse.getError() != null) {
                    errorDescription = reqResponse.getError().getDescription();
                    errorCode = reqResponse.getError().getCode(); // Use code from response if available
                    LOG.log(Level.SEVERE, "Requisition service returned an error. Code: {0}, Description: {1}",
                            new Object[]{errorCode, errorDescription});
                } else {
                    LOG.log(Level.SEVERE,
                            "Requisition service response indicates failure but error details are missing or response is null. Response JSON: {0}",
                            json);
                    errorDescription = "Requisition service failed, but error details are unavailable in the response.";
                }
                // Use the specific error code and description from the response if available
                throw new StatusCodeException(errorCode, "Requisition service error: " + errorDescription);
            }

            // Successfully retrieved data
            return reqResponse.getData();
        } catch (ProcessingException e) {
            // Handle client-side processing errors (e.g., network issues, connection
            // timeouts)
            LOG.log(Level.SEVERE, "Error processing request to " + finalUrl, e);
            throw new StatusCodeException(StatusCode.ServerError.getCode(),
                    "Network or client error during requisition request: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            // Handle JSON parsing errors
            LOG.log(Level.SEVERE, "Error parsing JSON response: " + json, e);
            throw new StatusCodeException(StatusCode.ServerError.getCode(),
                    "Invalid JSON response received from requisition service.");
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            LOG.log(Level.SEVERE, "Unexpected error during requisition number request to " + finalUrl, e);
            throw new StatusCodeException(StatusCode.ServerError.getCode(),
                    "An unexpected error occurred: " + e.getMessage());
        }

    }

    @Getter
    @Setter
    public static class RequisitionNumberResponse {

        @Getter
        @Setter
        public static class Error {

            private int code;
            private String description;
        }

        private boolean success;
        private Error error;
        private String data;
    }

}
