package com.presisco.shared.ui.framework.mode;

import android.content.res.Resources;

import com.presisco.shared.ui.framework.monitor.MonitorPanelFragment;
import com.presisco.shared.utils.ValueUtils;

/**
 * Created by presisco on 2017/6/2.
 */

/**
 * 历史分析模式的基类
 *
 * @param <EVENT_DATA> 测量数据类型
 */
public abstract class Analyze<EVENT_DATA> {
    protected final double[] PARTITION;
    protected final int[] COLORS;
    protected final String[] CLASSIFICATION;
    protected final String ADVICE_HEADER;
    protected final String[] ADVICE_BODY;
    MonitorPanelFragment mPanel;
    private int classification_index = 0;

    protected Analyze(
            Resources res,
            int partition_id,
            int colors_id,
            int classification_id,
            int advice_header_id,
            int advice_body_id) {
        PARTITION = ValueUtils.convertStringArray2DoubleArray(res.getStringArray(partition_id));
        COLORS = ValueUtils.convertStringArray2ColorArray(res.getStringArray(colors_id));
        CLASSIFICATION = res.getStringArray(classification_id);
        ADVICE_HEADER = res.getString(advice_header_id);
        ADVICE_BODY = res.getStringArray(advice_body_id);
    }

    public int getClassificationIndex() {
        return classification_index;
    }

    protected void setClassificationIndex(int index) {
        classification_index = index;
    }

    protected String getAdviceBody() {
        return ADVICE_BODY[classification_index];
    }

    public String getClassificationString() {
        return CLASSIFICATION[getClassificationIndex()];
    }

    /**
     * 获取当前的事件类型
     *
     * @return 事件类型字符串
     */
    public abstract String getEventType();

    /**
     * 获取当前显示的面板
     *
     * @return 面板的引用
     */
    public MonitorPanelFragment getPanel() {
        return mPanel;
    }

    /**
     * 设置当前显示的面板
     *
     * @param panel 面板的引用
     */
    public void setPanel(MonitorPanelFragment panel) {
        mPanel = panel;
    }

    /**
     * 返回要显示的面板的类型
     *
     * @return 面板的类型字符串
     */
    public abstract String getPanelType();

    /**
     * 对面板的界面进行设置
     */
    public abstract void initPanelView();

    /**
     * 对给定的数据进行分析
     *
     * @param data         测量到的数据数组
     * @param analyse_rate 每秒钟有多少次测量信息
     */
    public abstract void analyseData(EVENT_DATA[] data, int analyse_rate);

    /**
     * 对分析结果进行呈现
     */
    public abstract void displayResult();
}
