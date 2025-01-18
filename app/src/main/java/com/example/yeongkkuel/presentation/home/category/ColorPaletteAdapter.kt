package com.example.yeongkkuel.presentation.home.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R

class ColorPaletteAdapter(
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorPaletteAdapter.ColorViewHolder>() {

    private val colorList = mutableListOf<Int>()

    fun submitList(colors: List<Int>) {
        colorList.clear()
        colorList.addAll(colors)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_color_circle, parent, false
        )
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colorList[position])
    }

    override fun getItemCount(): Int = colorList.size

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.view_color_circle)

        fun bind(color: Int) {
            colorView.setBackgroundColor(color)
            colorView.setOnClickListener {
                onColorSelected(color)
            }
        }
    }
}
