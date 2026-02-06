package gov.nist.oism.asd.empbc.model;

public class RequestRoute extends Request {

    private String mRequesterName;
    private String mCreatedByName;
    private String mCreatedForName;
    private String mVendors;
    private String mItems;
    private Double mTotalCost;
    private Double mActualTotalCost;
    private Route mRoute;
    private Integer mOuId;
    private Integer mDivisionId;
    private Integer mGroupId;
    private Integer isDynamic;
    private Integer isDynamicReroute;
    private Integer rerouteStack;
    private String dynamicType;

    public String getDynamicType() {
        return dynamicType;
    }

    public void setDynamicType(String dynamicType) {
        this.dynamicType = dynamicType;
    }
    
    

    public Integer getRerouteStack() {
        return rerouteStack;
    }

    public void setRerouteStack(Integer rerouteStack) {
        this.rerouteStack = rerouteStack;
    }

    public Integer getIsDynamicReroute() {
        return isDynamicReroute;
    }

    public void setIsDynamicReroute(Integer isDynamicReroute) {
        this.isDynamicReroute = isDynamicReroute;
    }

    public Integer getIsDynamic() {
        return isDynamic;
    }

    public void setIsDynamic(Integer isDynamic) {
        this.isDynamic = isDynamic;
    }

    public String getRequesterName() {
        return mRequesterName;
    }

    public void setRequesterName(String requesterName) {
        mRequesterName = requesterName;
    }

    public String getCreatedByName() {
        return mCreatedByName;
    }

    public void setCreatedByName(String createdByName) {
        mCreatedByName = createdByName;
    }

    public String getCreatedForName() {
        return mCreatedForName;
    }

    public void setCreatedForName(String createdForName) {
        mCreatedForName = createdForName;
    }

    public String getVendors() {
        return mVendors;
    }

    public void setVendors(String vendors) {
        mVendors = vendors;
    }

    public String getItems() {
        return mItems;
    }

    public void setItems(String items) {
        mItems = items;
    }

    public Double getTotalCost() {
        return mTotalCost;
    }

    public void setTotalCost(Double totalCost) {
        mTotalCost = totalCost;
    }

    public Double getActualTotalCost() {
        return mActualTotalCost;
    }

    public void setActualTotalCost(Double actualTotalCost) {
        mActualTotalCost = actualTotalCost;
    }

    public Route getRoute() {
        return mRoute;
    }

    public void setRoute(Route route) {
        mRoute = route;
    }

    public Integer getOuId() {
        return mOuId;
    }

    public void setOuId(Integer ouId) {
        mOuId = ouId;
    }

    public Integer getDivisionId() {
        return mDivisionId;
    }

    public void setDivisionId(Integer divisionId) {
        mDivisionId = divisionId;
    }

    public Integer getGroupId() {
        return mGroupId;
    }

    public void setGroupId(Integer groupId) {
        mGroupId = groupId;
    }
}
