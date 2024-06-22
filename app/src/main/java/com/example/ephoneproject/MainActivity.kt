package com.example.ephoneproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Paint
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.example.ephoneproject.ui.theme.EPhoneProjectTheme
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            Greeting("Fannuel", context)
        }
    }
}

@Composable
fun Greeting(name: String, context: Context, modifier: Modifier = Modifier) {

    val sms = remember {
        mutableStateOf(emptyList<SmsMessage>())
    }
    val selected = remember {
        mutableStateOf<SmsMessage?>(null)
    }
    val min = remember {
        mutableStateOf(0.0)
    }
    val max = remember {
        mutableStateOf(0.0)
    }

    LaunchedEffect(true) {
        sms.value = getSms(context = context)
        sms.value.forEach {
            max.value = if (it.amount > max.value) it.amount else max.value
            min.value = if (it.amount < min.value) it.amount else min.value
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        item {
            Text(
                text = "    Total ${sms.value.firstOrNull()?.remaining_balance.toString()}$",
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp)
                    .background(Color.LightGray),
                verticalAlignment = Alignment.Bottom
            ) {
                items(sms.value) {
                    val height = (it.amount / max.value) * 200

                    Box(
                        modifier = Modifier
                            .height(height.dp)
                            .width(15.dp)
                            .background(if (it.debit_or_credit == "debited") Color.Red else Color.Green)
                            .clickable {
                                selected.value = it
                            }
                    )
                }
            }

        }
        item {
            SingleExpense(sms = selected.value)
        }
    }
}

@Composable
fun SingleExpense(
    sms: SmsMessage?
) {
    if (sms != null){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (sms.debit_or_credit == "debited") {
                Text(
                    text = "-  ${sms.amount}$",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    fontSize = 24.sp
                )
            } else {
                Text(
                    text = "+  ${sms.amount}$",
                    fontWeight = FontWeight.Bold,
                    color = Color.Green,
                    fontSize = 24.sp
                )
            }

            Text(
                text = "${sms.time} ${sms.date}",
                fontWeight = FontWeight.Normal,
                color = Color.LightGray,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.End)
            )
        }
    }
}


fun getSms(context: Context): List<SmsMessage> {
    val smsList = mutableListOf<SmsMessage>()

    try {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            val cursor: Cursor? = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                Telephony.Sms.ADDRESS + " = ?",
                arrayOf("CBE"),
                Telephony.Sms.DATE + " DESC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val date = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))

                    if (body.startsWith("Dear ")){
                        smsList.add(
                            SmsMessage(
                                date = SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date(date)).split(" ")[0],
                                time = SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date(date)).split(" ")[1],
                                bank_name = address,
                                debit_or_credit = buildString {
                                    if (body.contains("debited", ignoreCase = true)) {
                                        append("debited")
                                    } else if (body.contains("credited", ignoreCase = true)) {
                                        append("credited")
                                    }
                                },
                                amount = body.split("with ETB ")[1].split(" ")[0].replace(",", "").removeSuffix(".").toDouble(),
                                remaining_balance = body.split("Your Current Balance is ETB ")[1].split(" ")[0].replace(",", "").removeSuffix(".").toDouble()
                            )
                        )

                        //Log.e("TESTING", smsList.lastOrNull().toString())
                    }
                }
            }

        } else {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.READ_SMS), 0)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("TESTING", "Error reading SMS")
        Toast.makeText(context, "Error reading SMS", Toast.LENGTH_LONG).show()
    }

    return smsList
}

data class SmsMessage(
    val date: String,
    val time: String,
    val bank_name: String,
    val debit_or_credit: String?,
    val amount: Double,
    val remaining_balance: Double
)