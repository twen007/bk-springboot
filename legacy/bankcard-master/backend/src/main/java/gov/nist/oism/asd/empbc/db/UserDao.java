package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.UserDetailedPrivilege;
import gov.nist.oism.asd.empbc.model.UserPrivileges;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.OracleTypes;

public class UserDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(UserDao.class.getSimpleName());

    public static final String USER_KEY = "user_key";
    public static final String USER_LIST_KEY = "user_list_key";

    public Map<String, Object> selectUserByUsername(String username) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        User user = new User();
        String sql = "{ call sp_get_user_profile(?, ?) }";
        LOG.info(String.format("sql: %s, username = %s", sql, username.toLowerCase()));
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql);) {
            cstmt.setString("p_username", username.toLowerCase());
            cstmt.registerOutParameter("result_set", OracleTypes.CURSOR);
            cstmt.execute();
            try ( ResultSet rset = cstmt.getObject("result_set", ResultSet.class);) {
                if (rset.next()) {

                    // People Id.
                    int peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    // Boss id.
                    int bossId = rset.getInt("supervisor_people_id");
                    if (!rset.wasNull()) {
                        user.setBossId(bossId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    int ouId = rset.getInt("ou_org_id");
                    if (!rset.wasNull()) {
                        user.setOuId(ouId);
                    }

                    // Division id.
                    int divisionId = rset.getInt("div_org_id");
                    if (!rset.wasNull()) {
                        user.setDivisionId(divisionId);
                    }

                    // Group id.
                    int groupId = rset.getInt("grp_org_id");
                    if (!rset.wasNull()) {
                        user.setGroupId(groupId);
                    }

                    // Username.
                    user.setUsername(username);

                    // Email.
                    user.setEmail(rset.getString("email"));

                    // Phone number.
                    user.setPhoneNumber(rset.getString("phone"));

                    // Supervisor.
                    String supervisor = rset.getString("supervisor_yn");
                    if (supervisor != null && !supervisor.isEmpty()) {
                        user.setSupervisor(supervisor.equalsIgnoreCase("Y"));
                    }

                    // Last update date.
                    Timestamp lastUpdateDate = rset.getTimestamp("last_update_dt");
                    if (!rset.wasNull()) {
                        user.setLastUpdateDate(lastUpdateDate);
                    }

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    // Division cd.
                    user.setDivisionCode(rset.getString("div_org_cd"));

                    // Ou cd.
                    user.setOuCode(rset.getString("ou_org_cd"));

                    // Group cd.
                    user.setGroupCode(rset.getString("group_org_cd"));

                    results.put(USER_KEY, user);
                } else {
                    statusCode = StatusCode.UserNotFound;
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectUserByPeopleId(int peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        User user = new User();
        String sql = "SELECT "
                + "people_id, "
                + "first_name, "
                + "last_name, "
                + "mid_name, "
                + "ou_org_id, "
                + "div_org_id, "
                + "grp_org_id, "
                + "username, "
                + "email, "
                + "phone, "
                + "supervisor_yn, "
                + "last_update_dt, "
                + "staff_type, "
                + "supervisor_people_id, "
                + "(SELECT org_cd FROM nist_division WHERE org_id = div_org_id) AS div_org_cd, "
                + "(SELECT org_cd FROM nist_ou WHERE org_id = ou_org_id) AS ou_org_cd, "
                + "(SELECT org_cd FROM nist_group WHERE org_id = grp_org_id) AS group_org_cd"
                + " FROM "
                + "nist_user"
                + " WHERE "
                + " active_yn = 'Y' and "
                + "people_id = ?";
        LOG.info(String.format("sql: %s, people_id = %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, peopleId);
            try ( ResultSet rset = pstmt.executeQuery()) {
                if (rset.next()) {

                    // People Id.
                    peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    int bossId = rset.getInt("supervisor_people_id");
                    if (!rset.wasNull()) {
                        user.setBossId(bossId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    int ouId = rset.getInt("ou_org_id");
                    if (!rset.wasNull()) {
                        user.setOuId(ouId);
                    }

                    // Division id.
                    int divisionId = rset.getInt("div_org_id");
                    if (!rset.wasNull()) {
                        user.setDivisionId(divisionId);
                    }

                    // Group id.
                    int groupId = rset.getInt("grp_org_id");
                    if (!rset.wasNull()) {
                        user.setGroupId(groupId);
                    }

                    // Username.
                    user.setUsername(rset.getString("username"));

                    // Email.
                    user.setEmail(rset.getString("email"));

                    // Phone number.
                    user.setPhoneNumber(rset.getString("phone"));

                    // Supervisor.
                    String supervisor = rset.getString("supervisor_yn");
                    if (supervisor != null && !supervisor.isEmpty()) {
                        user.setSupervisor(supervisor.equalsIgnoreCase("Y"));
                    }

                    // Last update date.
                    Timestamp lastUpdateDate = rset.getTimestamp("last_update_dt");
                    if (!rset.wasNull()) {
                        user.setLastUpdateDate(lastUpdateDate);
                    }

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    // Division cd.
                    user.setDivisionCode(rset.getString("div_org_cd"));

                    // Ou cd.
                    user.setOuCode(rset.getString("ou_org_cd"));

                    // Group cd.
                    user.setGroupCode(rset.getString("group_org_cd"));

                    results.put(USER_KEY, user);
                } else {
                    statusCode = StatusCode.UserNotFound;
                }
            }
        } catch (Exception caught) {
            results.put(STATUS_CODE_KEY, StatusCode.DatabaseError);
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    //we only get active users so changed to use nist_user_active table
    public Map<String, Object> selectUsersInOuByPeopleId(Integer peopleId, String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        List<User> users = new ArrayList<>();
        boolean useFilter = filter != null && !filter.isEmpty();
        String sql;
        if (useFilter) {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type,"
                    + "email, "
                    + "active_yn "
                    + " FROM "
                    + "nist_user"
                    + " WHERE "
                    + "ou_org_id = (SELECT ou_org_id FROM nist_user WHERE people_id = ?)"
                    + " AND "
                    + " active_yn = 'Y' and "
                    + "(INSTR(LOWER(first_name), ?) > 0 OR INSTR(LOWER(last_name), ?) > 0)";
        } else {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type, "
                    + "email, "
                    + "div_org_id, " //add org data in return
                    + "grp_org_id " //add org data in return
                    //+ "active_yn "
                    + " FROM "
                    + "nist_user_active" //+ "nist_user"
                    + " WHERE "
                    //+ " active_yn = 'Y' and "
                    + "ou_org_id = (SELECT ou_org_id FROM nist_user_active WHERE people_id = ?)";
        }
        LOG.info(String.format("sql: %s, peopleId = %d, filter = %s", sql, peopleId, filter));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, peopleId);
            if (useFilter) {
                pstmt.setString(2, filter.toLowerCase().trim());
                pstmt.setString(3, filter.toLowerCase().trim());
            }
            try ( ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    User user = new User();

                    // People Id.
                    peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    user.setOuId(rset.getInt("ou_org_id"));
                    //div id
                    user.setDivisionId(rset.getInt("div_org_id"));
                    //grp id
                    user.setGroupId(rset.getInt("grp_org_id"));

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    // Email.
                    user.setEmail(rset.getString("email"));

                    //String active = rset.getString("active_yn");
                    //user.setActive(active.equalsIgnoreCase("Y"));
                    user.setActive(true);
                    users.add(user);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(USER_LIST_KEY, users);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
    public Map<String, Object> selectNistEmployees(String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        List<User> users = new ArrayList<>();
        boolean useFilter = filter != null && !filter.isEmpty();
        String sql;
        if (useFilter) {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type,"
                    + "email"
                    + " FROM "
                    + "nist_user_active"
                    + " WHERE "
                    + "staff_type = 'NIST Employee'"
                    + " AND "
                    + "(INSTR(LOWER(first_name), ?) > 0 OR INSTR(LOWER(last_name), ?) > 0)";
        } else {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type,"
                    + "email"
                    + " FROM "
                    + "nist_user_active"
                    + " WHERE "
                    + "staff_type = 'NIST Employee'";
        }
        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            if (useFilter) {
                pstmt.setString(1, filter.toLowerCase().trim());
                pstmt.setString(2, filter.toLowerCase().trim());
            }
            try ( ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    User user = new User();

                    // People Id.
                    int peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    user.setOuId(rset.getInt("ou_org_id"));

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    // email.
                    String email = rset.getString("email");
                    if (email != null && !email.isEmpty()) {
                        user.setEmail(email);
                    }

                    user.setActive(true);

                    users.add(user);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(USER_LIST_KEY, users);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectNistEmployeesInOu(Integer ouId, String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (ouId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        List<User> users = new ArrayList<>();
        boolean useFilter = filter != null && !filter.isEmpty();
        String sql;
        if (useFilter) {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type,"
                    + "email,"
                    + "active_yn "
                    + " FROM "
                    + "nist_user"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + "staff_type = 'NIST Employee'"
                    + " AND "
                    + " active_yn = 'Y' and "
                    + "(INSTR(LOWER(first_name), ?) > 0 OR INSTR(LOWER(last_name), ?) > 0)";
        } else {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type,"
                    + "email,"
                    + "active_yn "
                    + " FROM "
                    + "nist_user"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + " active_yn = 'Y' and "
                    + "staff_type = 'NIST Employee'";
        }
        LOG.info(String.format("sql: %s, ouId = %d", sql, ouId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, ouId);
            if (useFilter) {
                pstmt.setString(2, filter.toLowerCase().trim());
                pstmt.setString(3, filter.toLowerCase().trim());
            }
            try ( ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    User user = new User();

                    // People Id.
                    int peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    user.setOuId(rset.getInt("ou_org_id"));

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    // email.
                    String email = rset.getString("email");
                    if (email != null && !email.isEmpty()) {
                        user.setEmail(email);
                    }

                    String active = rset.getString("active_yn");
                    user.setActive(active.equalsIgnoreCase("Y"));

                    users.add(user);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(USER_LIST_KEY, users);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectNistEmployeesInDiv(Integer divId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (divId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        List<User> users = new ArrayList<>();

        String sql = "SELECT "
                + "people_id, "
                + "first_name, "
                + "last_name, "
                + "mid_name, "
                + "ou_org_id, "
                + "staff_type,"
                + "email,"
                + "active_yn "
                + " FROM "
                + "nist_user"
                + " WHERE "
                + "div_org_id = ?"
                + " AND "
                + " active_yn = 'Y' and "
                + "staff_type = 'NIST Employee'";

        LOG.info(String.format("sql: %s, divId = %d", sql, divId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, divId);

            try ( ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    User user = new User();

                    // People Id.
                    int peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    user.setOuId(rset.getInt("ou_org_id"));

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    // email.
                    String email = rset.getString("email");
                    if (email != null && !email.isEmpty()) {
                        user.setEmail(email);
                    }

                    String active = rset.getString("active_yn");
                    user.setActive(active.equalsIgnoreCase("Y"));

                    users.add(user);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(USER_LIST_KEY, users);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectReviewersInDivision(Integer divisionId, String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (divisionId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        List<User> users = new ArrayList<>();
        boolean useFilter = filter != null && !filter.isEmpty();
        String sql;
        if (useFilter) {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "div_org_id, "
                    + "supervisor_people_id, "
                    + "staff_type"
                    + " FROM "
                    + "nist_user_active"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + "(supervisor_yn = 'Y' OR is_group_leader = 'Y' OR is_division_chief = 'Y')"
                    + " AND "
                    + "(INSTR(LOWER(first_name), ?) > 0 OR INSTR(LOWER(last_name), ?) > 0)"
                    + " UNION "
                    + "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "div_org_id, "
                    + "supervisor_people_id, "
                    + "staff_type"
                    + " FROM "
                    + "reviewer"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + "(supervisor_yn = 'Y' OR is_group_leader = 'Y' OR is_division_chief = 'Y')"
                    + " AND "
                    + "(INSTR(LOWER(first_name), ?) > 0 OR INSTR(LOWER(last_name), ?) > 0)";
        } else {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "div_org_id, "
                    + "supervisor_people_id, "
                    + "staff_type"
                    + " FROM "
                    + "nist_user_active"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + "(supervisor_yn = 'Y' OR is_group_leader = 'Y' OR is_division_chief = 'Y')"
                    + " UNION "
                    + "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "div_org_id, "
                    + "supervisor_people_id, "
                    + "staff_type"
                    + " FROM "
                    + "reviewer"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + "(supervisor_yn = 'Y' OR is_group_leader = 'Y' OR is_division_chief = 'Y')";
        }
        LOG.info(String.format("sql: %s, divisionId = %d", sql, divisionId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, divisionId);

            if (useFilter) {
                pstmt.setString(2, filter.toLowerCase().trim());
                pstmt.setString(3, filter.toLowerCase().trim());
                pstmt.setInt(4, divisionId);
                pstmt.setString(5, filter.toLowerCase().trim());
                pstmt.setString(6, filter.toLowerCase().trim());
            } else {
                pstmt.setInt(2, divisionId);
            }
            try ( ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    User user = new User();

                    // People Id.
                    int peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    user.setOuId(rset.getInt("ou_org_id"));

                    // Division id.
                    user.setDivisionId(rset.getInt("div_org_id"));

                    // Boss id
                    user.setBossId(rset.getInt("supervisor_people_id"));

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }
                    //reviewer dropdown should always show active users; since used nist_user_active table, all user from that table are active
                    user.setActive(true);

                    users.add(user);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(USER_LIST_KEY, users);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectRequestForInOu(Integer ouId, String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (ouId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        List<User> users = new ArrayList<>();
        boolean useFilter = filter != null && !filter.isEmpty();
        String sql;
        if (useFilter) {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type,"
                    + "active_yn "
                    + " FROM "
                    + "nist_user"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + "(supervisor_yn = 'Y' OR is_group_leader = 'Y')"
                    + " AND "
                    + " active_yn = 'Y' and "
                    + "(INSTR(LOWER(first_name), ?) > 0 OR INSTR(LOWER(last_name), ?) > 0)";
        } else {
            sql = "SELECT "
                    + "people_id, "
                    + "first_name, "
                    + "last_name, "
                    + "mid_name, "
                    + "ou_org_id, "
                    + "staff_type,"
                    + "active_yn "
                    + " FROM "
                    + "nist_user"
                    + " WHERE "
                    + "ou_org_id = ?"
                    + " AND "
                    + " active_yn = 'Y' and "
                    + "(supervisor_yn = 'Y' OR is_group_leader = 'Y')";
        }
        LOG.info(String.format("sql: %s, ouId = %d", sql, ouId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, ouId);
            if (useFilter) {
                pstmt.setString(2, filter.toLowerCase().trim());
                pstmt.setString(3, filter.toLowerCase().trim());
            }
            try ( ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    User user = new User();

                    // People Id.
                    int peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    user.setOuId(rset.getInt("ou_org_id"));

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    String active = rset.getString("active_yn");
                    user.setActive(active.equalsIgnoreCase("Y"));

                    users.add(user);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(USER_LIST_KEY, users);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public StatusCode inserPrivileges(UserPrivileges userPrivileges) {
        StatusCode statusCode = StatusCode.OK;
        String sql = "{ call sp_update_user_privileges(?, ?, ?, ?, ?, ?) }";
        LOG.info(String.format("sql: %s, username = %s", sql, userPrivileges.getUsername()));
        try ( Connection connection = getConnection(false);  CallableStatement cstmt = connection.prepareCall(sql);) {
            cstmt.setString("p_username", userPrivileges.getUsername());
            cstmt.setString("p_change_ptc", userPrivileges.getChangePtc() != null && userPrivileges.getChangePtc() ? "Y" : "N");
            cstmt.setString("p_reroute", userPrivileges.getReroute() != null && userPrivileges.getReroute() ? "Y" : "N");
            cstmt.setString("p_access_group", userPrivileges.getAccessGroup() != null && userPrivileges.getAccessGroup() ? "Y" : "N");
            cstmt.setString("p_access_div", userPrivileges.getAccessDiv() != null && userPrivileges.getAccessDiv() ? "Y" : "N");
            cstmt.setString("p_access_ou", userPrivileges.getAccessOu() != null && userPrivileges.getAccessOu() ? "Y" : "N");
            cstmt.executeUpdate();
            connection.commit();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        return statusCode;
    }

    public boolean isAdminUser(String userName) {
        String sql = "select * from ADMIN_USERS where USER_NAME = ?";
        LOG.info(String.format("sql: %s, username = %s", sql, userName));
        try ( Connection con = getConnection(true);  PreparedStatement pstmt = con.prepareStatement(sql);) {

            pstmt.setString(1, userName);
            ResultSet rset = pstmt.executeQuery();

            if (rset.next()) {
                return true;
            }
        } catch (SQLException caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        return false;
    }

    public Map<String, Object> selectUserDetailedById(Integer peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        List<UserDetailedPrivilege> details = new ArrayList<>();
        String sql = "select * from user_detailed where people_id=? and (valid_until_date is null or trunc(valid_until_date) >= trunc(sysdate))";

        try ( Connection con = getConnection(true);  PreparedStatement pstmt = con.prepareStatement(sql);) {

            pstmt.setInt(1, peopleId);
            ResultSet rset = pstmt.executeQuery();
            while (rset.next()) {
                UserDetailedPrivilege detail = new UserDetailedPrivilege();
                detail.setPeopleId(peopleId);

                int ouId = rset.getInt("ou_org_id");
                if (!rset.wasNull()) {
                    detail.setOuId(ouId);
                }

                int divId = rset.getInt("div_org_id");
                if (!rset.wasNull()) {
                    detail.setDivisionId(divId);
                }

                int grpId = rset.getInt("grp_org_id");
                if (!rset.wasNull()) {
                    detail.setGroupId(grpId);
                }

                String accessOu = rset.getString("access_ou");
                if (!rset.wasNull()) {
                    detail.setAccessOu("Y".equals(accessOu));
                }

                String accessDiv = rset.getString("access_div");
                if (!rset.wasNull()) {
                    detail.setAccessDiv("Y".equals(accessDiv));
                }

                String accessGrp = rset.getString("access_group");
                if (!rset.wasNull()) {
                    detail.setAccessGroup("Y".equals(accessGrp));
                }

                details.add(detail);
            }

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(USER_LIST_KEY, details);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectSampleUserByGroup(Integer groupId, boolean anyFed) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        User user = new User();
        String sql = "SELECT "
                + "people_id, "
                + "first_name, "
                + "last_name, "
                + "mid_name, "
                + "ou_org_id, "
                + "div_org_id, "
                + "grp_org_id, "
                + "username, "
                + "email, "
                + "phone, "
                + "supervisor_yn, "
                + "last_update_dt, "
                + "staff_type, "
                + "supervisor_people_id, "
                + "(SELECT org_cd FROM nist_division WHERE org_id = div_org_id) AS div_org_cd, "
                + "(SELECT org_cd FROM nist_ou WHERE org_id = ou_org_id) AS ou_org_cd, "
                + "(SELECT org_cd FROM nist_group WHERE org_id = grp_org_id) AS group_org_cd"
                + " FROM "
                + "nist_user_active"
                + " WHERE 1=1 "
                + ((anyFed) ? "" : " and supervisor_yn = 'N' and is_division_chief ='N' and is_group_leader='N' ")
                + " and staff_type ='NIST Employee' "
                + " and grp_org_id = ? fetch first 1 rows only";
        LOG.info(String.format("sql: %s, groupId = %d", sql, groupId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, groupId);
            try ( ResultSet rset = pstmt.executeQuery()) {
                if (rset.next()) {

                    // People Id.
                    int peopleId = rset.getInt("people_id");
                    if (!rset.wasNull()) {
                        user.setPeopleId(peopleId);
                    }

                    int bossId = rset.getInt("supervisor_people_id");
                    if (!rset.wasNull()) {
                        user.setBossId(bossId);
                    }

                    // First name.
                    user.setFirstName(rset.getString("first_name"));

                    // Last name.
                    user.setLastName(rset.getString("last_name"));

                    // Midddle name.
                    user.setMiddleName(rset.getString("mid_name"));

                    // OU id.
                    int ouId = rset.getInt("ou_org_id");
                    if (!rset.wasNull()) {
                        user.setOuId(ouId);
                    }

                    // Division id.
                    int divisionId = rset.getInt("div_org_id");
                    if (!rset.wasNull()) {
                        user.setDivisionId(divisionId);
                    }

                    // Group id.
                    int grpId = rset.getInt("grp_org_id");
                    if (!rset.wasNull()) {
                        user.setGroupId(grpId);
                    }

                    // Username.
                    user.setUsername(rset.getString("username"));

                    // Email.
                    user.setEmail(rset.getString("email"));

                    // Phone number.
                    user.setPhoneNumber(rset.getString("phone"));

                    // Supervisor.
                    String supervisor = rset.getString("supervisor_yn");
                    if (supervisor != null && !supervisor.isEmpty()) {
                        user.setSupervisor(supervisor.equalsIgnoreCase("Y"));
                    }

                    // Last update date.
                    Timestamp lastUpdateDate = rset.getTimestamp("last_update_dt");
                    if (!rset.wasNull()) {
                        user.setLastUpdateDate(lastUpdateDate);
                    }

                    // Staff type.
                    String staffType = rset.getString("staff_type");
                    if (staffType != null && !staffType.isEmpty()) {
                        user.setStaffType(staffType);
                    }

                    // Division cd.
                    user.setDivisionCode(rset.getString("div_org_cd"));

                    // Ou cd.
                    user.setOuCode(rset.getString("ou_org_cd"));

                    // Group cd.
                    user.setGroupCode(rset.getString("group_org_cd"));

                    results.put(USER_KEY, user);
                } else {
                    statusCode = StatusCode.UserNotFound;
                }
            }
        } catch (Exception caught) {
            results.put(STATUS_CODE_KEY, StatusCode.DatabaseError);
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
}
