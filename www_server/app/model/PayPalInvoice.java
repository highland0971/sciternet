package model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by vivia on 2016/5/12.
 */

@Entity
public class PayPalInvoice extends Invoice{

    private String TOKEN;

    private String lastACK;

    @Enumerated(EnumType.STRING)
    private PAYPAL_METHOD lastMETHOD;

    private String FIRSTNAME;

    private String LASTNAME;

    private String EMAIL;

    private String COUNTRYCODE;

    private String CURRENCYCODE;

    private String PAYERID;

    private String VERSION;

    private String CORRELATIONID_0;
    private String TIMESTAMP_0;
    private String RAW_RESPONSE_0;

    private String CORRELATIONID_1;
    private String TIMESTAMP_1;
    private String RAW_RESPONSE_1;

    private String CORRELATIONID_2;
    private String TIMESTAMP_2;
    private String RAW_RESPONSE_2;

    private String CORRELATIONID_3;
    private String TIMESTAMP_3;
    private String RAW_RESPONSE_3;

    private Double PAYMENTREQUEST_0_AMT;

    private Double PAYMENTINFO_0_FEEAMT;

    private String L_PAYMENTREQUEST_0_NAME0;

    private String L_PAYMENTREQUEST_0_DESC0;

    private Double L_PAYMENTREQUEST_0_AMT0;

    private Integer L_PAYMENTREQUEST_0_QTY0;

    private String PAYMENTINFO_0_ACK;

    private String PAYMENTINFO_0_TRANSACTIONTYPE;

    private String PAYMENTINFO_0_PAYMENTTYPE;

    private String PAYMENTINFO_0_ORDERTIME;

    private String PAYMENTINFO_0_TRANSACTIONID;

    private String PAYMENTINFO_0_CURRENCYCODE;

    private String PAYMENTINFO_0_SECUREMERCHANTACCOUNTID;

    private String PAYMENTINFO_0_PAYMENTSTATUS;

    private String PAYMENTINFO_0_REASONCODE;

    private Double PAYMENTINFO_0_AMT;


    public String getTOKEN() {
        return TOKEN;
    }

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    public String getLastACK() {
        return lastACK;
    }

    public void setLastACK(String lastACK) {
        this.lastACK = lastACK;
    }

    public PAYPAL_METHOD getLastMETHOD() {
        return lastMETHOD;
    }

    public void setLastMETHOD(PAYPAL_METHOD lastMETHOD) {
        this.lastMETHOD = lastMETHOD;
    }

    public String getFIRSTNAME() {
        return FIRSTNAME;
    }

    public void setFIRSTNAME(String FIRSTNAME) {
        this.FIRSTNAME = FIRSTNAME;
    }

    public String getLASTNAME() {
        return LASTNAME;
    }

    public void setLASTNAME(String LASTNAME) {
        this.LASTNAME = LASTNAME;
    }

    public String getEMAIL() {
        return EMAIL;
    }

    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    public String getCOUNTRYCODE() {
        return COUNTRYCODE;
    }

    public void setCOUNTRYCODE(String COUNTRYCODE) {
        this.COUNTRYCODE = COUNTRYCODE;
    }

    public String getCURRENCYCODE() {
        return CURRENCYCODE;
    }

    public void setCURRENCYCODE(String CURRENCYCODE) {
        this.CURRENCYCODE = CURRENCYCODE;
    }

    public String getPAYERID() {
        return PAYERID;
    }

    public void setPAYERID(String PAYERID) {
        this.PAYERID = PAYERID;
    }

    public String getVERSION() {
        return VERSION;
    }

    public void setVERSION(String VERSION) {
        this.VERSION = VERSION;
    }

    public String getCORRELATIONID_0() {
        return CORRELATIONID_0;
    }

    public void setCORRELATIONID_0(String CORRELATIONID_0) {
        this.CORRELATIONID_0 = CORRELATIONID_0;
    }

    public String getTIMESTAMP_0() {
        return TIMESTAMP_0;
    }

    public void setTIMESTAMP_0(String TIMESTAMP_0) {
        this.TIMESTAMP_0 = TIMESTAMP_0;
    }

    public String getRAW_RESPONSE_0() {
        return RAW_RESPONSE_0;
    }

    public void setRAW_RESPONSE_0(String RAW_RESPONSE_0) {
        this.RAW_RESPONSE_0 = RAW_RESPONSE_0;
    }

    public String getCORRELATIONID_1() {
        return CORRELATIONID_1;
    }

    public void setCORRELATIONID_1(String CORRELATIONID_1) {
        this.CORRELATIONID_1 = CORRELATIONID_1;
    }

    public String getTIMESTAMP_1() {
        return TIMESTAMP_1;
    }

    public void setTIMESTAMP_1(String TIMESTAMP_1) {
        this.TIMESTAMP_1 = TIMESTAMP_1;
    }

    public String getRAW_RESPONSE_1() {
        return RAW_RESPONSE_1;
    }

    public void setRAW_RESPONSE_1(String RAW_RESPONSE_1) {
        this.RAW_RESPONSE_1 = RAW_RESPONSE_1;
    }

    public String getCORRELATIONID_2() {
        return CORRELATIONID_2;
    }

    public void setCORRELATIONID_2(String CORRELATIONID_2) {
        this.CORRELATIONID_2 = CORRELATIONID_2;
    }

    public String getTIMESTAMP_2() {
        return TIMESTAMP_2;
    }

    public void setTIMESTAMP_2(String TIMESTAMP_2) {
        this.TIMESTAMP_2 = TIMESTAMP_2;
    }

    public String getRAW_RESPONSE_2() {
        return RAW_RESPONSE_2;
    }

    public void setRAW_RESPONSE_2(String RAW_RESPONSE_2) {
        this.RAW_RESPONSE_2 = RAW_RESPONSE_2;
    }

    public String getCORRELATIONID_3() {
        return CORRELATIONID_3;
    }

    public void setCORRELATIONID_3(String CORRELATIONID_3) {
        this.CORRELATIONID_3 = CORRELATIONID_3;
    }

    public String getTIMESTAMP_3() {
        return TIMESTAMP_3;
    }

    public void setTIMESTAMP_3(String TIMESTAMP_3) {
        this.TIMESTAMP_3 = TIMESTAMP_3;
    }

    public String getRAW_RESPONSE_3() {
        return RAW_RESPONSE_3;
    }

    public void setRAW_RESPONSE_3(String RAW_RESPONSE_3) {
        this.RAW_RESPONSE_3 = RAW_RESPONSE_3;
    }

    public Double getPAYMENTREQUEST_0_AMT() {
        return PAYMENTREQUEST_0_AMT;
    }

    public void setPAYMENTREQUEST_0_AMT(Double PAYMENTREQUEST_0_AMT) {
        this.PAYMENTREQUEST_0_AMT = PAYMENTREQUEST_0_AMT;
    }

    public String getL_PAYMENTREQUEST_0_NAME0() {
        return L_PAYMENTREQUEST_0_NAME0;
    }

    public void setL_PAYMENTREQUEST_0_NAME0(String l_PAYMENTREQUEST_0_NAME0) {
        L_PAYMENTREQUEST_0_NAME0 = l_PAYMENTREQUEST_0_NAME0;
    }

    public String getL_PAYMENTREQUEST_0_DESC0() {
        return L_PAYMENTREQUEST_0_DESC0;
    }

    public void setL_PAYMENTREQUEST_0_DESC0(String l_PAYMENTREQUEST_0_DESC0) {
        L_PAYMENTREQUEST_0_DESC0 = l_PAYMENTREQUEST_0_DESC0;
    }

    public Double getL_PAYMENTREQUEST_0_AMT0() {
        return L_PAYMENTREQUEST_0_AMT0;
    }

    public void setL_PAYMENTREQUEST_0_AMT0(Double l_PAYMENTREQUEST_0_AMT0) {
        L_PAYMENTREQUEST_0_AMT0 = l_PAYMENTREQUEST_0_AMT0;
    }

    public Integer getL_PAYMENTREQUEST_0_QTY0() {
        return L_PAYMENTREQUEST_0_QTY0;
    }

    public void setL_PAYMENTREQUEST_0_QTY0(Integer l_PAYMENTREQUEST_0_QTY0) {
        L_PAYMENTREQUEST_0_QTY0 = l_PAYMENTREQUEST_0_QTY0;
    }

    public String getPAYMENTINFO_0_ACK() {
        return PAYMENTINFO_0_ACK;
    }

    public void setPAYMENTINFO_0_ACK(String PAYMENTINFO_0_ACK) {
        this.PAYMENTINFO_0_ACK = PAYMENTINFO_0_ACK;
    }

    public String getPAYMENTINFO_0_TRANSACTIONTYPE() {
        return PAYMENTINFO_0_TRANSACTIONTYPE;
    }

    public void setPAYMENTINFO_0_TRANSACTIONTYPE(String PAYMENTINFO_0_TRANSACTIONTYPE) {
        this.PAYMENTINFO_0_TRANSACTIONTYPE = PAYMENTINFO_0_TRANSACTIONTYPE;
    }

    public String getPAYMENTINFO_0_PAYMENTTYPE() {
        return PAYMENTINFO_0_PAYMENTTYPE;
    }

    public void setPAYMENTINFO_0_PAYMENTTYPE(String PAYMENTINFO_0_PAYMENTTYPE) {
        this.PAYMENTINFO_0_PAYMENTTYPE = PAYMENTINFO_0_PAYMENTTYPE;
    }

    public String getPAYMENTINFO_0_ORDERTIME() {
        return PAYMENTINFO_0_ORDERTIME;
    }

    public void setPAYMENTINFO_0_ORDERTIME(String PAYMENTINFO_0_ORDERTIME) {
        this.PAYMENTINFO_0_ORDERTIME = PAYMENTINFO_0_ORDERTIME;
    }

    public String getPAYMENTINFO_0_TRANSACTIONID() {
        return PAYMENTINFO_0_TRANSACTIONID;
    }

    public void setPAYMENTINFO_0_TRANSACTIONID(String PAYMENTINFO_0_TRANSACTIONID) {
        this.PAYMENTINFO_0_TRANSACTIONID = PAYMENTINFO_0_TRANSACTIONID;
    }

    public String getPAYMENTINFO_0_CURRENCYCODE() {
        return PAYMENTINFO_0_CURRENCYCODE;
    }

    public void setPAYMENTINFO_0_CURRENCYCODE(String PAYMENTINFO_0_CURRENCYCODE) {
        this.PAYMENTINFO_0_CURRENCYCODE = PAYMENTINFO_0_CURRENCYCODE;
    }

    public String getPAYMENTINFO_0_SECUREMERCHANTACCOUNTID() {
        return PAYMENTINFO_0_SECUREMERCHANTACCOUNTID;
    }

    public void setPAYMENTINFO_0_SECUREMERCHANTACCOUNTID(String PAYMENTINFO_0_SECUREMERCHANTACCOUNTID) {
        this.PAYMENTINFO_0_SECUREMERCHANTACCOUNTID = PAYMENTINFO_0_SECUREMERCHANTACCOUNTID;
    }

    public String getPAYMENTINFO_0_PAYMENTSTATUS() {
        return PAYMENTINFO_0_PAYMENTSTATUS;
    }

    public void setPAYMENTINFO_0_PAYMENTSTATUS(String PAYMENTINFO_0_PAYMENTSTATUS) {
        this.PAYMENTINFO_0_PAYMENTSTATUS = PAYMENTINFO_0_PAYMENTSTATUS;
    }

    public String getPAYMENTINFO_0_REASONCODE() {
        return PAYMENTINFO_0_REASONCODE;
    }

    public void setPAYMENTINFO_0_REASONCODE(String PAYMENTINFO_0_REASONCODE) {
        this.PAYMENTINFO_0_REASONCODE = PAYMENTINFO_0_REASONCODE;
    }

    public Double getPAYMENTINFO_0_FEEAMT() {
        return PAYMENTINFO_0_FEEAMT;
    }

    public void setPAYMENTINFO_0_FEEAMT(Double PAYMENTINFO_0_FEEAMT) {
        this.PAYMENTINFO_0_FEEAMT = PAYMENTINFO_0_FEEAMT;
    }

    public Double getPAYMENTINFO_0_AMT() {
        return PAYMENTINFO_0_AMT;
    }

    public void setPAYMENTINFO_0_AMT(Double PAYMENTINFO_0_AMT) {
        this.PAYMENTINFO_0_AMT = PAYMENTINFO_0_AMT;
    }
}
