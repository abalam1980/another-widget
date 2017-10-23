package com.tommasoberlose.anotherwidget.ui.widget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.view.View
import android.widget.RemoteViews

import com.tommasoberlose.anotherwidget.`object`.Constants
import com.tommasoberlose.anotherwidget.R
import com.tommasoberlose.anotherwidget.receiver.UpdatesReceiver
import com.tommasoberlose.anotherwidget.util.Util
import com.tommasoberlose.anotherwidget.receiver.WeatherReceiver

import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.app.PendingIntent
import android.provider.CalendarContract
import android.content.ContentUris
import android.util.Log
import com.tommasoberlose.anotherwidget.util.CalendarUtil
import com.tommasoberlose.anotherwidget.util.WeatherUtil
import android.graphics.Typeface
import android.net.Uri
import android.widget.TextClock
import android.widget.TextView
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat.startActivity
import android.provider.CalendarContract.Events
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import kotlinx.android.synthetic.main.the_widget.*
import kotlinx.android.synthetic.main.the_widget.view.*


/**
 * Implementation of App Widget functionality.
 */
class TheWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        UpdatesReceiver().setUpdates(context)
        WeatherReceiver().setUpdates(context)
        Util.showNotification(context)
    }

    override fun onDisabled(context: Context) {
        UpdatesReceiver().removeUpdates(context)
        WeatherReceiver().removeUpdates(context)
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val views = RemoteViews(context.packageName, R.layout.the_widget_sans)
            var v = View.inflate(context, R.layout.the_widget, null)
            v = updateCalendarViewByLayout(context, v)
            v = updateLocationViewByLayout(context, v)
            v = updateClockViewByLayout(context, v)
            views.setImageViewBitmap(R.id.bitmap_container, Util.getBitmapFromView(v))
            /*
            views = updateCalendarView(context, views, appWidgetId)

            views = updateLocationView(context, views, appWidgetId)

            val SP = PreferenceManager.getDefaultSharedPreferences(context)
            views.setTextColor(R.id.empty_date, Util.getFontColor(SP))
            views.setTextColor(R.id.divider1, Util.getFontColor(PreferenceManager.getDefaultSharedPreferences(context)))
            views.setTextColor(R.id.temp, Util.getFontColor(PreferenceManager.getDefaultSharedPreferences(context)))
            views.setTextColor(R.id.next_event, Util.getFontColor(PreferenceManager.getDefaultSharedPreferences(context)))
            views.setTextColor(R.id.next_event_difference_time, Util.getFontColor(PreferenceManager.getDefaultSharedPreferences(context)))
            views.setTextColor(R.id.next_event_date, Util.getFontColor(PreferenceManager.getDefaultSharedPreferences(context)))
            views.setTextColor(R.id.divider2, Util.getFontColor(PreferenceManager.getDefaultSharedPreferences(context)))
            views.setTextColor(R.id.calendar_temp, Util.getFontColor(PreferenceManager.getDefaultSharedPreferences(context)))

            views.setTextViewTextSize(R.id.empty_date, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            views.setTextViewTextSize(R.id.divider1, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))
            views.setTextViewTextSize(R.id.temp, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            views.setTextViewTextSize(R.id.next_event, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            views.setTextViewTextSize(R.id.next_event_difference_time, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            views.setTextViewTextSize(R.id.next_event_date, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))
            views.setTextViewTextSize(R.id.divider2, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))
            views.setTextViewTextSize(R.id.calendar_temp, TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))
            */
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateCalendarView(context: Context, views: RemoteViews, widgetID: Int): RemoteViews {
            val SP = PreferenceManager.getDefaultSharedPreferences(context)
            val now = Calendar.getInstance()
            val calendarLayout = SP.getBoolean(Constants.PREF_SHOW_EVENTS, true) && Util.checkGrantedPermission(context, Manifest.permission.READ_CALENDAR)

            views.setViewVisibility(R.id.empty_layout, View.VISIBLE)
            views.setViewVisibility(R.id.calendar_layout, View.GONE)
            var dateStringValue: String = Util.getCapWordString(Constants.engDateFormat.format(now.time))
            if (SP.getBoolean(Constants.PREF_ITA_FORMAT_DATE, false)) {
                dateStringValue = Util.getCapWordString(Constants.itDateFormat.format(now.time))
            }
            views.setTextViewText(R.id.empty_date, dateStringValue)
            //views.setImageViewBitmap(R.id.empty_date, Util.buildUpdate(context, Constants.dateFormat.format(now.time)[0].toUpperCase() + Constants.dateFormat.format(now.time).substring(1), "fonts/product_sans_regular.ttf"))


            val calPIntent = PendingIntent.getActivity(context, widgetID, Util.getCalendarIntent(context), 0)
            views.setOnClickPendingIntent(R.id.main_layout, calPIntent)


            if (calendarLayout) {
                val e = CalendarUtil.getNextEvent(context)

                if (e.id != 0) {
                    views.setTextViewText(R.id.next_event, e.title)
                    views.setTextViewText(R.id.next_event_difference_time, Util.getDifferenceText(context, now.timeInMillis, e.startDate))

                    if (!e.address.equals("") && SP.getBoolean(Constants.PREF_SHOW_EVENT_LOCATION, false)) {

                        val source = BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_location);
                        val result = Util.changeBitmapColor(source, Util.getFontColor(SP))
                        views.setImageViewBitmap(R.id.second_row_icon, result)

                        views.setTextViewText(R.id.next_event_date, e.address)

                        val mapIntent = PendingIntent.getActivity(context, widgetID, Util.getGoogleMapsIntentFromAddress(context, e.address), 0)
                        views.setOnClickPendingIntent(R.id.next_event_date, mapIntent)
                    } else {
                        val source = BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_calendar);
                        val result = Util.changeBitmapColor(source, Util.getFontColor(SP))
                        views.setImageViewBitmap(R.id.second_row_icon, result)

                        if (!e.allDay) {
                            val startHour: String = if (SP.getString(Constants.PREF_HOUR_FORMAT, "12").equals("12")) Constants.badHourFormat.format(e.startDate) else Constants.goodHourFormat.format(e.startDate)
                            val endHour: String = if (SP.getString(Constants.PREF_HOUR_FORMAT, "12").equals("12")) Constants.badHourFormat.format(e.endDate) else Constants.goodHourFormat.format(e.endDate)
                            var dayDiff = TimeUnit.MILLISECONDS.toDays(e.endDate - e.startDate)

                            val startCal = Calendar.getInstance()
                            startCal.timeInMillis = e.startDate

                            val endCal = Calendar.getInstance()
                            endCal.timeInMillis = e.endDate

                            if (startCal.get(Calendar.HOUR_OF_DAY) > endCal.get(Calendar.HOUR_OF_DAY)) {
                                dayDiff++
                            } else if (startCal.get(Calendar.HOUR_OF_DAY) == endCal.get(Calendar.HOUR_OF_DAY) && startCal.get(Calendar.MINUTE) >= endCal.get(Calendar.MINUTE)) {
                                dayDiff++
                            }

                            var multipleDay = ""
                            if (dayDiff > 0) {
                                multipleDay = String.format(" (+%s%s)", dayDiff, context.getString(R.string.day_char))
                            }

                            views.setTextViewText(R.id.next_event_date, String.format("%s - %s%s", startHour, endHour, multipleDay))
                        } else {
                            views.setTextViewText(R.id.next_event_date, dateStringValue)
                        }
                    }

                    views.setViewVisibility(R.id.empty_layout, View.GONE)
                    views.setViewVisibility(R.id.calendar_layout, View.VISIBLE)

                    val pIntent = PendingIntent.getActivity(context, widgetID, Util.getEventIntent(context, e), 0)
                    views.setOnClickPendingIntent(R.id.main_layout, pIntent)
                }
            }

            return views
        }

        fun updateLocationView(context: Context, views: RemoteViews, widgetID: Int): RemoteViews {
            val SP = PreferenceManager.getDefaultSharedPreferences(context)
            val locationLayout = SP.getBoolean(Constants.PREF_SHOW_WEATHER, true)

            if (locationLayout && SP.contains(Constants.PREF_WEATHER_TEMP) && SP.contains(Constants.PREF_WEATHER_ICON)) {
                views.setViewVisibility(R.id.weather, View.VISIBLE)
                views.setViewVisibility(R.id.calendar_weather, View.VISIBLE)
                val temp = String.format(Locale.getDefault(), "%.0f °%s", SP.getFloat(Constants.PREF_WEATHER_TEMP, 0f), SP.getString(Constants.PREF_WEATHER_REAL_TEMP_UNIT, "F"))


                views.setViewVisibility(R.id.weather_icon, View.VISIBLE)
                views.setViewVisibility(R.id.empty_weather_icon, View.VISIBLE)
                val icon: String = SP.getString(Constants.PREF_WEATHER_ICON, "")
                if (icon.equals("")) {
                    views.setViewVisibility(R.id.weather_icon, View.GONE)
                    views.setViewVisibility(R.id.empty_weather_icon, View.GONE)
                } else {
                    views.setImageViewResource(R.id.weather_icon, WeatherUtil.getWeatherIconResource(icon))
                    views.setImageViewResource(R.id.empty_weather_icon, WeatherUtil.getWeatherIconResource(icon))
                }

                views.setTextViewText(R.id.temp, temp)
                views.setTextViewText(R.id.calendar_temp, temp)


                val weatherPIntent = PendingIntent.getActivity(context, widgetID, Util.getWeatherIntent(context), 0)

                views.setOnClickPendingIntent(R.id.weather, weatherPIntent)
                views.setOnClickPendingIntent(R.id.calendar_weather, weatherPIntent)
            } else {
                views.setViewVisibility(R.id.weather, View.GONE)
                views.setViewVisibility(R.id.calendar_weather, View.GONE)
            }
            return views
        }

        fun updateClockView(context: Context, views: RemoteViews, widgetID: Int) {
            val SP = PreferenceManager.getDefaultSharedPreferences(context)
            if (!SP.getBoolean(Constants.PREF_SHOW_CLOCK, false)) {
                views.setViewVisibility(R.id.time, View.GONE)
            } else {
                views.setViewVisibility(R.id.time, View.VISIBLE)
            }
            val now = Calendar.getInstance()
            views.setTextViewText(R.id.time, if (SP.getString(Constants.PREF_HOUR_FORMAT, "12").equals("12")) Constants.badHourFormat.format(now.timeInMillis) else Constants.goodHourFormat.format(now.timeInMillis))
        }

        fun updateCalendarViewByLayout(context: Context, v: View): View {
            val SP = PreferenceManager.getDefaultSharedPreferences(context)
            val now = Calendar.getInstance()
            val calendarLayout = SP.getBoolean(Constants.PREF_SHOW_EVENTS, true) && Util.checkGrantedPermission(context, Manifest.permission.READ_CALENDAR)

            v.empty_layout.visibility = View.VISIBLE
            v.calendar_layout.visibility = View.GONE
            var dateStringValue: String = Util.getCapWordString(Constants.engDateFormat.format(now.time))
            if (SP.getBoolean(Constants.PREF_ITA_FORMAT_DATE, false)) {
                dateStringValue = Util.getCapWordString(Constants.itDateFormat.format(now.time))
            }
            v.empty_date.text = dateStringValue
            //empty_date.setImageBitmap(Util.buildUpdate(this,  String.format("%s%s", Constants.dateFormat.format(now.time)[0].toUpperCase(), Constants.dateFormat.format(now.time).substring(1)), "fonts/product_sans_regular.ttf"))

            if (calendarLayout) {
                val e = CalendarUtil.getNextEvent(context)

                if (e.id != 0) {
                    v.next_event.text = e.title
                    v.next_event_difference_time.text = Util.getDifferenceText(context, now.timeInMillis, e.startDate)

                    if (!e.address.equals("") && SP.getBoolean(Constants.PREF_SHOW_EVENT_LOCATION, false)) {
                        v.second_row_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_location))
                        v.next_event_date.text = e.address
                    } else {
                        v.second_row_icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_calendar))
                        if (!e.allDay) {
                            val startHour: String = if (SP.getString(Constants.PREF_HOUR_FORMAT, "12").equals("12")) Constants.badHourFormat.format(e.startDate) else Constants.goodHourFormat.format(e.startDate)
                            val endHour: String = if (SP.getString(Constants.PREF_HOUR_FORMAT, "12").equals("12")) Constants.badHourFormat.format(e.endDate) else Constants.goodHourFormat.format(e.endDate)
                            var dayDiff = TimeUnit.MILLISECONDS.toDays(e.endDate - e.startDate)

                            val startCal = Calendar.getInstance()
                            startCal.timeInMillis = e.startDate

                            val endCal = Calendar.getInstance()
                            endCal.timeInMillis = e.endDate

                            if (startCal.get(Calendar.HOUR_OF_DAY) > endCal.get(Calendar.HOUR_OF_DAY)) {
                                dayDiff++
                            } else if (startCal.get(Calendar.HOUR_OF_DAY) == endCal.get(Calendar.HOUR_OF_DAY) && startCal.get(Calendar.MINUTE) >= endCal.get(Calendar.MINUTE)) {
                                dayDiff++
                            }
                            var multipleDay: String = ""
                            if (dayDiff > 0) {
                                multipleDay = String.format(" (+%s%s)", dayDiff, context.getString(R.string.day_char))
                            }
                            v.next_event_date.text = String.format("%s - %s%s", startHour, endHour, multipleDay)
                        } else {
                            v.next_event_date.text = dateStringValue
                        }
                    }

                    v.empty_layout.visibility = View.GONE
                    v.calendar_layout.visibility = View.VISIBLE
                }
            }

            v.empty_date.setTextColor(Util.getFontColor(SP))
            v.divider1.setTextColor(Util.getFontColor(SP))
            v.temp.setTextColor(Util.getFontColor(SP))
            v.next_event.setTextColor(Util.getFontColor(SP))
            v.next_event_difference_time.setTextColor(Util.getFontColor(SP))
            v.next_event_date.setTextColor(Util.getFontColor(SP))
            v.divider2.setTextColor(Util.getFontColor(SP))
            v.calendar_temp.setTextColor(Util.getFontColor(SP))
            v.second_row_icon.setColorFilter(Util.getFontColor(SP))


            v.empty_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            v.divider1.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))
            v.temp.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            v.next_event.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            v.next_event_difference_time.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_MAIN_SIZE, 24f))
            v.next_event_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))
            v.divider2.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))
            v.calendar_temp.setTextSize(TypedValue.COMPLEX_UNIT_SP, SP.getFloat(Constants.PREF_TEXT_SECOND_SIZE, 16f))


            val product_sans: Typeface = Typeface.createFromAsset(context.assets, "fonts/product_sans_regular.ttf")
            v.empty_date.typeface = product_sans
            v.divider1.typeface = product_sans
            v.temp.typeface = product_sans
            v.next_event.typeface = product_sans
            v.next_event_difference_time.typeface = product_sans
            v.next_event_date.typeface = product_sans
            v.divider2.typeface = product_sans
            v.calendar_temp.typeface = product_sans

            return v
        }

        fun updateLocationViewByLayout(context: Context, v: View): View {
            val SP = PreferenceManager.getDefaultSharedPreferences(context)
            val locationLayout = SP.getBoolean(Constants.PREF_SHOW_WEATHER, true)

            if (locationLayout && SP.contains(Constants.PREF_WEATHER_TEMP) && SP.contains(Constants.PREF_WEATHER_ICON)) {
                v.weather.visibility = View.VISIBLE
                v.calendar_weather.visibility = View.VISIBLE
                val currentTemp = String.format(Locale.getDefault(), "%.0f °%s", SP.getFloat(Constants.PREF_WEATHER_TEMP, 0f), SP.getString(Constants.PREF_WEATHER_REAL_TEMP_UNIT, "F"))


                v.weather_icon.visibility = View.VISIBLE
                v.empty_weather_icon.visibility = View.VISIBLE
                val icon: String = SP.getString(Constants.PREF_WEATHER_ICON, "")
                if (icon.equals("")) {
                    v.weather_icon.visibility = View.GONE
                    v.empty_weather_icon.visibility = View.GONE
                } else {
                    v.weather_icon.setImageResource(WeatherUtil.getWeatherIconResource(icon))
                    v.empty_weather_icon.setImageResource(WeatherUtil.getWeatherIconResource(icon))
                }

                v.temp.text = currentTemp
                v.calendar_temp.text = currentTemp
            } else {
                v.weather.visibility = View.GONE
                v.calendar_weather.visibility = View.GONE
            }
            return v
        }

        fun updateClockViewByLayout(context: Context, v: View): View {
            val SP = PreferenceManager.getDefaultSharedPreferences(context)
            if (!SP.getBoolean(Constants.PREF_SHOW_CLOCK, false)) {
                v.time.visibility = View.GONE
            } else {
                v.time.visibility = View.VISIBLE
            }
            val now = Calendar.getInstance()
            v.time.text = if (SP.getString(Constants.PREF_HOUR_FORMAT, "12").equals("12")) Constants.badHourFormat.format(now.timeInMillis) else Constants.goodHourFormat.format(now.timeInMillis)
            return v
        }
    }
}

