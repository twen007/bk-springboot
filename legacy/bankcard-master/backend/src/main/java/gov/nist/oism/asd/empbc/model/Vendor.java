package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;
import java.util.Date;

public class Vendor implements Serializable {
    
    private Integer mId;
    private String mName;
    private String mStreet;
    private String mCity;
    private String mState;
    private String mZipCode;
    private String mWebUrl;
    private String mContactName;
    private String mPhoneNumber;
    private String mFaxNumber;
    private String mEmail;
    private String mAccountNumber;
    private Boolean mIsForeignAddress;
    private String mForeignAddress;
    private Integer mCreatedBy;
    private Date mCreatedDate;
    private Integer mUpdatedBy;
    private Date mUpdatedDate;
    private String mDunsNumber;
    private String mImportedFrom;
    private Boolean mIsActive;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getStreet() {
        return mStreet;
    }

    public void setStreet(String street) {
        mStreet = street;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        mState = state;
    }

    public String getZipCode() {
        return mZipCode;
    }

    public void setZipCode(String zipCode) {
        mZipCode = zipCode;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public void setWebUrl(String webUrl) {
        mWebUrl = webUrl;
    }

    public String getContactName() {
        return mContactName;
    }

    public void setContactName(String contactName) {
        mContactName = contactName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return mFaxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        mFaxNumber = faxNumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getAccountNumber() {
        return mAccountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        mAccountNumber = accountNumber;
    }

    public Boolean getIsForeignAddress() {
        return mIsForeignAddress;
    }

    public void setIsForeignAddress(Boolean isForeignAddress) {
        mIsForeignAddress = isForeignAddress;
    }

    public String getForeignAddress() {
        return mForeignAddress;
    }

    public void setForeignAddress(String foreignAddress) {
        mForeignAddress = foreignAddress;
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

    public String getDunsNumber() {
        return mDunsNumber;
    }

    public void setDunsNumber(String dunsNumber) {
        mDunsNumber = dunsNumber;
    }

    public String getImportedFrom() {
        return mImportedFrom;
    }

    public void setImportedFrom(String importedFrom) {
        mImportedFrom = importedFrom;
    }

    public Boolean getIsActive() {
        return mIsActive;
    }

    public void setIsActive(Boolean isActive) {
        mIsActive = isActive;
    }
}
