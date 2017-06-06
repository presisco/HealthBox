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
    private ProgressDialog mAnalyseProgress;
    private Executor mAnalyseExecutor = Executors.newSingleThreadExecutor();
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
                mEvents = mChildListener.loadEvents(pos);
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
                mCurrentEvent = mEvents[position];
                new HistoryTask().executeOnExecutor(mAnalyseExecutor, mCurrentEvent.id);
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
                mEventAdapter.remove(mCurrentEvent.start_time);
                mEventAdapter.notifyDataSetChanged();
            }
        });

        mAnalyseProgress = new ProgressDialog(getContext());
        mAnalyseProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mAnalyseProgress.setIndeterminate(true);
        mAnalyseProgress.setTitle("正在进行分析");

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
        mAnalyseProgress.dismiss();
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
            mAnalyseProgress.dismiss();
            mChildListener.displayEventData(mCurrentPanel, event_data, mCurrentEvent.analyse_rate);
        }

        @Override
        protected void onPreExecute() {
            mAnalyseProgress.show();
        }

        @Override
        protected BaseEventData[] doInBackground(Long... params) {
            return mChildListener.loadEventData(params[0]);
        }
    }
}
