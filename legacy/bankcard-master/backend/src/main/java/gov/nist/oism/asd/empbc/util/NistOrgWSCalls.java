package gov.nist.oism.asd.empbc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NistOrgWSCalls {

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

    // For MML bankcard approving official web service.
    public static class MmlBankcardApprovingOfficialCall {

        public static class BankcardApprovingOfficial {

            public Role role;
            public List<Employee> possible_holders;
            public List<Employee> current_holders;
        }

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public int group_id;
            public boolean active;
        }

        public int status;
        public String returnMessage;
        @SerializedName("Bankcard Approving Official")
        public BankcardApprovingOfficial bankcard_approving_official;
        public String error;
    }

    // For MML Funds Certifying Official web service.
    public static class MmlFundsCertifyingOfficialCall {

        public static class FundsCertifyingOfficial {

            public Role role;
            public List<Employee> possible_holders;
            public List<Employee> current_holders;
        }

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public int group_id;
            public boolean active;
        }

        public int status;
        public String returnMessage;
        @SerializedName("Funds Certifying Official")
        public FundsCertifyingOfficial funds_Certifying_Official;
        public String error;
    }

    // For Division Cheif web service.
    public static class DivChiefCall {

        public static class divisionChief {

            public Role role;
            public List<Employee> possible_holders;
            public List<Employee> current_holders;
        }

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public int group_id;
            public boolean active;
        }

        public int status;
        public String returnMessage;
        @SerializedName("Division Chief")
        public divisionChief division_chief;
        public String error;
    }
    
    public static class DrCall {

       public static class Roles {

            public Role role;
            public List<Employee> current_holder;
            public List<Employee> possible_holders;
        }

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public int group_id;
            public boolean active;
        }

        public int status;
        public String returnMessage;
        @SerializedName("roles")
        public List<Roles> roles;
        public String error;
    }

    // For ITSO web service.
    public static class itsoCall {

        public static class Roles {

            public Role role;
            public List<Employee> current_holder;
            public List<Employee> possible_holders;
        }

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public int group_id;
            public boolean active;
        }

        public int status;
        public String returnMessage;
        @SerializedName("roles")
        public List<Roles> roles;
        public String error;
    }

    public static class OuRolesCall {

        public static class Roles {

            public Role role;
            public List<Employee> current_holder;
        }

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public int group_id;
            public boolean active;
        }

        public int status;
        public String returnMessage;
        @SerializedName("roles")
        public List<Roles> roles;
        public String error;
    }

    // For MML bankcard holders web service.
    public static class MmlBankcardHolderCall {

        public static class BankcardHolder {

            public Role role;
            public List<Employee> possible_holders;
            public List<Employee> current_holders;
        }

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public int group_id;
            public boolean active;
        }

        public int status;
        public String returnMessage;
        @SerializedName("Bankcard Holder")
        public BankcardHolder bankcard_holder;
        public String error;
    }

    // For MML employee profile web service.
    public static class MmlEmployeeProfileCall {

        public static class Role {

            public int id;
            public String name;
            public String category;
        }

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
        }

        public int status;
        public String returnMessage;
        public Employee employee;
        public List<Role> roles;
        public String error;
    }

    // MB-474
    // For MML delegation web service
    public static class MmlDelegationsByUsernameCall {

        public static class Delegation {

            public int id;
            public int delegator_id;
            public int delegate_id;
        }

        public int status;
        public String returnMessage;
        public List<Delegation> delegations;
        public String error;
    }

    // For MML employee by nistorgid service
    public static class MmlEmployeeByNistOrgIdCall {

        public static class Employee {

            public String username;
            public String first_name;
            public String middle_name;
            public String last_name;
        }

        public int status;
        public String returnMessage;
        public Employee employee;
        public String error;
    }

    // For MML bankcard approvers web service.
    public static class MmlBankcardApproversCall {

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
            public String employee_type;
        }

        public static class Approver {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
        }

        public int status;
        public String returnMessage;
        public Employee employee;
        public Approver host;
        public Approver supervisor;
        public Approver group_leader;
        public Approver division_chief;
        public Approver bankcard_approving_official;
        public Approver bankcard_holder;
        public Approver administrative_officer;
        public List<Integer> approval_chain;
        public String error;
    }

    // For MML cispro users web service.
    public static class MmlCisproUsersUrlCall {

        public static class Group {

            public int id;
            public String name;
            public int division_id;
            public String code;
        }

        public static class CisproUser {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
        }

        public int status;
        public String returnMessage;
        public Group group;
        public List<CisproUser> cims_power_users;
        public String error;
    }

    public static class MmlPropertyCustodianUsersUrlCall {

        public static class PropertyCustodianUser {

            public int id;
            public String first_name;
            public String last_name;
            public int employee_number;
        }

        public int status;
        public String returnMessage;
        public List<PropertyCustodianUser> property_custodians;
        public String error;
    }

    public static class MmlSupportedDivisionsUrlCall {

        public static class Employee {

            public int id;
            public String first_name;
            public String last_name;
            public String group_id;
            public int employee_number;
        }

        public static class Division {

            public int id;
            public String name;
            public String organization_unit_id;
            public String code;
            public boolean active;
            public String created_at;
            public String updated_at;
            public int chief_id;
            public int administrative_officer_id;
            public int safety_officer_id;
            public int deputy_chief_id;
            public int secretary_id;
            public int property_officer_id;
            public int itso_id;
            public int administrative_office_assistant_id;
        }

        public int status;
        public String returnMessage;
        public Employee employee;
        public List<Division> divisions;
        public String error;
    }

    public static MmlBankcardApprovingOfficialCall callMmlBankcardApprovingOfficialService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlBankcardApprovingOfficialCall.class);
    }

    public static MmlFundsCertifyingOfficialCall callFundsCertifyingOfficialService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlFundsCertifyingOfficialCall.class);
    }

    public static OuRolesCall callOuRolesService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, OuRolesCall.class);
    }

    public static itsoCall callItsoService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, itsoCall.class);
    }

    public static DivChiefCall callDcService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, DivChiefCall.class);
    }
    
     public static DrCall callDrService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, DrCall.class);
    }

    public static MmlBankcardHolderCall callMmlBankcardHolderService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlBankcardHolderCall.class);
    }

    public static MmlEmployeeProfileCall callMmlEmployeeProfileService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlEmployeeProfileCall.class);
    }

    // MB-474
    public static MmlDelegationsByUsernameCall callMmlDelegationsByUsernameService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlDelegationsByUsernameCall.class);
    }

    public static MmlEmployeeByNistOrgIdCall callMmlEmployeeByNistOrgIdService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlEmployeeByNistOrgIdCall.class);
    }

    public static MmlBankcardApproversCall callMmlBankcardApproversService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlBankcardApproversCall.class);
    }

    public static MmlCisproUsersUrlCall callMmlCisproUsersService(String divOrgCode) {
        String wsUrl = ApiUtil.getMmlCisproUsersUrl(divOrgCode.substring(0, 3), divOrgCode.substring(3, 5));
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlCisproUsersUrlCall.class);
    }

    public static MmlPropertyCustodianUsersUrlCall callMmlPcUsersService(String divOrgCode) {
        String wsUrl = ApiUtil.getMmlPropertyCustodiansUrl(divOrgCode);
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlPropertyCustodianUsersUrlCall.class);
    }

    public static MmlSupportedDivisionsUrlCall callMmlSupportedDivisionsService(String wsUrl) {
        Client client = ClientBuilder.newBuilder().sslContext(mSslContext).build();
        Response response = client.target(wsUrl)
                .request(MediaType.APPLICATION_JSON)
                .get();
        String json = response.readEntity(String.class);
        response.close();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, MmlSupportedDivisionsUrlCall.class);
    }
}
