package com.ayush.weatherapp.repository.geocoding;

import com.ayush.weatherapp.entities.geocoding.GeolocationEntity;
import com.ayush.weatherapp.mapper.GeocodingRealmToEntityMapper;
import com.ayush.weatherapp.realm.RealmUtils;
import com.ayush.weatherapp.realm.model.geocoding.GeoLocation;
import io.reactivex.Observable;

public class LocalGeocodingRepositoryImpl implements GeocodingRepository {
  @Override public Observable<GeolocationEntity> getLocation(String latlng) {
    GeoLocation geoLocation = RealmUtils.getRealmModel(GeoLocation.class,
        RealmUtils.getMaxIdForPrimaryKey(GeoLocation.class));

    if (geoLocation != null) {
      return Observable.just(geoLocation).map(GeocodingRealmToEntityMapper::transform);
    }
    return Observable.error(new NullPointerException());
  }
}