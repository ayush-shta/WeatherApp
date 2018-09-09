package com.ayush.weatherapp.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

public abstract class MVPBaseActivity<T extends BasePresenterImpl> extends BaseActivity
    implements BaseContract.BaseView {
  private T presenter;

  public abstract T getPresenter();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    presenter = getPresenter();
    presenter.attachView(this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    presenter.detachView();
  }

  @Override public Context getContext() {
    return this;
  }
}
