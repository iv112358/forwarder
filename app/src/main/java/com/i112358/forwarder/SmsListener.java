package com.i112358.forwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by i112358 on 8/21/17.
 *
 */

public class SmsListener extends BroadcastReceiver {

    final private String TAG = "Forwarder";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if ( intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") ) {
            Bundle bundle = intent.getExtras();
            if ( bundle != null ) {
//                try{
//                    SmsMessage[] msgs;
//                    Object[] pdus = (Object[]) bundle.get("pdus");
//                    msgs = new SmsMessage[pdus.length];
//                    for(int i=0; i<msgs.length; i++){
//                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//                        Log.i(TAG, msgs[i].toString());
//                        Log.i(TAG, "message messagebody " + msgs[i].getDisplayMessageBody());
//                        Log.i(TAG, "message getTimestampMillis " + msgs[i].getTimestampMillis());
//                        Log.i(TAG, "message getDisplayOriginatingAddress " + msgs[i].getDisplayOriginatingAddress());
//                        Log.i(TAG, "message getStatusOnIcc " + msgs[i].getStatusOnIcc());
//                    }
//                } catch ( Exception e ) {
//                    Log.e(TAG,e.getMessage());
//                }

                final Context _context = context;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        _context.startService(new Intent(_context, ForwarderService.class));
                    }
                }, 2000);
            }
        } else if ( intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ) {
            Log.i(Utility.TAG, "boot complete");
            final Context _context = context;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    _context.startService(new Intent(_context, ForwarderService.class));
                }
            }, 2000);
        }
    }
}