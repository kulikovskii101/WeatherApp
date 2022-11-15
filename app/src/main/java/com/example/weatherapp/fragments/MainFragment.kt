package com.example.weatherapp.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.*
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.DaysActivity
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.adapters.VpAdapter
import com.example.weatherapp.dataClasses.DayWeather
import com.example.weatherapp.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONObject
import java.text.SimpleDateFormat


class MainFragment : Fragment() {

    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val model: MainViewModel by activityViewModels()

    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )

    inline fun FragmentManager.doTransaction(func: FragmentTransaction.() ->
    FragmentTransaction) {
        beginTransaction().func().commit()
    }
    private val tList = listOf(
        "По часам",
        "На неделю",
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
        getCityByLocated()
    }


    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vP.adapter = adapter
        bt7days.setOnClickListener {
            val intent = Intent(, DaysActivity::class.java)
            startActivity(intent)
        }
//        TabLayoutMediator(tabLayoutHoursAndDays, vP) { tab, position ->
//            tab.text = tList[position]
//        }.attach()
        iBGeoCity.setOnClickListener {
            getCityByLocated()
        }
        ibSearchCity.setOnClickListener {

            DialogManger.getCityByName(requireContext(), object : DialogManger.Listener {
                override fun onClick(name: String) {
                    requestWeatherData(name)
                }
            })
        }

    }

    private fun updateCurrentCard() = with(binding) {
        model.dataCurrent.observe(viewLifecycleOwner) {
            tVCityName.text = it.cityName
            tVTemperature.text = it.currentTemperature + "°C"
            tVConditionWeather.text = it.condition

        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=${Companion.API_KEY}" +
                "&q=" +
                city +
                "&days=7" +
                "&aqi=no&alerts=no"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result)
            },
            { err ->
                Log.e("Error", "ErrorRequestData: $err")
            }
        )
        queue.add(request)
    }

    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getCityByLocated() {
        if (!isLocationEnabled()) {
            Toast.makeText(requireContext(), "Местоположение выключено", Toast.LENGTH_SHORT).show()
            return
        }
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude}, ${it.result.longitude}")
            }

    }

    private fun parseDays(mainObject: JSONObject): List<DayWeather> {
        val list = ArrayList<DayWeather>()
        val name = mainObject.getJSONObject("location").getString("name")
        val dayArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        for (i in 0 until dayArray.length()) {
            val day = dayArray[i] as JSONObject
            val item = DayWeather(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                "",
                day.getJSONObject("day").getString("maxtemp_c"),
                day.getJSONObject("day").getString("mintemp_c"),
                day.getJSONArray("hour").toString()
            )
            model.dataList.value = list
            list.add(item)
        }
        return list
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseCurrentData(mainObject: JSONObject, dayWeather: DayWeather) {
        val item = DayWeather(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            mainObject.getJSONObject("current").getString("temp_c"),
            dayWeather.maxTemperature, dayWeather.minTemperature,
            dayWeather.forecastHours,
        )
        model.dataCurrent.value = item
        Log.d("Error", item.cityName)
    }

    private val simpleDateFormat = SimpleDateFormat("dd:mm:yyyy, HH:mm")

    private fun getDateString(time: String): String = simpleDateFormat.format(time.toLong() * 1000L)

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
        const val API_KEY = "841a92d48185425d94a115102221211"
    }
}