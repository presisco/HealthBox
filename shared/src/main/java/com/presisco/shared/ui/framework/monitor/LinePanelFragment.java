package com.presisco.shared.ui.framework.monitor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.presisco.shared.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LinePanelFragment#} factory method to
 * create an instance of this fragment.
 */
public class LinePanelFragment extends ChartPanelFragment {
    private int mMaxPoints = 10;

    private LinkedList<PointValue> mPoints = new LinkedList<>();

    private TextView mTitle;
    private TextView mHint;

    private LineChartView mLineChart;
    private LineChartData mLineData;
    private String mAxisXText;
    private String mAxisYText;
    private float mAxisYMin = 0;
    private float mAxisYMax = 100;
    private float mAxisXBaseline = 0;
    private float mLastXCoord = 0;
    private float mXStep = 1;
    private boolean mScrollable = false;

    private LineStyle mLineStyle = new LineStyle();

    public LinePanelFragment() {
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setHint(String hint) {
        mHint.setText(hint);
    }

    public void setMaxPoints(int count) {
        mMaxPoints = count;
    }

    public void setAxisXText(String text) {
        mAxisXText = text;
    }

    public void setAxisYText(String text) {
        mAxisYText = text;
    }

    public void setAxisYScale(float min, float max) {
        mAxisYMax = max;
        mAxisYMin = min;
    }

    public void setAxisXBaseline(float baseline) {
        mAxisXBaseline = baseline;
    }

    public void setStyle(LineStyle lineStyle) {
        mLineStyle = lineStyle;
    }

    public void setXStep(float step) {
        mXStep = step;
    }

    public void setScrollable(boolean flag) {
        mScrollable = flag;
    }

    public void appendPoints(PointValue[] points) {
        for (PointValue point : points) {
            mPoints.add(point);
            mLastXCoord += mXStep;
            if (mPoints.size() > mMaxPoints && (!mScrollable)) {
                mPoints.remove(0);
            }
        }
        initChart();
        scrollViewport();
    }

    public void appendPoint(PointValue point) {
        mPoints.add(point);
        if (mPoints.size() > mMaxPoints && (!mScrollable)) {
            mPoints.remove(0);
        }
        mLastXCoord += mXStep;
        initChart();
        scrollViewport();
    }

    public void appendValue(float value) {
        mPoints.add(new PointValue(mLastXCoord + mXStep, value));
        if (mPoints.size() > mMaxPoints && (!mScrollable)) {
            mPoints.remove(0);
        }
        mLastXCoord += mXStep;
        initChart();
        scrollViewport();
    }

    public void appendValue(float[] values) {
        for (int i = 1; i < values.length; ++i) {
            mPoints.add(new PointValue(mLastXCoord + mXStep, values[i]));
            mLastXCoord += mXStep;
            if (mPoints.size() > mMaxPoints && (!mScrollable)) {
                mPoints.remove(0);
            }
        }
        initChart();
        scrollViewport();
    }

    @Override
    public void redraw() {
        initChart();
        resetViewport();
    }

    @Override
    public void clear() {
        mPoints.clear();
        mLastXCoord = mAxisXBaseline;
        redraw();
    }

    @Override
    protected void initChart() {
        mLineChart.setOnValueTouchListener(null);

        // Disable viewport recalculations, see toggleCubic() method for more info.
        mLineChart.setViewportCalculationEnabled(false);

        List<Line> lines = new ArrayList<>();

        Line line = new Line(mPoints);
        line.setColor(mLineStyle.line_color);
        line.setShape(mLineStyle.line_shape);
        line.setCubic(mLineStyle.is_cubic);
        line.setFilled(mLineStyle.is_filled);
        line.setHasLabels(mLineStyle.has_label);
        line.setHasLabelsOnlyForSelected(mLineStyle.has_label_selected);
        line.setHasLines(mLineStyle.has_lines);
        line.setHasPoints(mLineStyle.has_points);
        line.setPointColor(mLineStyle.point_color);
        lines.add(line);

        mLineData = new LineChartData(lines);

        if (mAxisXText != null) {
            Axis axisX = new Axis();
            axisX.setName(mAxisXText);
            mLineData.setAxisXBottom(axisX);
        }
        if (mAxisYText != null) {
            Axis axisY = new Axis().setHasLines(true);
            axisY.setName(mAxisYText);
            mLineData.setAxisYLeft(axisY);
        }

        mLineData.setBaseValue(Float.NEGATIVE_INFINITY);

        mLineChart.setLineChartData(mLineData);
        mLineChart.setScrollEnabled(mScrollable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_line_panel, container, false);

        mTitle = (TextView) rootView.findViewById(R.id.textTitle);
        mHint = (TextView) rootView.findViewById(R.id.textHint);

        mLineChart = (LineChartView) rootView.findViewById(R.id.lineChart);

        mViewCreated.panelViewCreated(this);

        //initChart();

        //resetViewport();

        return rootView;
    }

    private void scrollViewport() {
        final Viewport maximum_viewport = new Viewport(mLineChart.getMaximumViewport());
        final Viewport current_viewport = new Viewport(mLineChart.getCurrentViewport());
        maximum_viewport.bottom = mAxisYMin;
        maximum_viewport.top = mAxisYMax;
        current_viewport.bottom = mAxisYMin;
        current_viewport.top = mAxisYMax;
        if (mScrollable) {
            maximum_viewport.left = mAxisXBaseline;
            if (mLastXCoord > mAxisXBaseline + mMaxPoints * mXStep) {
                maximum_viewport.right = mLastXCoord;
                current_viewport.right = mLastXCoord;
            } else {
                maximum_viewport.right = mAxisXBaseline + mMaxPoints * mXStep;
                current_viewport.right = mAxisXBaseline + mMaxPoints * mXStep;
            }
            current_viewport.left = mLastXCoord - mMaxPoints * mXStep;
            mLineChart.setMaximumViewport(maximum_viewport);
            mLineChart.setCurrentViewport(current_viewport);
        } else {
            if (mLastXCoord < mAxisXBaseline + mMaxPoints * mXStep) {
                maximum_viewport.left = mAxisXBaseline;
                maximum_viewport.right = mAxisXBaseline + mMaxPoints * mXStep;
            } else {
                maximum_viewport.left = mLastXCoord - mMaxPoints * mXStep;
                maximum_viewport.right = mLastXCoord;
            }
            mLineChart.setMaximumViewport(maximum_viewport);
            mLineChart.setCurrentViewport(maximum_viewport);
        }
    }

    private void resetViewport() {
        final Viewport v = new Viewport(mLineChart.getMaximumViewport());
        v.bottom = mAxisYMin;
        v.top = mAxisYMax;
        v.left = mAxisXBaseline;
        v.right = mAxisXBaseline + mMaxPoints * mXStep;
        mLineChart.setMaximumViewport(v);
        mLineChart.setCurrentViewport(v);
    }

    public static class LineStyle {
        public static final int COLOR_LINE_DEFAULT = Color.parseColor("#DFDFDF");
        public static final int COLOR_POINT_DEFAULT = Color.parseColor("#DFDFDF");

        public int line_color = ChartUtils.DEFAULT_COLOR;
        public int point_color = ChartUtils.DEFAULT_DARKEN_COLOR;
        public ValueShape line_shape = ValueShape.CIRCLE;
        public boolean is_cubic = false;
        public boolean is_filled = false;
        public boolean has_label = false;
        public boolean has_label_selected = false;
        public boolean has_lines = true;
        public boolean has_points = false;
    }
}
