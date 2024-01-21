package com.project.nearby.utils;

import android.location.Location;
import android.text.TextUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Utils {
    public static boolean isValidEmail(String target) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return (TextUtils.isEmpty(target) || !target.matches(emailPattern));
    }


    public static double getDistance(double latA, double lngA, double latB, double lngB){
        Location locationA = new Location("point A");
        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);
        Location locationB = new Location("point B");
        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);
        double distance = locationA.distanceTo(locationB) ;
        return distance;
    }

    public static boolean inTime(long time){
        long hours =  time/3600;
        return hours < 336;
    }

    public static long timeDifference(long time1, long time2){

        return  TimeUnit.MILLISECONDS.toMinutes(time1 - time2);
    }

    public static String convertTime(long time){
        Date date = new Date(time * 1000L);
        Format formatd = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss");
        return formatd.format(date);
    }

    public static String getTimeperiod (long longTime){

        String time="0;min";
        int timeP = (int) longTime;
        if(timeP < 60) {
            return timeP+";min";
        }else if(timeP >= 60){
            timeP = timeP / 60;
            if( timeP == 1){
                return timeP+";hour";
            } else if( timeP <= 24){
                return timeP+";hours";
            }else if (timeP > 24) {
                timeP = timeP / 24;
                if (timeP == 1) {
                    return timeP + ";day";
                } else if (timeP <= 7) {
                    return timeP + ";days";
                } else if (timeP > 7) {
                    timeP = timeP / 7;
                    if (timeP == 1) {
                        return timeP + ";week";
                    } else if (timeP <= 4) {
                        return timeP + ";weeks";
                    } else if (timeP > 4) {
                         timeP = timeP / 4;
                         if (timeP == 1) {
                            return timeP + ";month";
                         } else if (timeP <= 12) {
                            return timeP + ";months";
                         }else if (timeP > 12) {
                             timeP = timeP / 12;
                             if (timeP == 1) {
                                 return timeP + ";year";
                             } else if (timeP > 1) {
                                 return timeP + ";years";
                             }
                         }
                    }
                }
            }
        }
        return time;
    }
}
