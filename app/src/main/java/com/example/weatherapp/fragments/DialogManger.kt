package com.example.weatherapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.EditText


object DialogManger {

    fun getCityByName(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val ed = EditText(context)
        val dialog = builder.create()
        dialog.setView(ed)
        dialog.setTitle("Название города")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Подвердить") { _, _ ->
            listener.onClick(ed.text.toString())
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отменить"){ _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }


    interface Listener{
        fun onClick(name: String)
    }
}