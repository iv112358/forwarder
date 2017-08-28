package com.i112358.forwarder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class ForwerderActivity extends AppCompatActivity {

    final String TAG = "Forwarder";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 7632;
    final String SMS_FORWARD_ENABLE = "sms_enabled";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "onCreate called");
        setContentView(R.layout.activity_forwerder);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.w(TAG, "onResume called");
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean messagesState = _preferences.getBoolean(SMS_FORWARD_ENABLE, false);
        ((Switch)findViewById(R.id.messages_toggler)).setChecked(messagesState);
        requestPermission();
    }

    private void requestPermission() {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean messagesState = _preferences.getBoolean(SMS_FORWARD_ENABLE, false);
        if ( messagesState ) {
            int _hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
            if ( _hasPermission == PackageManager.PERMISSION_GRANTED ) {
                startService(new Intent(this, ForwarderService.class));
            } else {
                _preferences.edit().putBoolean(SMS_FORWARD_ENABLE, false).apply();
                ((Switch)findViewById(R.id.messages_toggler)).setChecked(false);
                ActivityCompat.requestPermissions(ForwerderActivity.this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
            }
        } else {
            Log.w(TAG, "Forwarder is disabled");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(new Intent(this, ForwarderService.class));
                    SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    _preferences.edit().putBoolean(SMS_FORWARD_ENABLE, true).apply();
                    ((Switch)findViewById(R.id.messages_toggler)).setChecked(true);
                    Toast.makeText(ForwerderActivity.this, "Permission Granted", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    _preferences.edit().putBoolean(SMS_FORWARD_ENABLE, false).apply();
                    ((Switch)findViewById(R.id.messages_toggler)).setChecked(false);
                    Toast.makeText(ForwerderActivity.this, "Permission Denied. Activate manually.", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onEnableForward( View view )
    {
        boolean state = ((Switch)view).isChecked();
        if ( state )
            requestPermission();
    }
}
