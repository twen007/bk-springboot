package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.BchInitial;
import gov.nist.oism.asd.empbc.model.DivisionPreference;
import gov.nist.oism.asd.empbc.model.NistOrg;
import gov.nist.oism.asd.empbc.util.ResultSetMapper;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.v1.OrgDataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrgDataDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(OrgDataDao.class.getSimpleName());

    public static final String NIST_ORG_LIST_KEY = "nist_org_list_key";

    public Map<String, Object> selectOrganizations() {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        String sql = "SELECT "
                + "org_id, "
                + "org_cd, "
                + "org_name, "
                + "org_acrnm"
                + " FROM "
                + "nist_ou"
                + " WHERE "
                + "active_yn = 'Y'";
        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<NistOrg> nistOrgs = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    NistOrg nistOrg = new NistOrg();

                    // Id.
                    int id = rset.getInt("org_id");
                    if (!rset.wasNull()) {
                        nistOrg.setOuId(id);
                    }

                    // Code.
                    String code = rset.getString("org_cd");
                    nistOrg.setCode(code);

                    // Name.
                    nistOrg.setName(rset.getString("org_name"));

                    // Acronym.
                    String acronym = rset.getString("org_acrnm");
                    nistOrg.setAcronym(acronym);

                    // Short name.
                    if (code != null && !code.isEmpty() && acronym != null && !acronym.isEmpty()) {
                        nistOrg.setShortName(code + "-" + acronym);
                    }

                    nistOrgs.add(nistOrg);
                }
            }

            results.put(NIST_ORG_LIST_KEY, nistOrgs);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectDivisions() {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        String sql = "SELECT "
                + "org_id, "
                + "org_cd, "
                + "org_name, "
                + "org_acrnm, "
                + "ou_org_id"
                + " FROM "
                + "nist_division"
                + " WHERE "
                + "active_yn = 'Y'";
        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<NistOrg> nistOrgs = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    NistOrg nistOrg = new NistOrg();

                    // Id.
                    int id = rset.getInt("org_id");
                    if (!rset.wasNull()) {
                        nistOrg.setDivisionId(id);
                    }

                    // Code.
                    String code = rset.getString("org_cd");
                    nistOrg.setCode(code);

                    // Name.
                    nistOrg.setName(rset.getString("org_name"));

                    // Acronym.
                    String acronym = rset.getString("org_acrnm");
                    nistOrg.setAcronym(acronym);

                    // Short name.
                    if (code != null && !code.isEmpty() && acronym != null && !acronym.isEmpty()) {
                        nistOrg.setShortName(code + "-" + acronym);
                    }

                    // Ou id.
                    int ouId = rset.getInt("ou_org_id");
                    if (!rset.wasNull()) {
                        nistOrg.setOuId(ouId);
                    }

                    nistOrgs.add(nistOrg);
                }
            }

            results.put(NIST_ORG_LIST_KEY, nistOrgs);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectGroups() {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        String sql = "SELECT "
                + "org_id, "
                + "org_cd, "
                + "org_name, "
                + "org_acrnm, "
                + "ou_org_id, "
                + "div_org_id"
                + " FROM "
                + "nist_group"
                + " WHERE "
                + "active_yn = 'Y'";
        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<NistOrg> nistOrgs = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    NistOrg nistOrg = new NistOrg();

                    // Id.
                    int id = rset.getInt("org_id");
                    if (!rset.wasNull()) {
                        nistOrg.setGroupId(id);
                    }

                    // Code.
                    String code = rset.getString("org_cd");
                    nistOrg.setCode(code);

                    // Name.
                    nistOrg.setName(rset.getString("org_name"));

                    // Acronym.
                    String acronym = rset.getString("org_acrnm");
                    nistOrg.setAcronym(acronym);

                    // Short name.
                    if (code != null && code.length() == 5) {
                        nistOrg.setShortName(code.substring(0, 3) + "." + code.substring(3, 5));
                    }

                    // Ou id.
                    int ouId = rset.getInt("ou_org_id");
                    if (!rset.wasNull()) {
                        nistOrg.setOuId(ouId);
                    }

                    // Division id.
                    int divisionId = rset.getInt("div_org_id");
                    if (!rset.wasNull()) {
                        nistOrg.setDivisionId(divisionId);
                    }

                    nistOrgs.add(nistOrg);
                }
            }

            results.put(NIST_ORG_LIST_KEY, nistOrgs);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> getDivPrefs(Integer divId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "select * from div_preferences where div_id = ?";

        if (divId <= 0) {
            //get all
            sql = "select * from div_preferences where div_id <> ?";
        }
        LOG.info(String.format("sql: %s, divId: %d", sql, divId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, divId);
            ResultSetMapper<DivisionPreference> resultSetMapper = new ResultSetMapper<>();
            List<DivisionPreference> divPrefList = null;
            ResultSet rs = pstmt.executeQuery();
            divPrefList = resultSetMapper.mapResultSetToObject(rs, DivisionPreference.class);

            results.put(NIST_ORG_LIST_KEY, divPrefList);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> setDivPrefs(DivisionPreference divPref) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "select count (*) cnt from div_preferences where div_id = " + divPref.getDivId();
        String sql1 = "insert into div_preferences (JUST_PREF_VAL,FINANCE_PREF_VAL,SHIPPING_COST_PREF_VAL,SHIPPING_COST_PREF_VAL_DETAIL,UP_TO_PREF_VAL,UP_TO_PREF_VAL_DETAIL,ADD_FCO_ROUTE_PREF_VAL,DIV_ID) values (?,?,?,?,?,?,?,?)";
        String sql2 = "update div_preferences set JUST_PREF_VAL = ?,FINANCE_PREF_VAL=?,SHIPPING_COST_PREF_VAL=?,SHIPPING_COST_PREF_VAL_DETAIL=?,UP_TO_PREF_VAL=?,UP_TO_PREF_VAL_DETAIL=?,ADD_FCO_ROUTE_PREF_VAL=? where div_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        int cnt = 0;

        try {
            conn = getConnection(true);
            pstmt = conn.prepareStatement(sql);
            rset = pstmt.executeQuery();

            if (rset.next()) {
                cnt = rset.getInt("cnt");
            }

            if (cnt < 1) {
                sql = sql1;
            } else {
                sql = sql2;
            }
            LOG.info(String.format("sql: %s, divId: %d", sql, divPref.getDivId()));

            pstmt = conn.prepareStatement(sql);
            setString(pstmt, 1, divPref.getJustPrefVal());
            setString(pstmt, 2, divPref.getFinancePrefVal());
            setString(pstmt, 3, divPref.getShippingCostPrefVal());
            setDouble(pstmt, 4, divPref.getShippingCostPrefValDetail());
            setString(pstmt, 5, divPref.getUpToPrefVal());
            setDouble(pstmt, 6, divPref.getUpToPrefValDetail());
            setString(pstmt, 7, divPref.getAddFcoRoutePrefVal());
            setInt(pstmt, 8, divPref.getDivId());
            pstmt.executeUpdate();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> setBchInitPrefs(OrgDataService.PostBchInitPref postBchInitPref) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "select count (*) cnt from bch_init_preferences where id = " + postBchInitPref.getId();
        String sql1 = "insert into bch_init_preferences (INITIALS,PEOPLE_ID,DIV_ID) values (?,?,?)";
        String sql2 = "update bch_init_preferences set INITIALS = ?, PEOPLE_ID=?, DIV_ID=? where id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        int cnt = 0;

        try {
            conn = getConnection(true);
            pstmt = conn.prepareStatement(sql);
            rset = pstmt.executeQuery();

            if (rset.next()) {
                cnt = rset.getInt("cnt");
            }

            if (cnt < 1) {
                sql = sql1;
            } else {
                sql = sql2;
            }
            LOG.info(String.format("sql: %s, divId: %d", sql, postBchInitPref.getDivId()));

            pstmt = conn.prepareStatement(sql);
            setString(pstmt, 1, postBchInitPref.getInitials());
            setInt(pstmt, 2, postBchInitPref.getPeopleId());
            setInt(pstmt, 3, postBchInitPref.getDivId());
            if (cnt == 1) {
                setInt(pstmt, 4, postBchInitPref.getId());
            }
            pstmt.executeUpdate();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> getBchInitPrefs(Integer divId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "select * from bch_init_preferences where div_id = ?  order by initials";
        LOG.info(String.format("sql: %s, divId: %d", sql, divId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, divId);
            ResultSetMapper<BchInitial> resultSetMapper = new ResultSetMapper<>();
            List<BchInitial> bchInitialList = null;
            ResultSet rs = pstmt.executeQuery();
            bchInitialList = resultSetMapper.mapResultSetToObject(rs, BchInitial.class);

            results.put(NIST_ORG_LIST_KEY, bchInitialList);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public String getBchInitbyId(Integer divId, Integer peopleId) {
        String sql = "select initials from bch_init_preferences where div_id = ? and people_id = ?";
        String initials = null;
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, divId);
            setInt(pstmt, 2, peopleId);

            try ( ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    // Requester id.
                    initials = rset.getString("initials");
                }
            }

        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        return initials;
    }

    public Map<String, Object> deleteBchInitPref(Integer id) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;
        if (id == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "Delete from bch_init_preferences WHERE id = ?";

        LOG.info(String.format("sql: %s, id:%d", sql, id));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, id);
            rowCount = pstmt.executeUpdate();

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(ROW_COUNT_KEY, rowCount);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
}
