package controllers;

/**
 * Created by vivia on 2016/5/13.
 */
public class ChargePolicy {

    private static double USD_CNY_Rate = 6.6;
    private static double changeRate = 1;

    private static int [] rangeStepForUsage = {17,24,32,44,50};

    private static double [] priceStepForUsage = {1.6,1.4,1.2,1};

    private static int [] rangeStepForMonth = {1,4,7,10,12};

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

    public static double getTotalCharge(String chargeType,int amount,String Currency)
    {
        if(Currency == "USD")
            changeRate = 1/ getUSD_CNY_Rate();

        switch (chargeType){
            case "MONTH":
            {
                for(int i=0;i<getRangeStepForMonth().length;i++)
                    if (getRangeStepForMonth()[i] >= amount)
                        return amount * getPriceStepForMonth()[i] * changeRate;
                return amount * getPriceStepForMonth()[getPriceStepForMonth().length -1 ] * changeRate;
            }
            case "Usage":
            {
                for(int i=0;i<getRangeStepForUsage().length;i++)
                    if (getRangeStepForUsage()[i] >= amount)
                        return amount * getPriceStepForUsage()[i] * changeRate;
                return amount * getPriceStepForUsage()[getPriceStepForUsage().length -1 ] * changeRate;
            }
            case "Year":
            {
                return amount *getPriceForYear()*changeRate;
            }
        }
        return -1;
    }

    public static double getUSD_CNY_Rate() {
        return USD_CNY_Rate;
    }

    public static void setUSD_CNY_Rate(double USD_CNY_Rate) {
        ChargePolicy.USD_CNY_Rate = USD_CNY_Rate;
    }
}
