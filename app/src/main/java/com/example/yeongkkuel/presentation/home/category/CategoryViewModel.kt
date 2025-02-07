package com.example.yeongkkuel.presentation.home.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.home.category.data.Category
import com.example.yeongkkuel.presentation.home.category.data.toCategory
import com.example.yeongkkuel.presentation.home.category.data.toRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CategoryViewModel : ViewModel() {

    // 초기 카테고리 데이터 없음
    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> get() = _categories

    // 최대 카테고리 개수
    private val maxCategories = 6

    // 에러 메시지 LiveData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // 카테고리 목록 가져오기 (서버와 동기화)
    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.yeongkkuelService.getCategories()
                if (response.isSuccess) { // 커스텀 Response의 isSuccess 확인
                    _categories.value = response.result.map { it.toCategory() } // result를 로컬 데이터로 변환
                } else {
                    _errorMessage.value = "Failed to fetch categories: ${response.message}" // 서버 메시지 활용
                }
            } catch (e: HttpException) {
                _errorMessage.value = "Server error: ${e.message()}"
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    // 카테고리 추가 (서버와 동기화)
    fun addCategory(category: Category): Boolean {
        val currentList = _categories.value.orEmpty()

        if (currentList.size >= maxCategories) {
            return false
        }

        viewModelScope.launch {
            try {
                val request = category.toRequest() // 로컬 데이터를 요청 데이터로 변환
                val response = RetrofitClient.yeongkkuelService.addCategory(request)
                if (response.isSuccess) { // 커스텀 Response의 isSuccess 확인
                    val updatedList = currentList.toMutableList().apply { add(category) }
                    _categories.value = updatedList
                } else {
                    _errorMessage.value = "Failed to add category: ${response.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
        return true
    }

    // 카테고리 삭제 (서버와 동기화)
    fun removeCategory(categoryName: String) {
        val categoryToDelete = _categories.value.orEmpty().find { it.name == categoryName }
        if (categoryToDelete != null) {
            viewModelScope.launch {
                try {
                    val response = RetrofitClient.yeongkkuelService.deleteCategory(categoryToDelete.id)
                    if (response.isSuccess) { // 커스텀 Response의 isSuccess 확인
                        val updatedList = _categories.value.orEmpty().filterNot { it.name == categoryName }
                        _categories.value = updatedList
                    } else {
                        _errorMessage.value = "Failed to delete category: ${response.message}"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
                }
            }
        }
    }

    // 카테고리 수정 (서버와 동기화)
    fun updateCategory(originalName: String, updatedCategory: Category) {
        val categoryToUpdate = _categories.value.orEmpty().find { it.name == originalName }
        if (categoryToUpdate != null) {
            viewModelScope.launch {
                try {
                    val request = updatedCategory.toRequest() // 로컬 데이터를 요청 데이터로 변환
                    val response = RetrofitClient.yeongkkuelService.updateCategory(categoryToUpdate.id, request)
                    if (response.isSuccess) { // 커스텀 Response의 isSuccess 확인
                        val currentCategories = _categories.value?.toMutableList() ?: mutableListOf()
                        val index = currentCategories.indexOfFirst { it.name == originalName }
                        if (index != -1) {
                            currentCategories[index] = updatedCategory
                            _categories.value = currentCategories
                        }
                    } else {
                        _errorMessage.value = "Failed to update category: ${response.message}"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
                }
            }
        }
    }

    // 카테고리 목록 비어 있는지 확인
    fun isCategoryListEmpty(): Boolean {
        return _categories.value.isNullOrEmpty()
    }
}
