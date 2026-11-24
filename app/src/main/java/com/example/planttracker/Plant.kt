package com.example.planttracker

data class Plant(
    val id: Int,
    val name: String,
    var lastWatered: Long // timestamp в миллисекундах
)