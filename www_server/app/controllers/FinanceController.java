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
