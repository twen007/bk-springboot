/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.oism.asd.empbc.model;

import java.util.Date;

/**
 *
 * @author xinweiw
 */
public class RequestJustification {
    private Integer requestId;
    private Boolean convenienceCheck;
    private String convenienceCheckJust;
    private Boolean gsaSchedule;
    private String gsaScheduleJust;
    private Boolean thirdPartyVendor;
    private String thirdPartyVendorJust;
    private Boolean commercialVendor;
    private String  commercialVendorJust;
    private String priceIsReasonableJust;
    private Boolean smallBusiness;
    private String smallBusinessJust;
    private Integer divisionOrgId;
    private Integer createdBy;
    private Date createdDate;
    private Integer updatedBy;
    private Date updatedDate;
    private Boolean professionalOrg;
    private Integer builtInVendor;
    private String isItPurchase;

    //IT Purchase (BANK-510, ITSO approval)
    public String getIsItPurchase() {
        return isItPurchase;
    }

    public void setIsItPurchase(String isItPurchase) {
        this.isItPurchase = isItPurchase;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Boolean getConvenienceCheck() {
        return convenienceCheck;
    }

    public void setConvenienceCheck(Boolean convenienceCheck) {
        this.convenienceCheck = convenienceCheck;
    }

    public String getConvenienceCheckJust() {
        return convenienceCheckJust;
    }

    public void setConvenienceCheckJust(String convenienceCheckJust) {
        this.convenienceCheckJust = convenienceCheckJust;
    }

    public Boolean getGsaSchedule() {
        return gsaSchedule;
    }

    public void setGsaSchedule(Boolean gsaSchedule) {
        this.gsaSchedule = gsaSchedule;
    }

    public String getGsaScheduleJust() {
        return gsaScheduleJust;
    }

    public void setGsaScheduleJust(String gsaScheduleJust) {
        this.gsaScheduleJust = gsaScheduleJust;
    }

    public Boolean getCommercialVendor() {
        return commercialVendor;
    }

    public void setCommercialVendor(Boolean commercialVendor) {
        this.commercialVendor = commercialVendor;
    }

    public String getCommercialVendorJust() {
        return commercialVendorJust;
    }

    public void setCommercialVendorJust(String commercialVendorJust) {
        this.commercialVendorJust = commercialVendorJust;
    }

    
    
    public Boolean getThirdPartyVendor() {
        return thirdPartyVendor;
    }

    public void setThirdPartyVendor(Boolean thirdPartyVendor) {
        this.thirdPartyVendor = thirdPartyVendor;
    }

    public String getThirdPartyVendorJust() {
        return thirdPartyVendorJust;
    }

    public void setThirdPartyVendorJust(String thirdPartyVendorJust) {
        this.thirdPartyVendorJust = thirdPartyVendorJust;
    }


    public String getPriceIsReasonableJust() {
        return priceIsReasonableJust;
    }

    public void setPriceIsReasonableJust(String priceIsReasonableJust) {
        this.priceIsReasonableJust = priceIsReasonableJust;
    }

    public Boolean getSmallBusiness() {
        return smallBusiness;
    }

    public void setSmallBusiness(Boolean smallBusiness) {
        this.smallBusiness = smallBusiness;
    }

    public String getSmallBusinessJust() {
        return smallBusinessJust;
    }

    public void setSmallBusinessJust(String smallBusinessJust) {
        this.smallBusinessJust = smallBusinessJust;
    }

    public Integer getDivisionOrgId() {
        return divisionOrgId;
    }

    public void setDivisionOrgId(Integer divisionOrgId) {
        this.divisionOrgId = divisionOrgId;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getProfessionalOrg() {
        return professionalOrg;
    }

    public void setProfessionalOrg(Boolean professionalOrg) {
        this.professionalOrg = professionalOrg;
    }

    public Integer getBuiltInVendor() {
        return builtInVendor;
    }

    public void setBuiltInVendor(Integer builtInVendor) {
        this.builtInVendor = builtInVendor;
    }
    
    
}
