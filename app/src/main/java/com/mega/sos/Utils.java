package com.mega.sos;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Ben-pc on 2017/12/11.
 */

public class Utils {


    public static final String SHARE_ANEM = "sos";
    private static  final int DEFAULT_SMS_INTERVAL = 1000*60 * 3;

    public static int  getSendSMSTimeInterval(Context context){
        SharedPreferences sh = context.getSharedPreferences(SHARE_ANEM, Context.MODE_PRIVATE);
        return sh.getInt("sendSMSTimeInterval", DEFAULT_SMS_INTERVAL);
    }

    public static void  putSendSMSTimeInterval(Context context,int time){
        SharedPreferences sh = context.getSharedPreferences(SHARE_ANEM, Context.MODE_PRIVATE);
       sh.edit().putInt("sendSMSTimeInterval",time).commit();
    }

    public static String  getStringShare(Context context,String key){
        SharedPreferences sh = context.getSharedPreferences(SHARE_ANEM, Context.MODE_PRIVATE);
        return sh.getString(key,"");
    }
}
