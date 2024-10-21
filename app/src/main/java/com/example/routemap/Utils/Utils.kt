package com.example.routemap.Utils

import android.content.Context
import com.google.android.gms.maps.GoogleMap

class Utils {
    companion object {
        var coordenadas = Coordenadas()
        fun markersDefault(nMap: GoogleMap, context: Context) {
            Marcadores(nMap, context).addMarkersDefault()
        }
    }
}
