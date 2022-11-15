package com.example.weatherapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.dataClasses.DayWeather

class MainViewModel : ViewModel() {

    val dataCurrent = MutableLiveData<DayWeather>()
    val dataList = MutableLiveData<List<DayWeather>>()

}