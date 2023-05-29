package com.clevertap.android.sdk;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class has all the parameters required to configure the styling of your {@link a1}
 * All the setter methods are public and the object of this class is made immutable by {@link CleverTapAPI}
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CTInboxStyleConfig implements Parcelable {

    private final static int MAX_TABS = 2;

    /**
     * CleverTap Android SDK targets to Android 28 (Pie), hence the support libraries
     * support-appcompat, support-v4 and support-design also point to version 28.0.0.
     * In this version of support-design, TabLayout.OnTabSelectedListener extends TabLayout.BaseOnTabSelectedListener.
     * While in previous versions of the support-design library (27.1.1 and below),
     * TabLayout.OnTabSelectedListener was a standalone interface. So if your app is targeting to
     * Android 26 or Android 27, the support-design library is missing a crucial interface needed to show
     * Tabs on the App Inbox. To ensure that this doesn't cause your app to crash, this method checks for
     * TabLayout.BaseOnTabSelectedListener before setting Tabs as a part of your App Inbox.
     */
    private static boolean platformSupportsTabs;
    static {
        try {
            TabLayout.OnTabSelectedListener test = new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {}
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            };
            // noinspection ConstantConditions
            platformSupportsTabs = test instanceof TabLayout.BaseOnTabSelectedListener;
        } catch (Throwable t){
            // no-op
        }
    }

    private String navBarColor;
    private String navBarTitle;
    private String navBarTitleColor;
    private String inboxBackgroundColor;
    private String backButtonColor;
    private String selectedTabColor;
    private String unselectedTabColor;
    private String selectedTabIndicatorColor;
    private String tabBackgroundColor;

    private String[] tabs;

    public CTInboxStyleConfig(){
        this.navBarColor = "#FFFFFF";
        this.navBarTitle = "App Inbox";
        this.navBarTitleColor = "#333333";
        this.inboxBackgroundColor = "#D3D4DA";
        this.backButtonColor = "#333333";
        this.selectedTabColor = "#1C84FE";
        this.unselectedTabColor = "#808080";
        this.selectedTabIndicatorColor = "#1C84FE";
        this.tabBackgroundColor = "#FFFFFF";
        this.tabs = new String[0];
    }

    CTInboxStyleConfig(CTInboxStyleConfig config){
        this.navBarColor = config.navBarColor;
        this.navBarTitle = config.navBarTitle;
        this.navBarTitleColor = config.navBarTitleColor;
        this.inboxBackgroundColor = config.inboxBackgroundColor;
        this.backButtonColor = config.backButtonColor;
        this.selectedTabColor = config.selectedTabColor;
        this.unselectedTabColor = config.unselectedTabColor;
        this.selectedTabIndicatorColor = config.selectedTabIndicatorColor;
        this.tabBackgroundColor = config.tabBackgroundColor;
        this.tabs = (config.tabs == null) ? new String[0] : Arrays.copyOf(config.tabs, config.tabs.length);
    }

    protected CTInboxStyleConfig(Parcel in) {
        navBarColor = in.readString();
        navBarTitle = in.readString();
        navBarTitleColor = in.readString();
        inboxBackgroundColor = in.readString();
        tabs = in.createStringArray();
        backButtonColor = in.readString();
        selectedTabColor = in.readString();
        unselectedTabColor = in.readString();
        selectedTabIndicatorColor = in.readString();
        tabBackgroundColor = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(navBarColor);
        dest.writeString(navBarTitle);
        dest.writeString(navBarTitleColor);
        dest.writeString(inboxBackgroundColor);
        dest.writeStringArray(tabs);
        dest.writeString(backButtonColor);
        dest.writeString(selectedTabColor);
        dest.writeString(unselectedTabColor);
        dest.writeString(selectedTabIndicatorColor);
        dest.writeString(tabBackgroundColor);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CTInboxStyleConfig> CREATOR = new Parcelable.Creator<CTInboxStyleConfig>() {
        @Override
        public CTInboxStyleConfig createFromParcel(Parcel in) {
            return new CTInboxStyleConfig(in);
        }

        @Override
        public CTInboxStyleConfig[] newArray(int size) {
            return new CTInboxStyleConfig[size];
        }
    };


    public String getNavBarColor() {
        return navBarColor;
    }

    /**
     * Sets the color for the top navigation toolbar
     * @param navBarColor String - hexcode of the color
     */
    public void setNavBarColor(String navBarColor) {
        this.navBarColor = navBarColor;
    }

    public String getNavBarTitle() {
        return navBarTitle;
    }

    /**
     * Sets the text for the top navigation toolbar
     * @param navBarTitle String
     */
    public void setNavBarTitle(String navBarTitle) {
        this.navBarTitle = navBarTitle;
    }

    public String getNavBarTitleColor() {
        return navBarTitleColor;
    }

    /**
     * Sets the color for the title in the top navigation toolbar
     * @param navBarTitleColor String - hexcode of the color
     */
    public void setNavBarTitleColor(String navBarTitleColor) {
        this.navBarTitleColor = navBarTitleColor;
    }

    public String getInboxBackgroundColor() {
        return inboxBackgroundColor;
    }

    /**
     * Sets the background color for the entire inbox
     * @param inboxBackgroundColor - String - hexcode of the color
     */
    public void setInboxBackgroundColor(String inboxBackgroundColor) {
        this.inboxBackgroundColor = inboxBackgroundColor;
    }

    /**
     * Sets the name of the optional two tabs.
     * The contents of the tabs are filtered based on the name of the tab.
     * @param tabs ArrayList of Strings
     */
    public void setTabs(ArrayList<String>tabs) {
        if (tabs == null || tabs.size() <= 0) return;

        if (platformSupportsTabs) {
            ArrayList<String> toAdd;
            if (tabs.size() > MAX_TABS) {
                toAdd = new ArrayList<>(tabs.subList(0, MAX_TABS));
            } else {
                toAdd = tabs;
            }
            this.tabs = toAdd.toArray(new String[0]);
        } else {
            Logger.d("Please upgrade com.android.support:design library to v28.0.0 to enable Tabs for App Inbox, dropping Tabs");
        }
    }

    public ArrayList<String> getTabs() {
        return this.tabs == null ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(this.tabs));
    }

    boolean isUsingTabs() {
        return (tabs != null && tabs.length > 0);
    }

    public String getBackButtonColor() {
        return backButtonColor;
    }

    /**
     * Sets the color of the back button on the top navigation toolbar
     * @param backButtonColor  String - hexcode of the color
     */
    public void setBackButtonColor(String backButtonColor) {
        this.backButtonColor = backButtonColor;
    }

    public String getSelectedTabColor() {
        return selectedTabColor;
    }

    /**
     * Sets the color of the selected tab
     * @param selectedTabColor String - hexcode of the color
     */
    public void setSelectedTabColor(String selectedTabColor) {
        this.selectedTabColor = selectedTabColor;
    }

    public String getUnselectedTabColor() {
        return unselectedTabColor;
    }

    /**
     * Sets the color of the unselected tab
     * @param unselectedTabColor String - hexcode of the color
     */
    public void setUnselectedTabColor(String unselectedTabColor) {
        this.unselectedTabColor = unselectedTabColor;
    }

    public String getSelectedTabIndicatorColor() {
        return selectedTabIndicatorColor;
    }

    /**
     * Sets the color of the indicator of the selected tab
     * @param selectedTabIndicatorColor String - hexcode of the color
     */
    public void setSelectedTabIndicatorColor(String selectedTabIndicatorColor) {
        this.selectedTabIndicatorColor = selectedTabIndicatorColor;
    }

    public String getTabBackgroundColor() {
        return tabBackgroundColor;
    }

    /**
     * Sets the background color for the tabs
     * @param tabBackgroundColor String - hexcode of the color
     */
    public void setTabBackgroundColor(String tabBackgroundColor) {
        this.tabBackgroundColor = tabBackgroundColor;
    }
}
