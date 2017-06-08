package com.presisco.boxmeter.UI.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.presisco.boxmeter.Data.Event;
import com.presisco.boxmeter.R;
import com.presisco.boxmeter.Service.HubService;
import com.presisco.shared.ui.fragment.BaseRealTimeFragment;
import com.presisco.shared.ui.framework.mode.RealTime;
import com.presisco.shared.ui.framework.monitor.LinePanelFragment;
import com.presisco.shared.ui.framework.monitor.MonitorHostFragment;
import com.presisco.shared.ui.framework.monitor.StringPanelFragment;
import com.presisco.shared.utils.ByteUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RealTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RealTimeFragment extends BaseRealTimeFragment implements BaseRealTimeFragment.ActionListener {
    private static final int MODE_SPO2H = 0;
    private static final int MODE_PULSE = 1;
    private static final int MODE_SPO2H_MINUTE = 2;
    private static final int MODE_PULSE_FIVE_SEC = 3;
    private static final String[] EVENT_TYPES = {
            Event.TYPE_DEFAULT,
            Event.TYPE_AEROBIC,
            Event.TYPE_ANAEROBIC,
            Event.TYPE_SLEEP
    };

    private RealTime[] mRealTimeModes = null;

    public RealTimeFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistoryFragment.
     */
    public static RealTimeFragment newInstance() {
        RealTimeFragment fragment = new RealTimeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(
                R.layout.fragment_real_time,
                R.id.spinnerType,
                R.id.spinnerDisplay,
                R.id.monitorHost,
                R.id.buttonStart,
                R.id.buttonStop,
                inflater, container, savedInstanceState);
    }

    private void initModes() {
        final String[] modes_hint = getResources().getStringArray(R.array.real_time_mode_hints);
        mRealTimeModes = new RealTime[]{
                new RealTime() {

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_STRING;
                    }

                    @Override
                    public String getBroadcastAction() {
                        return HubService.ACTION_SPO2H;
                    }

                    @Override
                    public void initPanelView() {
                        getPanel().setHint(modes_hint[MODE_SPO2H]);
                    }

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        ((StringPanelFragment) getPanel()).setValue(intent.getIntExtra(HubService.KEY_DATA, 0) + "");
                    }
                },
                new RealTime() {

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_STRING;
                    }

                    @Override
                    public String getBroadcastAction() {
                        return HubService.ACTION_PULSE;
                    }

                    @Override
                    public void initPanelView() {
                        getPanel().setHint(modes_hint[MODE_PULSE]);
                    }

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        ((StringPanelFragment) getPanel()).setValue(intent.getIntExtra(HubService.KEY_DATA, 0) + "");
                    }
                },
                new RealTime() {

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_LINE;
                    }

                    @Override
                    public String getBroadcastAction() {
                        return HubService.ACTION_SPO2H_VOLUME;
                    }

                    @Override
                    public void initPanelView() {
                        getPanel().setHint(modes_hint[MODE_SPO2H_MINUTE]);
                        LinePanelFragment linePanel = (LinePanelFragment) getPanel();
                        linePanel.setAxisYScale(0, 110);
                        linePanel.setMaxPoints(60);
                        linePanel.setXStep(1f);
                        linePanel.setAxisXText("Time");
                        linePanel.setAxisYText("SPO2H");
                        LinePanelFragment.LineStyle style = new LinePanelFragment.LineStyle();
                        style.line_color = Color.parseColor("#26A69A");
                        style.has_points = false;
                        linePanel.setStyle(style);
                        linePanel.redraw();
                    }

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int data = intent.getIntExtra(HubService.KEY_DATA, 25);
                        ((LinePanelFragment) getPanel()).appendValue(data);
                    }
                },
                new RealTime() {

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_LINE;
                    }

                    @Override
                    public String getBroadcastAction() {
                        return HubService.ACTION_PULSE_VOLUME;
                    }

                    @Override
                    public void initPanelView() {
                        getPanel().setHint(modes_hint[MODE_PULSE_FIVE_SEC]);
                        LinePanelFragment linePanel = (LinePanelFragment) getPanel();
                        linePanel.setAxisYScale(0, 20);
                        linePanel.setMaxPoints(500);
                        linePanel.setXStep(0.01f);
                        linePanel.setAxisXText("Time");
                        linePanel.setAxisYText("Pulse");
                        LinePanelFragment.LineStyle style = new LinePanelFragment.LineStyle();
                        style.line_color = Color.parseColor("#26A69A");
                        style.has_points = false;
                        linePanel.setStyle(style);
                        linePanel.redraw();
                    }

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int[] data = intent.getIntArrayExtra(HubService.KEY_DATA);
                        float[] converted = ByteUtils.intArray2floatArray(data);
                        ((LinePanelFragment) getPanel()).appendValue(converted);
                    }
                },
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initModes();
        setChildListener(this);
        setRealTimeModes(mRealTimeModes);
    }

    @Override
    public void startEvent(int event_type_index) {
        getLocalBroadcastManager().sendBroadcast(
                new Intent(HubService.ACTION_START_EVENT)
                        .putExtra(HubService.KEY_EVENT_TYPE, EVENT_TYPES[event_type_index]));
    }

    @Override
    public void endEvent() {
        getLocalBroadcastManager().sendBroadcast(
                new Intent(HubService.ACTION_STOP_EVENT));
    }
}
