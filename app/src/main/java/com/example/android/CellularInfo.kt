package com.example.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CellularInfo : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var telephonyManager: TelephonyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cellular_info)

        textView = findViewById(R.id.tv_cellular_info)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCellularInfo()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun getCellularInfo() {
        var info = ""

        try {
            info += "Operator: ${telephonyManager.networkOperatorName}\n"

            val networkType = telephonyManager.dataNetworkType
            info += "Network Type: ${getNetworkTypeString(networkType)}\n"

            val simState = telephonyManager.simState
            info += "SIM State: ${getSimStateString(simState)}\n"

            val allCells = telephonyManager.allCellInfo
            if (allCells != null && allCells.isNotEmpty()) {
                var foundLte = false
                for (cell in allCells) {
                    if (cell is CellInfoLte) {
                        val ci = cell.cellIdentity
                        val ss = cell.cellSignalStrength

                        info += "Cell ID: ${ci.ci}\n"
                        info += "Signal Strength: ${ss.dbm} dBm\n"
                        foundLte = true
                        break
                    }
                }
                if (!foundLte) {
                    info += "No LTE network available.\n"
                }
            } else {
                info += "No cell info available.\n"
            }

        } catch (e: Exception) {
            info = "Error: ${e.message}"
        }

        textView.text = info
    }

    private fun getNetworkTypeString(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G EDGE"
            TelephonyManager.NETWORK_TYPE_GPRS -> "2G GPRS"
            else -> "Unknown"
        }
    }

    private fun getSimStateString(simState: Int): String {
        return when (simState) {
            TelephonyManager.SIM_STATE_READY -> "Ready"
            TelephonyManager.SIM_STATE_ABSENT -> "Absent"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            TelephonyManager.SIM_STATE_NOT_READY -> "Not Ready"
            TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently Disabled"
            else -> "Unknown"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCellularInfo()  
            } else {
                Toast.makeText(this, "Permissions are required to fetch cellular information.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
