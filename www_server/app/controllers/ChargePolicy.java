package controllers;

/**
 * Created by vivia on 2016/5/13.
 */
public class ChargePolicy {

    private static int [] rangeStepForUsage = {1,17,24,32,44,50};

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
}
