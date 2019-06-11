package com.OdiousPanda.thefweather.API;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static WeatherCall createWeatherCall(){
        return retrofit.create(WeatherCall.class);
    }
}
