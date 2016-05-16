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
    private
    PAYMENT_GATEWAY paymentGateway;

    @ManyToOne
    private
    User paiedUser;

    private String promotionCode;

    private long invoice_date;

    private double payment_amount;

    private String contract_type;

    private Integer contract_amount;

    public UUID getId() {
        return id;
    }

    public PAYMENT_GATEWAY getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(PAYMENT_GATEWAY paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public User getPaiedUser() {
        return paiedUser;
    }

    public void setPaiedUser(User paiedUser) {
        this.paiedUser = paiedUser;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public long getInvoice_date() {
        return invoice_date;
    }

    public void setInvoice_date(long invoice_date) {
        this.invoice_date = invoice_date;
    }

    public double getPayment_amount() {
        return payment_amount;
    }

    public void setPayment_amount(double payment_amount) {
        this.payment_amount = payment_amount;
    }


    public Integer getContract_amount() {
        return contract_amount;
    }

    public void setContract_amount(Integer contract_amount) {
        this.contract_amount = contract_amount;
    }

    public String getContract_type() {
        return contract_type;
    }

    public void setContract_type(String contract_type) {
        this.contract_type = contract_type;
    }
}
