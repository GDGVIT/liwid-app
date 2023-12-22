package com.example.liwid_app.extension
import android.content.Context
import android.app.Activity


class LiveTrackingWidget(context: Context,activity: Activity):LiveWidget(context, activity){
    init {
        widgetType=WidgetType.TRACKING
    }

    override fun startLiveWidget() {
        TODO("Not yet implemented")
    }

    override fun stopLiveWidget() {
        TODO("Not yet implemented")
    }
}