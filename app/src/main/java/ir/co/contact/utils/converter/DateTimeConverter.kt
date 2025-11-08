package ir.co.contact.utils.converter

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeConverter {

    private val localTimeZone = TimeZone.getDefault().id
    private const val SERVER_TIME_ZONE = "UTC"
    private const val SERVER_ORDER_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
    private const val SERVER_EXPIRE_DATE_TIME_PATTERN = "yyyy-MM-dd"
    private const val LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
    private const val LOCAL_DATE_TIME_PATTERN_EXP = "yyyy-MM-dd"


    fun convertServerOrderTimeToLocalTime(orderTimestamp: String): String {
        val serverFormat = SimpleDateFormat(SERVER_ORDER_DATE_TIME_PATTERN, Locale.getDefault())
        serverFormat.timeZone = TimeZone.getTimeZone(SERVER_TIME_ZONE)
        val date: Date? = serverFormat.parse(orderTimestamp)

        val iranFormat = SimpleDateFormat(LOCAL_DATE_TIME_PATTERN, Locale.getDefault())
        iranFormat.timeZone = TimeZone.getTimeZone(localTimeZone)

        return iranFormat.format(date ?: Date())
    }

    fun convertServerExpireTimeToLocalTime(expireTimestamp: String): String {
        val serverFormat = SimpleDateFormat(SERVER_EXPIRE_DATE_TIME_PATTERN, Locale.getDefault())
        serverFormat.timeZone = TimeZone.getTimeZone(SERVER_TIME_ZONE)
        val date: Date? = serverFormat.parse(expireTimestamp)

        val iranFormat = SimpleDateFormat(LOCAL_DATE_TIME_PATTERN_EXP, Locale.getDefault())
        iranFormat.timeZone = TimeZone.getTimeZone(localTimeZone)

        return iranFormat.format(date ?: Date())
    }
}