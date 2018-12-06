package com.clevertap.android.sdk;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CTNotificationInboxActivity extends FragmentActivity {
    interface InboxActivityListener{
        void messageDidShow();
        void messageDidClick();
    }

    private ArrayList<CTInboxMessage> inboxMessageArrayList;
    private CleverTapInstanceConfig config;
    private CTInboxStyleConfig styleConfig;
    private WeakReference<InboxActivityListener> listenerWeakReference;
    private LinearLayout linearLayout;

    void setListener(InboxActivityListener listener) {
        listenerWeakReference = new WeakReference<>(listener);
    }

    InboxActivityListener getListener() {
        InboxActivityListener listener = null;
        try {
            listener = listenerWeakReference.get();
        } catch (Throwable t) {
            // no-op
        }
        if (listener == null) {
            config.getLogger().verbose(config.getAccountId(),"InboxActivityListener is null for notification inbox " );
        }
        return listener;
    }



    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try{
            Bundle extras = getIntent().getExtras();
            if(extras == null) throw new IllegalArgumentException();
            styleConfig = extras.getParcelable("styleConfig");
            inboxMessageArrayList = extras.getParcelableArrayList("messageList");
            config = extras.getParcelable("config");
            setListener((InboxActivityListener) CleverTapAPI.instanceWithConfig(getApplicationContext(),config));
        }catch (Throwable t){
            Logger.v("Cannot find a valid notification inbox bundle to show!", t);
            return;
        }

        setContentView(R.layout.inbox_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(styleConfig.getNavBarTitle());
        toolbar.setBackgroundColor(Color.parseColor(styleConfig.getNavBarColor()));
        Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        drawable.setColorFilter(Color.parseColor(styleConfig.getBackButtonColor()),PorterDuff.Mode.SRC_IN);
        toolbar.setNavigationIcon(drawable);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        linearLayout = findViewById(R.id.inbox_linear_layout);
        TabLayout tabLayout = linearLayout.findViewById(R.id.tab_layout);
        if(styleConfig.isUsingTabs()){
            ViewPager viewPager = linearLayout.findViewById(R.id.view_pager);
            CTInboxTabAdapter inboxTabAdapter = new CTInboxTabAdapter(getSupportFragmentManager());
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setSelectedTabIndicatorColor(Color.parseColor(styleConfig.getSelectedTabIndicatorColor()));
            tabLayout.setTabTextColors(Color.parseColor(styleConfig.getUnselectedTabColor()),Color.parseColor(styleConfig.getSelectedTabColor()));
            tabLayout.setBackgroundColor(Color.parseColor(styleConfig.getTabBackgroundColor()));
            tabLayout.addTab(tabLayout.newTab().setText("ALL"));
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("inboxMessages", inboxMessageArrayList);
            bundle.putParcelable("config", config);
            CTInboxAllTabFragment ctInboxAllTabFragment = new CTInboxAllTabFragment();
            ctInboxAllTabFragment.setArguments(bundle);
            inboxTabAdapter.addFragment(ctInboxAllTabFragment,"ALL");
            if(styleConfig.getFirstTab() != null) {
                CTInboxFirstTabFragment ctInboxFirstTabFragment = new CTInboxFirstTabFragment();
                ctInboxFirstTabFragment.setArguments(bundle);
                tabLayout.addTab(tabLayout.newTab().setText(styleConfig.getFirstTab()));
                inboxTabAdapter.addFragment(ctInboxFirstTabFragment, styleConfig.getFirstTab());
                viewPager.setOffscreenPageLimit(1);
            }
            if(styleConfig.getSecondTab() != null) {
                CTInboxSecondTabFragment ctInboxSecondTabFragment = new CTInboxSecondTabFragment();
                ctInboxSecondTabFragment.setArguments(bundle);
                tabLayout.addTab(tabLayout.newTab().setText(styleConfig.getSecondTab()));
                inboxTabAdapter.addFragment(ctInboxSecondTabFragment, styleConfig.getSecondTab());
                viewPager.setOffscreenPageLimit(2);
            }
            viewPager.setAdapter(inboxTabAdapter);
            tabLayout.setupWithViewPager(viewPager);
        }else{
            tabLayout.setVisibility(View.GONE);
            //TODO
        }
    }

}
