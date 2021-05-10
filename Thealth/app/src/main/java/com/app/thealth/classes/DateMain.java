package com.app.thealth.classes;

import android.content.Context;

import com.app.thealth.R;
import com.app.thealth.models.TimeObj;

import java.util.Calendar;
import java.util.Date;

public class DateMain {

    public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String getDateStr(Context context, long timestamp){
        String[] monthes={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String dateStr = "";
        Calendar c = Calendar.getInstance();
        //Set time in milliseconds
        c.setTimeInMillis(timestamp);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMin = c.get(Calendar.MINUTE);

        String datetimeStr = "";
        if(mHour > 12){
            mHour = mHour - 12;
            if(mHour < 10) {
                if(mMin < 10){
                    datetimeStr = " 0" + mHour + ":0" + mMin + " PM";
                }else {
                    datetimeStr = " 0" + mHour + ":" + mMin + " PM";
                }
            }
            else if(mMin < 10){
                datetimeStr = " " + mHour + ":0" + mMin + " PM";
            }
            else if(mHour < 10 && mMin < 10){
                datetimeStr = " 0" + mHour + ":0" + mMin + " PM";
            }
            else {
                if(mMin < 10){
                    datetimeStr = " " + mHour + ":0" + mMin + " PM";
                }else {
                    datetimeStr = " " + mHour + ":" + mMin + " PM";
                }
            }
        }else {
            if(mHour < 10) {
                if(mMin < 10){
                    datetimeStr = " 0" + mHour + ":0" + mMin + " AM";
                }else {
                    datetimeStr = " 0" + mHour + ":" + mMin + " AM";
                }
            }
            else if(mMin < 10){
                datetimeStr = " " + mHour + ":0" + mMin + " AM";
            }
            else if(mHour < 10 && mMin < 10){
                datetimeStr = " 0" + mHour + ":0" + mMin + " AM";
            }
            else {
                if(mMin < 10){
                    datetimeStr = " " + mHour + ":0" + mMin + " AM";
                }
                else {
                    datetimeStr = " " + mHour + ":" + mMin + " AM";
                }
            }
        }

        if(mDay<10)
            dateStr = monthes[mMonth] + " 0" + mDay + ", " + String.valueOf(mYear).substring(2,2) + "'" + datetimeStr;
        else
            dateStr = monthes[mMonth] + " " + mDay + ", " + String.valueOf(mYear).substring(2,2) + "'" + datetimeStr;

        long oneMinuteMilliseconds = 60000;
        long oneHourMilliseconds = 3600000;
        long oneDayMilliseconds = 24*3600*1000;
        long oneMonthMilliseconds = oneDayMilliseconds*30;
        long now = new Date().getTime();

        if(now - timestamp < oneMinuteMilliseconds) dateStr = context.getString(R.string.justnow);
        if(oneMinuteMilliseconds <= now - timestamp && now - timestamp < oneHourMilliseconds) dateStr = String.valueOf((int) (now - timestamp)/(60000)) + "m " + context.getString(R.string.ago);
        if(oneHourMilliseconds <= now - timestamp && now - timestamp < oneDayMilliseconds) dateStr = String.valueOf((int) (now - timestamp)/(3600000)) + "h " + context.getString(R.string.ago);
        if(oneDayMilliseconds <= now - timestamp && now - timestamp < oneMonthMilliseconds) dateStr = String.valueOf((int) (now - timestamp)/(24*3600000)) + "d " + context.getString(R.string.ago);

        return dateStr;
    }

    public static TimeObj getTime(long timestamp){
        TimeObj timeObj = new TimeObj();
        Calendar c = Calendar.getInstance();
        //Set time in milliseconds
        c.setTimeInMillis(timestamp);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMin = c.get(Calendar.MINUTE);
        timeObj.setYear(mYear);
        timeObj.setMonth(mMonth);
        timeObj.setDay(mDay);
        timeObj.setHour(mHour);
        timeObj.setMinute(mMin);
        return timeObj;
    }

}
























