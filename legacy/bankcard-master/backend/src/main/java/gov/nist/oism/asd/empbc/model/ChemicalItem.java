package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class ChemicalItem implements Serializable {
    
    private Integer mId;
    private Integer mOwnerId;
    private String mLocation;
    private String mSubLocation;
    private String mCasNumber;
    //not used anymore since 08/2019
    private String mChemicalForm;
     //not used anymore since 08/2019
    private String mChemicalGrade;
    private String mManufacturerName;
    private String mChemicalCatalogNumber;
    private String mCatalogNumberQuantity;
    private String mContainersPerPackage;
    private String mAmountPerContainer;
    private Integer mLabelsNeeded;
    private String mContainerType;
    //added since 08/2019
    private int mContainerTotal;
    //added since 08/2019
    private String mProductUrl;
     //not used anymore since 08/2019
    private Date mExpirationDate;
     //not used anymore since 08/2019
    private String mHealthNfpaValue;
     //not used anymore since 08/2019
    private String mFlammabilityNpfaValue;
     //not used anymore since 08/2019
    private String mReactivityNpfaValue;
     //not used anymore since 08/2019
    private String mSpecialCodeNpfaValue;
     //not used anymore since 08/2019
    private Boolean mIsRadioactiveMaterial;
     //not used anymore since 08/2019
    private Boolean mBiohazardRegistrationRequired;
    private String mSpecialInstructions;
    private Integer mIbbrRoomId;
    private String mIbbrRoomName;
    private Integer mPrimaryUserId;
    private String mPrimaryUserName;
    private String mCisproRemarks;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public Integer getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(Integer ownerId) {
        mOwnerId = ownerId;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getSubLocation() {
        return mSubLocation;
    }

    public void setSubLocation(String subLocation) {
        mSubLocation = subLocation;
    }

    public String getCasNumber() {
        return mCasNumber;
    }

    public void setCasNumber(String casNumber) {
        mCasNumber = casNumber;
    }

    public String getChemicalForm() {
        return mChemicalForm;
    }

    public void setChemicalForm(String chemicalForm) {
        mChemicalForm = chemicalForm;
    }

    public String getChemicalGrade() {
        return mChemicalGrade;
    }

    public void setChemicalGrade(String chemicalGrade) {
        mChemicalGrade = chemicalGrade;
    }

    public String getManufacturerName() {
        return mManufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        mManufacturerName = manufacturerName;
    }

    public String getChemicalCatalogNumber() {
        return mChemicalCatalogNumber;
    }

    public void setChemicalCatalogNumber(String chemicalcatalogNumber) {
        mChemicalCatalogNumber = chemicalcatalogNumber;
    }

    public String getCatalogNumberQuantity() {
        return mCatalogNumberQuantity;
    }

    public void setCatalogNumberQuantity(String catalogNumberQuantity) {
        mCatalogNumberQuantity = catalogNumberQuantity;
    }

    public String getContainersPerPackage() {
        return mContainersPerPackage;
    }

    public void setContainersPerPackage(String containersPerPackage) {
        mContainersPerPackage = containersPerPackage;
    }

    public String getAmountPerContainer() {
        return mAmountPerContainer;
    }

    public void setAmountPerContainer(String amountPerContainer) {
        mAmountPerContainer = amountPerContainer;
    }

    public Integer getLabelsNeeded() {
        return mLabelsNeeded;
    }

    public void setLabelsNeeded(Integer labelsNeeded) {
        mLabelsNeeded = labelsNeeded;
    }

    public String getContainerType() {
        return mContainerType;
    }

    public void setContainerType(String containerType) {
        mContainerType = containerType;
    }
     @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public Date getExpirationDate() {
        return mExpirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        mExpirationDate = expirationDate;
    }

    public String getHealthNfpaValue() {
        return mHealthNfpaValue;
    }

    public void setHealthNfpaValue(String healthNfpaValue) {
        mHealthNfpaValue = healthNfpaValue;
    }

    public String getFlammabilityNpfaValue() {
        return mFlammabilityNpfaValue;
    }

    public void setFlammabilityNpfaValue(String flammabilityNpfaValue) {
        mFlammabilityNpfaValue = flammabilityNpfaValue;
    }

    public String getReactivityNpfaValue() {
        return mReactivityNpfaValue;
    }

    public void setReactivityNpfaValue(String reactivityNpfaValue) {
        mReactivityNpfaValue = reactivityNpfaValue;
    }

    public String getSpecialCodeNpfaValue() {
        return mSpecialCodeNpfaValue;
    }

    public void setSpecialCodeNpfaValue(String specialCodeNpfaValue) {
        mSpecialCodeNpfaValue = specialCodeNpfaValue;
    }

    public Boolean getIsRadioactiveMaterial() {
        return mIsRadioactiveMaterial;
    }

    public void setIsRadioactiveMaterial(Boolean isRadioactiveMaterial) {
        mIsRadioactiveMaterial = isRadioactiveMaterial;
    }

    public Boolean getBiohazardRegistrationRequired() {
        return mBiohazardRegistrationRequired;
    }

    public void setBiohazardRegistrationRequired(Boolean biohazardRegistrationRequired) {
        mBiohazardRegistrationRequired = biohazardRegistrationRequired;
    }

    public String getSpecialInstructions() {
        return mSpecialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        mSpecialInstructions = specialInstructions;
    }
    
    public Integer getIbbrRoomId() {
        return mIbbrRoomId;
    }

    public void setIbbrRoomId(Integer ibbrRoomId) {
        mIbbrRoomId = ibbrRoomId;
    }

    public String getIbbrRoomName() {
        return mIbbrRoomName;
    }

    public void setIbbrRoomName(String ibbrRoomName) {
        mIbbrRoomName = ibbrRoomName;
    }

    public Integer getPrimaryUserId() {
        return mPrimaryUserId;
    }

    public void setPrimaryUserId(Integer primaryUserId) {
        mPrimaryUserId = primaryUserId;
    }

    public String getPrimaryUserName() {
        return mPrimaryUserName;
    }

    public void setPrimaryUserName(String primaryUserName) {
        mPrimaryUserName = primaryUserName;
    }

    public String getCisproRemarks() {
        return mCisproRemarks;
    }

    public void setCisproRemarks(String cisproRemarks) {
        mCisproRemarks = cisproRemarks;
    }

    public int getContainerTotal() {
        return mContainerTotal;
    }

    public void setContainerTotal(int containerTotal) {
        mContainerTotal = containerTotal;
    }

    public String getProductUrl() {
        return mProductUrl;
    }

    public void setProductUrl(String productUrl) {
        mProductUrl = productUrl;
    }
    
    
}
