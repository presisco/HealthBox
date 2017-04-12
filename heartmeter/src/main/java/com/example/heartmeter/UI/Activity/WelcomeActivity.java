package com.example.heartmeter.UI.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.heartmeter.R;
import com.example.heartmeter.Service.BTService;
import com.example.heartmeter.Service.DBService;
import com.example.heartmeter.Service.HubService;

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
            startService(new Intent(WelcomeActivity.this, HubService.class));
            startService(new Intent(WelcomeActivity.this, BTService.class));
            startService(new Intent(WelcomeActivity.this, DBService.class));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
