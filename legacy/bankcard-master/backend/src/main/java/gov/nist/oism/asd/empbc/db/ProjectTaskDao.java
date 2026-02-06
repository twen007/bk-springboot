package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.ProjectTask;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.v1.ProjectTaskService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectTaskDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(ProjectTaskDao.class.getSimpleName());

    public static final String PROJECT_TASK_LIST_KEY = "project_task_list_key";

    public Map<String, Object> selectProjectTasksForOuWithFilter(String ouCode, String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (ouCode == null || ouCode.isEmpty()) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        boolean useFilter = filter != null && !filter.isEmpty();
        String sql;
        if (useFilter) {
            sql = "SELECT "
                    + "project_code, "
                    + "fund_code, "
                    + "p_org1_code, "
                    + "p_org2_code, "
                    + "p_org3_code, "
                    + "p_org4_code, "
                    + "p_org5_code, "
                    + "p_org6_code, "
                    + "p_org7_code, "
                    + "project_descr, "
                    + "project_type, "
                    + "task_code, "
                    + "task_descr, "
                    + "begin_date"
                    + " FROM "
                    + "project_task"
                    + " WHERE "
                    + "p_org2_code = ?"
                    + " AND "
                    + "project_code LIKE ?";
        } else {
            sql = "SELECT "
                    + "project_code, "
                    + "fund_code, "
                    + "p_org1_code, "
                    + "p_org2_code, "
                    + "p_org3_code, "
                    + "p_org4_code, "
                    + "p_org5_code, "
                    + "p_org6_code, "
                    + "p_org7_code, "
                    + "project_descr, "
                    + "project_type, "
                    + "task_code, "
                    + "task_descr, "
                    + "begin_date"
                    + " FROM "
                    + "project_task"
                    + " WHERE "
                    + "p_org2_code = ?";
        }

        LOG.info(String.format("sql: %s, ou_code: %s, filter: %s", sql, ouCode, filter));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<ProjectTask> projectTasks = new ArrayList<>();
            //allow PTC search across all OUs
            setString(pstmt, 1, ouCode);
            if (useFilter) {
                setString(pstmt, 2, filter + "%");
                //pstmt.setString(1, filter + "%");
            }
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    ProjectTask projectTask = new ProjectTask();
                    projectTask.setProjectCode(getString(rset, "project_code"));
                    projectTask.setFundCode(getInt(rset, "fund_code"));
                    projectTask.setProjectOrg1Code(getString(rset, "p_org1_code"));
                    projectTask.setProjectOrg2Code(getString(rset, "p_org2_code"));
                    projectTask.setProjectOrg3Code(getString(rset, "p_org3_code"));
                    projectTask.setProjectOrg4Code(getInt(rset, "p_org4_code"));
                    projectTask.setProjectOrg5Code(getInt(rset, "p_org5_code"));
                    projectTask.setProjectOrg6Code(getInt(rset, "p_org6_code"));
                    projectTask.setProjectOrg7Code(getInt(rset, "p_org7_code"));
                    projectTask.setProjectDescription(getString(rset, "project_descr"));
                    projectTask.setProjectType(getString(rset, "project_type"));
                    projectTask.setTaskCode(getString(rset, "task_code"));
                    projectTask.setTaskDescription(getString(rset, "task_descr"));
                    projectTask.setBeginDate(getTimestamp(rset, "begin_date"));
                    projectTasks.add(projectTask);
                }
            }

            results.put(PROJECT_TASK_LIST_KEY, projectTasks);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectProjectTasksSearch(String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        boolean useFilter = filter != null && !filter.isEmpty();

        String sql;

        if (useFilter) {
            System.out.println("use filter 1, filter = " + filter);
            sql = "select distinct project_task from item where project_task like ? order by project_task";
        } else {
            sql = "select distinct project_task from item where project_task is not null and Length(project_task)=11 order by project_task";
        }

        LOG.info(String.format("sql: %s, filter: %s", sql, filter));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<ProjectTaskService.Ptc> projectTasks = new ArrayList<>();

            if (useFilter) {
                setString(pstmt, 1, filter + "%");
            }
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    ProjectTaskService.Ptc projectTask = new ProjectTaskService.Ptc();

                    // Project code.
                    projectTask.code = getString(rset, "project_task");
                    //display
                    //projectTask.display=rset.getString("project_task");

                    projectTasks.add(projectTask);
                }
            }

            results.put(PROJECT_TASK_LIST_KEY, projectTasks);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    /**
     * optimized code to only get what needed
     *
     * @param ouCode
     * @return
     */
    public Map<String, Object> selectProjectTasksForOu(String ouCode) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (ouCode == null || ouCode.isEmpty()) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        String sql = "SELECT"
                + "    project_code || '-' || task_code as code,"
                + "    project_code"
                + "    || '-'"
                + "    || task_code"
                + "    || ' '"
                + "    ||"
                + "    CASE"
                + "        WHEN ( task_descr IS NULL"
                + "               OR task_descr = 'NO TASK' ) THEN"
                + "                project_descr"
                + "        ELSE"
                + "            task_descr"
                + "    END as display"
                + "   FROM"
                + "   project_task"
                + " WHERE "
                + " p_org2_code = ? "
                //BANK-504
                + " order by display ";

        LOG.info(String.format("sql: %s, ou_code: %s", sql, ouCode));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<ProjectTaskService.Ptc> projectTasks = new ArrayList<>();
            //allow PTC search across all OUs
            setString(pstmt, 1, ouCode);
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    ProjectTaskService.Ptc projectTask = new ProjectTaskService.Ptc();

                    // Project code.
                    projectTask.code = getString(rset, "code");
                    //display
                    projectTask.display = getString(rset, "display");
                    projectTasks.add(projectTask);
                }
            }

            results.put(PROJECT_TASK_LIST_KEY, projectTasks);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;

    }
}
