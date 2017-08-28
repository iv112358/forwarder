package com.i112358.forwarder;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by admin on 7/11/17.
 *
 */

class Sms {

    Sms()
    {
        m_id = "";
        m_address = "";
        m_msg = "";
        m_readState = "-1";
        m_time = "";
        m_timeLong = 0;
    }
    private String m_id;
    public String getId() { return m_id; }
    public void setId( final String id ) { m_id = id; }

    private String m_address;
    public String getAddress() { return m_address; }
    public void setAddress( final String address ) { m_address = address; }

    private String m_msg;
    public String getMsg() { return m_msg; }
    public void setMsg( final String msg ) { m_msg = msg; }

    private String m_readState; // "0" / "1" read sms
    public String getReadState() { return m_readState; }
    public void setReadState( final String readState ) { m_readState = readState; }

    private String m_time;
    public String getTime() { return m_time; }
    public void setTime( final String time ) {
        m_timeLong = Long.parseLong(time);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
        m_time = formatter.format(new Date(m_timeLong));
    }

    private long m_timeLong;
    public long getTimeLong() { return m_timeLong; }


    public void toLog()
    {
        Log.i("info", "sms:\n" + makeString());
    }

    public String makeString()
    {
        StringBuilder _builder = new StringBuilder();

        _builder.append("[\n");

        _builder.append("\tid : ");
        _builder.append(m_id);

        _builder.append("\n");
        _builder.append("\tsender : ");
        _builder.append(m_address);

        _builder.append("\n");
        _builder.append("\tis readed : ");
        _builder.append(m_readState);

        _builder.append("\n");
        _builder.append("\ttime : ");
        _builder.append(m_time);

        _builder.append("\n");
        _builder.append("\tmessage : ");
        _builder.append(m_msg);

        _builder.append("\n]");

        _builder.append("\n");

        return _builder.toString();
    }
}
