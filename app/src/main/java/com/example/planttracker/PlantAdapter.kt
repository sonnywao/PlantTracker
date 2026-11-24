package com.example.planttracker
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantAdapter(
    private val plants: MutableList<Plant>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    class PlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPlantName)
        val tvLastWatered: TextView = view.findViewById(R.id.tvLastWatered)
        val btnWater: Button = view.findViewById(R.id.btnWater)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plants[position]
        val day = 24L * 60 * 60 * 1000
        val daysAgo = ((System.currentTimeMillis() - plant.lastWatered) / day).toInt()

        holder.tvName.text = plant.name

        val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(plant.lastWatered))

        holder.tvLastWatered.text = "Последний полив: $formattedDate ($daysAgo дн. назад)"

        // Подсветка, если пора полить (7+ дней)
        if (daysAgo >= 7) {
            holder.tvLastWatered.setTextColor(0xFFD32F2F.toInt())
        } else {
            holder.tvLastWatered.setTextColor(0xFF388E3C.toInt())
        }

        holder.btnWater.setOnClickListener {
            plant.lastWatered = System.currentTimeMillis()
            notifyItemChanged(position)
            onChanged()
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setMessage("Удалить «${plant.name}»?")
                .setPositiveButton("Да") { _, _ ->
                    plants.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, plants.size)
                    onChanged()
                }
                .setNegativeButton("Нет", null)
                .show()
            true
        }
    }

    override fun getItemCount() = plants.size
}