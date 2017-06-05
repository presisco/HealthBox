package com.example.heartmeter.UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.heartmeter.R;
import com.presisco.shared.network.request.SignUpRequest;

import java.util.HashMap;

/**
 * 用户注册及调查窗口
 */
public class SurveyActivity extends AppCompatActivity {
    //所有的执行结果定义
    public static final int RESULT_CANCELED = 0;
    public static final int RESULT_PASSED = 1;

    //界面所有输入控件的引用
    private EditText mNameEdit;
    private EditText mPasswordEdit;
    private EditText mRepeatEdit;
    private EditText mAgeEdit;
    private Spinner mGenderSpinner;
    private Spinner mPoliticalStatusSpinner;
    private Spinner mEducationStatusSpinner;
    private Spinner mCareerStatusSpinner;
    private EditText mAnnualIncomeEdit;
    private Spinner mSocialStatusSpinner;
    private Spinner mUsageFrequencySpinner;
    private Spinner mTrustSpinnerSpinner;
    private Spinner mChannelSpinner;

    private RequestQueue mRequestQueue;

    /**
     * 获取文字编辑控件的引用
     *
     * @param id 控件的id
     * @return 返回控件的引用
     */
    private EditText findEdit(int id) {
        return (EditText) findViewById(id);
    }

    /**
     * 获取下拉列表控件的引用
     *
     * @param id 控件的id
     * @return 返回控件的引用
     */
    private Spinner findSpinner(int id) {
        return (Spinner) findViewById(id);
    }

    /**
     * 将控件与变量对应起来
     */
    private void linkViews() {
        mNameEdit = findEdit(R.id.editName);
        mPasswordEdit = findEdit(R.id.editPassword);
        mRepeatEdit = findEdit(R.id.editRepeatPassword);
        mAgeEdit = findEdit(R.id.editAge);
        mGenderSpinner = findSpinner(R.id.spinnerGender);
        mPoliticalStatusSpinner = findSpinner(R.id.spinnerPoliticalStatus);
        mEducationStatusSpinner = findSpinner(R.id.spinnerEducationStatus);
        mCareerStatusSpinner = findSpinner(R.id.spinnerCareerStatus);
        mAnnualIncomeEdit = findEdit(R.id.editAnnualIncome);
        mSocialStatusSpinner = findSpinner(R.id.spinnerSocialStatus);
        mUsageFrequencySpinner = findSpinner(R.id.spinnerUsageFrequency);
        mTrustSpinnerSpinner = findSpinner(R.id.spinnerTrust);
        mChannelSpinner = findSpinner(R.id.spinnerChannel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        linkViews();
        setResult(RESULT_CANCELED);
        mRequestQueue = Volley.newRequestQueue(this);
    }

    /**
     * "取消"按钮的响应函数
     *
     * @param v "取消"按钮的引用
     */
    public void onCancel(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * 获取文本编辑控件的字符串值
     *
     * @param edit 文本编辑控件的引用
     * @return 返回字符串值
     */
    private String getValue(EditText edit) {
        return edit.getText().toString().trim();
    }

    /**
     * 将下拉列表的当前选择项位置转换为调查所需的值
     *
     * @param spinner 下拉列表的引用
     * @return 返回字符串值
     */
    private String getValue(Spinner spinner) {
        return (spinner.getSelectedItemPosition() + 1) + "";
    }

    /**
     * "提交"按钮的响应函数
     *
     * @param v "提交"按钮的引用
     */
    public void onApply(View v) {
        if (!getValue(mPasswordEdit).equals(getValue(mRepeatEdit))) {
            Toast.makeText(this, R.string.text_wrong_password, Toast.LENGTH_SHORT);
            return;
        }

        final HashMap<String, String> survey = new HashMap<>();
        survey.put("username", getValue(mNameEdit));
        survey.put("password", getValue(mPasswordEdit));
        survey.put("age", getValue(mAgeEdit));
        survey.put("gender", getValue(mGenderSpinner));
        survey.put("political_status", getValue(mPoliticalStatusSpinner));
        survey.put("education_status", getValue(mEducationStatusSpinner));
        survey.put("career_status", getValue(mCareerStatusSpinner));
        survey.put("annual_income", getValue(mAnnualIncomeEdit));
        survey.put("social_status", getValue(mSocialStatusSpinner));
        survey.put("usage_frequency", getValue(mUsageFrequencySpinner));
        survey.put("trust", getValue(mTrustSpinnerSpinner));
        survey.put("channel", getValue(mChannelSpinner));

        SignUpRequest request = new SignUpRequest(survey
                , new Response.Listener<String>() {
            /**
             * Called when a response is received.
             *
             * @param response
             */
            @Override
            public void onResponse(String response) {
                Toast.makeText(SurveyActivity.this, response, Toast.LENGTH_SHORT).show();
                SurveyActivity.this.setResult(
                        RESULT_PASSED, new Intent()
                                .putExtra("username", survey.get("username"))
                                .putExtra("age", survey.get("age")));
                SurveyActivity.this.finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SurveyActivity.this, "sign up failed", Toast.LENGTH_SHORT).show();
            }
        });

        mRequestQueue.add(request);
    }
}
