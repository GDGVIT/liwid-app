package com.example.liwid_app.extension.model
import java.util.Date

data class TrackerWidgetData(
    val success:Int,
    val result:List<TrackerData>
)

data class TrackerData(
    var orderId: Number,
    var orderName: String,
    var orderStatus: String,
    var orderImage: String?,
    var orderDate: Date?,
)