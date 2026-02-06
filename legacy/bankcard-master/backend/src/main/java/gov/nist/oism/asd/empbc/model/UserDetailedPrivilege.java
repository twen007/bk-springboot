/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;

/**
 *
 * @author xinweiw
 */
public class UserDetailedPrivilege implements Serializable {
    private Integer peopleId;
    private Integer ouId;
    private Integer divisionId;
    private Integer groupId;
    private Boolean accessGroup;
    private Boolean accessDiv;
    private Boolean accessOu;

    public Integer getPeopleId() {
        return peopleId;
    }

    public void setPeopleId(Integer peopleId) {
        this.peopleId = peopleId;
    }

    public Integer getOuId() {
        return ouId;
    }

    public void setOuId(Integer ouId) {
        this.ouId = ouId;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Boolean getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(Boolean accessGroup) {
        this.accessGroup = accessGroup;
    }

    public Boolean getAccessDiv() {
        return accessDiv;
    }

    public void setAccessDiv(Boolean accessDiv) {
        this.accessDiv = accessDiv;
    }

    public Boolean getAccessOu() {
        return accessOu;
    }

    public void setAccessOu(Boolean accessOu) {
        this.accessOu = accessOu;
    }

}
