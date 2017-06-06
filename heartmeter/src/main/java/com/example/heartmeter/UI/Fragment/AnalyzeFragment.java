package com.example.heartmeter.UI.Fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.heartmeter.Data.Event;
import com.example.heartmeter.Data.EventData;
import com.example.heartmeter.R;
import com.example.heartmeter.UI.Activity.AdviceActivity;
import com.example.heartmeter.storage.SQLiteManager;
import com.presisco.shared.data.BaseEvent;
import com.presisco.shared.data.BaseEventData;
import com.presisco.shared.ui.fragment.BaseAnalyzeFragment;
import com.presisco.shared.ui.framework.mode.Analyze;
import com.presisco.shared.ui.framework.monitor.MonitorHostFragment;
import com.presisco.shared.ui.framework.monitor.PiePanelFragment;
import com.presisco.shared.utils.Classifier;
import com.presisco.shared.utils.LCAT;

import java.util.ArrayList;

import lecho.lib.hellocharts.model.SliceValue;

public class AnalyzeFragment extends BaseAnalyzeFragment implements BaseAnalyzeFragment.ActionListener {
    private SQLiteManager mDataManager;
    private int user_age;

    private AnalyzeMode[] mAnalyseModes = null;

    private Resources res = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        if (mDataManager == null) {
            mDataManager = new SQLiteManager(getContext());
        }
        initModes();
        setChildListener(this);
        setHistoryModes(mAnalyseModes);
        user_age = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("age", 30);
        LCAT.d(this, "user age: " + user_age);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(
                R.layout.fragment_analyze,
                R.id.spinnerMode,
                R.id.spinnerEvent,
                R.id.analyzeHost,
                R.id.buttonAdvice,
                inflater, container, savedInstanceState);
    }

    private void initModes() {
        mAnalyseModes = new AnalyzeMode[]{
                new AnalyzeMode(
                        res,
                        R.array.default_partition,
                        R.array.default_colors,
                        R.array.default_classification,
                        R.string.advice_default,
                        R.array.default_advice_bodies) {
                    private SliceValue[] distribution = new SliceValue[0];
                    private int average_stat = 0;
                    private int duration = 0;

                    @Override
                    public String getEventType() {
                        return Event.TYPE_DEFAULT;
                    }

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_PIE;
                    }

                    @Override
                    public void initPanelView() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        panel.clear();
                        PiePanelFragment.PieStyle style = new PiePanelFragment.PieStyle();
                        style.has_label = true;
                        style.has_label_outside = false;
                        panel.setStyle(style);
                    }

                    @Override
                    public void analyseData(EventData[] data, int analyse_rate) {
                        int[] dist_value = new int[CLASSIFICATION.length];

                        int average = 0;
                        for (int i = 0; i < data.length; ++i) {
                            int class_index = Classifier.classify(data[i].heart_rate, PARTITION);
                            dist_value[class_index]++;
                            average += data[i].heart_rate;
                        }
                        average /= data.length;
                        average_stat = average;
                        duration = data.length / 60;

                        setClassificationIndex(Classifier.classify(average, PARTITION));

                        ArrayList<SliceValue> temp = new ArrayList<>();
                        for (int i = 0; i < dist_value.length; ++i) {
                            if (dist_value[i] > 0) {
                                temp.add(
                                        new SliceValue(dist_value[i])
                                                .setLabel(CLASSIFICATION[i])
                                                .setColor(COLORS[i]));
                            }
                        }
                        distribution = temp.toArray(new SliceValue[temp.size()]);
                    }

                    @Override
                    public void displayResult() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        String hint = ADVICE_HEADER.replace("%duration%", Integer.toString(duration));
                        hint = hint.replace("%average%", Integer.toString(average_stat));
                        hint = hint.replace("\\n", "\n");
                        panel.setHint(hint + getAdviceBody());
                        panel.appendSlices(distribution);
                    }
                },
                new AnalyzeMode(
                        res,
                        R.array.aerobic_partition,
                        R.array.aerobic_colors,
                        R.array.aerobic_classification,
                        R.string.advice_default,
                        R.array.aerobic_advice_bodies) {
                    private int average_stat = 0;
                    private int duration = 0;

                    private SliceValue[] distribution = new SliceValue[0];

                    @Override
                    public String getEventType() {
                        return Event.TYPE_AEROBIC;
                    }

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_PIE;
                    }

                    @Override
                    public void initPanelView() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        panel.clear();
                        PiePanelFragment.PieStyle style = new PiePanelFragment.PieStyle();
                        style.has_label = true;
                        style.has_label_outside = false;
                        panel.setStyle(style);
                    }

                    @Override
                    public void analyseData(EventData[] data, int analyse_rate) {
                        if (data.length == 0) {
                            return;
                        }

                        int[] dist_value = new int[CLASSIFICATION.length];

                        int average = 0;
                        for (int i = 0; i < data.length; ++i) {
                            int class_index = Classifier.classify((double) data[i].heart_rate / (220 - user_age), PARTITION);
                            dist_value[class_index]++;
                            average += data[i].heart_rate;
                        }
                        average /= data.length;
                        average_stat = average;
                        duration = data.length / 60;

                        setClassificationIndex(Classifier.classify((double) average / (220 - user_age), PARTITION));

                        ArrayList<SliceValue> temp = new ArrayList<>();
                        for (int i = 0; i < dist_value.length; ++i) {
                            if (dist_value[i] > 0) {
                                temp.add(
                                        new SliceValue(dist_value[i])
                                                .setLabel(CLASSIFICATION[i])
                                                .setColor(COLORS[i]));
                            }
                        }
                        distribution = temp.toArray(new SliceValue[temp.size()]);
                    }

                    @Override
                    public void displayResult() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        String hint = ADVICE_HEADER.replace("%duration%", Integer.toString(duration));
                        hint = hint.replace("%average%", Integer.toString(average_stat));
                        hint = hint.replace("\\n", "\n");
                        panel.setHint(hint + getAdviceBody());
                        panel.appendSlices(distribution);
                    }
                },
                new AnalyzeMode(
                        res,
                        R.array.anaerobic_partition,
                        R.array.anaerobic_colors,
                        R.array.anaerobic_classification,
                        R.string.advice_default,
                        R.array.anaerobic_advice_bodies) {
                    private SliceValue[] distribution = new SliceValue[0];
                    private int average_stat = 0;
                    private int duration = 0;

                    /**
                     * 获取当前的事件类型
                     *
                     * @return 事件类型字符串
                     */
                    @Override
                    public String getEventType() {
                        return Event.TYPE_ANAEROBIC;
                    }

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_PIE;
                    }

                    @Override
                    public void initPanelView() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        panel.clear();
                        PiePanelFragment.PieStyle style = new PiePanelFragment.PieStyle();
                        style.has_label = true;
                        style.has_label_outside = false;
                        panel.setStyle(style);
                    }

                    @Override
                    public void analyseData(EventData[] data, int analyse_rate) {
                        int[] dist_value = new int[CLASSIFICATION.length];

                        int average = 0;
                        for (int i = 0; i < data.length; ++i) {
                            int class_index = Classifier.classify((double) data[i].heart_rate / (220 - user_age), PARTITION);
                            dist_value[class_index]++;
                            average += data[i].heart_rate;
                        }
                        average /= data.length;
                        average_stat = average;
                        duration = data.length / 60;

                        setClassificationIndex(Classifier.classify((double) average / (220 - user_age), PARTITION));

                        ArrayList<SliceValue> temp = new ArrayList<>();
                        for (int i = 0; i < dist_value.length; ++i) {
                            if (dist_value[i] > 0) {
                                temp.add(
                                        new SliceValue(dist_value[i])
                                                .setLabel(CLASSIFICATION[i])
                                                .setColor(COLORS[i]));
                            }
                        }
                        distribution = temp.toArray(new SliceValue[temp.size()]);
                    }

                    @Override
                    public void displayResult() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        String hint = ADVICE_HEADER.replace("%duration%", Integer.toString(duration));
                        hint = hint.replace("%average%", Integer.toString(average_stat));
                        hint = hint.replace("\\n", "\n");
                        panel.setHint(hint+getAdviceBody());
                        panel.appendSlices(distribution);
                    }
                },
                new AnalyzeMode(
                        res,
                        R.array.sleep_partition,
                        R.array.sleep_colors,
                        R.array.sleep_classification,
                        R.string.advice_default,
                        R.array.sleep_advice_bodies) {
                    private SliceValue[] distribution = new SliceValue[0];
                    private int average_stat = 0;
                    private int duration = 0;

                    /**
                     * 获取当前的事件类型
                     *
                     * @return 事件类型字符串
                     */
                    @Override
                    public String getEventType() {
                        return Event.TYPE_SLEEP;
                    }

                    @Override
                    public String getPanelType() {
                        return MonitorHostFragment.PANEL_PIE;
                    }

                    @Override
                    public void initPanelView() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        panel.clear();
                        PiePanelFragment.PieStyle style = new PiePanelFragment.PieStyle();
                        style.has_label = true;
                        style.has_label_outside = false;
                        panel.setStyle(style);
                    }

                    @Override
                    public void analyseData(EventData[] data, int analyse_rate) {
                        int[] dist_value = new int[CLASSIFICATION.length];

                        int average = 0;
                        for (int i = 0; i < data.length; ++i) {
                            int class_index = Classifier.classify(data[i].heart_rate, PARTITION);
                            dist_value[class_index]++;
                            average += data[i].heart_rate;
                        }
                        average /= data.length;
                        average_stat = average;
                        duration = data.length / 60;

                        setClassificationIndex(Classifier.classify(average, PARTITION));

                        ArrayList<SliceValue> temp = new ArrayList<>();
                        for (int i = 0; i < dist_value.length; ++i) {
                            if (dist_value[i] > 0) {
                                temp.add(
                                        new SliceValue(dist_value[i])
                                                .setLabel(CLASSIFICATION[i])
                                                .setColor(COLORS[i]));
                            }
                        }
                        distribution = temp.toArray(new SliceValue[temp.size()]);
                    }

                    @Override
                    public void displayResult() {
                        PiePanelFragment panel = (PiePanelFragment) getPanel();
                        String hint = ADVICE_HEADER.replace("%duration%", Integer.toString(duration));
                        hint = hint.replace("%average%", Integer.toString(average_stat));
                        hint = hint.replace("\\n", "\n");
                        panel.setHint(hint+getAdviceBody());
                        panel.appendSlices(distribution);
                    }
                }
        };
    }

    @Override
    public BaseEvent[] loadEvents(String event_type) {
        return mDataManager.getEventsByType(event_type);
    }

    @Override
    public BaseEventData[] loadEventData(long event_id) {
        return mDataManager.getAllDataInEvent(event_id);
    }

    @Override
    public void onGetAdvice(String event_type, int classification_index) {
        Intent intent = new Intent(getActivity(), AdviceActivity.class);
        intent.putExtra("classification", classification_index);
        intent.putExtra("event_type",event_type);
        startActivity(intent);
    }

    public abstract static class AnalyzeMode extends Analyze<EventData> {
        protected AnalyzeMode(Resources res, int partition_id, int colors_id, int classification_id, int advice_header_id, int advice_body_id) {
            super(res, partition_id, colors_id, classification_id, advice_header_id, advice_body_id);
        }
    }
}
