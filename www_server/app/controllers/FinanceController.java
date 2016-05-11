package controllers;

import play.mvc.Result;

import java.util.Map;

import static play.mvc.Controller.session;
import static play.mvc.Results.*;

/**
 * Created by vivia on 2016/5/7.
 */
public class FinanceController {

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
