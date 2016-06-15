package controllers;

import model.*;
import play.Configuration;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.Result;
import views.html.chargeFailure;
import views.html.chargeSuccess;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.Map;

import static play.mvc.Controller.session;
import static play.mvc.Results.*;

/**
 * Created by vivia on 2016/5/7.
 */
public class FinanceController {

    @Inject
    FormFactory formFactory;

    @Inject
    JPAApi jpaApi;

    public Result cancelPayPalCheckout() {
        DynamicForm requestData = formFactory.form().bindFromRequest();
        return ok(requestData.data().toString());
    }

    @Transactional
    public boolean chargePlanActivation(Long invoiceId, boolean forceActivation) {
        //TODO change invoice activation status to activated
        if (invoiceId != null && invoiceId > 0) {
            EntityManager em = jpaApi.em();
            Invoice invoice = em.find(Invoice.class, invoiceId);
            if (invoice != null && invoice.isValid()) {
                User user = invoice.getPaiedUser();
                if (invoice.getContract_type().equals("year")) {
                    Long targetExpireDate = TimeUtil.toOrdinal(LocalDate.now().plusYears(invoice.getContract_amount()));
                    if (user.getAudit_type() == AUDIT_TYPE.period) {
                        if (user.getCredit_data_gb() == invoice.getAffiliate_limit()) {
                            if (user.getExpire_date() > TimeUtil.toOrdinal(LocalDate.now()))
                                targetExpireDate = user.getExpire_date() + 365 * invoice.getContract_amount();
                            user.setExpire_date(targetExpireDate);
                            invoice.setActivated(true);
                            return true;
                        } else if (forceActivation) {
                            user.setExpire_date(targetExpireDate);
                            user.setCredit_data_gb(invoice.getAffiliate_limit());
                            invoice.setActivated(true);
                            return true;
                        }
                    } else if (forceActivation) {
                        user.setExpire_date(targetExpireDate);
                        user.setCredit_data_gb(invoice.getAffiliate_limit());
                        user.setAudit_type(AUDIT_TYPE.period);
                        invoice.setActivated(true);
                    }
                } else if (invoice.getContract_type().equals("month")) {
                    Long targetExpireDate = TimeUtil.toOrdinal(LocalDate.now().plusMonths(invoice.getContract_amount()));
                    if (user.getAudit_type() == AUDIT_TYPE.period) {
                        if (user.getCredit_data_gb() == invoice.getAffiliate_limit()) {
                            if (user.getExpire_date() > TimeUtil.toOrdinal(LocalDate.now()))
                                targetExpireDate = TimeUtil.toOrdinal(TimeUtil.fromOrdinal(user.getExpire_date()).plusMonths(invoice.getContract_amount()));
                            user.setExpire_date(targetExpireDate);
                            invoice.setActivated(true);
                            return true;
                        } else if (forceActivation) {
                            user.setExpire_date(targetExpireDate);
                            user.setCredit_data_gb(invoice.getAffiliate_limit());
                            invoice.setActivated(true);
                            return true;
                        }
                    } else if (forceActivation) {
                        user.setExpire_date(targetExpireDate);
                        user.setCredit_data_gb(invoice.getAffiliate_limit());
                        user.setAudit_type(AUDIT_TYPE.period);
                        invoice.setActivated(true);
                        return true;
                    }
                } else if (invoice.getContract_type().equals("usage")) {
                    if (user.getAudit_type() == AUDIT_TYPE.usage_duration) {
                        user.setCredit_data_gb(user.getCredit_data_gb() + invoice.getContract_amount());
                        user.setExpire_date(TimeUtil.toOrdinal(LocalDate.now().plusMonths(invoice.getAffiliate_limit())));
                        invoice.setActivated(true);
                        return true;
                    } else if (forceActivation) {
                        user.setCredit_data_gb(user.getCredit_data_gb() + invoice.getContract_amount());
                        user.setExpire_date(TimeUtil.toOrdinal(LocalDate.now().plusMonths(invoice.getAffiliate_limit())));
                        user.setAudit_type(AUDIT_TYPE.usage_duration);
                        invoice.setActivated(true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Transactional
    public Result confirmPayPalCheckout() {

        //TODO change Result into promised result async
        if (null == session("user_id"))
            return status(401, "Invalid access!");

        DynamicForm requestData = formFactory.form().bindFromRequest();
        String token = requestData.get("token");

        System.out.println("PayPal checkout confirmed with token:" + token);

        String failureMessage = "";

        if (token != null && !token.isEmpty()) {
            EntityManager em = jpaApi.em();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery criteria = cb.createQuery(PayPalInvoice.class);
            Root<PayPalInvoice> i = criteria.from(PayPalInvoice.class);
            ParameterExpression<String> pToken = cb.parameter(String.class);
            criteria.select(i).where(cb.equal(i.get("TOKEN"), pToken));
            TypedQuery<PayPalInvoice> query = em.createQuery(criteria);
            query.setParameter(pToken, token);
            try {
                PayPalInvoice invoice = query.getSingleResult();
                Configuration cfg = Configuration.root();
                PayPalExpressCheckoutHelper helper = new PayPalExpressCheckoutHelper(
                        cfg.getBoolean("paypal.sandbox"),
                        cfg.getString("paypal.user"),
                        cfg.getString("paypal.pwd"),
                        cfg.getString("paypal.signature"));


//                em.persist(invoice);

                invoice.setLastMETHOD(PAYPAL_METHOD.GetExpressCheckoutDetails);
                Map<String, String> response = helper.GetExpressCheckoutDetails(token);
                System.out.println(response.toString());
                if (response != null) {
                    invoice.setLastACK(response.get("ACK"));
                    invoice.setTIMESTAMP_1(response.get("TIMESTAMP"));
                    invoice.setRAW_RESPONSE_1(response.toString());
                    System.out.println("ACK:" + response.get("ACK"));
                    if (response.get("ACK").equals("Success")) {
                        invoice.setCORRELATIONID_1(response.get("CORRELATIONID"));

                        invoice.setEMAIL(response.get("EMAIL"));
                        invoice.setPAYERID(response.get("PAYERID"));
                        invoice.setFIRSTNAME(response.get("FIRSTNAME"));
                        invoice.setLASTNAME(response.get("LASTNAME"));

                        invoice.setCOUNTRYCODE(response.get("COUNTRYCODE"));
                        invoice.setCURRENCYCODE(response.get("CURRENCYCODE"));

                        invoice.setPAYMENTREQUEST_0_AMT(Double.valueOf(response.get("PAYMENTREQUEST_0_AMT")));
                        invoice.setL_PAYMENTREQUEST_0_AMT0(Double.valueOf(response.get("L_PAYMENTREQUEST_0_AMT0")));
                        invoice.setL_PAYMENTREQUEST_0_QTY0(Integer.valueOf(response.get("L_PAYMENTREQUEST_0_QTY0")));
                        invoice.setL_PAYMENTREQUEST_0_DESC0(response.get("L_PAYMENTREQUEST_0_DESC0"));
                        invoice.setL_PAYMENTREQUEST_0_NAME0(response.get("L_PAYMENTREQUEST_0_NAME0"));

                        invoice.setLastMETHOD(PAYPAL_METHOD.DoExpressCheckoutPayment);
                        Map<String, String> doResponse = helper.DoExpressCheckoutPayment(response.get("TOKEN"), response.get("PAYERID"), response.get("PAYMENTREQUEST_0_INVNUM"), Double.valueOf(response.get("PAYMENTREQUEST_0_AMT")));
                        System.out.println(doResponse.toString());
                        if (doResponse != null) {
                            invoice.setLastACK(doResponse.get("ACK"));
                            invoice.setTIMESTAMP_2(doResponse.get("TIMESTAMP"));
                            invoice.setRAW_RESPONSE_2(doResponse.toString());
                            if (doResponse.get("ACK").equals("Success") && doResponse.get("PAYMENTINFO_0_PAYMENTSTATUS").equals("Completed")) {
                                invoice.setCORRELATIONID_2(doResponse.get("CORRELATIONID"));
                                invoice.setPAYMENTINFO_0_ACK(doResponse.get("PAYMENTINFO_0_ACK"));
                                invoice.setPAYMENTINFO_0_AMT(Double.valueOf(doResponse.get("PAYMENTINFO_0_AMT")));
                                invoice.setPAYMENTINFO_0_TRANSACTIONTYPE(doResponse.get("PAYMENTINFO_0_TRANSACTIONTYPE"));
                                invoice.setPAYMENTINFO_0_FEEAMT(Double.valueOf(doResponse.get("PAYMENTINFO_0_FEEAMT")));
                                invoice.setPAYMENTINFO_0_PAYMENTTYPE(doResponse.get("PAYMENTINFO_0_PAYMENTTYPE"));
                                invoice.setPAYMENTINFO_0_TRANSACTIONID(doResponse.get("PAYMENTINFO_0_TRANSACTIONID"));
                                invoice.setPAYMENTINFO_0_CURRENCYCODE(doResponse.get("PAYMENTINFO_0_CURRENCYCODE"));
                                invoice.setPAYMENTINFO_0_SECUREMERCHANTACCOUNTID(doResponse.get("PAYMENTINFO_0_SECUREMERCHANTACCOUNTID"));
                                invoice.setPAYMENTINFO_0_PAYMENTSTATUS(doResponse.get("PAYMENTINFO_0_PAYMENTSTATUS"));
                                invoice.setPAYMENTINFO_0_REASONCODE(doResponse.get("PAYMENTINFO_0_REASONCODE"));

                                invoice.setValid(true);


                                return ok(chargeSuccess.render(
                                        invoice.getPAYMENTINFO_0_TRANSACTIONID(),
                                        invoice.getPAYMENTINFO_0_AMT(),
                                        invoice.getContract_type(),
                                        invoice.getContract_amount(),
                                        chargePlanActivation(invoice.getId(), false)));
                            } else {
                                failureMessage = "DoExpressCheckoutPayment return non-success info";
                            }
                        } else {
                            invoice.setLastACK("NonResponse of DoExpressCheckoutPayment");
                            failureMessage = "NonResponse of DoExpressCheckoutPayment";
                        }
                    } else {
                        failureMessage = "GetExpressCheckoutDetails return non-success info";
                    }
                } else {
                    invoice.setLastACK("NonResponse of GetExpressCheckoutDetails");
                    failureMessage = "NonResponse of GetExpressCheckoutDetails";
                }
            } catch (NoResultException ex) {
                failureMessage = "Internal SetupExpressCheckout Query error";
            }
        } else {
            failureMessage = "No valid TOKEN provided";
        }

        return ok(chargeFailure.render(failureMessage));
    }

    @Transactional
    public Result PayPalCheckout() {
        //TODO change Result into promised result async
        if(null == session("user_id"))
            return status(401,"Invalid access!");

        DynamicForm requestData = formFactory.form().bindFromRequest();
        String chargeType = requestData.get("charge_type");
        String chargeAmount = requestData.get("charge_amount");
        Configuration cfg = Configuration.root();
        PayPalExpressCheckoutHelper helper = new PayPalExpressCheckoutHelper(cfg.getBoolean("paypal.sandbox"),cfg.getString("paypal.user"),
                cfg.getString("paypal.pwd"),
                cfg.getString("paypal.signature"));
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip=addr.getHostAddress().toString();
            Map<String,String> response = null;
            EntityManager em = jpaApi.em();
            PayPalInvoice invoice = new PayPalInvoice();
            invoice.setValid(false);
            if(chargeType !=null && !chargeType.isEmpty() && chargeAmount!=null && Integer.valueOf(chargeAmount) > 0)
            {
                invoice.setPaiedUser(em.find(User.class, Long.valueOf(session("user_id"))));
                System.out.println("Amount:" + chargeAmount);
                invoice.setContract_amount(Integer.valueOf(chargeAmount));
                invoice.setContract_type(chargeType);
                invoice.setInvoice_date(TimeUtil.toOrdinal(LocalDate.now()));
                invoice.setPaymentGateway(PAYMENT_GATEWAY.PAYPAL);

                em.persist(invoice);

                String chargeName = null;
                String chargeDesc = null;
                switch (chargeType)
                {
                    case "year":
                        chargeName = "包年套餐（包年数）";
                        chargeDesc = "每月200GB流量";
                        invoice.setAffiliate_limit(200);
                        break;
                    case "month":
                        chargeName = "包月套餐（包月数）";
                        chargeDesc = "每月100GB流量";
                        invoice.setAffiliate_limit(100);
                        break;
                    case "usage":
                        chargeName = "流量套餐（GB）";
                        chargeDesc = "总套餐流量，一年期";
                        invoice.setAffiliate_limit(12);
                        break;
                }
                if (chargeName != null) {
                    System.out.println("invoice.setLastMETHOD(PAYPAL_METHOD.SetExpressCheckout);");
                    invoice.setLastMETHOD(PAYPAL_METHOD.SetExpressCheckout);

                    response = helper.SetExpressCheckout(
                            "http://" + ip + ":" + cfg.getString("http.port") + routes.FinanceController.confirmPayPalCheckout(),
                            "http://" + ip + ":" + cfg.getString("http.port") + routes.FinanceController.cancelPayPalCheckout(),
                            invoice.getId().toString(),
                            chargeName,
                            chargeDesc,
                            Integer.valueOf(chargeAmount),
                            ChargePolicy.getChargeUnitPrice(chargeType, Integer.valueOf(chargeAmount), "USD"),
                            "Demo test"
                    );
                }
                if(null != response)
                {
                    invoice.setLastACK(response.get("ACK"));
                    invoice.setVERSION(response.get("VERSION"));
                    invoice.setTIMESTAMP_0(response.get("TIMESTAMP"));
                    invoice.setRAW_RESPONSE_0(response.toString());

                    if(response.get("ACK").equals("Success"))
                    {
                        invoice.setCORRELATIONID_0(response.get("CORRELATIONID"));
                        invoice.setTOKEN(response.get("TOKEN"));
                        return redirect(helper.getPayPalAddr()+"/cgi-bin/webscr?cmd=_express-checkout&token="+response.get("TOKEN"));
                    }
                    else{
                        return status(401,response.toString());
                    }
                }
                else {
                    invoice.setLastACK(response.get("NonResponse"));
                    return status(401,"None response encountered");
                }
            }
            else {
                return status(401,"Invalid charging request!");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return status(401,"Unknown status!");
    }

    public Result GetExpressCheckoutDetails() {

        HttpUtil helper = new HttpUtil("https://api-3t.sandbox.paypal.com/nvp","UTF-8");
        helper.setupConnection("POST");

        helper.feedPayload("USER","joy.highland-facilitator_api1.gmail.com");
        helper.feedPayload("PWD","KD5EEL7CJ5JBKT2C");
        helper.feedPayload("SIGNATURE","AFcWxV21C7fd0v3bYYYRCpSSRl31A-.56hvs1Mc6lr992jugDRWcMECK");
        helper.feedPayload("METHOD","GetExpressCheckoutDetails");
        helper.feedPayload("VERSION","124");
        helper.feedPayload("TOKEN",session("token"));
        helper.doPostRequest();
        Map<String,String> response = helper.getResponsePair();
        System.out.println(response.toString());
        /***
         * [info] {BUILD=22120179, L_PAYMENTREQUEST_0_NAME0=L_PAYMENTREQUEST_0_NAME0, FIRSTNAME=test, EMAIL=joy.highland-buyer@gmail.com,
         * L_NAME1=L_PAYMENTREQUEST_0_NAME1, L_NAME0=L_PAYMENTREQUEST_0_NAME0, L_PAYMENTREQUEST_0_NAME1=L_PAYMENTREQUEST_0_NAME1, L_AMT0=1.00,
         * L_PAYMENTREQUEST_0_ITEMWIDTHVALUE0=   0.00000, LASTNAME=buyer, L_AMT1=0.50, L_PAYMENTREQUEST_0_ITEMWIDTHVALUE1=   0.00000,
         * TIMESTAMP=2016-05-11T15:22:23Z, SHIPPINGAMT=0.00, L_PAYMENTREQUEST_0_TAXAMT0=0.00, TOKEN=EC-8EP9675188031105T,
         * L_PAYMENTREQUEST_0_AMT0=1.00, L_PAYMENTREQUEST_0_TAXAMT1=0.00, L_PAYMENTREQUEST_0_AMT1=0.50, L_PAYMENTREQUEST_0_ITEMLENGTHVALUE1=   0.00000,
         * L_PAYMENTREQUEST_0_ITEMLENGTHVALUE0=   0.00000, L_ITEMWIDTHVALUE0=   0.00000, L_PAYMENTREQUEST_0_QTY1=4, L_PAYMENTREQUEST_0_QTY0=1,
         * AMT=3.00, TAXAMT=0.00, PAYMENTREQUEST_0_HANDLINGAMT=0.00, SHIPDISCAMT=0.00, PAYMENTREQUEST_0_INSURANCEOPTIONOFFERED=false,
         * BILLINGAGREEMENTACCEPTEDSTATUS=0, PAYMENTREQUEST_0_INVNUM=UX332US217621, COUNTRYCODE=CN, PAYMENTREQUEST_0_AMT=3.00,
         * L_PAYMENTREQUEST_0_ITEMHEIGHTVALUE1=   0.00000, L_PAYMENTREQUEST_0_ITEMHEIGHTVALUE0=   0.00000, L_PAYMENTREQUEST_0_ITEMWEIGHTVALUE1=   0.00000,
         * L_PAYMENTREQUEST_0_ITEMWEIGHTVALUE0=   0.00000, PAYMENTREQUEST_0_CURRENCYCODE=USD, PAYMENTREQUESTINFO_0_ERRORCODE=0,
         * CHECKOUTSTATUS=PaymentActionNotInitiated, CORRELATIONID=cbe97e7c7c9fe, L_DESC0=L_PAYMENTREQUEST_0_DESC0, L_DESC1=L_PAYMENTREQUEST_0_DESC1,
         * L_ITEMWIDTHVALUE1=   0.00000, HANDLINGAMT=0.00, PAYMENTREQUEST_0_TAXAMT=0.00, ITEMAMT=3.00, L_PAYMENTREQUEST_0_DESC0=L_PAYMENTREQUEST_0_DESC0,
         * PAYERSTATUS=verified, L_PAYMENTREQUEST_0_DESC1=L_PAYMENTREQUEST_0_DESC1, PAYMENTREQUEST_0_INSURANCEAMT=0.00, PAYMENTREQUEST_0_SHIPDISCAMT=0.00,
         * ACK=Success, L_TAXAMT1=0.00, L_TAXAMT0=0.00, CURRENCYCODE=USD, PAYMENTREQUEST_0_SHIPPINGAMT=0.00, INVNUM=UX332US217621, INSURANCEAMT=0.00,
         * PAYMENTREQUEST_0_ADDRESSNORMALIZATIONSTATUS=None, L_ITEMHEIGHTVALUE1=   0.00000, L_ITEMLENGTHVALUE0=   0.00000, L_ITEMHEIGHTVALUE0=   0.00000,
         * PAYERID=XBWY78WMZ8ZZ8, L_ITEMLENGTHVALUE1=   0.00000,
         * VERSION=124, L_ITEMWEIGHTVALUE1=   0.00000, L_ITEMWEIGHTVALUE0=   0.00000, L_QTY0=1, PAYMENTREQUEST_0_ITEMAMT=3.00, L_QTY1=4}
         */

        if(response != null & response.get("ACK").equals("Success"))
        {
            HttpUtil doHelper = new HttpUtil("https://api-3t.sandbox.paypal.com/nvp","UTF-8");
            doHelper.setupConnection("POST");
            doHelper.feedPayload("USER","joy.highland-facilitator_api1.gmail.com");
            doHelper.feedPayload("PWD","KD5EEL7CJ5JBKT2C");
            doHelper.feedPayload("SIGNATURE","AFcWxV21C7fd0v3bYYYRCpSSRl31A-.56hvs1Mc6lr992jugDRWcMECK");
            doHelper.feedPayload("METHOD","DoExpressCheckoutPayment");
            doHelper.feedPayload("VERSION","124");
            doHelper.feedPayload("TOKEN",session("token"));
            doHelper.feedPayload("PAYERID",response.get("PAYERID"));
            doHelper.feedPayload("PAYMENTREQUEST_0_AMT",response.get("PAYMENTREQUEST_0_AMT"));
            doHelper.feedPayload("PAYMENTREQUEST_0_ITEMAMT",response.get("PAYMENTREQUEST_0_ITEMAMT"));
            doHelper.feedPayload("PAYMENTREQUEST_0_PAYMENTACTION","Sale");
            doHelper.doPostRequest();
            Map<String,String> finalResult = doHelper.getResponsePair();
            System.out.println(finalResult.toString());
            /***
             * {PAYMENTINFO_0_AMT=3.00, PAYMENTINFO_0_ACK=Success, BUILD=22120179, SHIPPINGOPTIONISDEFAULT=false,
             * PAYMENTINFO_0_TRANSACTIONTYPE=expresscheckout, PAYMENTINFO_0_FEEAMT=0.40, SUCCESSPAGEREDIRECTREQUESTED=false,
             * PAYMENTINFO_0_PROTECTIONELIGIBILITY=Ineligible, PAYMENTINFO_0_PAYMENTTYPE=instant, PAYMENTINFO_0_ORDERTIME=2016-05-11T15:22:26Z,
             * PAYMENTINFO_0_PROTECTIONELIGIBILITYTYPE=None, TIMESTAMP=2016-05-11T15:22:26Z, CORRELATIONID=d0036d38e682f, TOKEN=EC-8EP9675188031105T,
             * PAYMENTINFO_0_PENDINGREASON=None, PAYMENTINFO_0_TAXAMT=0.00, PAYMENTINFO_0_ERRORCODE=0, PAYMENTINFO_0_TRANSACTIONID=8MF72127MV549664L,
             * ACK=Success, PAYMENTINFO_0_CURRENCYCODE=USD, PAYMENTINFO_0_SECUREMERCHANTACCOUNTID=SQ6SV9CUZAZSG, INSURANCEOPTIONSELECTED=false,
             * PAYMENTINFO_0_REASONCODE=None, VERSION=124, PAYMENTINFO_0_PAYMENTSTATUS=Completed}
             */
            return ok(finalResult.toString());
        }
        else{
            return status(401,response.toString());
        }
    }

    public Result SetExpressCheckout() {
        HttpUtil helper = new HttpUtil("https://api-3t.sandbox.paypal.com/nvp","UTF-8");
        helper.setupConnection("POST");

        helper.feedPayload("USER","joy.highland-facilitator_api1.gmail.com");
        helper.feedPayload("PWD","KD5EEL7CJ5JBKT2C");
        helper.feedPayload("SIGNATURE","AFcWxV21C7fd0v3bYYYRCpSSRl31A-.56hvs1Mc6lr992jugDRWcMECK");
        helper.feedPayload("METHOD","SetExpressCheckout");
        helper.feedPayload("VERSION","124");
        helper.feedPayload("RETURNURL","http://204.44.94.126:9000/GetExpressCheckoutDetails");
        helper.feedPayload("CANCELURL","http://204.44.94.126:9000/pay_cancel");
        helper.feedPayload("REQCONFIRMSHIPPING","0");
        helper.feedPayload("NOSHIPPING","1");
        helper.feedPayload("CALLBACKVERSION","61.0");
        helper.feedPayload("BRANDNAME","Sciternet Tech Ltd.,Co.");
        helper.feedPayload("PAYMENTREQUEST_0_INVNUM","UX332US217621");

        helper.feedPayload("PAYMENTREQUEST_0_AMT","3.0");
        helper.feedPayload("PAYMENTREQUEST_0_PAYMENTACTION","Sale");
        helper.feedPayload("PAYMENTREQUEST_0_PAYMENTREASON","None");


        helper.feedPayload("NOTETOBUYER","This is a demo goods");

        helper.feedPayload("L_PAYMENTREQUEST_0_NAME0","L_PAYMENTREQUEST_0_NAME0");
        helper.feedPayload("L_PAYMENTREQUEST_0_DESC0","L_PAYMENTREQUEST_0_DESC0");
        helper.feedPayload("L_PAYMENTREQUEST_0_AMT0","1");
        helper.feedPayload("L_PAYMENTREQUEST_0_QTY0","1");

        helper.feedPayload("L_PAYMENTREQUEST_0_NAME1","L_PAYMENTREQUEST_0_NAME1");
        helper.feedPayload("L_PAYMENTREQUEST_0_DESC1","L_PAYMENTREQUEST_0_DESC1");
        helper.feedPayload("L_PAYMENTREQUEST_0_AMT1","0.5");
        helper.feedPayload("L_PAYMENTREQUEST_0_QTY1","4");


        helper.doPostRequest();
        Map<String,String> response = helper.getResponsePair();
        System.out.println(response.toString());
        /***
         * {TIMESTAMP=2016-05-11T15:21:45Z, BUILD=22120179, CORRELATIONID=fe5d75696224b, VERSION=124, ACK=Success, TOKEN=EC-8EP9675188031105T}
         */

        if(response != null & response.get("ACK").equals("Success"))
        {
            session("token",response.get("TOKEN"));
            return redirect("https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+response.get("TOKEN"));
        }
        else{
            return status(401,response.toString());
        }
    }


}
