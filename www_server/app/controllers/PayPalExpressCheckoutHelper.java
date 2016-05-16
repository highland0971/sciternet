package controllers;

import java.util.Map;

/**
 * Created by vivia on 2016/5/15.
 */


public class PayPalExpressCheckoutHelper {

    private String trialEndPoint = "https://api-3t.sandbox.paypal.com/nvp";
    private String liveEndPoint = "https://api-3t.paypal.com/nvp";

    private String sandPayPalAddr = "https://www.sandbox.paypal.com";
    private String livePayPalAddr = "https://www.paypal.com";

    static private String codec = "UTF-8";
    static private String brandName = "Sciternet Tech Ltd.,Co.";
    private String API_VERSION = "124";

    private String endPoint;

    private String PayPalAddr;


    private String user;
    private String password;
    private String signature;

    public PayPalExpressCheckoutHelper(boolean trailMode,String user,String password,String signature)
    {
        if(trailMode) {
            endPoint = trialEndPoint;
            PayPalAddr = sandPayPalAddr;
        }
        else {
            endPoint = liveEndPoint;
            PayPalAddr = livePayPalAddr;
        }
        this.user = user;
        this.password = password;
        this.signature = signature;
    }


    public Map<String,String> SetExpressCheckout(String succUrl, String failUrl, String invNum, String stuffName, String stuffDesc, int stuffQuantity, double stuffPrice,String noteToUser){

        HttpUtil helper = new HttpUtil(endPoint,codec);
        helper.setupConnection("POST");

        helper.feedPayload("USER",user);
        helper.feedPayload("PWD",password);
        helper.feedPayload("SIGNATURE",signature);
        helper.feedPayload("METHOD","SetExpressCheckout");
        helper.feedPayload("VERSION",API_VERSION);

        helper.feedPayload("REQCONFIRMSHIPPING","0");
        helper.feedPayload("NOSHIPPING","1");
        helper.feedPayload("BRANDNAME",brandName);

        helper.feedPayload("RETURNURL",succUrl);
        helper.feedPayload("CANCELURL",failUrl);
        helper.feedPayload("NOTETOBUYER",noteToUser);

        helper.feedPayload("PAYMENTREQUEST_0_PAYMENTACTION","Sale");
        helper.feedPayload("PAYMENTREQUEST_0_PAYMENTREASON","None");

        helper.feedPayload("PAYMENTREQUEST_0_INVNUM",invNum);
        helper.feedPayload("PAYMENTREQUEST_0_AMT",String.valueOf(stuffPrice*stuffQuantity));

        helper.feedPayload("L_PAYMENTREQUEST_0_NAME0",stuffName);
        helper.feedPayload("L_PAYMENTREQUEST_0_DESC0",stuffDesc);
        helper.feedPayload("L_PAYMENTREQUEST_0_AMT0",String.valueOf(stuffPrice));
        helper.feedPayload("L_PAYMENTREQUEST_0_QTY0",String.valueOf(stuffQuantity));
        helper.doPostRequest();
        Map<String,String> response = helper.getResponsePair();
        return response;
    }

    public Map<String,String> GetExpressCheckoutDetails(String token, String invNum)
    {
        HttpUtil helper = new HttpUtil(endPoint,codec);
        helper.setupConnection("POST");

        helper.feedPayload("USER",user);
        helper.feedPayload("PWD",password);
        helper.feedPayload("SIGNATURE",signature);
        helper.feedPayload("METHOD","SetExpressCheckout");
        helper.feedPayload("VERSION",API_VERSION);
        helper.feedPayload("TOKEN",token);

        helper.doPostRequest();
        Map<String,String> response = helper.getResponsePair();

        return response;
    }

    public Map<String,String> DoExpressCheckoutPayment(String token,String payerId, String invNum,double totalChargeAmount )
    {
        HttpUtil helper = new HttpUtil(endPoint,codec);
        helper.setupConnection("POST");

        helper.feedPayload("USER",user);
        helper.feedPayload("PWD",password);
        helper.feedPayload("SIGNATURE",signature);
        helper.feedPayload("METHOD","DoExpressCheckoutPayment");
        helper.feedPayload("VERSION",API_VERSION);
        helper.feedPayload("TOKEN",token);
        helper.feedPayload("PAYERID",payerId);
        helper.feedPayload("PAYMENTREQUEST_0_PAYMENTACTION","Sale");

        helper.feedPayload("PAYMENTREQUEST_0_AMT",String.valueOf(totalChargeAmount));
        helper.feedPayload("PAYMENTREQUEST_0_ITEMAMT",String.valueOf(totalChargeAmount));

        helper.doPostRequest();
        Map<String,String> response = helper.getResponsePair();

        return response;
    }

    public String getPayPalAddr() {
        return PayPalAddr;
    }
}
