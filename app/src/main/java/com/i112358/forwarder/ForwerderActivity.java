package com.i112358.forwarder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ForwerderActivity extends AppCompatActivity {


    final private int REQUEST_CODE_ASK_PERMISSIONS = 7632;

    Switch m_forwarderEnable = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(Utility.TAG, "onCreate called");
        setContentView(R.layout.activity_forwerder);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.w(Utility.TAG, "onResume called");

        m_forwarderEnable = ((Switch)findViewById(R.id.messages_toggler));

        initRecipientField();
        initTextField(R.id.senderField, Utility.SENDER_ADDRESS, false);
        initTextField(R.id.passwordField, Utility.SENDER_PASSWORD, false);

        initPasswordField();

        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean messagesState = _preferences.getBoolean(Utility.SMS_FORWARD_ENABLE, false);
        m_forwarderEnable.setChecked(messagesState);
        if ( messagesState ) {
            requestPermission();
        }
        requestBattaryOptimization();

        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();
        if ( ni == null || ni.isConnected() )
        {
            setInfoMessage("Check connection");
        }
    }

    private void requestBattaryOptimization() {

        Button _battaryButton = (Button)findViewById(R.id.battaryButton);

        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ) {

            String packageName = getApplicationContext().getPackageName();
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if ( pm.isIgnoringBatteryOptimizations(packageName) ) {
                _battaryButton.setText("Check for battary settings");
                _battaryButton.setTextColor(0xde000000);
            }
            else {
                _battaryButton.setTextColor(Color.RED);
                _battaryButton.setText("Disable doze mode");
            }
        } else {
            _battaryButton.setVisibility(View.GONE);
        }
    }

    private void requestPermission() {
        int _hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if ( _hasPermission == PackageManager.PERMISSION_GRANTED ) {
            boolean _forwarderEnabled = m_forwarderEnable.isChecked();
            SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            _preferences.edit().putBoolean(Utility.SMS_FORWARD_ENABLE, _forwarderEnabled).apply();
            startService(new Intent(this, ForwarderService.class));
        } else {
            ActivityCompat.requestPermissions(ForwerderActivity.this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    _preferences.edit().putBoolean(Utility.SMS_FORWARD_ENABLE, true).apply();
                    m_forwarderEnable.setChecked(true);
                    Toast.makeText(ForwerderActivity.this, "Permission Granted", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    _preferences.edit().putBoolean(Utility.SMS_FORWARD_ENABLE, false).apply();
                    m_forwarderEnable.setChecked(false);
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
        else {
            SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            _preferences.edit().putBoolean(Utility.SMS_FORWARD_ENABLE, false).apply();
            m_forwarderEnable.setChecked(false);
        }
    }

    public void onBattarySettings( View view )
    {
        Log.d(Utility.TAG, "change battary settings");
        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ) {
            Intent intent = new Intent();
            String packageName = getApplicationContext().getPackageName();
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if ( pm.isIgnoringBatteryOptimizations(packageName) ) {
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }
            else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));

            }
            getApplicationContext().startActivity(intent);
        }
    }

    private void setInfoMessage( final String userMessage )
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        TextView _messageText = (TextView)findViewById(R.id.errorView);
        if ( userMessage.isEmpty()) {
            String _message = _preferences.getString(Utility.ERROR_MESSAGE, "");
            if ( _message.isEmpty() ) {
                long _lastMessageTime = _preferences.getLong(Utility.LAST_MESSAGE_TIME, 0);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
                _message = "Last sent message " + formatter.format(new Date(_lastMessageTime));
            }
            _messageText.setText(_message);
        } else {
            _messageText.setVisibility(View.VISIBLE);
            _messageText.setText(userMessage);
        }
    }

    private void initPasswordField() {
        EditText password = (EditText) findViewById(R.id.passwordField);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
    }

    private void initTextField( final int fieldId, final String savedTag, final boolean validate )
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        EditText _editableText = (EditText)findViewById(fieldId);
        _editableText.setText(_preferences.getString(savedTag, ""));

        _editableText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                _preferences.edit().putString(savedTag, s.toString()).apply();

                if ( validate )
                    isValidEmail(true);
            }
        });

        if ( validate )
            isValidEmail(true);
    }

    private void initRecipientField()
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        EditText _recipient = (EditText)findViewById(R.id.recipientField);
        _recipient.setText(_preferences.getString(Utility.RECIPIENT_ADDRESS, ""));

        _recipient.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                _preferences.edit().putString(Utility.RECIPIENT_ADDRESS, s.toString()).apply();
                isValidEmail(true);
            }
        });

        isValidEmail(true);
    }

    public final boolean isValidEmail( boolean showError )
    {
        SharedPreferences _preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String target = _preferences.getString(Utility.RECIPIENT_ADDRESS, "");
        Log.i(Utility.TAG, "check " + target);
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
