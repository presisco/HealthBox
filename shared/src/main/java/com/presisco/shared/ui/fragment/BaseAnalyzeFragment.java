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
import android.widget.Button;
import android.widget.Spinner;

import com.presisco.shared.R;
import com.presisco.shared.data.BaseEvent;
import com.presisco.shared.data.BaseEventData;
import com.presisco.shared.ui.framework.mode.Analyze;
import com.presisco.shared.ui.framework.monitor.MonitorHostFragment;
import com.presisco.shared.ui.framework.monitor.MonitorPanelFragment;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseAnalyzeFragment extends Fragment implements MonitorPanelFragment.ViewCreatedListener {
    private ActionListener mChildListener;
    private Spinner mModeSpinner;
    private Spinner mEventSpinner;
    private Button mGetAdviceBtn;
    private EventAdapter mEventAdapter;
    private MonitorHostFragment mMonitorHost;
    private MonitorPanelFragment mCurrentPanel = null;
    private Analyze[] mAnalyzeModes = null;
    private Analyze mCurrentMode = null;
    private BaseEvent[] mEvents = null;
    private BaseEvent mCurrentEvent = null;
    private ArrayList<String> mEventTitles = new ArrayList<>();
    private ProgressDialog mAnalyseProgress;
    private Executor mAnalyseExecutor = Executors.newSingleThreadExecutor();

    public BaseAnalyzeFragment() {
        // Required empty public constructor
    }

    protected void setChildListener(ActionListener listener) {
        mChildListener = listener;
    }

    protected void setHistoryModes(Analyze[] modes) {
        mAnalyzeModes = modes;
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
            int get_advice_btn_id,
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(layout_id, container, false);
        if (mMonitorHost == null) {
            mMonitorHost = MonitorHostFragment.newInstance();
        }
        mMonitorHost.clearState();
        mMonitorHost.setPanelViewCreatedListener(this);
        FragmentTransaction trans = getChildFragmentManager().beginTransaction();
        trans.replace(monitor_host_id, mMonitorHost);
        trans.commit();

        mModeSpinner = (Spinner) rootView.findViewById(mode_spinner_id);
        mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                mCurrentMode = mAnalyzeModes[pos];

                mMonitorHost.displayPanel(mCurrentMode.getPanelType());
                mEvents = mChildListener.loadEvents(mCurrentMode.getEventType());

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
                new AnalyseTask().executeOnExecutor(mAnalyseExecutor, mCurrentEvent.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mEventSpinner.setAdapter(mEventAdapter);

        mAnalyseProgress = new ProgressDialog(getContext());
        mAnalyseProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mAnalyseProgress.setIndeterminate(true);
        mAnalyseProgress.setTitle("正在进行分析");

        mGetAdviceBtn = (Button) rootView.findViewById(get_advice_btn_id);
        mGetAdviceBtn.setEnabled(false);
        mGetAdviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChildListener.onGetAdvice(mCurrentMode.getEventType(), mCurrentMode.getClassificationString());
            }
        });

        return rootView;
    }

    @Override
    public void panelViewCreated(MonitorPanelFragment panel) {
        mCurrentPanel = panel;
        mCurrentPanel.clear();
        mCurrentMode.setPanel(mCurrentPanel);
        mCurrentMode.initPanelView();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAnalyseProgress.hide();
    }

    /**
     * 子类监听器，使基类能调用子类代码
     */
    public interface ActionListener {
        BaseEvent[] loadEvents(String event_type);

        BaseEventData[] loadEventData(long event_id);

        void onGetAdvice(String event_type, String classification);
    }

    private class EventAdapter extends ArrayAdapter<String> {
        public EventAdapter(Context _context) {
            super(_context, R.layout.item_event_spinner, R.id.itemTitle, mEventTitles);
        }
    }

    private class AnalyseTask extends AsyncTask<Long, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            mAnalyseProgress.hide();
            mCurrentMode.displayResult();
            mGetAdviceBtn.setEnabled(true);
        }

        @Override
        protected void onPreExecute() {
            mGetAdviceBtn.setEnabled(false);
            mAnalyseProgress.show();
            mMonitorHost.displayPanel(mCurrentMode.getPanelType());
        }

        @Override
        protected Void doInBackground(Long... params) {
            BaseEventData[] event_data = mChildListener.loadEventData(params[0]);
            mCurrentMode.analyseData(event_data, mCurrentEvent.analyse_rate);
            return null;
        }
    }
}
