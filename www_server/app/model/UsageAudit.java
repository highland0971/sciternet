package model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by vivia on 2016/4/14.
 */


@Entity
@Table(name="usage_trace",uniqueConstraints = @UniqueConstraint(name = "auditKeys",columnNames = {"user_id","assigned_server","audit_date"}))
public class UsageAudit implements Serializable {

    @Id
    @GeneratedValue
    private long auditId;

    @NotNull
    private int audit_date;

    @NotNull
    private long usage_gb;

    @NotNull
    private double usage_mb;

    @NotNull
    private String usage_raw;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id",referencedColumnName = "user_id")
    private User auditedUser;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "assigned_server",referencedColumnName = "router_ip")
    private ProxyServer auditedServer;

    public UsageAudit(int audit_date,long audit_gb,Double audit_mb){
        this.audit_date = audit_date;
        this.usage_gb = audit_gb;
        this.usage_mb = audit_mb;
    }

    public int getAudit_date() {
        return audit_date;
    }

    public void setAudit_date(int audit_date) {
        this.audit_date = audit_date;
    }

    public long getUsage_gb() {
        return usage_gb;
    }

    public void setUsage_gb(int usage_gb) {
        this.usage_gb = usage_gb;
    }

    public double getUsage_mb() {
        return usage_mb;
    }

    public void setUsage_mb(double usage_mb) {
        this.usage_mb = usage_mb;
    }

    public String getUsage_raw() {
        return usage_raw;
    }

    public void setUsage_raw(String usage_raw) {
        this.usage_raw = usage_raw;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsageAudit other = (UsageAudit) o;

        if(getAudit_date() != other.getAudit_date()) return false;
        if(getAuditedServer() != other.getAuditedServer()) return false;
        if(getAuditedUser() != other.getAuditedUser()) return false;

        return  true;
    }

    @Override
    public int hashCode()
    {
        return getAudit_date()+getAuditedUser().hashCode()+getAuditedServer().hashCode();
    }

    public User getAuditedUser() {
        return auditedUser;
    }

    public void setAuditedUser(User auditedUser) {
        this.auditedUser = auditedUser;
    }

    public ProxyServer getAuditedServer() {
        return auditedServer;
    }

    public void setAuditedServer(ProxyServer auditedServer) {
        this.auditedServer = auditedServer;
    }

    public long getAuditId() {
        return auditId;
    }

    public void setAuditId(long auditId) {
        this.auditId = auditId;
    }
}
