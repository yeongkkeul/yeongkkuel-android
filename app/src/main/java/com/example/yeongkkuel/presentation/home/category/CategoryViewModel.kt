package com.example.yeongkkuel.presentation.home.category

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

    // 카테고리 추가 함수
    fun addCategory(category: Category): Boolean {
        val currentList = _categories.value.orEmpty()

        if (currentList.size >= maxCategories) {
            return false
        }

        // 새로운 카테고리 리스트로 업데이트
        val updatedList = currentList.toMutableList().apply { add(category) }
        _categories.value = updatedList
        return true
    }

    // 카테고리 삭제 함수
    fun removeCategory(categoryName: String) {
        // 기존 카테고리 리스트에서 해당 이름 삭제
        val updatedList = _categories.value.orEmpty().filterNot { it.name == categoryName }

        // LiveData 갱신
        _categories.value = updatedList
    }


    // 카테고리 수정 함수
    fun updateCategory(originalName: String, updatedCategory: Category) {
        val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
        val index = currentCategories.indexOfFirst { it.name == originalName }

        if (index != -1) {
            // 기존 카테고리 업데이트
            currentCategories[index] = updatedCategory
            _categories.value = currentCategories // LiveData 갱신
        }
    }

    fun isCategoryListEmpty(): Boolean {
        return _categories.value.isNullOrEmpty()
    }
}
