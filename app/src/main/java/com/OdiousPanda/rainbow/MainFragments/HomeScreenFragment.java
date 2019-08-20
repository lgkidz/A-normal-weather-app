package com.OdiousPanda.rainbow.MainFragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.OdiousPanda.rainbow.Adapters.DailyForecastAdapter;
import com.OdiousPanda.rainbow.CustomUI.ExpandCollapseAnimation;
import com.OdiousPanda.rainbow.CustomUI.MovableConstrainLayout;
import com.OdiousPanda.rainbow.DataModel.Quote;
import com.OdiousPanda.rainbow.DataModel.Unsplash.Unsplash;
import com.OdiousPanda.rainbow.DataModel.Weather.Weather;
import com.OdiousPanda.rainbow.R;
import com.OdiousPanda.rainbow.Utilities.MyColorUtil;
import com.OdiousPanda.rainbow.Utilities.MyTextUtil;
import com.OdiousPanda.rainbow.Utilities.PreferencesUtil;
import com.OdiousPanda.rainbow.Utilities.QuoteGenerator;
import com.OdiousPanda.rainbow.Utilities.UnitConverter;

import java.util.Objects;

public class HomeScreenFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static HomeScreenFragment instance;
    private final int pointerAnimationDuration = 1000;
    public boolean photoDetailsShowing = false;
    private MovableConstrainLayout layoutData;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTemp;
    private TextView tvDescription;
    private TextView tvBigText;
    private TextView tvSmallText;
    private ImageView iconInfo;
    private TextView tvLocation;
    private View tempBar;
    private View tempPointer;
    private TextView tvMinTemp;
    private TextView tvMaxTemp;
    private TextView tvUvIndex;
    private TextView tvHumidity;
    private TextView tvPrecipitation;
    private ImageView icPrecipitationType;
    private TextView tvUvSummary;
    private ConstraintLayout tempLayout;
    private RecyclerView rvDailyForecast;
    private ConstraintLayout photoDetailsLayout;
    private ImageView btnClosePhotoDetails;
    private TextView tvPhotoTitle;
    private TextView tvPhotoBy;
    private TextView tvCamera;
    private TextView tvAperture;
    private TextView tvExposureTime;
    private TextView tvIso;
    private TextView tvFocalLength;
    private QuoteGenerator quoteGenerator;
    private Weather currentWeather;
    private float pointerPreviousX = 0;
    private float previousTemp = 0;
    private int previousTempColor = 0;
    private float tempPreviousX = pointerPreviousX;
    private OnLayoutRefreshListener callback;

    public HomeScreenFragment() {
        // Required empty public constructor
    }

    public static HomeScreenFragment getInstance() {
        if (instance == null) {
            instance = new HomeScreenFragment();
        }

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_screen, container, false);
        initViews(v);
        quoteGenerator = new QuoteGenerator(getActivity());
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initViews(View v) {
        layoutData = v.findViewById(R.id.layout_data);
        tvBigText = v.findViewById(R.id.big_text);
        tvDescription = v.findViewById(R.id.tv_description);
        tvSmallText = v.findViewById(R.id.small_text);
        tvTemp = v.findViewById(R.id.tv_temp);
        iconInfo = v.findViewById(R.id.icon);
        tvLocation = v.findViewById(R.id.tv_location);
        tempBar = v.findViewById(R.id.temperature_bar);
        tempPointer = v.findViewById(R.id.temperature_pointer);
        tvMinTemp = v.findViewById(R.id.min_temp);
        tvMaxTemp = v.findViewById(R.id.max_temp);
        tvUvIndex = v.findViewById(R.id.uv_value);
        tvHumidity = v.findViewById(R.id.humidity_value);
        tvPrecipitation = v.findViewById(R.id.precipitation_value);
        icPrecipitationType = v.findViewById(R.id.precipitation_type);
        tvUvSummary = v.findViewById(R.id.uv_summary);
        tempLayout = v.findViewById(R.id.temp_layout);
        if (PreferencesUtil.getBackgroundSetting(Objects.requireNonNull(getActivity())).equals(PreferencesUtil.BACKGROUND_PICTURE_RANDOM)) {
            iconInfo.setVisibility(View.VISIBLE);
        } else {
            iconInfo.setVisibility(View.GONE);
        }
        photoDetailsLayout = v.findViewById(R.id.photo_detail_layout);
        tvPhotoTitle = v.findViewById(R.id.photo_name);
        tvPhotoBy = v.findViewById(R.id.photo_by);
        btnClosePhotoDetails = v.findViewById(R.id.close_photo_detail);
        tvCamera = v.findViewById(R.id.camera_name);
        tvAperture = v.findViewById(R.id.tvAperture);
        tvExposureTime = v.findViewById(R.id.tvExposureTime);
        tvFocalLength = v.findViewById(R.id.tvFocalLength);
        tvIso = v.findViewById(R.id.tvIso);
        tvPhotoBy.setClickable(true);
        tvPhotoBy.setMovementMethod(LinkMovementMethod.getInstance());
        ExpandCollapseAnimation.collapse(photoDetailsLayout);
        iconInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoDetailBox();
            }
        });

        btnClosePhotoDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePhotoDetailBox();
            }
        });

        rvDailyForecast = v.findViewById(R.id.daily_forecast_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvDailyForecast.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvDailyForecast.getContext(),
                linearLayoutManager.getOrientation());
        rvDailyForecast.addItemDecoration(dividerItemDecoration);
        swipeRefreshLayout = v.findViewById(R.id.home_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callback.updateData();
            }
        });
    }

    public void setColorTheme(int textColor) {
        int colorFrom = tvTemp.getCurrentTextColor();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, textColor);
        colorAnimation.setDuration(200); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                tvLocation.setTextColor((int) animator.getAnimatedValue());
                tvBigText.setTextColor((int) animator.getAnimatedValue());
                tvSmallText.setTextColor((int) animator.getAnimatedValue());
            }

        });
        if (PreferencesUtil.getBackgroundSetting(Objects.requireNonNull(getActivity())).equals(PreferencesUtil.BACKGROUND_PICTURE_RANDOM)) {
            iconInfo.setVisibility(View.VISIBLE);
        } else {
            iconInfo.setVisibility(View.GONE);
        }
        colorAnimation.start();
        if (textColor == Color.WHITE) {
            iconInfo.setImageResource(R.drawable.ic_info_w);
        } else {
            iconInfo.setImageResource(R.drawable.ic_info_b);
        }
    }

    void updateUnit() {
        String currentTempUnit = PreferencesUtil.getTemperatureUnit(Objects.requireNonNull(getActivity()));
        tvTemp.setText(UnitConverter.convertToTemperatureUnitClean(currentWeather.getCurrently().getTemperature(), currentTempUnit));
        tvMinTemp.setText(UnitConverter.convertToTemperatureUnitClean(currentWeather.getDaily().getData().get(0).getTemperatureLow(), currentTempUnit));
        tvMaxTemp.setText(UnitConverter.convertToTemperatureUnitClean(currentWeather.getDaily().getData().get(0).getTemperatureMax(), currentTempUnit));
        updatePrecipitationData();
        DailyForecastAdapter adapter = new DailyForecastAdapter(getActivity(), currentWeather.getDaily());
        rvDailyForecast.setAdapter(adapter);
    }

    public void updateData(Weather weather) {
        currentWeather = weather;
        quoteGenerator.updateHomeScreenQuote(weather);
        updateUvData();
        updateTemperatureData();
        updateHumidityData();
        updatePrecipitationData();
        updateDailyForecastData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateTemperatureData() {
        float currentTemp = currentWeather.getCurrently().getTemperature();
        final float minTemp = currentWeather.getDaily().getData().get(0).getTemperatureLow();
        final float maxTemp = currentWeather.getDaily().getData().get(0).getTemperatureMax();
        final String currentTempUnit = PreferencesUtil.getTemperatureUnit(Objects.requireNonNull(getActivity()));
        currentTemp = Math.min(maxTemp, Math.max(currentTemp, minTemp));
        tvMinTemp.setText(UnitConverter.convertToTemperatureUnitClean(minTemp, currentTempUnit));
        tvMaxTemp.setText(UnitConverter.convertToTemperatureUnitClean(maxTemp, currentTempUnit));
        tvDescription.setText(currentWeather.getCurrently().getSummary());
        if (previousTempColor == 0) {
            previousTempColor = ContextCompat.getColor(getActivity(), R.color.coldBlue);
        }
        final float finalCurrentTemp = currentTemp;
        tempBar.post(new Runnable() {
            @Override
            public void run() {
                try {
                    tempPointer.setVisibility(View.VISIBLE);
                    ConstraintLayout.LayoutParams pointerParams = (ConstraintLayout.LayoutParams) tempPointer.getLayoutParams();
                    float layoutWidth = (float) tempLayout.getWidth();
                    float tempBarWidth = (float) tempBar.getWidth();
                    float tvTempWidth = (float) tvTemp.getWidth();
                    float leftMargin = (finalCurrentTemp - minTemp) / (maxTemp - minTemp) * tempBarWidth;
                    float tempLeftMargin = leftMargin;
                    if (leftMargin < tvTempWidth / 2) {
                        tempLeftMargin = tvTempWidth / 2;
                    }
                    if (layoutWidth - tempLeftMargin < tvTempWidth) {
                        tempLeftMargin = layoutWidth - tvTempWidth;
                    }
                    if (leftMargin < Objects.requireNonNull(getActivity()).getResources().getDimension(R.dimen.margin_4)) {
                        leftMargin = getActivity().getResources().getDimension(R.dimen.margin_4);
                    }
                    if (tempBarWidth - leftMargin < Objects.requireNonNull(getActivity()).getResources().getDimension(R.dimen.margin_4)) {
                        leftMargin -= getActivity().getResources().getDimension(R.dimen.margin_4);
                    }
                    if (pointerPreviousX == 0) {
                        pointerPreviousX = pointerParams.leftMargin;
                    }
                    final int newTempColor = MyColorUtil.getTemperaturePointerColor(Objects.requireNonNull(getActivity()), (finalCurrentTemp - minTemp) / (maxTemp - minTemp));
                    Animation pointerSlideAnimation = new TranslateAnimation(pointerPreviousX, leftMargin, 0, 0);
                    pointerSlideAnimation.setInterpolator(new DecelerateInterpolator());
                    pointerSlideAnimation.setDuration(pointerAnimationDuration);
                    pointerSlideAnimation.setFillAfter(true);
                    final float finalLeftMargin = leftMargin;
                    final float finalTempLeftMargin = tempLeftMargin;
                    pointerSlideAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            pointerPreviousX = finalLeftMargin;
                            tempPreviousX = finalTempLeftMargin;
                            previousTemp = finalCurrentTemp;
                            previousTempColor = newTempColor;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    Animation tempSlideAnimation = new TranslateAnimation(tempPreviousX, tempLeftMargin, 0, 0);
                    tempSlideAnimation.setInterpolator(new DecelerateInterpolator());
                    tempSlideAnimation.setDuration(pointerAnimationDuration);
                    tempSlideAnimation.setFillAfter(true);
                    ValueAnimator tempChange = ValueAnimator.ofFloat(previousTemp, finalCurrentTemp);
                    tempChange.setDuration(pointerAnimationDuration);
                    tempChange.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            tvTemp.setText(UnitConverter.convertToTemperatureUnitClean((float) animation.getAnimatedValue(), currentTempUnit));
                        }
                    });

                    ValueAnimator tempColorChange = ValueAnimator.ofObject(new ArgbEvaluator(), previousTempColor, newTempColor);
                    tempColorChange.setDuration(pointerAnimationDuration);
                    tempColorChange.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int color = (int) animation.getAnimatedValue();
                            Drawable pointerBackground = DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(getActivity(), R.drawable.temperature_pointer)));
                            pointerBackground.setTint(color);
                            tempPointer.setBackground(pointerBackground);
                            tvTemp.setTextColor(color);
                            tvDescription.setTextColor(color);
                        }
                    });
                    tempChange.start();
                    tempColorChange.start();
                    tvTemp.startAnimation(tempSlideAnimation);
                    tempPointer.startAnimation(pointerSlideAnimation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateDailyForecastData() {
        DailyForecastAdapter adapter = new DailyForecastAdapter(getActivity(), currentWeather.getDaily());
        rvDailyForecast.setAdapter(adapter);
    }

    private void updateUvData() {
        String uvSummary;
        if (currentWeather.getCurrently().getUvIndex() == 0) {
            uvSummary = getString(R.string.uv_summary_0);
        } else if (currentWeather.getCurrently().getUvIndex() < 3) {
            uvSummary = getString(R.string.uv_summary_3);
        } else if (currentWeather.getCurrently().getUvIndex() < 6) {
            uvSummary = getString(R.string.uv_summary_6);
        } else if (currentWeather.getCurrently().getUvIndex() < 8) {
            uvSummary = getString(R.string.uv_summary_8);
        } else if (currentWeather.getCurrently().getUvIndex() < 11) {
            uvSummary = getString(R.string.uv_summary_11);
        } else {
            uvSummary = getString(R.string.uv_summary_11_above);
        }
        String uvValueText = String.valueOf(Math.round(currentWeather.getCurrently().getUvIndex()));
        tvUvIndex.setText(uvValueText);
        tvUvSummary.setText(uvSummary);
    }

    private void updatePrecipitationData() {
        String type = currentWeather.getDaily().getData().get(0).getPrecipType();
        float value = currentWeather.getCurrently().getPrecipProbability();
        String valueString = (int) (value * 100) + "%";
        tvPrecipitation.setText(valueString);
        if (type != null) {
            icPrecipitationType.setImageResource(type.equals("rain") ? R.drawable.ic_rain_chance : R.drawable.ic_snow_chance);
        }
    }

    private void updateHumidityData() {
        String humidityText = Math.round(currentWeather.getCurrently().getHumidity() * 100) + "%";
        tvHumidity.setText(humidityText);
    }

    public void updateCurrentLocationName(String locationName) {
        if (this.tvLocation != null) {
            this.tvLocation.setText(locationName);
        }
    }

    void updateExplicitSetting() {
        quoteGenerator.updateHomeScreenQuote(currentWeather);
    }

    public void setOnRefreshListener(OnLayoutRefreshListener callback) {
        this.callback = callback;
    }

    public void updateQuote(Quote quote) {
        tvBigText.setText(quote.getMain());
        tvSmallText.setText(quote.getSub());
    }

    public void updatePhotoDetail(Unsplash unsplash) {
        String photoTitle = unsplash.description != null ? unsplash.description.substring(0, 1).toUpperCase() + unsplash.description.substring(1) : getString(R.string.photo_name_na);
        String userProfileLink = unsplash.user.links.html;
        String userName = unsplash.user.name;
        String cameraMaker = unsplash.exif.make;
        String cameraModel = unsplash.exif.model != null ? unsplash.exif.model : getString(R.string.not_available);
        if (cameraMaker != null) {
            if (cameraMaker.toLowerCase().contains("canon") || cameraMaker.toLowerCase().contains("nikon")) {
                cameraMaker = "";
            }
        } else {
            cameraMaker = "";
        }
        String camera = cameraMaker + " " + cameraModel;
        String aperture = unsplash.exif.aperture != null ? "f/" + unsplash.exif.aperture : getString(R.string.not_available);
        String focalLength = unsplash.exif.focalLength != null ? unsplash.exif.focalLength + " mm" : getString(R.string.not_available);
        String iso = unsplash.exif.iso != null ? String.valueOf(unsplash.exif.iso) : getString(R.string.not_available);
        String exposureTIme = unsplash.exif.exposureTime != null ? unsplash.exif.exposureTime : getString(R.string.not_available);

        tvPhotoTitle.setText(photoTitle);

        tvPhotoBy.setText(MyTextUtil.getReferralHtml(getActivity(), userProfileLink, userName));
        tvCamera.setText(camera);
        tvAperture.setText(aperture);
        tvFocalLength.setText(focalLength);
        tvIso.setText(iso);
        tvExposureTime.setText(exposureTIme);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void openPhotoDetailBox() {
        iconInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        btnClosePhotoDetails.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        ExpandCollapseAnimation.expand(photoDetailsLayout);
        photoDetailsShowing = true;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void closePhotoDetailBox() {
        btnClosePhotoDetails.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        iconInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        ExpandCollapseAnimation.collapse(photoDetailsLayout);
        photoDetailsShowing = false;
    }

    public interface OnLayoutRefreshListener {
        void updateData();
    }

}
