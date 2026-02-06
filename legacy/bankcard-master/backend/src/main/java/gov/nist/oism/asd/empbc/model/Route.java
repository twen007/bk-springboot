package gov.nist.oism.asd.empbc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with parameters for all fields

public class Route implements Serializable {
    
    private static final long serialVersionUID = 1L; // Unique identifier for serialization
    
    private Integer id;
    private Integer requestId;
    private Integer typeId;
    private String typeName; // Joined.
    private String notes;
    private Integer routeBy;
    private Date routeDate;
    private String routeByName; // Joined.
    private Integer statusId;
    private String statusName; // Joined.
    private Integer routeTo;
    private String routeToName; // Joined.
    private Integer rerouteBy;
    private String rerouteByName; // Joined.
    private Integer isDynamic;
    private Integer isDynamicReroute;
    private Integer rerouteStack;
    private String alsoNotify;
    private Integer routeStep;
    private String dynamicType;
    private Integer routeByDelegate;
    private String routeByDelegateName;
    private Integer omitNotification;
}
