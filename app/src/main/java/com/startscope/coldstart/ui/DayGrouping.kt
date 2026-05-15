package com.startscope.coldstart.ui

import com.startscope.coldstart.data.local.StartEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class DaySection(
    val dayKey: String,
    val dayLabel: String,
    val items: List<StartEntity>,
)

fun groupStartsByDay(
    starts: List<StartEntity>,
    locale: java.util.Locale,
): List<DaySection> {
    if (starts.isEmpty()) return emptyList()
    val zone = ZoneId.systemDefault()
    val dayFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    return starts
        .groupBy { entity ->
            Instant.ofEpochMilli(entity.timestampMs).atZone(zone).toLocalDate().toString()
        }
        .map { (key, list) ->
            val date = list.first().let { Instant.ofEpochMilli(it.timestampMs).atZone(zone).toLocalDate() }
            DaySection(
                dayKey = key,
                dayLabel = dayFormatter.format(date),
                items = list.sortedByDescending { it.timestampMs },
            )
        }
        .sortedByDescending { it.dayKey }
}
