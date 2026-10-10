package com.comp1786.events

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object EventValidation {
    private val dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)
    fun validText(value: String) = value.trim().length in 1..120
    fun validDate(value: String): Boolean {
        if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)) return false
        return try { LocalDate.parse(value, dateFormat).year in 1900..2100 }
        catch (_: java.time.format.DateTimeParseException) { false }
    }
}
