package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;

public class UserPrivileges implements Serializable {
    
    private String mUsername;
    private Boolean mChangePtc;
    private Boolean mReroute;
    private Boolean mAccessGroup;
    private Boolean mAccessDiv;
    private Boolean mAccessOu;

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public Boolean getChangePtc() {
        return mChangePtc;
    }

    public void setChangePtc(Boolean changePtc) {
        mChangePtc = changePtc;
    }

    public Boolean getReroute() {
        return mReroute;
    }

    public void setReroute(Boolean reroute) {
        mReroute = reroute;
    }

    public Boolean getAccessGroup() {
        return mAccessGroup;
    }

    public void setAccessGroup(Boolean accessGroup) {
        mAccessGroup = accessGroup;
    }

    public Boolean getAccessDiv() {
        return mAccessDiv;
    }

    public void setAccessDiv(Boolean accessDiv) {
        mAccessDiv = accessDiv;
    }

    public Boolean getAccessOu() {
        return mAccessOu;
    }

    public void setAccessOu(Boolean accessOu) {
        mAccessOu = accessOu;
    }
}
