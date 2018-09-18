package com.ayush.weatherapp.repository.geocoding;

import com.ayush.weatherapp.entities.geocoding.GeolocationEntity;
import com.ayush.weatherapp.mapper.GeocodingDTOToRealmMapper;
import com.ayush.weatherapp.mapper.GeocodingRealmToEntityMapper;
import com.ayush.weatherapp.retrofit.geocodingApi.GeocodingAPIClient;
import com.ayush.weatherapp.retrofit.geocodingApi.GeocodingAPIInterface;
import io.reactivex.Observable;

public class OnlineGeocodingRepositoryImpl implements GeocodingRepository {
  private GeocodingAPIInterface geocodingAPIInterface;

  public OnlineGeocodingRepositoryImpl() {
    geocodingAPIInterface = GeocodingAPIClient.getClient().create(GeocodingAPIInterface.class);
  }

  @Override
  public Observable<GeolocationEntity> getLocation(String latlng, boolean isCurrentLocation) {
    return geocodingAPIInterface.getLocationDetails(latlng)
        .map(GeocodingDTOToRealmMapper::transform)
        .map(GeocodingRealmToEntityMapper::transform);
  }
}
