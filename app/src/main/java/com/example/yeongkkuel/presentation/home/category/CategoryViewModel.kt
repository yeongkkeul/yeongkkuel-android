package com.example.yeongkkuel.presentation.home.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.R

class CategoryViewModel : ViewModel() {

    // 초기 카테고리 데이터
    private val _categories = MutableLiveData<List<Category>>(
        listOf(
            Category("간식/음료", R.color.green9),
            Category("밥/배달", R.color.purple5),
            Category("화장품", R.color.green10),
            Category("택시비", R.color.pink3)
        )
    )
    val categories: LiveData<List<Category>> get() = _categories

    fun addCategory(category: Category) {
        if (_categories.value?.size ?: 0 >= 6) {
            Log.d("CategoryViewModel", "카테고리 추가 제한 도달")
            return
        }
        val updatedList = _categories.value.orEmpty().toMutableList().apply { add(category) }
        _categories.value = updatedList
        Log.d("CategoryViewModel", "카테고리 추가 후: ${_categories.value}")
    }




    // 카테고리 삭제 함수
    fun removeCategory(category: Category) {
        _categories.value = _categories.value?.filterNot { it == category }
    }
}
