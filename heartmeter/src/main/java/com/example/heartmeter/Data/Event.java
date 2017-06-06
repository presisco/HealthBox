package com.example.heartmeter.Data;

import com.presisco.shared.data.BaseEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by presisco on 2017/4/19.
 */

public class Event extends BaseEvent {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd-HH:mm");

    public static final String TYPE_DEFAULT = "default";
    public static final String TYPE_AEROBIC = "aerobic";
    public static final String TYPE_ANAEROBIC = "anaerobic";
    public static final String TYPE_SLEEP = "sleep";

    public static final int ANALYSE_RATE_DEFAULT = 1;
    public static final int ANALYSE_RATE_AEROBIC = 1;
    public static final int ANALYSE_RATE_ANAEROBIC = 1;
    public static final int ANALYSE_RATE_SLEEP = 1;

    public Event() {

    }

    public Event(String _type) {
        this(_type, DATE_FORMAT.format(new Date(System.currentTimeMillis())));
    }

    public Event(String _type, String _time) {
        type = _type;
        start_time = _time;
        switch (type) {
            case TYPE_DEFAULT:
                analyse_rate = ANALYSE_RATE_DEFAULT;
                break;
            case TYPE_AEROBIC:
                analyse_rate = ANALYSE_RATE_AEROBIC;
                break;
            case TYPE_ANAEROBIC:
                analyse_rate = ANALYSE_RATE_ANAEROBIC;
                break;
            case TYPE_SLEEP:
                analyse_rate = ANALYSE_RATE_SLEEP;
                break;
            default:
                analyse_rate = ANALYSE_RATE_DEFAULT;
        }
    }
}
