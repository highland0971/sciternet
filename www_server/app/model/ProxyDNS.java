package model;

import javax.annotation.Generated;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by vivia on 2016/4/16.
 */

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "socketPair",columnNames = {"port","router_ip"}),
        name = "router_dns"
)
public class ProxyDNS implements Serializable {

    @Id
    @GeneratedValue
    private long records_id;

    @NotNull
    private int port;

    @Column(length = 17)
    private String client_mac;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 16)
    private AllocType alloc_type;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "user_id",nullable = false)
    private User allocUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "router_ip",referencedColumnName = "router_ip",nullable = false)
    private ProxyServer servingServer;

    public long getRecords_id() {
        return records_id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClient_mac() {
        return client_mac;
    }

    public void setClient_mac(String client_mac) {
        this.client_mac = client_mac;
    }

    public AllocType getAlloc_type() {
        return alloc_type;
    }

    public void setAlloc_type(AllocType alloc_type) {
        this.alloc_type = alloc_type;
    }
}
