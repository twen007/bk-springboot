package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class ProjectTask implements Serializable {
    
    private String mProjectCode;
    private Integer mFundCode;
    private String mProjectOrg1Code;
    private String mProjectOrg2Code;
    private String mProjectOrg3Code;
    private Integer mProjectOrg4Code;
    private Integer mProjectOrg5Code;
    private Integer mProjectOrg6Code;
    private Integer mProjectOrg7Code;
    private String mProjectDescription;
    private String mProjectType;
    private String mTaskCode;
    private String mTaskDescription;
    private Date mBeginDate;

    public String getProjectCode() {
        return mProjectCode;
    }

    public void setProjectCode(String projectCode) {
        mProjectCode = projectCode;
    }

    public Integer getFundCode() {
        return mFundCode;
    }

    public void setFundCode(Integer fundCode) {
        mFundCode = fundCode;
    }

    public String getProjectOrg1Code() {
        return mProjectOrg1Code;
    }

    public void setProjectOrg1Code(String projectOrg1Code) {
        mProjectOrg1Code = projectOrg1Code;
    }

    public String getProjectOrg2Code() {
        return mProjectOrg2Code;
    }

    public void setProjectOrg2Code(String projectOrg2Code) {
        mProjectOrg2Code = projectOrg2Code;
    }

    public String getProjectOrg3Code() {
        return mProjectOrg3Code;
    }

    public void setProjectOrg3Code(String projectOrg3Code) {
        mProjectOrg3Code = projectOrg3Code;
    }

    public Integer getProjectOrg4Code() {
        return mProjectOrg4Code;
    }

    public void setProjectOrg4Code(Integer projectOrg4Code) {
        mProjectOrg4Code = projectOrg4Code;
    }

    public Integer getProjectOrg5Code() {
        return mProjectOrg5Code;
    }

    public void setProjectOrg5Code(Integer projectOrg5Code) {
        mProjectOrg5Code = projectOrg5Code;
    }

    public Integer getProjectOrg6Code() {
        return mProjectOrg6Code;
    }

    public void setProjectOrg6Code(Integer projectOrg6Code) {
        mProjectOrg6Code = projectOrg6Code;
    }

    public Integer getProjectOrg7Code() {
        return mProjectOrg7Code;
    }

    public void setProjectOrg7Code(Integer projectOrg7Code) {
        mProjectOrg7Code = projectOrg7Code;
    }

    public String getProjectDescription() {
        return mProjectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        mProjectDescription = projectDescription;
    }

    public String getProjectType() {
        return mProjectType;
    }

    public void setProjectType(String projectType) {
        mProjectType = projectType;
    }

    public String getTaskCode() {
        return mTaskCode;
    }

    public void setTaskCode(String taskCode) {
        mTaskCode = taskCode;
    }

    public String getTaskDescription() {
        return mTaskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        mTaskDescription = taskDescription;
    }
     @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public Date getBeginDate() {
        return mBeginDate;
    }

    public void setBeginDate(Date beginDate) {
        mBeginDate = beginDate;
    }
}
