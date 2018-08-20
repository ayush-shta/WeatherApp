package com.ayush.weatherapp.home;

import android.support.annotation.NonNull;
import com.ayush.weatherapp.home.pojo.CurrentForecast;
import com.ayush.weatherapp.home.pojo.DailyForecast;
import com.ayush.weatherapp.home.pojo.Forecast;
import com.ayush.weatherapp.mvp.BaseContract;
import com.ayush.weatherapp.utils.MapperUtils;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class HomePresenterImpl implements HomeContract.Presenter {

  private HomeContract.View view;

  HomePresenterImpl(BaseContract.View view) {
    this.view = (HomeContract.View) view;
  }

  @Override public HomeContract.View getView() {
    return view;
  }

  @Override public void fetchWeatherDetails() {
    getView().showProgressDialog("Loading", false);

    APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);

    //TODO get coordinates based on a location
    final double TEST_LATITUDE = 37.8267;
    final double TEST_LONGITUDE = -122.4233;
    String requestString = TEST_LATITUDE + "," + TEST_LONGITUDE;

    Call<Forecast> forecastCall = apiInterface.getForecast(requestString);

    forecastCall.enqueue(new Callback<Forecast>() {
      @Override
      public void onResponse(@NonNull Call<Forecast> call, @NonNull Response<Forecast> response) {
        Forecast forecast = response.body();

        CurrentForecast currentForecast = forecast.getCurrentForecast();

        DailyForecast dailyForecast = forecast.getDailyForecast();
        List<DailyForecast.DailyData> dailyForecastList = dailyForecast.getDailyDataList();

        getView().setCurrentForecast(TEST_LATITUDE, TEST_LONGITUDE, currentForecast);

        getView().setDailyForeCast(dailyForecastList);

        getView().hideProgressDialog();
      }

      @Override public void onFailure(@NonNull Call<Forecast> call, @NonNull Throwable t) {
        Timber.e("Request Failed");
        getView().hideProgressDialog();
      }
    });
  }
}
