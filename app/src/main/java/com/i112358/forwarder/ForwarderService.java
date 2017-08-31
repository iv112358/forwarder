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

import java.util.Timer;
import java.util.TimerTask;

public class ForwarderService extends Service {
    final private String TAG = "Forwarder";
    final private String LAST_MESSAGE_TIME = "lastMessageTime";
    final private String MESSAGES_TO_SEND = "messagesToSend";
    final private String REPEAT_DELAY = "on_error_repeat_delay";

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

    void searchNewContent()
    {
        final int _hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if ( _hasPermission != PackageManager.PERMISSION_GRANTED ) {
            Log.e(TAG, "No permission granted");
            stopSelf();
            return;
        }

        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean _messagesState = _preferences.getBoolean(Utility.SMS_FORWARD_ENABLE, false);
        if ( !_messagesState ) {
            Log.w(TAG, "Forwarder is disabled");
            stopSelf();
            return;
        }

        serchNewMessages();

        final String _newMessages = _preferences.getString(MESSAGES_TO_SEND, "");
        if ( !_newMessages.isEmpty() ) {
            Log.i(TAG, "try to Send messages " + _newMessages.length());
            new Thread(new Runnable() {
                public void run() {
                    boolean sentResult = true;
                    try {
                        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

                        MailSender sender = new MailSender(
                                "iv7enov@gmail.com",
                                "4ZyRBDQg%QQ");

                        String _recipient = _preferences.getString(Utility.RECIPIENT_ADDRESS, "");
                        sentResult = sender.sendMail("Test mail", _newMessages, "iv7enov@gmail.com", _recipient);
                    } catch (Exception e) {
                        sentResult = false;
                        Log.e("Forwarder", e.toString());
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                    } finally {
                        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                        if ( sentResult ) {
                            Log.i(TAG, "Messages sent successfully");
                            _preferences.edit().remove(MESSAGES_TO_SEND).apply();
                            stopSelf();
                        } else {
                            Log.w(TAG, "Messages NOT sent. Trying to repeat");
                            final long repeatTime = _preferences.getLong(REPEAT_DELAY, 60000);
                            final Context _context = ForwarderService.this;
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    _context.startService(new Intent(_context, ForwarderService.class));
                                }
                            }, repeatTime);
                        }
                    }
                }
            }).start();
        } else {
            Log.w(TAG, "No new messages");
            stopSelf();
        }
    }

    void serchNewMessages()
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String _newMessagesToSend = _preferences.getString(MESSAGES_TO_SEND, "");
        long previousSmsTime = _preferences.getLong(LAST_MESSAGE_TIME, 0);
        long _newSmsTimeForSave = previousSmsTime;
        Log.i(TAG, "Last message saved time " + previousSmsTime);

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        StringBuilder _messageBuilder = new StringBuilder();
        int _counter = 0;
        int _totalMessages = 0;

        if ( cursor != null && cursor.getCount() > 0 ) {
            if ( cursor.moveToFirst() ) {
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
            }
            cursor.close();
        }
        _newMessagesToSend += _messageBuilder.toString();

        SharedPreferences.Editor _editor = _preferences.edit();
        _editor.putString(MESSAGES_TO_SEND, _newMessagesToSend);
        _editor.putLong(LAST_MESSAGE_TIME, _newSmsTimeForSave);
        _editor.apply();

        Log.i(TAG, "Found new messages: " + _counter + "/" + _totalMessages);
        Log.i(TAG, "Update last message time " + _newSmsTimeForSave);
    }
}
