package controllers;

import java.math.BigDecimal;

/**
 * Created by vivia on 2016/5/13.
 */
public class ChargePolicy {

    private static double USD_CNY_Rate = 6.6;
    private static double changeRate = 1;

    private static int [] rangeStepForUsage = {17,24,32,44};

    private static double [] priceStepForUsage = {1.6,1.4,1.2,1};

    private static int [] rangeStepForMonth = {4,7,10,12};

    private static double [] priceStepForMonth = {27.2,23.8,20.4,17};

    private static double priceForYear = 196;


    public static int[] getRangeStepForUsage() {
        return rangeStepForUsage;
    }

    public static double[] getPriceStepForUsage() {
        return priceStepForUsage;
    }

    public static int[] getRangeStepForMonth() {
        return rangeStepForMonth;
    }

    public static double[] getPriceStepForMonth() {
        return priceStepForMonth;
    }

    public static double getPriceForYear() {
        return priceForYear;
    }

    public static double getChargeUnitPrice(String chargeType,int amount,String Currency){

        System.out.println("ChargeType:"+chargeType+" amount:"+amount);

        BigDecimal decimal = new BigDecimal(-1) ;

        if(Currency == "USD")
            changeRate = 1/ getUSD_CNY_Rate();

        switch (chargeType){
            case "month":
            {
                for(int i=0;i<getRangeStepForMonth().length;i++)
                    if (getRangeStepForMonth()[i] >= amount){
                        decimal = new BigDecimal(getPriceStepForMonth()[i] * changeRate);
                        break;
                    }
                if(decimal == new BigDecimal(-1))
                    decimal = new BigDecimal(getPriceStepForMonth()[getPriceStepForMonth().length -1 ] * changeRate);
                break;
            }
            case "usage":
            {
                for(int i=0;i<getRangeStepForUsage().length;i++)
                    if (getRangeStepForUsage()[i] >= amount) {
                        decimal = new BigDecimal(getPriceStepForUsage()[i] * changeRate);
                        break;
                    }
                if(decimal == new BigDecimal(-1))
                    decimal = new BigDecimal(getPriceStepForUsage()[getPriceStepForUsage().length -1 ] * changeRate);
                break;
            }
            case "year":
            {
                decimal = new BigDecimal(getPriceForYear()*changeRate);
                break;
            }
        }
        return decimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    public static double getTotalCharge(String chargeType,int amount,String Currency)
    {
        BigDecimal decimal = new BigDecimal(-1);
        decimal = new BigDecimal(getChargeUnitPrice(chargeType,amount,Currency) * amount);
        return decimal.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();

    }

    public static double getUSD_CNY_Rate() {
        return USD_CNY_Rate;
    }

    public static void setUSD_CNY_Rate(double USD_CNY_Rate) {
        ChargePolicy.USD_CNY_Rate = USD_CNY_Rate;
    }
}
