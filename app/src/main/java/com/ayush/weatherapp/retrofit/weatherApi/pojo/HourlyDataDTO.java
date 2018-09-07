package com.ayush.weatherapp.retrofit.weatherApi.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class HourlyDataDTO implements Parcelable {
  public static final Creator<HourlyDataDTO> CREATOR =
      new Creator<HourlyDataDTO>() {
        @Override
        public HourlyDataDTO createFromParcel(Parcel in) {
          return new HourlyDataDTO(in);
        }

        @Override
        public HourlyDataDTO[] newArray(int size) {
          return new HourlyDataDTO[size];
        }
      };
  @SerializedName("time") private long time;
  @SerializedName("summary") private String summary;
  @SerializedName("icon") private String icon;
  @SerializedName("temperature") private double temperature;
  @SerializedName("apparentTemperature") private double apparentTemperature;

  HourlyDataDTO(Parcel in) {
    time = in.readLong();
    summary = in.readString();
    icon = in.readString();
    temperature = in.readDouble();
    apparentTemperature = in.readDouble();
  }

  public long getTime() {
    return time;
  }

  public String getSummary() {
    return summary;
  }

  public String getIcon() {
    return icon;
  }

  public double getTemperature() {
    return temperature;
  }

  public double getApparentTemperature() {
    return apparentTemperature;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(time);
    dest.writeString(summary);
    dest.writeString(icon);
    dest.writeDouble(temperature);
    dest.writeDouble(apparentTemperature);
  }

  public void setTime(long time) {
    this.time = time;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public void setApparentTemperature(double apparentTemperature) {
    this.apparentTemperature = apparentTemperature;
  }
}