package com.ayush.weatherapp.entities.geocoding;

import java.util.List;

public class AddressComponentsEntity {
  private String longName;
  private String shortName;
  private List<String> types;

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public List<String> getTypes() {
    return types;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }
}
