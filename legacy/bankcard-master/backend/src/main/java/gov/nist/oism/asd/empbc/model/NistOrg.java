package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;

public class NistOrg implements Serializable {
    
    private Integer mOuId;
    private Integer mDivisionId;
    private Integer mGroupId;
    private String mName;
    private String mCode;
    private String mAcronym;
    private String mShortName;

    public Integer getOuId() {
        return mOuId;
    }

    public void setOuId(Integer ouId) {
        mOuId = ouId;
    }

    public Integer getDivisionId() {
        return mDivisionId;
    }

    public void setDivisionId(Integer divisionId) {
        mDivisionId = divisionId;
    }

    public Integer getGroupId() {
        return mGroupId;
    }

    public void setGroupId(Integer groupId) {
        mGroupId = groupId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getAcronym() {
        return mAcronym;
    }

    public void setAcronym(String acronym) {
        mAcronym = acronym;
    }

    public String getShortName() {
        return mShortName;
    }

    public void setShortName(String shortName) {
        mShortName = shortName;
    }
}
