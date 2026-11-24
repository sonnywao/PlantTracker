package com.example.planttracker

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlantRepository(context: Context) {

    private val prefs = context.getSharedPreferences("plants_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): MutableList<Plant> {
        val json = prefs.getString("plants_list", null)
        if (json == null) return defaultPlants()
        val type = object : TypeToken<MutableList<Plant>>() {}.type
        return try {
            gson.fromJson(json, type) ?: defaultPlants()
        } catch (e: Exception) {
            defaultPlants()
        }
    }

    fun save(plants: List<Plant>) {
        prefs.edit().putString("plants_list", gson.toJson(plants)).apply()
    }

    private fun defaultPlants(): MutableList<Plant> {
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000
        return mutableListOf(
            Plant(1, "Монстера", now - 10 * day),
            Plant(2, "Фикус", now - 2 * day),
            Plant(3, "Сансевиерия", now - 5 * day),
            Plant(4, "Алоэ", now - 1 * day)
        )
    }
}