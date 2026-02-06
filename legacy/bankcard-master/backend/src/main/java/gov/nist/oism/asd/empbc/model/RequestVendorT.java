/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 *
 * @author xinweiw
 */
public class RequestVendorT {
    private Integer requestId;
    private Integer refVendorId;
    //vendorId only exists in vendor but we need it here because the json we passed in could be a vendor selected from dropdown
    //and we need to save the vendorId into the refVendorId column in Request_Vendor_T table
    private Integer vendorId;
    //convenienceCheckJust is only used to transport convenienceCheck Justification data from frontend to the backend;
    //when user create the convenienceCheck request vendor, we need to create a request justification record
    private String convenienceCheckJust;
    private String vendorName;
    private Boolean convenienceCheck;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String webUrl;
    private String contactPerson;
    private String phone;
    private String fax;
    private String email;
    private String accountNumber;
    private Boolean isForeignAddress;
    private String foreignAddress;
    private Integer createdBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT") 
    private Date createdDate;
    private Integer updatedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT") 
    private Date updatedDate;
    private String dunsNumber;
    private String additionalInfo;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getRefVendorId() {
        return refVendorId;
    }

    public void setRefVendorId(Integer refVendorId) {
        this.refVendorId = refVendorId;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public String getConvenienceCheckJust() {
        return convenienceCheckJust;
    }

    public void setConvenienceCheckJust(String convenienceCheckJust) {
        this.convenienceCheckJust = convenienceCheckJust;
    }
    
    

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Boolean getConvenienceCheck() {
        return convenienceCheck;
    }

    public void setConvenienceCheck(Boolean convenienceCheck) {
        this.convenienceCheck = convenienceCheck;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Boolean getIsForeignAddress() {
        return isForeignAddress;
    }

    public void setIsForeignAddress(Boolean isForeignAddress) {
        this.isForeignAddress = isForeignAddress;
    }

    public String getForeignAddress() {
        return foreignAddress;
    }

    public void setForeignAddress(String foreignAddress) {
        this.foreignAddress = foreignAddress;
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

    public String getDunsNumber() {
        return dunsNumber;
    }

    public void setDunsNumber(String dunsNumber) {
        this.dunsNumber = dunsNumber;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    
    
}
