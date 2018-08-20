package com.ayush.weatherapp.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ayush.weatherapp.R;

public class ForecastDetailCompoundView extends RelativeLayout {

  private ImageView ivLeftIcon;
  private TextView tvTop;
  private TextView tvBottom;

  public ForecastDetailCompoundView(Context context) {
    this(context, null);
  }

  public ForecastDetailCompoundView(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public ForecastDetailCompoundView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  public ForecastDetailCompoundView(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs);
  }

  private void init(@Nullable AttributeSet attributeSet) {
    inflate(getContext(), R.layout.forecast_detail_compound_view, this);

    ivLeftIcon = findViewById(R.id.iv_detail_icon);
    tvTop = findViewById(R.id.tv_top);
    tvBottom = findViewById(R.id.tv_bottom);

    TypedArray typedArray =
        getContext().obtainStyledAttributes(attributeSet, R.styleable.ForecastDetailCompoundView);

    setValues(typedArray);

    typedArray.recycle();
  }

  private void setValues(TypedArray typedArray) {
    ivLeftIcon.setImageDrawable(
        typedArray.getDrawable(R.styleable.ForecastDetailCompoundView_detail_icon));
    tvTop.setText(typedArray.getString(R.styleable.ForecastDetailCompoundView_txt_top));
    tvBottom.setText(typedArray.getString(R.styleable.ForecastDetailCompoundView_txt_bottom));
  }

  public ImageView getIvLeftIcon() {
    return ivLeftIcon;
  }

  public void setIvLeftIcon(int imgResource) {
    ivLeftIcon.setImageResource(imgResource);
  }

  public TextView getTvTop() {
    return tvTop;
  }

  public void setTvTop(String text) {
    tvTop.setText(text);
  }

  public TextView getTvBottom() {
    return tvBottom;
  }

  public void setTvBottom(String text) {
    tvBottom.setText(text);
  }
}