package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;
import java.util.Date;

public class RequestVendor implements Serializable {
    
    private Integer mRequestId;
    private Integer mVendorId;
    private Boolean mConvenienceCheck;
    private String mConvenienceCheckJustification;
    private Boolean mGsaSchedule;
    private String mGsaScheduleJustification;
    private Boolean mCommercialVendor;
    private String mCommercialVendorJustification;
    private Boolean mThirdPartyVendor;
    private String mThirdPartyJustification;
    private String mPriceJustification;
    private Boolean mSmallBusiness;
    private String mSmallBusinessJustification;
    private String mConvenienceCheckNumber;
    private Integer mDivisionId;
    private Integer mCreatorId;
    private Integer mCreatedBy;
    private Date mCreatedDate;
    private Integer mUpdatedBy;
    private Date mUpdatedDate;
    private Vendor mVendor;
    private Boolean mProfessionalOrg;
    
    public Integer getRequestId() {
        return mRequestId;
    }

    public void setRequestId(Integer requestId) {
        mRequestId = requestId;
    }

    public Integer getVendorId() {
        return mVendorId;
    }

    public void setVendorId(Integer vendorId) {
        mVendorId = vendorId;
    }
    
    public Boolean getConvenienceCheck() {
        return mConvenienceCheck;
    }

    public void setConvenienceCheck(Boolean convenienceCheck) {
        mConvenienceCheck = convenienceCheck;
    }

    public String getConvenienceCheckJustification() {
        return mConvenienceCheckJustification;
    }

    public void setConvenienceCheckJustification(String convenienceCheckJustification) {
        mConvenienceCheckJustification = convenienceCheckJustification;
    }

    public Boolean getGsaSchedule() {
        return mGsaSchedule;
    }

    public void setGsaSchedule(Boolean gsaSchedule) {
        mGsaSchedule = gsaSchedule;
    }

    public String getGsaScheduleJustification() {
        return mGsaScheduleJustification;
    }

    public void setGsaScheduleJustification(String gsaScheduleJustification) {
        mGsaScheduleJustification = gsaScheduleJustification;
    }
    
    public Boolean getCommercialVendor() {
        return mCommercialVendor;
    }

    public void setCommercialVendor(Boolean commercialVendor) {
        mCommercialVendor = commercialVendor;
    }
    
    public String getCommercialVendorJustification() {
        return mCommercialVendorJustification;
    }

    public void setCommercialVendorJustification(String commercialVendorJustification) {
        mCommercialVendorJustification = commercialVendorJustification;
    }

    public Boolean getThirdPartyVendor() {
        return mThirdPartyVendor;
    }

    public void setThirdPartyVendor(Boolean thirdPartyVendor) {
        mThirdPartyVendor = thirdPartyVendor;
    }

    public String getThirdPartyJustification() {
        return mThirdPartyJustification;
    }

    public void setThirdPartyJustification(String thirdPartyJustification) {
        mThirdPartyJustification = thirdPartyJustification;
    }

    public String getPriceJustification() {
        return mPriceJustification;
    }

    public void setPriceJustification(String priceJustification) {
        mPriceJustification = priceJustification;
    }

    public Boolean getSmallBusiness() {
        return mSmallBusiness;
    }

    public void setSmallBusiness(Boolean smallBusiness) {
        mSmallBusiness = smallBusiness;
    }

    public String getSmallBusinessJustification() {
        return mSmallBusinessJustification;
    }

    public void setSmallBusinessJustification(String smallBusinessJustification) {
        mSmallBusinessJustification = smallBusinessJustification;
    }

    public String getConvenienceCheckNumber() {
        return mConvenienceCheckNumber;
    }

    public void setConvenienceCheckNumber(String convenienceCheckNumber) {
        mConvenienceCheckNumber = convenienceCheckNumber;
    }

    public Integer getDivisionId() {
        return mDivisionId;
    }

    public void setDivisionId(Integer divisionId) {
        mDivisionId = divisionId;
    }

    public Integer getCreatorId() {
        return mCreatorId;
    }

    public void setCreatorId(Integer creatorId) {
        mCreatorId = creatorId;
    }
    
    public Integer getCreatedBy() {
        return mCreatedBy;
    }

    public void setCreatedBy(Integer createdBy) {
        mCreatedBy = createdBy;
    }

    public Date getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(Date createdDate) {
        mCreatedDate = createdDate;
    }
    
    public Integer getUpdatedBy() {
        return mUpdatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        mUpdatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return mUpdatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        mUpdatedDate = updatedDate;
    }
    
    public Vendor getVendor() {
        return mVendor;
    }
    
    public void setVendor(Vendor vendor) {
        mVendor = vendor;
    }

    public Boolean getProfessionalOrg() {
        return mProfessionalOrg;
    }

    public void setProfessionalOrg(Boolean professionalOrg) {
        mProfessionalOrg = professionalOrg;
    }
}
