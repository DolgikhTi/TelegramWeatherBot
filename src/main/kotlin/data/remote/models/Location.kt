package data.remote.models

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("country")
    val country: String,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("localtime")
    val localTime: String,
    @SerializedName("localtime_epoch")
    val localtimeInEpoch: Int,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("name")
    val name: String,
    @SerializedName("region")
    val region: String,
    @SerializedName("tz_id")
    val timezoneId: String
)
