/**
 * pojo used for the EA report search
 */
package gov.nist.oism.asd.empbc.model;

import lombok.Data;

/**
 *
 * @author xinweiw
 */
@Data // Generates getters, setters, toString, equals, and hashCode
public class ItemQueryParam {

    private Integer ouId;
    private String divCode;
    private String orgCodes; //comma separated
    private String fromDate;
    private String toDate;

}
