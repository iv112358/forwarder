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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ForwerderActivity extends AppCompatActivity {

    final String TAG = "Forwarder";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 7632;
    final String SMS_FORWARD_ENABLE = "sms_enabled";
    final String RECIPIENT_ADDRESS = "recipient_address";

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

        initRecipientField();

        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean messagesState = _preferences.getBoolean(SMS_FORWARD_ENABLE, false);
        ((Switch)findViewById(R.id.messages_toggler)).setChecked(messagesState);
        if ( messagesState ) {
            requestPermission();
        }
    }

    private void requestPermission() {
        int _hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if ( _hasPermission == PackageManager.PERMISSION_GRANTED ) {
            startService(new Intent(this, ForwarderService.class));
        } else {
            ((Switch)findViewById(R.id.messages_toggler)).setChecked(false);
            ActivityCompat.requestPermissions(ForwerderActivity.this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    private void setInfoMessage( final String message )
    {
        TextView _messageText = (TextView)findViewById(R.id.errorView);
        if ( message.isEmpty()) {
            _messageText.setVisibility(View.GONE);
        } else {
            _messageText.setVisibility(View.VISIBLE);
            _messageText.setText(message);
        }
    }

    private void initRecipientField()
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        EditText _recipient = (EditText)findViewById(R.id.recipientField);
        _recipient.setText(_preferences.getString(RECIPIENT_ADDRESS, ""));

        _recipient.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged " + s);
                SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                _preferences.edit().putString(RECIPIENT_ADDRESS, s.toString()).apply();
                isValidEmail(true);
            }
        });

        isValidEmail(true);
    }

    public final boolean isValidEmail( boolean showError )
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String target = _preferences.getString(RECIPIENT_ADDRESS, "");
        Log.i(TAG, "check " + target);
        if ( target == null || target.isEmpty() ) {
            if ( showError )
                setInfoMessage("Enter recipient email");
            return false;
        } else {
            boolean result = android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
            if ( showError && !result )
                setInfoMessage("Enter correct email");
            else
                setInfoMessage("");
            return result;
        }
    }
}
