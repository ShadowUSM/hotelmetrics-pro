package com.travio.app.data

import android.content.Context
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripRepository(private val context: Context) {

    private val gson = Gson()

    fun loadTrip(): Trip {
        val json = context.assets.open("trip_barcelona2.json").bufferedReader().use { it.readText() }
        return gson.fromJson(json, Trip::class.java)
    }

    fun currentDayIndex(trip: Trip): Int {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = fmt.format(Date())
        val idx = trip.days.indexOfFirst { it.date >= today }
        return if (idx >= 0) idx else 0
    }
}

object ExpenseStore {
    private val expenses = mutableListOf(
        Expense("e1", "Cervecería Catalana", "food", 58.0, "2026-10-29"),
        Expense("e2", "Recinte Sant Pau — bilety", "landmark", 54.0, "2026-10-29"),
        Expense("e3", "Metro T-Casual x3", "transport", 14.0, "2026-10-29")
    )

    fun all(): List<Expense> = expenses.toList()

    fun add(expense: Expense) {
        expenses.add(0, expense)
    }

    fun totalSpent(): Double = expenses.sumOf { it.amount }

    fun totalsByCategory(): Map<String, Double> =
        expenses.groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amount } }
}
