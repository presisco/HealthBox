package com.presisco.shared.ui.framework.monitor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.presisco.shared.R;
import com.presisco.shared.utils.LCAT;

import java.util.LinkedList;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by presisco on 2017/4/20.
 */

public class PiePanelFragment extends ChartPanelFragment {
    PieChartData mPieChartData;
    PieChartView mPieChartView;
    List<SliceValue> mSlices = new LinkedList<>();
    PieStyle mPieStyle = new PieStyle();
    private TextView mTitle;
    private TextView mHint;

    public PiePanelFragment() {
        super();
        LCAT.d(this, "created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LCAT.d(this, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_pie_panel, container, false);

        mPieChartView = (PieChartView) rootView.findViewById(R.id.pieChart);

        mTitle = (TextView) rootView.findViewById(R.id.textTitle);
        mHint = (TextView) rootView.findViewById(R.id.textHint);

        mViewCreated.panelViewCreated(this);

        return rootView;
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setHint(String hint) {
        mHint.setText(hint);
    }

    public void setStyle(PieStyle style) {
        mPieStyle = style;
    }

    public void appendSlices(SliceValue[] slices) {
        for (SliceValue slice : slices) {
            mSlices.add(slice);
        }
        redraw();
    }

    @Override
    public void appendValue(float[] values) {
        for (float value : values) {
            mSlices.add(new SliceValue(value));
        }
        redraw();
    }

    @Override
    public void appendValue(float value) {

    }

    @Override
    protected void initChart() {
        LCAT.d(this, "initChart");
        mPieChartData = new PieChartData(mSlices);

        mPieChartData.setHasLabels(mPieStyle.has_label);
        mPieChartData.setHasLabelsOnlyForSelected(mPieStyle.has_label_selected);
        mPieChartData.setHasLabelsOutside(mPieStyle.has_label_outside);
        mPieChartData.setHasCenterCircle(mPieStyle.has_center_circle);

        mPieChartData.setSlicesSpacing(mPieStyle.slice_spacing);
        mPieChartData.setCenterText1(mPieStyle.center_text);
        mPieChartData.setCenterText2(mPieStyle.center_text2);

        mPieChartView.setPieChartData(mPieChartData);
    }

    @Override
    public void redraw() {
        initChart();
    }

    @Override
    public void clear() {
        mSlices.clear();
        mSlices = new LinkedList<>();
        mHint.setText("");
        redraw();
    }

    public static class PieStyle {

        public boolean has_label = false;
        public boolean has_label_selected = false;
        public boolean has_label_outside = false;
        public boolean has_center_circle = false;
        public int slice_spacing = 0;
        public String center_text = "";
        public String center_text2 = "";
    }
}
