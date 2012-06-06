package com.bk.sunwidgt.task;

public class RainInformation {
    public final static int NOSET_INT = -1;
    public final static float NOSET_FLOAT = -1.0f;
    public final static String NOSET_STRING = null;
    public final String location;
    public final float mesure;
    public final int month;
    public final int day_of_month;
    public final int fromHours;
    public final int toHours;

    public RainInformation(String location,int month,int day_of_month,int fromHours,int toHours,float measure) {
        this.location = location;
        this.month = month;
        this.day_of_month = day_of_month;
        this.fromHours = fromHours;
        this.toHours = toHours;
        this.mesure = measure;
    }

    
    public RainInformation(int month,int day_of_month,int fromHours,int toHours) {
        this(NOSET_STRING,month,day_of_month,fromHours,toHours,NOSET_FLOAT);
    }
    
    public RainInformation(int fromHours,int toHours) {
        this(NOSET_STRING,NOSET_INT,NOSET_INT,fromHours,toHours,NOSET_FLOAT);
    }
    
    public RainInformation(float measure) {
        this(NOSET_STRING,NOSET_INT,NOSET_INT,NOSET_INT,NOSET_INT,measure);
    }
    
    public RainInformation() {
        this(NOSET_STRING,NOSET_INT,NOSET_INT,NOSET_INT,NOSET_INT,NOSET_FLOAT);
    }
    
    public RainInformation(String location) {
        this(location,NOSET_INT,NOSET_INT,NOSET_INT,NOSET_INT,NOSET_FLOAT);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if(location != null) {
            sb.append(location);
            sb.append(" ");
        }
        if(month != NOSET_INT) {
            sb.append((month + 1));
            sb.append(" ");            
        }
        if(day_of_month != NOSET_INT) {
            sb.append(day_of_month);
            sb.append(" ");                   
        }
        if(fromHours != NOSET_INT) {
            sb.append(fromHours);
            sb.append(" ");                
        }
        if(toHours != NOSET_INT) {
            sb.append(toHours);
            sb.append(" ");                
        }
        if(mesure != NOSET_FLOAT) {
            sb.append(mesure);
            sb.append(" ");                
        }
        if(0 == sb.length()) {
            sb.append("X");
        }
        return sb.toString();
    }
    
}
