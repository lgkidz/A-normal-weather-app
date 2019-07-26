package com.OdiousPanda.thefweather.MainFragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import com.OdiousPanda.thefweather.Adapters.DailyForecastAdapter;
import com.OdiousPanda.thefweather.DataModel.Quote;
import com.OdiousPanda.thefweather.DataModel.Weather.Weather;
import com.OdiousPanda.thefweather.R;
import com.OdiousPanda.thefweather.Utilities.MyColorUtil;
import com.OdiousPanda.thefweather.Utilities.PreferencesUtil;
import com.OdiousPanda.thefweather.Utilities.QuoteGenerator;
import com.OdiousPanda.thefweather.Utilities.UnitConverter;

import java.util.Objects;

public class HomeScreenFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static HomeScreenFragment instance;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTemp;
    private TextView tvDescription;
    private TextView tvBigText;
    private TextView tvSmallText;
    private ImageView icon;
    private String iconName;
    private TextView tvLocation;
    private View tempBar;
    private View tempPointer;
    private TextView tvMinTemp;
    private TextView tvMaxTemp;
    private TextView tvUvIndex;
    private TextView tvHumidity;
    private TextView tvVisibility;
    private TextView tvUvSummary;
    private ConstraintLayout tempLayout;
    private RecyclerView rvDailyForecast;
    private QuoteGenerator quoteGenerator;
    private Weather currentWeather;
    private float pointerPreviousX = 0;
    private float previousTemp = 0;
    private int previousTempColor = 0;
    private float tempPreviousX = pointerPreviousX;
    private final int pointerAnimationDuration = 1000;
    private OnLayoutRefreshListener callback;
    private boolean showingBackground = false;
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
        tvBigText = v.findViewById(R.id.big_text);
        tvDescription = v.findViewById(R.id.tv_description);
        tvSmallText = v.findViewById(R.id.small_text);
        tvTemp = v.findViewById(R.id.tv_temp);
        icon = v.findViewById(R.id.icon);
        tvLocation = v.findViewById(R.id.tv_location);
        tempBar = v.findViewById(R.id.temperature_bar);
        tempPointer = v.findViewById(R.id.temperature_pointer);
        tvMinTemp = v.findViewById(R.id.min_temp);
        tvMaxTemp = v.findViewById(R.id.max_temp);
        tvUvIndex = v.findViewById(R.id.uv_value);
        tvHumidity = v.findViewById(R.id.humidity_value);
        tvVisibility = v.findViewById(R.id.visibility_value);
        tvUvSummary = v.findViewById(R.id.uv_summary);
        tempLayout = v.findViewById(R.id.temp_layout);
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
        colorAnimation.start();
        if (textColor == Color.WHITE) {
            int iconResourceId = Objects.requireNonNull(getActivity()).getResources().getIdentifier("drawable/" + iconName + "_w", null, getActivity().getPackageName());
            icon.setImageResource(iconResourceId);
        } else {
            int iconResourceId = Objects.requireNonNull(getActivity()).getResources().getIdentifier("drawable/" + iconName + "_b", null, getActivity().getPackageName());
            icon.setImageResource(iconResourceId);
        }
    }

    void updateUnit() {
        String currentTempUnit = PreferencesUtil.getTemperatureUnit(Objects.requireNonNull(getActivity()));
        tvTemp.setText(UnitConverter.convertToTemperatureUnitClean(currentWeather.getCurrently().getTemperature(), currentTempUnit));
        tvMinTemp.setText(UnitConverter.convertToTemperatureUnitClean(currentWeather.getDaily().getData().get(0).getTemperatureLow(),currentTempUnit));
        tvMaxTemp.setText(UnitConverter.convertToTemperatureUnitClean(currentWeather.getDaily().getData().get(0).getTemperatureMax(),currentTempUnit));
        DailyForecastAdapter adapter = new DailyForecastAdapter(getActivity(), currentWeather.getDaily());
        rvDailyForecast.setAdapter(adapter);
    }

    public void updateData(Weather weather) {
        currentWeather = weather;
        quoteGenerator.updateHomeScreenQuote(weather);

        String iconNameRaw = currentWeather.getCurrently().getIcon();
        iconName = iconNameRaw.replace("-", "_");
        updateUvData();
        updateTemperatureData();
        updateHumidityData();
        updateVisibilityData();
        updateDailyForecastData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateTemperatureData(){
        float currentTemp = currentWeather.getCurrently().getTemperature();
        final float minTemp = currentWeather.getDaily().getData().get(0).getTemperatureLow();
        final float maxTemp = currentWeather.getDaily().getData().get(0).getTemperatureMax();
        final String currentTempUnit = PreferencesUtil.getTemperatureUnit(Objects.requireNonNull(getActivity()));
        currentTemp = Math.min(maxTemp, Math.max(currentTemp,minTemp));
        tvMinTemp.setText(UnitConverter.convertToTemperatureUnitClean(minTemp,currentTempUnit));
        tvMaxTemp.setText(UnitConverter.convertToTemperatureUnitClean(maxTemp,currentTempUnit));
        tvDescription.setText(currentWeather.getCurrently().getSummary());
        if(previousTempColor == 0){
            previousTempColor = ContextCompat.getColor(getActivity(),R.color.coldBlue);
        }
        final float finalCurrentTemp = currentTemp;
        tempBar.post(new Runnable() {
            @Override
            public void run() {
                try{
                    ConstraintLayout.LayoutParams pointerParams = (ConstraintLayout.LayoutParams) tempPointer.getLayoutParams();
                    float layoutWidth = (float) tempLayout.getWidth();
                    float tempBarWidth = (float) tempBar.getWidth();
                    float tvTempWidth = (float) tvTemp.getWidth();
                    float leftMargin = (finalCurrentTemp - minTemp) / (maxTemp - minTemp) * tempBarWidth;
                    float tempLeftMargin = leftMargin;
                    if(leftMargin < tvTempWidth/2){
                        tempLeftMargin = tvTempWidth/2;
                    }
                    if(layoutWidth - tempLeftMargin < tvTempWidth){
                        tempLeftMargin = layoutWidth - tvTempWidth;
                    }
                    if(leftMargin < Objects.requireNonNull(getActivity()).getResources().getDimension(R.dimen.margin_4)){
                        leftMargin = getActivity().getResources().getDimension(R.dimen.margin_4);
                    }
                    if(tempBarWidth - leftMargin < Objects.requireNonNull(getActivity()).getResources().getDimension(R.dimen.margin_4)){
                        leftMargin -= getActivity().getResources().getDimension(R.dimen.margin_4);
                    }
                    if(pointerPreviousX == 0){
                        pointerPreviousX = pointerParams.leftMargin;
                    }
                    final int newTempColor = MyColorUtil.getTemperaturePointerColor(Objects.requireNonNull(getActivity()),(finalCurrentTemp - minTemp) / (maxTemp - minTemp));
                    Animation pointerSlideAnimation = new TranslateAnimation(pointerPreviousX,leftMargin,0,0);
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
                    Animation tempSlideAnimation = new TranslateAnimation(tempPreviousX,tempLeftMargin,0,0);
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
                            Drawable pointerBackground = DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(getActivity(),R.drawable.temperature_pointer)));
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
                } catch (Exception e){
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
            uvSummary  = getString(R.string.uv_summary_8);
        } else if (currentWeather.getCurrently().getUvIndex() < 11) {
            uvSummary = getString(R.string.uv_summary_11);
        } else {
            uvSummary = getString(R.string.uv_summary_11_above);
        }
        String uvValueText = String.valueOf(Math.round(currentWeather.getCurrently().getUvIndex()));
        tvUvIndex.setText(uvValueText);
        tvUvSummary.setText(uvSummary);
    }

    private void updateVisibilityData() {
        String currentDistanceUnit = PreferencesUtil.getDistanceUnit(Objects.requireNonNull(getActivity()));
        tvVisibility.setText(UnitConverter.convertToDistanceUnit(currentWeather.getCurrently().getVisibility(), currentDistanceUnit));
    }

    private void updateHumidityData() {
        String humidityText = Math.round(currentWeather.getCurrently().getHumidity() * 100) + "%";
        tvHumidity.setText(humidityText);
    }

    public void updateCurrentLocationName(String locationName) {
        if(this.tvLocation != null){
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

    public interface OnLayoutRefreshListener {
        void updateData();
    }
}
