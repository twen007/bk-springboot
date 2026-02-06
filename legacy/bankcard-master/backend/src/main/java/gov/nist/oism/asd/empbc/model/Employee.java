/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.model;

/**
 *
 * @author xinweiw
 */
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {

    @JsonProperty("PEOPLE_ID")
    private Integer peopleId;

    @JsonProperty("FIRST_NAME")
    private String firstName;

    @JsonProperty("LAST_NAME")
    private String lastName;

    @JsonProperty("MID_NAME")
    private String midName;

    @JsonProperty("OU_ORG_ID")
    private Integer ouOrgId;

    @JsonProperty("DIV_ORG_ID")
    private Integer divOrgId;

    @JsonProperty("GRP_ORG_ID")
    private Integer grpOrgId;

    @JsonProperty("USERNAME")
    private String username;

    @JsonProperty("EMAIL")
    private String email;

    @JsonProperty("PHONE")
    private String phone;

    @JsonProperty("SUPERVISOR_YN")
    private String supervisorYn;

    @JsonProperty("LAST_UPDATE_DT")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdateDt;

    @JsonProperty("IS_DIVISION_CHIEF")
    private String isDivisionChief;

    @JsonProperty("IS_AO")
    private String isAo;

    @JsonProperty("IS_AA")
    private String isAa;

    @JsonProperty("IS_GROUP_LEADER")
    private String isGroupLeader;

    @JsonProperty("IS_SECRETARY")
    private String isSecretary;

    @JsonProperty("STAFF_TYPE")
    private String staffType;

    @JsonProperty("SUPERVISOR_PEOPLE_ID")
    private Integer supervisorPeopleId;
}
