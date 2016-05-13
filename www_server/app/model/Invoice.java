package model;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by vivia on 2016/5/12.
 */

@org.hibernate.annotations.GenericGenerator(
        name = "uuid_generator",
        strategy = "uuid2"
)

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Invoice {

    @Id
    @GeneratedValue(generator = "uuid_generator")
    private UUID id;

    @Enumerated(EnumType.STRING)
    PAYMENT_GATEWAY paymentGateway;

    @ManyToOne
    User paiedUser;

    String promotionCode;

    private int invoice_date;

    private double payment_amount;

    @Enumerated(EnumType.STRING)
    AUDIT_TYPE contract_type;

    Integer contract_amount;

}
