package model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by highland0971 on 2016/3/26.
 */


@Entity
@Table(name = "user_info")
public class User implements Serializable {

    @Id
    @GeneratedValue
    private long user_id;

    @NotNull
    @Column(nullable = false,length = 320,unique = true)
    private String email;

    @NotNull
    @Column(nullable = false,length = 320)
    private String password;

    @NotNull
    @Column(nullable = false)
    private String token;

    @NotNull
    private long reg_data;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64, name = "payment_type")
    private AUDIT_TYPE audit_type;

    @Column(nullable = false)
    private int credit_data_gb = 0;

    private long expire_date = -1;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ServerType server_type;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.PERSIST,mappedBy = "dedicatedUser")
    private ProxyServer dedicateServer;


    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getReg_data() {
        return reg_data;
    }

    public void setReg_data(long reg_data) {
        this.reg_data = reg_data;
    }

    public AUDIT_TYPE getAudit_type() {
        return audit_type;
    }

    public void setAudit_type(AUDIT_TYPE audit_type) {
        this.audit_type = audit_type;
    }

    public int getCredit_data_gb() {
        return credit_data_gb;
    }

    public void setCredit_data_gb(int credit_data_gb) {
        this.credit_data_gb = credit_data_gb;
    }

    public long getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(long expire_date) {
        this.expire_date = expire_date;
    }

    public ServerType getServer_type() {
        return server_type;
    }

    public void setServer_type(ServerType server_type) {
        this.server_type = server_type;
    }


}


