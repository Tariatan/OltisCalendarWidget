package com.oltis.calendarwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Vector;

public class OltisCalendarWidget extends AppWidgetProvider
{
    private static final String ACTION_PREVIOUS_MONTH   = "com.oltis.calendarwidget.action.PREVIOUS_MONTH";
    private static final String ACTION_NEXT_MONTH       = "com.oltis.calendarwidget.action.NEXT_MONTH";
    private static final String ACTION_RESET_MONTH      = "com.oltis.calendarwidget.action.RESET_MONTH";
    private static final String ACTION_SELECT_DAY       = "com.oltis.calendarwidget.action.SELECT_DAY";
    private static final String ACTION_STORE_DAY        = "com.oltis.calendarwidget.action.STORE_DAY";
    private static final String ACTION_ERASE_DAY        = "com.oltis.calendarwidget.action.ERASE_DAY";

    private static final String EXTRA_YEAR              = "com.oltis.calendarwidget.extra.YEAR";
    private static final String EXTRA_MONTH             = "com.oltis.calendarwidget.extra.MONTH";
    private static final String EXTRA_DAY_OF_YEAR       = "com.oltis.calendarwidget.extra.DAY_OF_YEAR";

    private static final String PREF_MONTH          = "month";
    private static final String PREF_YEAR           = "year";
    private static final String PREF_DAY_OF_YEAR    = "day_of_year";

    private static class DayClass
    {
        DayClass(int y, int d)
        {
            Year = y;
            DayOfYear = d;
        }
        public int Year;
        public int DayOfYear;
    }


    @Override
    public void onEnabled(Context context)
    {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        sp.edit()
                .putInt(PREF_MONTH, cal.get(Calendar.MONTH))
                .putInt(PREF_YEAR, cal.get(Calendar.YEAR))
                .putInt(PREF_DAY_OF_YEAR, -1)
                .apply();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds)
        {
            drawWidget(context, appWidgetId);
        }
    }

    private void redrawWidgets(Context context)
    {
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, OltisCalendarWidget.class));
        for (int appWidgetId : appWidgetIds)
        {
            drawWidget(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (action.equals(ACTION_PREVIOUS_MONTH))
        {
            shiftMonth(context, -1);
            redrawWidgets(context);
        }
        else if (action.equals(ACTION_NEXT_MONTH))
        {
            shiftMonth(context, 1);
            redrawWidgets(context);
        }
        else if (action.equals(ACTION_RESET_MONTH))
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            sp.edit().remove(PREF_MONTH).remove(PREF_YEAR).remove(PREF_DAY_OF_YEAR).apply();
            redrawWidgets(context);
        }
        else if(action.contains(ACTION_SELECT_DAY))
        {
            int year    = intent.getIntExtra(EXTRA_YEAR, 0);
            int month   = intent.getIntExtra(EXTRA_MONTH, 0);
            int day     = intent.getIntExtra(EXTRA_DAY_OF_YEAR, 0);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            sp.edit().putInt(PREF_YEAR, year).putInt(PREF_MONTH, month).putInt(PREF_DAY_OF_YEAR, day).apply();
            redrawWidgets(context);
        }
        else if(action.equals(ACTION_STORE_DAY))
        {
            performDateAction(context, action);
            redrawWidgets(context);
        }
        else if(action.equals(ACTION_ERASE_DAY))
        {
            performDateAction(context, action);
            redrawWidgets(context);
        }
    }

    private void shiftMonth(Context context, int shift)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar cal = Calendar.getInstance();
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);
        cal.add(Calendar.MONTH, shift);
        sp.edit().putInt(PREF_MONTH, cal.get(Calendar.MONTH)).putInt(PREF_YEAR, cal.get(Calendar.YEAR)).apply();
    }

    private void performDateAction(Context context, String action)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar cal = Calendar.getInstance();
        int thisDay = sp.getInt(PREF_DAY_OF_YEAR, -1);
        if(thisDay != -1)
        {
            int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));

            // Reset Calendar to first day of year
            cal.set(Calendar.DAY_OF_MONTH, 0);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.YEAR, thisYear);

            // Add number of days till selected day
            cal.add(Calendar.DAY_OF_MONTH, thisDay);

            if(action.equals(ACTION_STORE_DAY))
            {
                DatabaseHelper.getInstance(context).insert(thisYear, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }
            else if(action.equals(ACTION_ERASE_DAY))
            {
                DatabaseHelper.getInstance(context).delete(thisYear, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        drawWidget(context, appWidgetId);
    }

    private void drawWidget(Context context, int appWidgetId)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR);
        int todayYear = cal.get(Calendar.YEAR);
        int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
        int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
        int selectedDay = sp.getInt(PREF_DAY_OF_YEAR, -1);
        Vector<DayClass> recentDays = getRecentDays(context, thisYear, thisMonth);

        //Vector<DayClass> recentDays = new Vector<>();
        //recentDays.add(new DayClass(thisYear, today));

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, thisMonth);
        cal.set(Calendar.YEAR, thisYear);

        // Month label
        String[] months = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};

        rv.setTextViewText(R.id.month_label, ((cal.get(Calendar.MONTH) < months.length) ? months[cal.get(Calendar.MONTH)] : "") + " " + DateFormat.format("yyyy", cal));

        // Map US days to EU days
        int monthStartDayOfWeek = 1;
        switch (cal.get(Calendar.DAY_OF_WEEK))
        {
            case 1: monthStartDayOfWeek = 7; break;
            case 2: monthStartDayOfWeek = 1; break;
            case 3: monthStartDayOfWeek = 2; break;
            case 4: monthStartDayOfWeek = 3; break;
            case 5: monthStartDayOfWeek = 4; break;
            case 6: monthStartDayOfWeek = 5; break;
            case 7: monthStartDayOfWeek = 6; break;
        }
        cal.add(Calendar.DAY_OF_MONTH, 1 - monthStartDayOfWeek);

        // Clear widget
        rv.removeAllViews(R.id.calendar);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Weekdays row
        RemoteViews headerRowRv = new RemoteViews(context.getPackageName(), R.layout.row_header);
        String[] weekdays = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
        for (int day = 0; day <= 6; day++)
        {
            RemoteViews dayRv = new RemoteViews(context.getPackageName(), R.layout.cell_header);
            dayRv.setTextViewText(android.R.id.text1, weekdays[day]);
            headerRowRv.addView(R.id.row_container, dayRv);
        }
        rv.addView(R.id.calendar, headerRowRv);
        ////////////////////////////////////////////////////////////////////////////////////////////

        boolean isSelectedDayStored = false;
        ////////////////////////////////////////////////////////////////////////////////////////////
        // Calendar with 6 weeks
        for (int week = 0; week < 6; week++)
        {
            RemoteViews rowRv = new RemoteViews(context.getPackageName(), R.layout.row_week);
            for (int day = 0; day < 7; day++)
            {
                int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                Calendar nextDayCal = Calendar.getInstance();
                DayClass lastDay = new DayClass(0, 0);
                if(recentDays.size() > 0)
                {
                    lastDay = recentDays.get(recentDays.size() - 1);
                }
                nextDayCal.set(Calendar.YEAR, lastDay.Year);
                nextDayCal.set(Calendar.DAY_OF_MONTH, 0);
                nextDayCal.set(Calendar.MONTH, Calendar.JANUARY);
                nextDayCal.add(Calendar.DAY_OF_MONTH, lastDay.DayOfYear + 27);

                boolean inMonth = (cal.get(Calendar.MONTH) == thisMonth);
                boolean inYear  = (cal.get(Calendar.YEAR) == todayYear);
                boolean isToday = (inYear && inMonth && (dayOfYear == today));
                boolean isWeekend = (day == 5 || day == 6);
                boolean isStored = false;
                for(DayClass recentDay : recentDays)
                {
                    if((recentDay.DayOfYear == dayOfYear) &&
                       (recentDay.Year == cal.get(Calendar.YEAR)))
                    {
                        isStored = true;
                        break;
                    }
                }

                boolean isNextDay = ((cal.get(Calendar.YEAR)         == nextDayCal.get(Calendar.YEAR)) &&
                                     (cal.get(Calendar.MONTH)        == nextDayCal.get(Calendar.MONTH)) &&
                                     (cal.get(Calendar.DAY_OF_MONTH) == nextDayCal.get(Calendar.DAY_OF_MONTH)));

                boolean isSelectedDay = (selectedDay == dayOfYear);

                int cellLayoutResId;

                if(isNextDay)
                {
                    cellLayoutResId = isSelectedDay ? R.layout.cell_theday_selected : (isToday ? R.layout.cell_theday_today : R.layout.cell_theday);
                }
                else if(isSelectedDay)
                {
                    isSelectedDayStored = isStored;
                    cellLayoutResId = isStored ? R.layout.cell_stored_day : R.layout.cell_selected_day;
                }
                else if (isToday)
                {
                    cellLayoutResId = isStored ? R.layout.cell_stored_today : ((isWeekend ? R.layout.cell_today_weekend : R.layout.cell_today));
                }
                else if(inMonth)
                {
                    cellLayoutResId = isStored ? R.layout.cell_stored_day : (isWeekend ? R.layout.cell_weekend_this_month : R.layout.cell_day_this_month);
                }
                else if(isWeekend)
                {
                    cellLayoutResId = isStored ? R.layout.cell_stored_day : R.layout.cell_weekend;
                }
                else
                {
                    cellLayoutResId = isStored ? R.layout.cell_stored_day : R.layout.cell_day;
                }

                RemoteViews cellRv = new RemoteViews(context.getPackageName(), cellLayoutResId);
                cellRv.setTextViewText(android.R.id.text1, Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));

                Intent dayIntent = new Intent(context, OltisCalendarWidget.class);
                dayIntent.setAction(ACTION_SELECT_DAY + cal.get(Calendar.DAY_OF_YEAR));
                dayIntent.putExtra(EXTRA_YEAR, cal.get(Calendar.YEAR));
                dayIntent.putExtra(EXTRA_MONTH, cal.get(Calendar.MONTH));
                dayIntent.putExtra(EXTRA_DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR));
                cellRv.setOnClickPendingIntent(android.R.id.text1,
                        PendingIntent.getBroadcast(context, 0,
                                dayIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT));

                rowRv.addView(R.id.row_container, cellRv);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            rv.addView(R.id.calendar, rowRv);
        }

        rv.setOnClickPendingIntent(R.id.prev_month_button,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, OltisCalendarWidget.class)
                                .setAction(ACTION_PREVIOUS_MONTH),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        rv.setOnClickPendingIntent(R.id.next_month_button,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, OltisCalendarWidget.class)
                                .setAction(ACTION_NEXT_MONTH),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        rv.setOnClickPendingIntent(R.id.month_label,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, OltisCalendarWidget.class)
                                .setAction(ACTION_RESET_MONTH),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        rv.setViewVisibility(R.id.add_button, isSelectedDayStored ? View.GONE : View.VISIBLE);
        rv.setOnClickPendingIntent(R.id.add_button,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, OltisCalendarWidget.class)
                                .setAction(ACTION_STORE_DAY),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        rv.setViewVisibility(R.id.delete_button, isSelectedDayStored ? View.VISIBLE : View.GONE);
        rv.setOnClickPendingIntent(R.id.delete_button,
                PendingIntent.getBroadcast(context, 0,
                        new Intent(context, OltisCalendarWidget.class)
                                .setAction(ACTION_ERASE_DAY),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        rv.setOnClickPendingIntent(R.id.history_button,
                PendingIntent.getActivity(context, 0,
                        new Intent(context, HistoryList.class)
                                .addCategory(Intent.CATEGORY_LAUNCHER)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        rv.setViewVisibility(R.id.month_bar, View.VISIBLE);

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, rv);
    }

    @org.jetbrains.annotations.NotNull
    private Vector<DayClass> getRecentDays(Context context, int year, int month)
    {
        Vector<DayClass> days = new Vector<>();

        // Retrieve days for previous month
        int newMonth = month - 1;
        int newYear = year;
        if(newMonth < 0)
        {
            newMonth = 11;
            --newYear;
        }
        int daysInLastMonth[] = DatabaseHelper.getInstance(context).getDaysForDate(newYear, newMonth);
        if(daysInLastMonth[0] != 0)
        {
            days.add(new DayClass(newYear, getDayOfYear(newYear, newMonth, daysInLastMonth[0])));
        }
        if(daysInLastMonth[1] != 0)
        {
            days.add(new DayClass(newYear, getDayOfYear(newYear, newMonth, daysInLastMonth[1])));
        }

        // Retrieve days for this month
        int daysInThisMonth[] = DatabaseHelper.getInstance(context).getDaysForDate(year, month);
        if(daysInThisMonth[0] != 0)
        {
            days.add(new DayClass(year, getDayOfYear(year, month, daysInThisMonth[0])));
        }
        if(daysInThisMonth[1] != 0)
        {
            days.add(new DayClass(year, getDayOfYear(year, month, daysInThisMonth[1])));
        }

        // Retrieve days for next month
        newMonth = month + 1;
        newYear = year;
        if(newMonth > 11)
        {
            newMonth = 0;
            ++newYear;
        }
        int daysInNextMonth[] = DatabaseHelper.getInstance(context).getDaysForDate(newYear, newMonth);
        if(daysInNextMonth[0] != 0)
        {
            days.add(new DayClass(newYear, getDayOfYear(newYear, newMonth, daysInNextMonth[0])));
        }
        if(daysInNextMonth[1] != 0)
        {
            days.add(new DayClass(newYear, getDayOfYear(newYear, newMonth, daysInNextMonth[1])));
        }

        return days;
    }

    private int getDayOfYear(int year, int month, int day)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        cal.add(Calendar.DAY_OF_MONTH, day);

        return cal.get(Calendar.DAY_OF_YEAR);
    }
}
