package com.example.ephoneproject

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            MainScreen(context)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(context: Context) {

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
        while (sms.value.isEmpty()){
            sms.value = getSms(context = context)
            sms.value.forEach {
                max.value = if (it.amount > max.value) it.amount else max.value
                min.value = if (it.amount < min.value) it.amount else min.value
            }
            selected.value = sms.value.firstOrNull()
        }
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        if (sms.value.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.error),
                        contentDescription = "setting",
                        modifier = Modifier
                            .size(60.dp)
                    )
                    Text(
                        text = "No CBE messages detected!",

                        )
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 50.dp, start = 16.dp, top = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Remaining Amount",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.darkergray)
                        )
                        Text(
                            text = "$ ${sms.value.firstOrNull()?.remaining_balance.toString()}",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            sms.value = getSms(context = context)
                            sms.value.forEach {
                                max.value = if (it.amount > max.value) it.amount else max.value
                                min.value = if (it.amount < min.value) it.amount else min.value
                            }
                            selected.value = sms.value.firstOrNull()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "refresh"
                        )
                    }
                }
            }
            item {
                val spacingFromLeft = 150f
                val spacingFromBottom = 40f

                val upperValue = sms.value.maxOfOrNull { it.amount.toInt() }?.plus(1) ?: 1
                val lowerValue = sms.value.minOfOrNull { it.amount.toInt() } ?: 0

                val density = LocalDensity.current

                val textPaint = remember(density) {
                    Paint().apply {
                        color = android.graphics.Color.BLACK
                        textAlign = Paint.Align.CENTER
                        textSize = density.run { 12.sp.toPx() }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()){
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        val canvasHeight = size.height

                        //we will show 5 data values on vertical line
                        val valuesToShow = 5f

                        val eachStep = (upperValue - lowerValue) / valuesToShow
                        //data is shown vertically
                        (0..4).forEach { i ->
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    round(lowerValue + eachStep * i).toString(),
                                    20f,
                                    canvasHeight - 65f - i * canvasHeight / 5f,
                                    textPaint
                                )
                            }

                            //draw horizontal line at each value
                            drawLine(
                                start = Offset(
                                    spacingFromLeft - 20f,
                                    canvasHeight - spacingFromBottom - i * canvasHeight / 5f
                                ),
                                end = Offset(
                                    spacingFromLeft,
                                    canvasHeight - spacingFromBottom - i * canvasHeight / 5f
                                ),
                                color = Color.Black,
                                strokeWidth = 3f
                            )
                        }

                        //This is for the vertical line
                        drawLine(
                            start = Offset(spacingFromLeft, canvasHeight - spacingFromBottom),
                            end = Offset(spacingFromLeft, 0f),
                            color = Color.Black,
                            strokeWidth = 3f
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(start = (spacingFromLeft / density.density).dp)
                    ) {
                        items(sms.value) { chartPair ->
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .width(75.dp)
                                    .height(300.dp)
                                    .border(
                                        2.dp,
                                        if (selected.value?.date == chartPair.date && selected.value?.time == chartPair.time) colorResource(
                                            id = R.color.lightgray
                                        ) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(Color.White)
                                    .padding(top = 16.dp, bottom = spacingFromBottom.dp)
                                    .clickable {
                                        selected.value = chartPair
                                    }
                            ) {
                                val canvasHeight = size.height

                                val barHeight =
                                    (chartPair.amount.toFloat() / upperValue) * (canvasHeight - spacingFromBottom)
                                val topLeft = Offset(10f, canvasHeight - spacingFromBottom - barHeight)

                                drawContext.canvas.nativeCanvas.apply {
                                    drawText(
                                        if (chartPair.debit_or_credit == "debited") "- ${chartPair.amount}" else "+ ${chartPair.amount}",
                                        size.width / 2,
                                        topLeft.y - 10f,
                                        Paint().apply {
                                            color =
                                                if (chartPair.debit_or_credit == "debited") android.graphics.Color.RED else android.graphics.Color.GREEN
                                            textAlign = Paint.Align.CENTER
                                            textSize = density.run { 12.sp.toPx() }
                                        }
                                    )
                                }

                                drawRoundRect(
                                    color = if (chartPair.debit_or_credit == "debited") Color.Red else Color.Green,
                                    topLeft = topLeft,
                                    size = Size(
                                        55f,
                                        barHeight
                                    ),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )

                                drawContext.canvas.nativeCanvas.apply {
                                    drawText(
                                        chartPair.date,
                                        size.width / 2,
                                        canvasHeight,
                                        textPaint
                                    )
                                }

                                if(selected.value?.date == chartPair.date && selected.value?.time == chartPair.time){
                                    drawContext.canvas.nativeCanvas.apply {
                                        drawText(
                                            "^",
                                            size.width / 2,
                                            canvasHeight + 60,
                                            Paint().apply {
                                                color = android.graphics.Color.DKGRAY
                                                textAlign = Paint.Align.CENTER
                                                textSize = density.run { 20.sp.toPx() }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                SingleExpenseItem(sms = selected.value)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SingleExpenseItem(
    sms: SmsMessage?
) {
    if (sms != null){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "  ${month(sms.date.split("/")[1].toInt())} ${sms.date.split("/")[0]}, ${sms.date.split("/")[2]}  ",
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ){
                if (sms.debit_or_credit == "debited") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = "$ ${sms.amount}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.debited),
                            contentDescription = "debited",
                            tint = Color.Red
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){

                        Text(
                            text = "$ ${sms.amount}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Green,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.credited),
                            contentDescription = "credited",
                            tint = Color.Green
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                val time = LocalTime.parse(sms.time, DateTimeFormatter.ofPattern("HH:mm:ss"))
                val formattedTime = time.format(DateTimeFormatter.ofPattern("hh:mma"))

                Text(
                    text = formattedTime,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(id = R.color.lightgray),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                )
            }
        }
    }
}

//Name of the respective month
fun month(m: Int): String {
    val x = when(m) {
        1 -> return "January"
        2 -> return "February"
        3 -> return "March"
        4 -> return "April"
        5 -> return "May"
        6 -> return "June"
        7 -> return "July"
        8 -> return "August"
        9 -> return "September"
        10 -> return "October"
        11 -> return "November"
        12 -> return "December"
        else -> return ""
    }

    return x
}

//to get the sms messages
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