package com.bk.sunwidgt.lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * Sync from http://www.esrl.noaa.gov/gmd/grad/solcalc/
 * 
 * */

public class SunCalculator {
    
    public final static String TAG = SunCalculator.class.getSimpleName();
    
    static double getJD(double docyear,double docmonth,double docday) {
        
        if (docmonth <= 2) {
            docyear -= 1.0;
            docmonth += 12.0;
        }
        
        final double A = Math.floor(docyear/100.0);
        final double B = 2.0 - A + Math.floor(A/4.0);
        final double JD = Math.floor(365.25*(docyear + 4716.0)) + Math.floor(30.6001*(docmonth+1.0)) + docday + B - 1524.5;
        
        return JD;

    }
    
    
    static double getTimeLocal(double dochr,double docmn,double docsc) {
        return dochr * 60 + docmn + docsc/ 60.0;
    }
    
    static double calcTimeJulianCent(double jd) {
        return (jd - 2451545.0)/36525.0;
    }
    
    static double calcJDFromJulianCent(double t) {
        return t * 36525.0 + 2451545.0;
    }

    static boolean isLeapYear(double yr)  {
      return ((yr % 4 == 0 && yr % 100 != 0) || yr % 400 == 0);
    }
    
    static double calcDoyFromJD(double jd) {
      double z = Math.floor(jd + 0.5);
      double f = (jd + 0.5) - z;
      double A;
      if (z < 2299161) {
          A = z;
      } else {
        double alpha = Math.floor((z - 1867216.25)/36524.25);
        A = z + 1 + alpha - Math.floor(alpha/4);
      }
      double B = A + 1524;
      double C = Math.floor((B - 122.1)/365.25);
      double D = Math.floor(365.25 * C);
      double E = Math.floor((B - D)/30.6001);
      double day = B - D - Math.floor(30.6001 * E) + f;
      double month = (E < 14) ? E - 1 : E - 13;
      double year = (month > 2) ? C - 4716 : C - 4715;

      double k = (isLeapYear(year) ? 1 : 2);
      double doy = Math.floor((275 * month)/9) - k * Math.floor((month + 9)/12) + day -30;
      return doy;
    }


    static double radToDeg(double angleRad) {
      return (180.0 * angleRad / Math.PI);
    }

    static double degToRad(double angleDeg) {
      return (Math.PI * angleDeg / 180.0);
    }

    static double calcGeomMeanLongSun(double t) {
      double L0 = 280.46646 + t * (36000.76983 + t*(0.0003032));
      while(L0 > 360.0) {
        L0 -= 360.0;
      }
      while(L0 < 0.0) {
        L0 += 360.0;
      }
      return L0;     // in degrees
    }

    static double calcGeomMeanAnomalySun(double t) {
      double M = 357.52911 + t * (35999.05029 - 0.0001537 * t);
      return M;     // in degrees
    }

    static double calcEccentricityEarthOrbit(double t) {
      double e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t);
      return e;     // unitless
    }

   static double calcSunEqOfCenter(double t) {
      double m = calcGeomMeanAnomalySun(t);
      double mrad = degToRad(m);
      double sinm = Math.sin(mrad);
      double sin2m = Math.sin(mrad+mrad);
      double sin3m = Math.sin(mrad+mrad+mrad);
      double C = sinm * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin2m * (0.019993 - 0.000101 * t) + sin3m * 0.000289;
      return C;     // in degrees
    }

    static double calcSunTrueLong(double t) {
      double l0 = calcGeomMeanLongSun(t);
      double c = calcSunEqOfCenter(t);
      double O = l0 + c;
      return O;     // in degrees
    }

    static double calcSunTrueAnomaly(double t) {
      double m = calcGeomMeanAnomalySun(t);
      double c = calcSunEqOfCenter(t);
      double v = m + c;
      return v;     // in degrees
    }

    static double calcSunRadVector(double t) {
      double v = calcSunTrueAnomaly(t);
      double e = calcEccentricityEarthOrbit(t);
      double R = (1.000001018 * (1 - e * e)) / (1 + e * Math.cos(degToRad(v)));
      return R;     // in AUs
    }

    static double calcSunApparentLong(double t) {
      double o = calcSunTrueLong(t);
      double omega = 125.04 - 1934.136 * t;
      double lambda = o - 0.00569 - 0.00478 * Math.sin(degToRad(omega));
      return lambda;        // in degrees
    }

    static double calcMeanObliquityOfEcliptic(double t) {
      double seconds = 21.448 - t*(46.8150 + t*(0.00059 - t*(0.001813)));
      double e0 = 23.0 + (26.0 + (seconds/60.0))/60.0;
      return e0;        // in degrees
    }

    static double calcObliquityCorrection(double t) {
      double e0 = calcMeanObliquityOfEcliptic(t);
      double omega = 125.04 - 1934.136 * t;
      double e = e0 + 0.00256 * Math.cos(degToRad(omega));
      return e;     // in degrees
    }
    
    static double calcSunRtAscension(double t) {
      double e = calcObliquityCorrection(t);
      double lambda = calcSunApparentLong(t);
      double tananum = (Math.cos(degToRad(e)) * Math.sin(degToRad(lambda)));
      double tanadenom = (Math.cos(degToRad(lambda)));
      double alpha = radToDeg(Math.atan2(tananum, tanadenom));
      return alpha;     // in degrees
    }

    static double calcSunDeclination(double t) {
      double e = calcObliquityCorrection(t);
      double lambda = calcSunApparentLong(t);

      double sint = Math.sin(degToRad(e)) * Math.sin(degToRad(lambda));
      double theta = radToDeg(Math.asin(sint));
      return theta;     // in degrees
    }

    static double calcEquationOfTime(double t) {
      double epsilon = calcObliquityCorrection(t);
      double l0 = calcGeomMeanLongSun(t);
      double e = calcEccentricityEarthOrbit(t);
      double m = calcGeomMeanAnomalySun(t);

      double y = Math.tan(degToRad(epsilon)/2.0);
      y *= y;

      double sin2l0 = Math.sin(2.0 * degToRad(l0));
      double sinm   = Math.sin(degToRad(m));
      double cos2l0 = Math.cos(2.0 * degToRad(l0));
      double sin4l0 = Math.sin(4.0 * degToRad(l0));
      double sin2m  = Math.sin(2.0 * degToRad(m));

      double Etime = y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0 - 0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m;
      return radToDeg(Etime)*4.0;   // in minutes of time
    }

    static double calcHourAngleSunrise(double lat, double solarDec) {
      double latRad = degToRad(lat);
      double sdRad  = degToRad(solarDec);
      double HAarg = (Math.cos(degToRad(90.833))/(Math.cos(latRad)*Math.cos(sdRad))-Math.tan(latRad) * Math.tan(sdRad));
      double HA = Math.acos(HAarg);
      return HA;        // in radians (for sunset, use -HA)
    }
    
    static double calcSunriseSetUTC(boolean rise, double JD, double latitude, double longitude) {
        double t = calcTimeJulianCent(JD);
        double eqTime = calcEquationOfTime(t);
        double solarDec = calcSunDeclination(t);
        double hourAngle = calcHourAngleSunrise(latitude, solarDec);

        if (!rise) {
            hourAngle = -hourAngle;
        }
        
        double delta = longitude + radToDeg(hourAngle);
        double timeUTC = 720 - (4.0 * delta) - eqTime;   // in minutes
        return timeUTC;
    }
    
    static void dayString(double jd,Calendar cal) {
    // returns a string in the form DDMMMYYYY[ next] to display prev/next rise/set
    // flag=2 for DD MMM, 3 for DD MM YYYY, 4 for DDMMYYYY next/prev
      if ( (jd < 900000) || (jd > 2817000) ) {
        throw new IllegalArgumentException("jd is incorrect");
      } else {
          double z = Math.floor(jd + 0.5);
          double f = (jd + 0.5) - z;
          double A;
          if (z < 2299161) {
            A = z;
          } else {
            double alpha = Math.floor((z - 1867216.25)/36524.25);
            A = z + 1 + alpha - Math.floor(alpha/4);
          }
          double B = A + 1524;
          double C = Math.floor((B - 122.1)/365.25);
          double D = Math.floor(365.25 * C);
          double E = Math.floor((B - D)/30.6001);
          double day = B - D - Math.floor(30.6001 * E) + f;
          double month = (E < 14) ? E - 1 : E - 13;
          double year = ((month > 2) ? C - 4716 : C - 4715);
          
          if(year < 1900 || month < 1 || month > 11 || day < 0 || day > 31) {
              throw new IllegalArgumentException("incorrect date year=" + year + " month=" + month + " day=" + day);
          }
          
          cal.set(Calendar.YEAR, (int) year);
          cal.set(Calendar.MONTH,(int) (month-1.0));
          cal.set(Calendar.DAY_OF_MONTH,(int) day);
      }
    }
    
    static void timeString(double minutes,Calendar cal) {
     // timeString returns a zero-padded string (HH:MM:SS) given time in minutes
     // flag=2 for HH:MM, 3 for HH:MM:SS
       if ( (minutes >= 0) && (minutes < 1440) ) {
         double floatHour = minutes / 60.0;
         double hour = Math.floor(floatHour);
         double floatMinute = 60.0 * (floatHour - Math.floor(floatHour));
         double minute = Math.floor(floatMinute);
         double floatSec = 60.0 * (floatMinute - Math.floor(floatMinute));
         double second = Math.floor(floatSec + 0.5);
         if (second > 59) {
           second = 0;
           minute += 1;
         }
         
         cal.set(Calendar.HOUR_OF_DAY, (int) hour);
         cal.set(Calendar.MINUTE, (int) minute);
         cal.set(Calendar.SECOND, (int) second);
         
       } else { 
         throw new IllegalArgumentException("incorrect minutes");
       }
 
     }
    
    static double calcJDofNextPrevRiseSet(boolean next, 
                                            boolean rise, 
                                            double JD, 
                                            double latitude, 
                                            double longitude, 
                                            double tz, 
                                            boolean dst) {
      double julianday = JD;
      double increment = ((next) ? 1.0 : -1.0);

      double time = calcSunriseSetUTC(rise, julianday, latitude, longitude);
      while(Double.isNaN(time)){
        julianday += increment;
        time = calcSunriseSetUTC(rise, julianday, latitude, longitude);
      }
      double timeLocal = time + tz * 60.0 + ((dst) ? 60.0 : 0.0);
      while ((timeLocal < 0.0) || (timeLocal >= 1440.0))
      {
        double incr = ((timeLocal < 0) ? 1 : -1);
        timeLocal += (incr * 1440.0);
        julianday -= incr;
      }
      return julianday;
    }
    
    static double calcAzEl(boolean output,double T,double localtime, double latitude, double longitude,double zone)
    {
      double eqTime = calcEquationOfTime(T);
      double theta  = calcSunDeclination(T);
      if (output) {
       // document.getElementById("eqtbox").value = Math.floor(eqTime*100 +0.5)/100.0
        //document.getElementById("sdbox").value = Math.floor(theta*100+0.5)/100.0
      }
      double solarTimeFix = eqTime + 4.0 * longitude - 60.0 * zone;
      double earthRadVec = calcSunRadVector(T);
              double trueSolarTime = localtime + solarTimeFix;
      while (trueSolarTime > 1440)
      {
        trueSolarTime -= 1440;
      }
      double hourAngle = trueSolarTime / 4.0 - 180.0;
      if (hourAngle < -180) 
      {
        hourAngle += 360.0;
      }
      double haRad = degToRad(hourAngle);
              double csz = Math.sin(degToRad(latitude)) * Math.sin(degToRad(theta)) + Math.cos(degToRad(latitude)) * Math.cos(degToRad(theta)) * Math.cos(haRad);
      if (csz > 1.0) 
      {
        csz = 1.0;
      } else if (csz < -1.0) 
      { 
        csz = -1.0;
      }
      double zenith = radToDeg(Math.acos(csz));
      double azDenom = ( Math.cos(degToRad(latitude)) * Math.sin(degToRad(zenith)) );
      double azRad;
      double azimuth;
      if (Math.abs(azDenom) > 0.001) {
        azRad = (( Math.sin(degToRad(latitude)) * Math.cos(degToRad(zenith)) ) - Math.sin(degToRad(theta))) / azDenom;
        if (Math.abs(azRad) > 1.0) {
          if (azRad < 0) {
        azRad = -1.0;
          } else {
        azRad = 1.0;
          }
        }
        azimuth = 180.0 - radToDeg(Math.acos(azRad));
        if (hourAngle > 0.0) {
          azimuth = -azimuth;
        }
      } else {
        if (latitude > 0.0) {
          azimuth = 180.0;
        } else { 
          azimuth = 0.0;
        }
      }
      if (azimuth < 0.0) {
        azimuth += 360.0;
      }
      double exoatmElevation = 90.0 - zenith;

    // Atmospheric Refraction correction
      double refractionCorrection;
      if (exoatmElevation > 85.0) {
        refractionCorrection = 0.0;
      } else {
        double te = Math.tan (degToRad(exoatmElevation));
        if (exoatmElevation > 5.0) {
          refractionCorrection = 58.1 / te - 0.07 / (te*te*te) + 0.000086 / (te*te*te*te*te);
        } else if (exoatmElevation > -0.575) {
          refractionCorrection = 1735.0 + exoatmElevation * (-518.2 + exoatmElevation * (103.4 + exoatmElevation * (-12.79 + exoatmElevation * 0.711) ) );
        } else {
          refractionCorrection = -20.774 / te;
        }
        refractionCorrection = refractionCorrection / 3600.0;
      }

      double solarZen = zenith - refractionCorrection;
      /*
      if ((output) && (solarZen > 108.0) ) {
        document.getElementById("azbox").value = "dark"
        document.getElementById("elbox").value = "dark"
      } else if (output) {
        document.getElementById("azbox").value = Math.floor(azimuth*100 +0.5)/100.0
        document.getElementById("elbox").value = Math.floor((90.0-solarZen)*100+0.5)/100.0
        if (document.getElementById("showae").checked) {
          showLineGeodesic("#ffff00", azimuth)
        }
      }*/
      return (azimuth);
    }
    
    static double calcSunriseSunsetAZEL(boolean rise,
            double JD,
            double latitude,
            double longitude,
            double timezone,
            boolean dst) {
        
        double timeUTC = calcSunriseSetUTC(rise, JD, latitude, longitude);
        double newTimeUTC = calcSunriseSetUTC(rise, JD + timeUTC/1440.0, latitude, longitude); 
        
        if (!Double.isNaN(newTimeUTC)) {
            double timeLocal = newTimeUTC + (timezone * 60.0);
            double riseT = calcTimeJulianCent(JD + newTimeUTC/1440.0);
            double riseAz = calcAzEl(false, riseT, timeLocal, latitude, longitude, timezone);
            
            return riseAz;
        }
        else {
            return Double.NaN;
        }
    }

    static Date calcSunriseSet(boolean rise,
            double JD,
            double latitude,
            double longitude,
            double timezone,
            boolean dst) {
        
//      //var id = ((rise) ? "risebox" : "setbox")
        Calendar suntime = Calendar.getInstance();
        
        suntime.clear();
        
       double timeUTC = calcSunriseSetUTC(rise, JD, latitude, longitude);
       double newTimeUTC = calcSunriseSetUTC(rise, JD + timeUTC/1440.0, latitude, longitude); 
       if (!Double.isNaN(newTimeUTC)) {
         double timeLocal = newTimeUTC + (timezone * 60.0);

         /*if (document.getElementById(rise ? "showsr" : "showss").checked) {
           var riseT = calcTimeJulianCent(JD + newTimeUTC/1440.0)
           var riseAz = calcAzEl(0, riseT, timeLocal, latitude, longitude, timezone)
           showLineGeodesic(rise ? "#66ff00" : "#ff0000", riseAz)
         }*/
         timeLocal += ((dst) ? 60.0 : 0.0);
         if ( (timeLocal >= 0.0) && (timeLocal < 1440.0) ) {
             timeString(timeLocal,suntime);
             dayString(JD,suntime);
             //document.getElementById(id).value = timeString(timeLocal,2)
         } else  {
            double jday = JD;
            double increment = ((timeLocal < 0) ? 1 : -1);
            while ((timeLocal < 0.0)||(timeLocal >= 1440.0)) {
             timeLocal += increment * 1440.0;
             jday -= increment;
            }
            timeString(timeLocal,suntime);
            dayString(jday,suntime);
           //document.getElementById(id).value = timeDateString(jday,timeLocal)
         }
       } else { // no sunrise/set found
         double doy = calcDoyFromJD(JD);
         double jdy;
         if ( ((latitude > 66.4) && (doy > 79) && (doy < 267)) ||
         ((latitude < -66.4) && ((doy < 83) || (doy > 263))) ) {
             //previous sunrise/next sunset
           if (rise) { // find previous sunrise
             jdy = calcJDofNextPrevRiseSet(false, rise, JD, latitude, longitude, timezone, dst);
           } else { // find next sunset
             jdy = calcJDofNextPrevRiseSet(true, rise, JD, latitude, longitude, timezone, dst);
           }
           dayString(jdy,suntime);
           //document.getElementById(((rise)? "risebox":"setbox")).value = dayString(jdy,0,3)
         } else {   //previous sunset/next sunrise
           if (rise) { // find previous sunrise
             jdy = calcJDofNextPrevRiseSet(true, rise, JD, latitude, longitude, timezone, dst);
           } else { // find next sunset
             jdy = calcJDofNextPrevRiseSet(false, rise, JD, latitude, longitude, timezone, dst);
           }
           dayString(jdy,suntime);
           //document.getElementById(((rise)? "risebox":"setbox")).value = dayString(jdy,0,3)
         }
       }
       
       return suntime.getTime();
     }
    
    public static class SunriseSunset {
        public final Date sunrise;
        public final Date sunset;
        public final double sunrise_azel;
        public final double sunset_azel;
        public SunriseSunset(Date sunrise,Date sunset,double sunrise_azel,double sunset_azel) {
            this.sunrise = sunrise;
            this.sunset = sunset;
            this.sunset_azel = sunset_azel;
            this.sunrise_azel = sunrise_azel;
        }
    }
        
    public static SunriseSunset getSunriseSunset(Calendar cal,double lat,double lng,boolean dst) {
        final double jday = getJD(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1.0,cal.get(Calendar.DAY_OF_MONTH));
        
        final double tl = getTimeLocal(cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND));
        
        final double tz = cal.get(Calendar.ZONE_OFFSET) / (60.0 * 60.0 * 1000.0);
        
        final double total = jday + tl/1440.0 - tz/24.0;
        
        final double T = calcTimeJulianCent(total);
        
        //final double lat = 25.045792;
        //final double lng = 121.453857;
        
        Date sunrise = calcSunriseSet(true, jday, lat, lng, tz, false);
        Date sunset  = calcSunriseSet(false, jday, lat, lng, tz, false);
        double sunrise_azel= calcSunriseSunsetAZEL(true, jday, lat, lng, tz, false);
        double sunset_azel = calcSunriseSunsetAZEL(false, jday, lat, lng, tz, false);
        return new SunriseSunset(sunrise,sunset,sunrise_azel,sunset_azel);
    }
    
    //for test
    /*
    public static void main(String[] args) {
        final Calendar cal = Calendar.getInstance();
        //final TimeZone timeZone = cal.getTimeZone();
        
        //cal.set(2012, 3-1, 27, 02, 34, 44);
        
        final SunriseSunset answer = getSunriseSunset(cal,25.045792,121.453857,false);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        System.out.println("sunrise=" + fmt.format(answer.sunrise));
        System.out.println("sunset=" + fmt.format(answer.sunset));
        
        
        
    }
    */
}
