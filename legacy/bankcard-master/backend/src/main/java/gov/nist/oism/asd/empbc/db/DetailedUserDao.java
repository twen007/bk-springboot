package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.DetailedUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD operations for Detailee privilege management
 * @author xinweiw
 */
public class DetailedUserDao extends OracleDao {

    // Create
    public void insertUserDetailed(DetailedUser user) throws SQLException {
        String sql = "INSERT INTO USER_DETAILED (PEOPLE_ID, OU_ORG_ID, DIV_ORG_ID, GRP_ORG_ID, ACCESS_GROUP, ACCESS_DIV, ACCESS_OU, VALID_UNTIL_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, user.getPeopleId());
            pstmt.setInt(2, user.getOuOrgId());
            pstmt.setInt(3, user.getDivOrgId());
            pstmt.setInt(4, user.getGrpOrgId());
            pstmt.setString(5, user.getAccessGroup());
            pstmt.setString(6, user.getAccessDiv());
            pstmt.setString(7, user.getAccessOu());
            pstmt.setDate(8, new java.sql.Date(user.getValidUntilDate().getTime()));
            pstmt.executeUpdate();
        }
    }

    // Read
    public DetailedUser getUserDetailed(Integer id) throws SQLException {
        String sql = "SELECT * FROM USER_DETAILED WHERE ID = ?";
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                DetailedUser user = new DetailedUser();
                user.setId(rs.getInt("ID"));
                user.setPeopleId(rs.getInt("PEOPLE_ID"));
                user.setOuOrgId(rs.getInt("OU_ORG_ID"));
                user.setDivOrgId(rs.getInt("DIV_ORG_ID"));
                user.setGrpOrgId(rs.getInt("GRP_ORG_ID"));
                user.setAccessGroup(rs.getString("ACCESS_GROUP"));
                user.setAccessDiv(rs.getString("ACCESS_DIV"));
                user.setAccessOu(rs.getString("ACCESS_OU"));
                user.setValidUntilDate(rs.getDate("VALID_UNTIL_DATE"));
                return user;
            }
        }
        return null;
    }

    // Update
    public void updateUserDetailed(DetailedUser user) throws SQLException {
        String sql = "UPDATE USER_DETAILED SET PEOPLE_ID = ?, OU_ORG_ID = ?, DIV_ORG_ID = ?, GRP_ORG_ID = ?, ACCESS_GROUP = ?, ACCESS_DIV = ?, ACCESS_OU = ?, VALID_UNTIL_DATE = ? WHERE ID = ?";
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, user.getPeopleId());
            pstmt.setLong(2, user.getOuOrgId());
            pstmt.setLong(3, user.getDivOrgId());
            pstmt.setLong(4, user.getGrpOrgId());
            pstmt.setString(5, user.getAccessGroup());
            pstmt.setString(6, user.getAccessDiv());
            pstmt.setString(7, user.getAccessOu());
            pstmt.setDate(8, new java.sql.Date(user.getValidUntilDate().getTime()));
            pstmt.setLong(9, user.getId());
            pstmt.executeUpdate();
        }
    }

    // Delete
    public void deleteUserDetailed(Integer id) throws SQLException {
        String sql = "DELETE FROM USER_DETAILED WHERE ID = ?";
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    // List all Users
    public List<DetailedUser> getAllUsers() throws SQLException {
        List<DetailedUser> userList = new ArrayList<>();
        String sql = "SELECT * FROM USER_DETAILED";
        try ( Connection connection = getConnection(true);  Statement stmt = connection.createStatement();  ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DetailedUser user = new DetailedUser();
                user.setId(rs.getInt("ID"));
                user.setPeopleId(rs.getInt("PEOPLE_ID"));
                user.setOuOrgId(rs.getInt("OU_ORG_ID"));
                user.setDivOrgId(rs.getInt("DIV_ORG_ID"));
                user.setGrpOrgId(rs.getInt("GRP_ORG_ID"));
                user.setAccessGroup(rs.getString("ACCESS_GROUP"));
                user.setAccessDiv(rs.getString("ACCESS_DIV"));
                user.setAccessOu(rs.getString("ACCESS_OU"));
                user.setValidUntilDate(rs.getDate("VALID_UNTIL_DATE"));
                userList.add(user);
            }
        }
        return userList;
    }

}
