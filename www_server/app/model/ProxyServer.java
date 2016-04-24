package model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vivia on 2016/4/16.
 */


@Entity
@Table(name = "proxy_router_list")
public class ProxyServer implements Serializable {

    @Id
    @GeneratedValue
    private long router_id;

    @NotNull
    @Column(nullable = false,length = 64)
    private String router_ip;

    private long expire_date = -1;

    private int credit_datavol_gb;

    private int credit_throughput_mbps;

    private double consumed_datavol_gb;


    @Column(length = 500)
    private String backend_url;

    @Enumerated(EnumType.STRING)
    private ServerType server_type;

    @Column(length = 45)
    private String cypher_method = "rc4-md5";

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.PERSIST
    )
    @JoinColumn(name = "allocated_user_id",referencedColumnName = "user_id")
    private User dedicatedUser;


    @OneToMany(fetch = FetchType.LAZY,mappedBy = "servingServer")
    private Set<ProxyDNS> sharedUserAllocations = new HashSet<>();


}
