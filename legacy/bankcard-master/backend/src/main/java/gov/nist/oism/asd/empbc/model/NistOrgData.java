/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.model;

import lombok.Data;

/**
 *
 * @author xinweiw
 */
@Data
public class NistOrgData {

    private Integer ouId;
    private Integer divisionId;
    private Integer groupId;
    private String name;
    private String code;
    private String acronym;
    private String shortName;
}
