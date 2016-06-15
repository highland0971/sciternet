package model;

/**
 * Created by vivia on 2016/4/16.
 */
public enum AUDIT_TYPE {
    /**
     * period is audited daily with expire date and usage limit (month credit)
     * usage_duration is audited daily with expire date and usage limit (year credit)
     * usage is audited by usage limit daily,no expire date limit
     * duration is audited by expire without usage limit
     */
    period,usage,duration,usage_duration
}


