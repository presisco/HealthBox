package com.presisco.boxmeter.UI.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.presisco.boxmeter.R;
import com.presisco.boxmeter.Service.BTService;
import com.presisco.boxmeter.Service.DBService;
import com.presisco.boxmeter.Service.HubService;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        new IgniteTask().execute();
    }

    private class IgniteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            WelcomeActivity.this.finish();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                startService(new Intent(WelcomeActivity.this, HubService.class));
                startService(new Intent(WelcomeActivity.this, BTService.class));
                startService(new Intent(WelcomeActivity.this, DBService.class));
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
