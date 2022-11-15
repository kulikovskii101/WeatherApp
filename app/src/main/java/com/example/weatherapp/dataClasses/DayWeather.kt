package com.example.weatherapp.dataClasses

data class DayWeather(
    val cityName: String,
    val dateTime: String,
    val condition: String,
    val imgUrl: String,
    val currentTemperature: String,
    val maxTemperature: String,
    val minTemperature: String,
    val forecastHours: String
)


