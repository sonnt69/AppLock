package com.tapbi.applock.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class ImageThief implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "uri")
    private String uri;

    @ColumnInfo(name = "cachePath")
    private String cachePath;

    @ColumnInfo(name = "time")
    private String time;

    private boolean isSelected = false;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    @Ignore
    public ImageThief(){
    }

    public ImageThief(@NonNull Integer id, String uri, String cachePath, String time) {
        this.id = id;
        this.uri = uri;
        this.cachePath = cachePath;
        this.time = time;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public String getCachePath() {
        return cachePath;
    }

    public String getTime() {
        return time;
    }
}
