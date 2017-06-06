package com.presisco.boxmeter.UI.Activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.presisco.boxmeter.R;
import com.presisco.boxmeter.UI.Fragment.AnalyzeFragment;
import com.presisco.boxmeter.UI.Fragment.HistoryFragment;
import com.presisco.boxmeter.UI.Fragment.PersonalFragment;
import com.presisco.boxmeter.UI.Fragment.RealTimeFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ContentPage[] mContentPages;
    private Resources res;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int default_color;
    private int selected_color;

    private void prepareContentPages() {
        mContentPages = new ContentPage[]{
                new ContentPage(
                        RealTimeFragment.newInstance(),
                        res.getString(R.string.title_real_time),
                        R.drawable.ic_realtime_default,
                        R.drawable.ic_realtime_selected
                ),
                new ContentPage(
                        HistoryFragment.newInstance(),
                        res.getString(R.string.title_history),
                        R.drawable.ic_history_default,
                        R.drawable.ic_history_selected
                ),
                new ContentPage(
                        new AnalyzeFragment(),
                        res.getString(R.string.label_analyze_mode),
                        R.drawable.ic_analyze_default,
                        R.drawable.ic_analyze_selected
                ),
                new ContentPage(
                        PersonalFragment.newInstance(),
                        res.getString(R.string.title_personal),
                        R.drawable.ic_personal_default,
                        R.drawable.ic_personal_selected)
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        res = getResources();
        default_color = res.getColor(R.color.colorIconDefault);
        selected_color = res.getColor(R.color.colorIconSelected);

        prepareContentPages();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.label_drawer_open,
                R.string.label_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        replacePage(0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private void replacePage(int position) {
        getSupportActionBar().setTitle(mContentPages[position].mTitle);
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.layoutHost, mContentPages[position].mFragment);
        trans.commitNow();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_real_time:
                replacePage(0);
                break;
            case R.id.nav_history:
                replacePage(1);
                break;
            case R.id.nav_analyze:
                replacePage(2);
                break;
            case R.id.nav_personal:
                replacePage(3);
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class ContentPage {
        public Fragment mFragment;
        public String mTitle;
        public int mIcon;
        public int mIcon2;

        public ContentPage(Fragment fragment, String title, int icon, int icon2) {
            mFragment = fragment;
            mTitle = title;
            mIcon = icon;
            mIcon2 = icon2;
        }
    }
}
