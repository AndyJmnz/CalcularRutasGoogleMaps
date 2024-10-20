package com.example.routemap.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.example.routemap.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Marcadores(private val nMap: GoogleMap, private val context: Context) {

    fun addMarkersDefault() {
        val lugares = listOf(
            Pair(LatLng(20.778876607830476, -103.4316065872849), "Mi casa"),
            Pair(LatLng(20.703127385197277, -103.38866274681408), "CETI"),
            Pair(LatLng(20.71238543495462, -103.37476570306252), "Plaza Patria"),
            Pair(LatLng(20.677776791294608, -103.3429499569828), "La catedral"),
            Pair(LatLng(20.52565200595726, -103.30848428965311), "Aeropuerto")
        )

        for ((latLng, titulo) in lugares) {
            addMarker(latLng, titulo)
        }
    }

    private fun addMarker(latLng: LatLng, titulo: String) {
        val height = 140
        val width = 165
        val uno = context.resources.getDrawable(R.drawable.dos) as BitmapDrawable
        val unos = uno.bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(unos, width, height, false)

        nMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(titulo)
                .snippet("Información sobre $titulo")
                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
        )
    }
}
