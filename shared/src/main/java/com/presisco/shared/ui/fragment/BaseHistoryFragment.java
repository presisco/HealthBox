package com.presisco.shared.ui.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.presisco.shared.R;
import com.presisco.shared.data.BaseEvent;
import com.presisco.shared.data.BaseEventData;
import com.presisco.shared.ui.framework.monitor.MonitorHostFragment;
import com.presisco.shared.ui.framework.monitor.MonitorPanelFragment;
import com.presisco.shared.utils.LCAT;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseHistoryFragment extends Fragment implements MonitorPanelFragment.ViewCreatedListener {
    private ActionListener mChildListener;
    private Spinner mModeSpinner;
    private Spinner mEventSpinner;
    private EventAdapter mEventAdapter;
    private MonitorHostFragment mMonitorHost;
    private MonitorPanelFragment mCurrentPanel = null;
    private BaseEvent[] mEvents = null;
    private BaseEvent mCurrentEvent = null;
    private ArrayList<String> mEventTitles = new ArrayList<>();
    private ProgressDialog mHistoryProgress;
    private Executor mHistoryExecutor = Executors.newSingleThreadExecutor();
    private int mCurrentModeId = 0;
    private boolean is_first_load = false;

    public BaseHistoryFragment() {
        // Required empty public constructor
    }

    protected void setChildListener(ActionListener listener) {
        mChildListener = listener;
    }

    private void refreshEventTitles() {
        mEventAdapter.clear();
        for (BaseEvent event : mEvents) {
            mEventAdapter.add(event.start_time);
        }
        mEventAdapter.notifyDataSetChanged();
        int showed_item = mEventSpinner.getSelectedItemPosition();
        if (showed_item > -1 && showed_item < mEvents.length) {
            mCurrentEvent = mEvents[showed_item];
        } else {
            mCurrentEvent = mEvents[0];
        }
        new HistoryTask().executeOnExecutor(mHistoryExecutor, mCurrentEvent.id);
    }

    protected View onCreateView(
            int layout_id,
            int mode_spinner_id,
            int event_spinner_id,
            int monitor_host_id,
            int delete_btn_id,
            int comment_btn_id,
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LCAT.d(this, "create view");
        is_first_load = true;
        View rootView = inflater.inflate(layout_id, container, false);
        if (mMonitorHost == null) {
            mMonitorHost = MonitorHostFragment.newInstance();
        }
        mMonitorHost.setPanelViewCreatedListener(this);
        FragmentTransaction trans = getChildFragmentManager().beginTransaction();
        trans.replace(monitor_host_id, mMonitorHost);
        trans.commit();

        mModeSpinner = (Spinner) rootView.findViewById(mode_spinner_id);
        mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                LCAT.d(this, "mode spinner item selected: " + pos);
                mEvents = mChildListener.loadEvents(pos);
                mCurrentModeId = pos;
                refreshEventTitles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEventAdapter = new EventAdapter(getContext());
        mEventSpinner = (Spinner) rootView.findViewById(event_spinner_id);
        mEventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LCAT.d(this, "event spinner item selected: " + position);
                if (is_first_load) {
                    LCAT.d(this, "first load, task skipped");
                    is_first_load = false;
                    return;
                }
                LCAT.d(this, "loading data for: " + mCurrentEvent.start_time);
                mCurrentEvent = mEvents[position];
                new HistoryTask().executeOnExecutor(mHistoryExecutor, mCurrentEvent.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mEventSpinner.setAdapter(mEventAdapter);

        rootView.findViewById(delete_btn_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChildListener.deleteEvent(mCurrentEvent.id);
                mEvents = mChildListener.loadEvents(mCurrentModeId);
                refreshEventTitles();
            }
        });

        mHistoryProgress = new ProgressDialog(getContext());
        mHistoryProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mHistoryProgress.setIndeterminate(true);
        mHistoryProgress.setTitle("正在进行分析");

        rootView.findViewById(comment_btn_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChildListener.comment();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMonitorHost.displayPanel(MonitorHostFragment.PANEL_LINE);
    }

    @Override
    public void panelViewCreated(MonitorPanelFragment panel) {
        mCurrentPanel = panel;
        mCurrentPanel.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHistoryProgress.dismiss();
    }

    /**
     * 子类监听器，使基类能调用子类代码
     */
    public interface ActionListener {
        BaseEvent[] loadEvents(int position);

        BaseEventData[] loadEventData(long event_id);

        void displayEventData(MonitorPanelFragment panel, BaseEventData[] event_data_set, int analyze_rate);

        void deleteEvent(long event_id);

        void comment();
    }

    private class EventAdapter extends ArrayAdapter<String> {
        public EventAdapter(Context _context) {
            super(_context, R.layout.item_event_spinner, R.id.itemTitle, mEventTitles);
        }
    }

    private class HistoryTask extends AsyncTask<Long, Void, BaseEventData[]> {
        @Override
        protected void onPostExecute(BaseEventData[] event_data) {
            LCAT.d(this, "task finished");
            mHistoryProgress.dismiss();
            mChildListener.displayEventData(mCurrentPanel, event_data, mCurrentEvent.analyse_rate);
        }

        @Override
        protected void onPreExecute() {
            LCAT.d(this, "task prepared");
            mCurrentPanel.clear();
            mHistoryProgress.show();
        }

        @Override
        protected BaseEventData[] doInBackground(Long... params) {
            LCAT.d(this, "task started");
            return mChildListener.loadEventData(params[0]);
        }
    }
}
