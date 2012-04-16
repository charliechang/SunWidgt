package com.bk.sunwidgt.lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * 
 * Sync from http://www.lowellhighlandsweather.com/History/mooncalc.html
 * */
public class MoonCalculator {
    
    private final static double PI = Math.PI;
    private final static double DR = PI/180;
    private final static double K1 = 15.0*DR*1.0027379;

    private final static double[] Rise_time = new double[2];
    private final static double[] Set_time  = new double[2];
    private static double Rise_az = Double.NaN;
    private static double Set_az = Double.NaN;
    
    private static boolean Moonrise;
    private static boolean Moonset;
    
    private final static double[] Sky = new double[3];
    private final static double[] RAn = new double[3];
    private final static double[] Dec = new double[3];
    private final static double[] VHz = new double[3];

    
    public static class MoonriseMoonset {
        public final Date moonrise;
        public final Date moonset;
        public final double rise_az;
        public final double set_sz;
        public MoonriseMoonset(Date moonrise,Date moonset,double rise_az,double set_sz) {
           this.moonrise = moonrise;
           this.moonset = moonset;
           this.rise_az = rise_az;
           this.set_sz = set_sz;
        }
        
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append("moonrise=").append(moonrise).append(" ");
            sb.append("moonset=").append(moonset).append(" ");
            sb.append("rise_az=").append(rise_az).append(" ");
            sb.append("set_sz=").append(set_sz).append(" ");
            return sb.toString();
        }
    }
    
 // moon's position using fundamental arguments 
     // (Van Flandern & Pulkkinen, 1979)
     static void moon(double jd ) {
         double d, f, g, h, m, n, s, u, v, w;
    
         h = 0.606434 + 0.03660110129*jd;
         m = 0.374897 + 0.03629164709*jd;
         f = 0.259091 + 0.0367481952 *jd;
         d = 0.827362 + 0.03386319198*jd;
         n = 0.347343 - 0.00014709391*jd;
         g = 0.993126 + 0.0027377785 *jd;
    
         h = h - Math.floor(h);
         m = m - Math.floor(m);
         f = f - Math.floor(f);
         d = d - Math.floor(d);
         n = n - Math.floor(n);
         g = g - Math.floor(g);
    
         h = h*2*PI;
         m = m*2*PI;
         f = f*2*PI;
         d = d*2*PI;
         n = n*2*PI;
         g = g*2*PI;
    
         v = 0.39558*Math.sin(f + n);
         v = v + 0.082  *Math.sin(f);
         v = v + 0.03257*Math.sin(m - f - n);
         v = v + 0.01092*Math.sin(m + f + n);
         v = v + 0.00666*Math.sin(m - f);
         v = v - 0.00644*Math.sin(m + f - 2*d + n);
         v = v - 0.00331*Math.sin(f - 2*d + n);
         v = v - 0.00304*Math.sin(f - 2*d);
         v = v - 0.0024 *Math.sin(m - f - 2*d - n);
         v = v + 0.00226*Math.sin(m + f);
         v = v - 0.00108*Math.sin(m + f - 2*d);
         v = v - 0.00079*Math.sin(f - n);
         v = v + 0.00078*Math.sin(f + 2*d + n);
         
         u = 1 - 0.10828*Math.cos(m);
         u = u - 0.0188 *Math.cos(m - 2*d);
         u = u - 0.01479*Math.cos(2*d);
         u = u + 0.00181*Math.cos(2*m - 2*d);
         u = u - 0.00147*Math.cos(2*m);
         u = u - 0.00105*Math.cos(2*d - g);
         u = u - 0.00075*Math.cos(m - 2*d + g);
         
         w = 0.10478*Math.sin(m);
         w = w - 0.04105*Math.sin(2*f + 2*n);
         w = w - 0.0213 *Math.sin(m - 2*d);
         w = w - 0.01779*Math.sin(2*f + n);
         w = w + 0.01774*Math.sin(n);
         w = w + 0.00987*Math.sin(2*d);
         w = w - 0.00338*Math.sin(m - 2*f - 2*n);
         w = w - 0.00309*Math.sin(g);
         w = w - 0.0019 *Math.sin(2*f);
         w = w - 0.00144*Math.sin(m + n);
         w = w - 0.00144*Math.sin(m - 2*f - n);
         w = w - 0.00113*Math.sin(m + 2*f + 2*n);
         w = w - 0.00094*Math.sin(m - 2*d + g);
         w = w - 0.00092*Math.sin(2*m - 2*d);
    
         s = w/Math.sqrt(u - v*v);                  // compute moon's right ascension ...  
         Sky[0] = h + Math.atan(s/Math.sqrt(1 - s*s));
    
         s = v/Math.sqrt(u);                        // declination ...
         Sky[1] = Math.atan(s/Math.sqrt(1 - s*s));
    
         Sky[2] = 60.40974*Math.sqrt( u );          // and parallax
     }
    
    static double julian_day(Calendar cal)
    {
        double a, b, jd;
        boolean gregorian;

        double month = cal.get(Calendar.MONTH) + 1;
        double day   = cal.get(Calendar.DAY_OF_MONTH);
        double year  = cal.get(Calendar.YEAR);
        
        System.out.println(month + " " + day + " " + year);

        gregorian = (year < 1583) ? false : true;
        
        if ((month == 1)||(month == 2))
        {
            year  = year  - 1;
            month = month + 12;
        }

        a = Math.floor(year/100);
        if (gregorian) b = 2 - a + Math.floor(a/4);
        else           b = 0.0;

        jd = Math.floor(365.25*(year + 4716)) 
           + Math.floor(30.6001*(month + 1)) 
           + day + b - 1524.5;
        
        System.out.println("jd=" + jd);
        return jd;
    }
    
 // Local Sidereal Time for zone
    static double lst(double lon,double jd,double z )
    {
        double s = 24110.5 + 8640184.812999999*jd/36525 + 86636.6*z + 86400*lon;
        s = s/86400.0;
        s = s - Math.floor(s);
        return s*360.0*DR;
    }
    
 // 3-point interpolation
    static double interpolate(double f0,double f1,double f2,double p )
    {
        double a = f1 - f0;
        double b = f2 - f1 - a;
        double f = f0 + p*(2.0*a + b*(2.0*p - 1.0));

        return f;
    }
    
    static int sgn(double x )
    {
        int rv;
        if (x > 0.0)      rv =  1;
        else if (x < 0.0) rv = -1;
        else              rv =  0;
        return rv;
    }
    
    // test an hour for an event
    static double test_moon(double k, double zone,double t0,double lat,double plx )
    {
        double[] ha = new double[3];
        double a, b, c, d, e, s, z;
        double hr, min, time;
        double az, hz, nz, dz;

        if (RAn[2] < RAn[0])
            RAn[2] = RAn[2] + 2.0*PI;
        
        ha[0] = t0 - RAn[0] + k*K1;
        ha[2] = t0 - RAn[2] + k*K1 + K1;
        
        ha[1]  = (ha[2] + ha[0])/2;                // hour angle at half hour
        Dec[1] = (Dec[2] + Dec[0])/2;              // declination at half hour

        s = Math.sin(DR*lat);
        c = Math.cos(DR*lat);

        // refraction + sun semidiameter at horizon + parallax correction
        z = Math.cos(DR*(90.567 - 41.685/plx));

        if (k <= 0)                                // first call of function
            VHz[0] = s*Math.sin(Dec[0]) + c*Math.cos(Dec[0])*Math.cos(ha[0]) - z;

        VHz[2] = s*Math.sin(Dec[2]) + c*Math.cos(Dec[2])*Math.cos(ha[2]) - z;
        
        if (sgn(VHz[0]) == sgn(VHz[2]))
            return VHz[2];                         // no event this hour
        
        VHz[1] = s*Math.sin(Dec[1]) + c*Math.cos(Dec[1])*Math.cos(ha[1]) - z;

        a = 2*VHz[2] - 4*VHz[1] + 2*VHz[0];
        b = 4*VHz[1] - 3*VHz[0] - VHz[2];
        d = b*b - 4*a*VHz[0];

        if (d < 0)
            return VHz[2];                         // no event this hour
        
        d = Math.sqrt(d);
        e = (-b + d)/(2*a);

        if (( e > 1 )||( e < 0 ))
            e = (-b - d)/(2*a);

        time = k + e + 1/120;                      // time of an event + round up
        hr   = Math.floor(time);
        min  = Math.floor((time - hr)*60);

        hz = ha[0] + e*(ha[2] - ha[0]);            // azimuth of the moon at the event
        nz = -Math.cos(Dec[1])*Math.sin(hz);
        dz = c*Math.sin(Dec[1]) - s*Math.cos(Dec[1])*Math.cos(hz);
        az = Math.atan2(nz, dz)/DR;
        if (az < 0) az = az + 360;
        
        if ((VHz[0] < 0)&&(VHz[2] > 0))
        {
            Rise_time[0] = hr;
            Rise_time[1] = min;
            Rise_az = az;
            Moonrise = true;
        }
        
        if ((VHz[0] > 0)&&(VHz[2] < 0))
        {
            Set_time[0] = hr;
            Set_time[1] = min;
            Set_az = az;
            Moonset = true;
        }

        return VHz[2];
    }

    
    synchronized public static MoonriseMoonset getMoonriseMoonset(Calendar cal,double lat,double lon )
    {
        int i, j, k;
        double zone = -cal.get(Calendar.ZONE_OFFSET) / (60.0 * 60.0 * 1000.0);
        double jd = julian_day(cal) - 2451545;           // Julian day relative to Jan 1.5, 2000
        
//        if ((sgn(zone) == sgn(lon))&&(zone != 0))
//            window.alert("WARNING: time zone and longitude are incompatible!");
        double[][] mp = new double[3][3];                     // create a 3x3 array

    /////////// new stuff
        double x = lon;
        //calc.zone.value = Math.round(-x/15);
        zone = Math.round(-x/15);
        
        System.out.println("zone=" + zone);
    ///////////
        lon = lon/360.0;
        double tz = zone/24.0;
        double t0 = lst(lon, jd, tz);                 // local sidereal time
        jd = jd + tz;                              // get moon position at start of day
        for (k = 0; k < 3; k++)
        {
            moon(jd);
            mp[k][0] = Sky[0];
            mp[k][1] = Sky[1];
            mp[k][2] = Sky[2];
            jd = jd + 0.5;      
        }   
        if (mp[1][0] <= mp[0][0])
            mp[1][0] = mp[1][0] + 2*PI;

        if (mp[2][0] <= mp[1][0])
            mp[2][0] = mp[2][0] + 2*PI;

        RAn[0] = mp[0][0];
        Dec[0] = mp[0][1];
        
        Moonrise = false;                          // initialize
        Moonset  = false;
        
        for (k = 0; k < 24; k++)                   // check each hour of this day
        {
            double ph = ((double) k + 1.0)/24.0;
            
            RAn[2] = interpolate(mp[0][0], mp[1][0], mp[2][0], ph);
            Dec[2] = interpolate(mp[0][1], mp[1][1], mp[2][1], ph);
            
            VHz[2] = test_moon(k, zone, t0, lat, mp[1][2]);

            RAn[0] = RAn[2];                       // advance to next hour
            Dec[0] = Dec[2];
            VHz[0] = VHz[2];
        }
        System.out.println();
        // display results
        Calendar risetime = null;
        Calendar settime = null;
        if(Moonrise) {
            risetime = Calendar.getInstance();
            risetime.clear();
            risetime.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), (int) Rise_time[0], (int) Rise_time[1]);

        }
        if(Moonset) {

            settime = Calendar.getInstance();
            settime.clear();
            settime.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), (int) Set_time[0], (int) Set_time[1]);
        }
        return new MoonriseMoonset(null == risetime ? null : risetime.getTime(), null == settime ? null : settime.getTime(),Rise_az,Set_az);
    }
    /*
    public static void main(String[] args) {
        
        //System.out.println(-Calendar.getInstance().get(Calendar.ZONE_OFFSET) / (60.0 * 60.0 * 1000.0));
        SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();
        double lat = 25.05;
        double lng = 121.5;
        
        cal.set(Calendar.YEAR, 2013);
        cal.set(Calendar.MONTH, 12 - 1);
        cal.set(Calendar.DAY_OF_MONTH, 18);
        
        MoonriseMoonset answer = getMoonriseMoonset(cal,lat,lng);
        
        if(answer.moonrise != null) {
            System.out.println("moonrise=" + fmtTime.format(answer.moonrise) + " Rise_az=" + Rise_az);
        }
        if(answer.moonset != null) {
            System.out.println("moonmoonset=" + fmtTime.format(answer.moonset) + " Set_sz=" + Set_az);
        }
    }*/
}
