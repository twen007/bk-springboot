package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;

public class ObjectClass implements Serializable {
    
    private String mDescription;
    private String mCode;

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }
}
