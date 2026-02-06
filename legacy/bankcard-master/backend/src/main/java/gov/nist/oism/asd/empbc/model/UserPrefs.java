package gov.nist.oism.asd.empbc.model;

/* File: UserPrefs.java
 * Author: PPG
 * Create Date: October 2020
 * Purpose: Allow user to set the preferences.
 */

import java.io.Serializable;

public class UserPrefs implements Serializable {
    
    private Integer peopleId;
    private Integer prefTypeId;
    private String prefValue;

    public Integer getPeopleId () {
        return peopleId;
    }

    public void setPeopleId (Integer i) {
        peopleId = i;
    }

    public Integer getPrefTypeId () {
        return prefTypeId;
    }

    public void setPrefTypeId (Integer i) {
        prefTypeId = i;
    }

    public String getPrefValue () {
        return prefValue;
    }

    public void setPrefValue (String s) {
        prefValue = s;
    }
}
