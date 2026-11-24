package com.example.planttracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlantAdapter
    private lateinit var plants: MutableList<Plant>
    private lateinit var repository: PlantRepository

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Разрешение на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        repository = PlantRepository(this)
        plants = repository.load()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PlantAdapter(plants) {
            repository.save(plants)
        }
        recyclerView.adapter = adapter

        // Кнопка "Добавить растение"
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }

        // Планирование напоминаний раз в день
        scheduleReminder()
    }

    private fun showAddDialog() {
        val input = EditText(this)
        input.hint = "Название растения"

        AlertDialog.Builder(this)
            .setTitle("Новое растение")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val newId = (plants.maxOfOrNull { it.id } ?: 0) + 1
                    plants.add(Plant(newId, name, System.currentTimeMillis()))
                    adapter.notifyItemInserted(plants.size - 1)
                    recyclerView.scrollToPosition(plants.size - 1)
                    repository.save(plants)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun scheduleReminder() {
        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "water_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}