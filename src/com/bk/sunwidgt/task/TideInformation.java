package com.bk.sunwidgt.task;

public class TideInformation {
    public final static int NOSET_INT = -1;
    final int month;
    final int day_of_month;
    final int hours;
    final int mins;
    final int tide_height;
    
    public TideInformation(int month,int day_of_month,int hours,int mins,int tide_height) {
        this.month = month;
        this.day_of_month = day_of_month;
        this.hours = hours;
        this.mins = mins;
        this.tide_height = tide_height;
    }
    
    public TideInformation(int a,int b,boolean isDate) {
        this.month = isDate? a : NOSET_INT;
        this.day_of_month = isDate ? b : NOSET_INT;
        this.hours = !isDate ? a : NOSET_INT;
        this.mins = !isDate ? b : NOSET_INT;
        this.tide_height = NOSET_INT;
    }
    
    public TideInformation(int tide_height) {
        this(NOSET_INT,NOSET_INT,NOSET_INT,NOSET_INT,tide_height);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if(month != NOSET_INT && day_of_month != NOSET_INT) {
            sb.append("D").append((month + 1)).append(" ").append(day_of_month).append(" ");
        }
        
        if(hours != NOSET_INT && mins != NOSET_INT) {
            sb.append("T").append(hours).append(" ").append(mins).append(" ");
        }
        
        if(tide_height != NOSET_INT) {
            sb.append(tide_height).append(" ");
        }
        
        
        return sb.toString();
    }
}
