package gov.nist.oism.asd.empbc.model;

/* File: DivisionPreference.java
 * Author: xinweiw
 * Purpose: Allow Division to set the preferences.
 */
import java.io.Serializable;

public class DivisionPreference implements Serializable {

    private Integer divId;
    private String justPrefVal;
    private String financePrefVal;
    private String shippingCostPrefVal;
    Double shippingCostPrefValDetail;
    private String upToPrefVal;
    Double upToPrefValDetail;
    private String addFcoRoutePrefVal;

    public String getAddFcoRoutePrefVal() {
        return addFcoRoutePrefVal;
    }

    public void setAddFcoRoutePrefVal(String addFcoRoutePrefVal) {
        this.addFcoRoutePrefVal = addFcoRoutePrefVal;
    }


    public Integer getDivId() {
        return divId;
    }

    public void setDivId(Integer divId) {
        this.divId = divId;
    }

    public String getJustPrefVal() {
        return justPrefVal;
    }

    public void setJustPrefVal(String justPrefVal) {
        this.justPrefVal = justPrefVal;
    }

    public String getFinancePrefVal() {
        return financePrefVal;
    }

    public void setFinancePrefVal(String financePrefVal) {
        this.financePrefVal = financePrefVal;
    }

    public String getShippingCostPrefVal() {
        return shippingCostPrefVal;
    }

    public void setShippingCostPrefVal(String shippingCostPrefVal) {
        this.shippingCostPrefVal = shippingCostPrefVal;
    }

    public Double getShippingCostPrefValDetail() {
        return shippingCostPrefValDetail;
    }

    public void setShippingCostPrefValDetail(Double shippingCostPrefValDetail) {
        this.shippingCostPrefValDetail = shippingCostPrefValDetail;
    }

    public String getUpToPrefVal() {
        return upToPrefVal;
    }

    public void setUpToPrefVal(String upToPrefVal) {
        this.upToPrefVal = upToPrefVal;
    }

    public Double getUpToPrefValDetail() {
        return upToPrefValDetail;
    }

    public void setUpToPrefValDetail(Double upToPrefValDetail) {
        this.upToPrefValDetail = upToPrefValDetail;
    }

}
