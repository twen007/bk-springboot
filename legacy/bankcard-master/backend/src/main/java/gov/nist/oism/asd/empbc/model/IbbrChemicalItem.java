package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;

public class IbbrChemicalItem implements Serializable {

    private String amount;
    private String cas;
    private String room;
    private String catalog;
    private String cost;
    private String name;
    private String owner_sn;
    private String owner_given;
    private String location;
    private String supplier;
    private String owner_email;
    private Integer quantity;

    public IbbrChemicalItem() {
        super();
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCas() {
        return cas;
    }

    public void setCas(String cas) {
        this.cas = cas;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner_sn() {
        return owner_sn;
    }

    public void setOwner_sn(String owner_sn) {
        this.owner_sn = owner_sn;
    }

    public String getOwner_given() {
        return owner_given;
    }

    public void setOwner_given(String owner_given) {
        this.owner_given = owner_given;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getOwner_Email() {
        return owner_email;
    }

    public void setOwner_Email(String email) {
        this.owner_email = email;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String toString()
    {
        return "name: " +  this.name + ", cost: " + cost + ", Owner Name: {" + this.owner_sn +", "+ this.owner_given + "}";
    }
}
