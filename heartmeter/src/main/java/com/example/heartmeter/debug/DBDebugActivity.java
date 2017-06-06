package com.example.heartmeter.debug;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.heartmeter.Data.Event;
import com.example.heartmeter.Data.EventData;
import com.example.heartmeter.R;
import com.example.heartmeter.storage.SQLiteManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DBDebugActivity extends AppCompatActivity {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    private Spinner mModeSpinner;
    private EditText mCountEdit;
    private EditText mRanBaseEdit;
    private EditText mRanBiasEdit;
    private EditText mMinDateEdit;
    private EditText mMaxDateEdit;
    private EditText mDurationEdit;
    private EditText mDurationBiasEdit;

    private SQLiteManager mDataManager;

    private Executor mExecutor;

    private ProgressDialog mGenerateProgress;
    private ProgressDialog mEraseProgress;

    private EditText findEdit(int resId) {
        return (EditText) findViewById(resId);
    }

    private void findWidgets() {
        mModeSpinner = (Spinner) findViewById(R.id.spinnerMode);
        mCountEdit = findEdit(R.id.editCount);
        mRanBaseEdit = findEdit(R.id.editRanBase);
        mRanBiasEdit = findEdit(R.id.editRanBias);
        mMinDateEdit = findEdit(R.id.editMinDate);
        mMaxDateEdit = findEdit(R.id.editMaxDate);
        mDurationEdit = findEdit(R.id.editDuration);
        mDurationBiasEdit = findEdit(R.id.editDurationBias);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataManager = new SQLiteManager(this);
        mExecutor = Executors.newSingleThreadExecutor();

        setContentView(R.layout.activity_dbdebug);
        findWidgets();

        mGenerateProgress = new ProgressDialog(this);
        mGenerateProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mGenerateProgress.setTitle("生成数据");
        mGenerateProgress.setIndeterminate(false);
        mGenerateProgress.setCancelable(false);

        mEraseProgress = new ProgressDialog(this);
        mEraseProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mEraseProgress.setTitle("删除数据");
        mEraseProgress.setIndeterminate(false);
        mEraseProgress.setCancelable(false);
    }

    public void onGenerate(View v) {
        new GenerateTask().executeOnExecutor(mExecutor);
    }

    public void onEraseMode(View v) {
        new EraseTask().executeOnExecutor(mExecutor);
    }

    private class GenerateTask extends AsyncTask<Void, Integer, Void> {
        String event_type = "";
        int analyse_rate = 1;
        Date min_date;
        Date max_date;
        long date_range;
        long date_base;
        int event_duration_min;
        int event_duration_range;
        int data_base;
        int data_bias;
        int insert_count;

        @Override
        protected void onPreExecute() {
            mGenerateProgress.show();

            int selected_mode_index = mModeSpinner.getSelectedItemPosition();
            switch (selected_mode_index) {
                case 0:
                    event_type = Event.TYPE_DEFAULT;
                    analyse_rate = Event.ANALYSE_RATE_DEFAULT;
                    break;
                case 1:
                    event_type = Event.TYPE_AEROBIC;
                    analyse_rate = Event.ANALYSE_RATE_AEROBIC;
                    break;
                case 2:
                    event_type = Event.TYPE_ANAEROBIC;
                    analyse_rate = Event.ANALYSE_RATE_ANAEROBIC;
                    break;
                case 3:
                    event_type = Event.TYPE_SLEEP;
                    analyse_rate = Event.ANALYSE_RATE_SLEEP;
                    break;
            }
            try {
                min_date = DATE_FORMAT.parse(mMinDateEdit.getText().toString());
                max_date = DATE_FORMAT.parse(mMaxDateEdit.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(DBDebugActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            date_range = max_date.getTime() - min_date.getTime();
            date_base = min_date.getTime();

            event_duration_min = Integer.parseInt(mDurationEdit.getText().toString());
            event_duration_range = Integer.parseInt(mDurationBiasEdit.getText().toString());

            data_base = Integer.parseInt(mRanBaseEdit.getText().toString());
            data_bias = Integer.parseInt(mRanBiasEdit.getText().toString());

            insert_count = Integer.parseInt(mCountEdit.getText().toString());

            mGenerateProgress.setMax(insert_count);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mGenerateProgress.dismiss();
            Toast.makeText(DBDebugActivity.this, "选定数据已生成完毕", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mGenerateProgress.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < insert_count; ++i) {
                publishProgress(i);
                Event random_event = new Event();
                random_event.type = event_type;
                random_event.analyse_rate = analyse_rate;
                random_event.start_time = Event.DATE_FORMAT.format((long) (date_base + (date_range * Math.random())));
                int event_duration = (int) (event_duration_min + event_duration_range * Math.random()) * 60;
                long event_id = mDataManager.addEvent(random_event);
                EventData[] data_set = new EventData[event_duration];
                for (int j = 0; j < event_duration; ++j) {
                    data_set[j] = new EventData();
                    data_set[j].event_id = event_id;
                    data_set[j].heart_rate = (int) (data_base + (data_bias * (Math.random() - 0.5) * 2));
                    data_set[j].offset_time = j;
                }
                mDataManager.addDataToEvent(data_set);
            }
            return null;
        }
    }

    private class EraseTask extends AsyncTask<Void, Integer, Void> {
        String event_type = "";

        @Override
        protected void onPreExecute() {
            mEraseProgress.show();
            int selected_mode_index = mModeSpinner.getSelectedItemPosition();
            switch (selected_mode_index) {
                case 0:
                    event_type = Event.TYPE_DEFAULT;
                    break;
                case 1:
                    event_type = Event.TYPE_AEROBIC;
                    break;
                case 2:
                    event_type = Event.TYPE_ANAEROBIC;
                    break;
                case 3:
                    event_type = Event.TYPE_SLEEP;
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(DBDebugActivity.this, "选定模式数据已清除", Toast.LENGTH_SHORT).show();
            mEraseProgress.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values.length > 1) {
                mEraseProgress.setMax(values[1]);
            } else {
                mEraseProgress.setProgress(values[0]);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            Event[] events = mDataManager.getEventsByType(event_type);
            publishProgress(-1, events.length);
            for (int i = 0; i < events.length; ++i) {
                publishProgress(i);
                mDataManager.deleteEvent(events[i].id);
            }
            return null;
        }
    }
}
