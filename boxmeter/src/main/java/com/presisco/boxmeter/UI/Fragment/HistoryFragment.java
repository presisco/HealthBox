package com.presisco.boxmeter.UI.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.presisco.boxmeter.Data.Event;
import com.presisco.boxmeter.Data.EventData;
import com.presisco.boxmeter.R;
import com.presisco.boxmeter.UI.Activity.CommentActivity;
import com.presisco.boxmeter.storage.SQLiteManager;
import com.presisco.shared.data.BaseEvent;
import com.presisco.shared.data.BaseEventData;
import com.presisco.shared.ui.fragment.BaseHistoryFragment;
import com.presisco.shared.ui.framework.monitor.LinePanelFragment;
import com.presisco.shared.ui.framework.monitor.MonitorPanelFragment;

import lecho.lib.hellocharts.util.ChartUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends BaseHistoryFragment implements BaseHistoryFragment.ActionListener {
    private static final int MODE_DEFAULT = 0;
    private static final int MODE_AEROBIC = 1;
    private static final int MODE_ANAEROBIC = 2;
    private static final int MODE_SLEEP = 3;
    private static final String[] EVENT_TYPES = {Event.TYPE_DEFAULT, Event.TYPE_AEROBIC, Event.TYPE_ANAEROBIC, Event.TYPE_SLEEP};
    private SQLiteManager mDataManager;

    public HistoryFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistoryFragment.
     */
    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(
                R.layout.fragment_history,
                R.id.spinnerMode,
                R.id.spinnerEvent,
                R.id.monitorHost,
                R.id.deleteButton,
                R.id.buttonComment,
                inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mDataManager == null) {
            mDataManager = new SQLiteManager(getContext());
        }
        setChildListener(this);
    }

    @Override
    public BaseEvent[] loadEvents(int position) {
        return mDataManager.getEventsByType(EVENT_TYPES[position]);
    }

    @Override
    public BaseEventData[] loadEventData(long event_id) {
        return mDataManager.getAllDataInEvent(event_id);
    }

    @Override
    public void displayEventData(MonitorPanelFragment monitor_panel, BaseEventData[] event_data_set, int analyze_rate) {
        LinePanelFragment panel = (LinePanelFragment) monitor_panel;
        EventData[] data_set = (EventData[]) event_data_set;
        panel.clear();
        LinePanelFragment.LineStyle main_line_style = new LinePanelFragment.LineStyle();
        main_line_style.line_color = ChartUtils.COLOR_BLUE;
        panel.setStyle(main_line_style);
        panel.setAxisYScale(70, 110);
        panel.setMaxPoints(60);
        panel.setXStep(1);
        panel.setAxisXText("Time");
        panel.setAxisYText("SPO2H");
        panel.setScrollable(true);
        // panel.redraw();
        float[] data = new float[data_set.length];
        for (int i = 0; i < data_set.length; ++i) {
            data[i] = data_set[i].spo2h;
        }
        panel.appendValue(data);
    }

    @Override
    public void deleteEvent(long event_id) {
        mDataManager.deleteEvent(event_id);
    }

    @Override
    public void comment() {
        startActivity(new Intent(getActivity(), CommentActivity.class));
    }
}
