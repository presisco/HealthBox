package com.presisco.shared.ui.framework.navigationdrawerhost;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.presisco.shared.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DrawerHostFragment extends Fragment {
    private DrawerAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean has_header = false;
    private boolean has_footer = false;
    private DrawerAdapter.HeaderListener mOnHeader;
    private int mHeaderLayoutId = 0;
    private DrawerAdapter.ItemListener mOnItem;
    private int mItemLayoutId = 0;
    private DrawerAdapter.FooterListener mOnFooter;
    private int mFooterLayoutId = 0;
    private int mDrawerItemCount = 0;

    public DrawerHostFragment() {
        // Required empty public constructor
    }

    public void setDrawerItemCount(int count) {
        mDrawerItemCount = count;
    }

    public void setHeader(int id, DrawerAdapter.HeaderListener listener) {
        has_header = true;
        mHeaderLayoutId = id;
        mOnHeader = listener;
    }

    public void setItem(int id, DrawerAdapter.ItemListener listener) {
        mItemLayoutId = id;
        mOnItem = listener;
    }

    public void setFooter(int id, DrawerAdapter.FooterListener listener) {
        has_footer = true;
        mFooterLayoutId = id;
        mOnFooter = listener;
    }

    public void replaceContent(Fragment fragment) {
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.contentFrame, fragment);
        trans.commitNow();
    }

    public void setDrawerToggle(ActionBarDrawerToggle toggle) {
        mDrawerToggle = toggle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drawer_host, container, false);

        DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawerLayout);
        if (mDrawerToggle != null) {
            drawerLayout.addDrawerListener(mDrawerToggle);
        }

        RecyclerView drawerListView = (RecyclerView) rootView.findViewById(R.id.drawerList);
        drawerListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDrawerAdapter = new DrawerAdapter(getContext(), mItemLayoutId, mOnItem);
        if (has_header)
            mDrawerAdapter.setHeader(mHeaderLayoutId, mOnHeader);
        if (has_footer)
            mDrawerAdapter.setFooter(mFooterLayoutId, mOnFooter);
        mDrawerAdapter.setItemCount(mDrawerItemCount);
        drawerListView.setAdapter(mDrawerAdapter);

        return rootView;
    }
}
