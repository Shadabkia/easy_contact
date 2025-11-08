package ir.co.contact.utils

import android.annotation.SuppressLint
import android.text.format.DateFormat
import ir.co.contact.utils.CalendarTool.convertAllNumbersToPersian
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
fun getTimeInPersian(dateStr: String?): String {
    return try {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        val date: String = DateFormat.format("yyyy/MM/dd",
            dateStr?.let { sdf.parse(it) }).toString()
        val dateHours = DateFormat.format("dd-M-yyyy HH:mm:ss",
            dateStr.let { sdf.parse(it) }).toString().split(" ").getOrNull(1)
        val ct = _root_ide_package_.ir.co.contact.utils.CalendarTool(date)
        "${convertAllNumbersToPersian(ct.iranianDate)} ${convertAllNumbersToPersian(dateHours)}"
    } catch (e: Exception) {
        "Error"
    }
}