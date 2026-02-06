package gov.nist.oism.asd.empbc.util;

import gov.nist.oism.asd.empbc.config.PropertyLoader;

public class ApiUtil {

    public static String getMmlOuItsoUrl(String ouCode) {
        return String.format(PropertyLoader.getProperty("mml.ou.itso.url"), ouCode);
    }

    public static String getMmlEmployeeRolesUrl(Integer peopleId) {
        return String.format(PropertyLoader.getProperty("mml.employee.roles.url"), peopleId);
    }

    public static String getMmlEmployeeBankcardApproversUrl(Integer peopleId) {
        return String.format(PropertyLoader.getProperty("mml.employee.bankcard.approvers.url"), peopleId);
    }

    public static String getMmlBankcardHoldersUrl(String divisionCode) {
        return String.format(PropertyLoader.getProperty("mml.bankcard.holders.url"), divisionCode);
    }

    public static String getMmlBankcardApprovingOfficialUrl(String divisionCode) {
        return String.format(PropertyLoader.getProperty("mml.bankcard.approving.official.url"), divisionCode);
    }

    public static String getMmlFundsCertifyingOfficialUrl(String divisionCode) {
        return String.format(PropertyLoader.getProperty("mml.funds.certifying.official.url"), divisionCode);
    }

    public static String getMmlCisproUsersUrl(String divCode, String grpCode) {
        return String.format(PropertyLoader.getProperty("mml.cispro.users.url"), divCode, grpCode);
    }

    public static String getMmlSupportedDivisionsUrl(Integer peopleId) {
        return String.format(PropertyLoader.getProperty("mml.supported.divisions.url"), peopleId);
    }

    public static String getMmlPropertyCustodiansUrl(String divisionCode) {
        return String.format(PropertyLoader.getProperty("mml.property.custodians.url"), divisionCode);
    }

    public static String getMmlOuRolesUrl(String ouCode) {
        return String.format(PropertyLoader.getProperty("mml.ou.roles.url"), ouCode);
    }

    //not used
    public static String getMmlDivisionItsoUrl(String divisionCode) {
        return String.format(PropertyLoader.getProperty("mml.division.itso.url"), divisionCode);
    }

    public static String getMmlDivisionChiefUrl(String divisionCode) {
        return String.format(PropertyLoader.getProperty("mml.division.chief.url"), divisionCode);
    }
    
    public static String getDrUrl(String ouCode) {
        return String.format(PropertyLoader.getProperty("mml.mission.critical.director.approver.url"), ouCode);
    }

    //not used
    public static String getMmlDeputyChiefUrl(String divisionCode) {
        return String.format(PropertyLoader.getProperty("mml.deputy.chief.url"), divisionCode);
    }

    public static String getMmlDelegationByUsernameUrl(String username) {
        return String.format(PropertyLoader.getProperty("mml.delegation.by.username.url"), username);
    }

    public static String getMmlEmployeeByNistOrgIdUrl(Integer nistOrgId) {
        return String.format(PropertyLoader.getProperty("mml.employee.by.nist.org.id.url"), nistOrgId);
    }
}
