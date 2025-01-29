package com.example.yeongkkuel.presentation.home.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.presentation.util.Colors

class CategoryViewModel : ViewModel() {

    // 초기 카테고리 데이터 없음
    private val _categories = MutableLiveData<List<Category>>(emptyList())

    val categories: LiveData<List<Category>> get() = _categories

    // 최대 카테고리 개수
    private val maxCategories = 6

    /**
     * 카테고리 추가 함수
     */
    fun addCategory(category: Category): Boolean {
        val currentList = _categories.value.orEmpty()

        if (currentList.size >= maxCategories) {
            Log.d("CategoryViewModel", "카테고리 추가 제한 도달")
            return false
        }

        // 새로운 카테고리 리스트로 업데이트
        val updatedList = currentList.toMutableList().apply { add(category) }
        _categories.value = updatedList
        Log.d("CategoryViewModel", "카테고리 추가 후: ${_categories.value}")
        return true
    }

    /**
     * 카테고리 삭제 함수
     */
    fun removeCategory(categoryName: String) {
        val updatedList = _categories.value.orEmpty().filterNot { it.name == categoryName }
        _categories.value = updatedList
        Log.d("CategoryViewModel", "카테고리 삭제 후: ${_categories.value}")
    }

    /**
     * 카테고리 수정 함수
     */
    fun updateCategory(originalCategoryName: String, updatedCategory: Category) {
        val currentList = _categories.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.name == originalCategoryName }
        if (index != -1) {
            currentList[index] = updatedCategory
            _categories.value = currentList
            Log.d("CategoryViewModel", "카테고리 수정 후: ${_categories.value}")
        } else {
            Log.d("CategoryViewModel", "수정할 카테고리를 찾을 수 없습니다.")
        }
    }

    fun isCategoryListEmpty(): Boolean {
        return _categories.value.isNullOrEmpty()
    }
}
