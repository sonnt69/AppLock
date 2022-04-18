package com.tapbi.applock.common.model;

public class SearchApp {
    private String appName;
    private boolean showSearch;

    public SearchApp(String appName, boolean showSearch) {
        this.appName = appName;
        this.showSearch = showSearch;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isShowSearch() {
        return showSearch;
    }

    public void setShowSearch(boolean showSearch) {
        this.showSearch = showSearch;
    }
}
