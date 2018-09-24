package com.ayush.weatherapp.home;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import com.ayush.weatherapp.R;
import com.ayush.weatherapp.constants.Temperature;
import com.ayush.weatherapp.constants.TemperatureUnit;
import com.ayush.weatherapp.entities.ForecastEntity;
import com.ayush.weatherapp.mvp.BasePresenterImpl;
import com.ayush.weatherapp.repository.geocoding.GeocodingRepository;
import com.ayush.weatherapp.repository.geocoding.GeocodingRepositoryImpl;
import com.ayush.weatherapp.repository.preferences.PreferenceRepository;
import com.ayush.weatherapp.repository.preferences.PreferenceRepositoryImpl;
import com.ayush.weatherapp.repository.weather.WeatherRepository;
import com.ayush.weatherapp.retrofit.geocodingApi.pojo.Address;
import com.ayush.weatherapp.retrofit.geocodingApi.pojo.AddressComponents;
import com.ayush.weatherapp.retrofit.geocodingApi.pojo.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class HomePresenterImpl extends BasePresenterImpl<HomeContract.View>
    implements HomeContract.Presenter {
  private static final int LOCATION_REQ_INTERVAL = 10000;
  private static final int FASTEST_LOCATION_REQ_INTERVAL = 5000;

  private static final String ADDRESS_STREET = "route";
  private static final String ADDRESS_CITY = "locality";
  private static final String ADDRESS_COUNTRY = "country";
  private static final String ADDRESS_ADMINISTRATIVE_AREA = "administrative_area_level_1";

  private FusedLocationProviderClient fusedLocationProviderClient;
  private LocationRequest locationRequest;
  private LocationCallback locationCallback;

  private PreferenceRepository preferenceRepository;
  private ForecastEntity forecast;

  private GeocodingRepository geocodingRepository;
  private WeatherRepository weatherRepository;

  // TODO dagger
  @Inject public HomePresenterImpl(WeatherRepository weatherRepository) {
    preferenceRepository = PreferenceRepositoryImpl.get();
    this.weatherRepository = weatherRepository;
  }

  @Override public void attachView(HomeContract.View view) {
    super.attachView(view);
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
    preferenceRepository.onPreferenceChangeListener(newTemperature ->
    {
      //need null check because change listener gets called in first launch after app data is cleared
      if (forecast == null) {
        return;
      }
      setForecastView();
    });//todo
    //weatherRepositoryImpl = new WeatherRepositoryImpl(getContext());
    geocodingRepository = new GeocodingRepositoryImpl();
  }

  @Override public void onViewPause() {
    stopLocationUpdates();
  }

  @Override public void initHome() {
    getView().showProgressBar("");
    fetchByCurrentLocation();
    setTemperatureUnit();
  }

  private void setTemperatureUnit() {
    if (preferenceRepository.getTemperatureUnit() == TemperatureUnit.CELSIUS) {
      getView().checkCelsiusButton(true);
    } else {
      getView().checkFahrenheitButton(true);
    }
  }

  @Override public void onViewRestart() {
    if (forecast != null) {
      return;
    }
    initHome();
  }

  @Override public void onSwipeRefresh() {
    fetchByGivenLocation(preferenceRepository.getCurrentLocationCoordinates());
  }

  @Override public void onCurrentLocationClicked() {
    getView().showProgressBar("");
    fetchByCurrentLocation();
  }

  @Override public void saveTemperatureUnitPref(@Temperature int unit) {
    preferenceRepository.saveTemperatureUnit(unit);
  }

  private void fetchByCurrentLocation() {
    if (isLocationServicesEnabled()) {
      getLastKnownLocation();
    }
  }

  private void getLastKnownLocation() {
    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      throw new RuntimeException("Location permission not provided");
    }
    fusedLocationProviderClient.getLastLocation()
        .addOnSuccessListener((Activity) getContext(), location -> {
          if (location == null) {
            initLocationRequest();
            startLocationUpdates();
          } else {
            String latLng = location.getLatitude() + "," + location.getLongitude();
            fetchAddress(latLng);
            fetchWeatherForecast(latLng);
          }
        });
  }

  private void initLocationRequest() {
    locationRequest = new LocationRequest();
    locationRequest.setInterval(LOCATION_REQ_INTERVAL);
    locationRequest.setFastestInterval(FASTEST_LOCATION_REQ_INTERVAL);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    locationCallback = new LocationCallback() {
      @Override public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
          Timber.e("location result null");
          return;
        }
        //get only first location
        Location currentLocation = locationResult.getLocations().get(0);
        String latLng = currentLocation.getLatitude() + "," + currentLocation.getLongitude();

        fetchAddress(latLng);
        fetchWeatherForecast(latLng);

        stopLocationUpdates();
      }
    };
  }

  private void startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      throw new RuntimeException("Location permission not provided");
    }
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
  }

  private void stopLocationUpdates() {
    if (locationCallback != null) {
      fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
  }

  private void fetchAddress(String latLng) {
    Disposable disposable = geocodingRepository.getLocationDetails(latLng)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<GeoLocation>() {
          @Override public void onNext(GeoLocation geoLocation) {
            setLocation(geoLocation);
          }

          @Override public void onError(Throwable e) {
            Timber.e(e);
            e.printStackTrace();
          }

          @Override public void onComplete() {
          }
        });

    addSubscription(disposable);
  }

  private void setLocation(GeoLocation geoLocation) {
    List<Address> addressList = geoLocation.getAddresses();
    getView().setAddress(getAddress(addressList));
  }

  @Override public void searchLocation(double lat, double lng) {
    String latlng = lat + "," + lng;
    fetchByGivenLocation(latlng);
  }

  private void fetchByGivenLocation(String latlng) {
    if (isLocationServicesEnabled()) {
      fetchWeatherForecast(latlng);
      fetchAddress(latlng);
    }
  }

  private String getAddress(List<Address> addressList) {

    if (addressList == null || addressList.isEmpty()) {
      return getString(R.string.not_available);
    }

    List<AddressComponents> addressComponentsList = addressList.get(0).getAddressComponents();

    String primaryAddress = getAddressPrimary(addressComponentsList);
    String secondaryAddress = getAddressSecondary(addressComponentsList);
    String address = "";

    if (!TextUtils.isEmpty(primaryAddress)) {
      address = primaryAddress;
      if (!TextUtils.isEmpty(secondaryAddress)) {
        address += ", " + secondaryAddress;
      }
      return address;
    }

    //case when primary address is empty
    if (!TextUtils.isEmpty(secondaryAddress)) {
      address = secondaryAddress;
      return address;
    }
    return address;
  }

  private String getAddressPrimary(List<AddressComponents> addressComponentsList) {
    for (AddressComponents addressComponent : addressComponentsList) {
      if (addressComponent.getTypes().contains(ADDRESS_STREET)) {
        return addressComponent.getLongName();
      }
    }
    return "";
  }

  private String getAddressSecondary(List<AddressComponents> addressComponentsList) {
    for (AddressComponents addressComponent : addressComponentsList) {
      if (addressComponent.getTypes().contains(ADDRESS_CITY)) {
        return addressComponent.getLongName();
      }
      if (addressComponent.getTypes().contains(ADDRESS_ADMINISTRATIVE_AREA)) {
        return addressComponent.getLongName();
      }
      if (addressComponent.getTypes().contains(ADDRESS_COUNTRY)) {
        return addressComponent.getLongName();
      }
    }
    return "";
  }

  private void fetchWeatherForecast(String latLng) {
    Disposable disposable = weatherRepository.getForecast(latLng)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(d -> getView().showProgressBar(""))
        .subscribeWith(new DisposableObserver<ForecastEntity>() {
          @Override public void onNext(ForecastEntity forecast) {
            setForecast(forecast, latLng);
            getView().hideProgressBar();
          }

          @Override public void onError(Throwable e) {
            getView().onFailure("Error fetching new data");
            e.printStackTrace();
          /*  getView().changeErrorVisibility(true);
            getView().showErrorMessage();*/
            getView().hideProgressBar();
          }

          @Override public void onComplete() {
          }
        });

    addSubscription(disposable);
  }

  private void setForecast(ForecastEntity forecast, String latLng) {
    this.forecast = forecast;
    setForecastView();
    getView().changeErrorVisibility(false);

    //save to provide coordinates during refresh
    preferenceRepository.saveCurrentLocationCoordinates(latLng);
  }

  private void setForecastView() {
    weatherRepository.checkTemperatureUnit(forecast);
    getView().setDailyForeCast(forecast.getDailyForecastEntity().getDailyDataEntityList());
    getView().setHourlyForeCast(forecast.getHourlyForecastEntity().getHourlyDataEntityList());
    getView().setCurrentForecast(forecast.getCurrentForecastEntity());
    getView().setCurrentTemperature(forecast.getCurrentForecastEntity().getTemperature(),
        preferenceRepository.getTemperatureUnit());
    getView().setTodaysForecastDetail(forecast.getDailyForecastEntity().getTodaysDataEntity(),
        preferenceRepository.getTemperatureUnit());
    getView().setTabLayout();
  }

  private boolean isLocationServicesEnabled() {
    LocationManager locationManager =
        (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
    boolean gpsEnabled;
    boolean networkEnabled;

    gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    if (!gpsEnabled && !networkEnabled) {
      getView().showGPSNotEnabledDialog(getString(R.string.location_services_not_enabled),
          getString(R.string.open_location_settings));
      return false;
    }
    return true;
  }
}
