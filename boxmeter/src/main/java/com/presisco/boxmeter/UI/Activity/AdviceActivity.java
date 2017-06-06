package com.presisco.boxmeter.UI.Activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.presisco.boxmeter.R;
import com.presisco.shared.network.Constant;
import com.presisco.shared.network.request.PostFormRequest;

import java.util.HashMap;

public class AdviceActivity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener {

    private ProgressBar mProgressBar;
    private TextView mAdviceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mAdviceText = (TextView) findViewById(R.id.textAdvice);
        int class_index = getIntent().getIntExtra("classification", 0) + 1;

        HashMap<String, String> params = new HashMap<>();
        params.put("classification", Integer.toString(class_index));
        params.put("event_type", getIntent().getStringExtra("event_type"));
        String username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        if (username == "") {
            mProgressBar.setVisibility(View.INVISIBLE);
            mAdviceText.setVisibility(View.VISIBLE);
            mAdviceText.setText("本功能要求登陆");
        } else {
            mAdviceText.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            params.put("username", username);
            params.put("body_sign", "spo2h");
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(new PostFormRequest(Constant.HOST_ADDRESS + Constant.PATH_GET_ADVICE, params, this, this));
        }
    }

    /**
     * Callback method that an error has been occurred with the
     * provided error code and optional user-readable message.
     *
     * @param error
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mAdviceText.setVisibility(View.VISIBLE);
        mAdviceText.setText(error.toString());
    }

    /**
     * Called when a response is received.
     *
     * @param response
     */
    @Override
    public void onResponse(String response) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mAdviceText.setVisibility(View.VISIBLE);
        mAdviceText.setText(response);
    }
}
