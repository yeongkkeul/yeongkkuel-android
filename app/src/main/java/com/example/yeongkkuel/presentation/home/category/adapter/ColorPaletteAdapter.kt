package com.example.yeongkkuel.presentation.home.category.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryAddBinding

class ColorPaletteAdapter(
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorPaletteAdapter.ColorViewHolder>() {

    private var _binding: FragmentCategoryAddBinding? = null
    private val binding get() = _binding!!
    private var selectedColor: Int? = null
    private val colorList = mutableListOf<Int>()
    private val colorPaletteAdapter by lazy {
        ColorPaletteAdapter(onColorSelected = { selectedColor ->
            updateSelectedColor(selectedColor) // 선택된 색상을 처리하는 함수 호출
        })
    }

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
        val color = colorList[position]
        holder.bind(color)

        // 로그 추가
        Log.d("ColorPaletteAdapter", "Binding color at position $position: $color")
    }

    override fun getItemCount(): Int = colorList.size

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.view_color_circle)

        fun bind(color: Int) {
            colorView.setBackgroundResource(R.drawable.bg_color_circle) // 원형 배경 설정
            colorView.background?.mutate()?.setTint(color) // 내부 색상 적용
            colorView.setOnClickListener {
                onColorSelected(color) // 선택된 색상 콜백 전달
            }
        }

    }
    private fun updateSelectedColor(color: Int) {
        selectedColor = color
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle)
        binding.ivSelectedColor.background?.mutate()?.setTint(color)
        binding.rvColorPalette.visibility = View.GONE
    }
}
