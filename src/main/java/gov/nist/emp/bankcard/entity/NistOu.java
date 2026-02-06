package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "NIST_OU")
public class NistOu {
    @Id
    @Column(name = "ORG_ID")
    private Long orgId;

    @Column(name = "ORG_CD", nullable = false, length = 20)
    private String orgCd;

    @Column(name = "ORG_NAME", nullable = false, length = 200)
    private String orgName;

    @Column(name = "EFFECTIVE_DT")
    @Temporal(TemporalType.DATE)
    private Date effectiveDt;

    @Column(name = "ORG_ACRNM", length = 6)
    private String orgAcrnm;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn;

    @Column(name = "AD_ORG_ID")
    private Long adOrgId;

    @Column(name = "LAST_UPDATE_DT", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date lastUpdateDt;

    // Getters and setters
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getOrgCd() { return orgCd; }
    public void setOrgCd(String orgCd) { this.orgCd = orgCd; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Date getEffectiveDt() { return effectiveDt; }
    public void setEffectiveDt(Date effectiveDt) { this.effectiveDt = effectiveDt; }
    public String getOrgAcrnm() { return orgAcrnm; }
    public void setOrgAcrnm(String orgAcrnm) { this.orgAcrnm = orgAcrnm; }
    public String getActiveYn() { return activeYn; }
    public void setActiveYn(String activeYn) { this.activeYn = activeYn; }
    public Long getAdOrgId() { return adOrgId; }
    public void setAdOrgId(Long adOrgId) { this.adOrgId = adOrgId; }
    public Date getLastUpdateDt() { return lastUpdateDt; }
    public void setLastUpdateDt(Date lastUpdateDt) { this.lastUpdateDt = lastUpdateDt; }
}
