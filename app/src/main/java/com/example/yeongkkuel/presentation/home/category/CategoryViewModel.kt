package com.example.yeongkkuel.presentation.home.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.home.category.Category

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

    // 카테고리 추가 함수
    fun addCategory(category: Category) {
        if (_categories.value?.size ?: 0 >= 6) return // 최대 6개 제한
        _categories.value = _categories.value?.plus(category)
    }

    // 카테고리 삭제 함수
    fun removeCategory(category: Category) {
        _categories.value = _categories.value?.filterNot { it == category }
    }
}
