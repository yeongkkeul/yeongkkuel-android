package com.example.yeongkkuel.presentation.home.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.home.category.data.Category
import com.example.yeongkkuel.presentation.home.category.data.toCategory
import com.example.yeongkkuel.presentation.home.category.data.toRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CategoryViewModel : ViewModel() {

    private val botSheetViewModel: BotSheetViewModel by lazy {
        BotSheetViewModel() // BotSheetViewModel 인스턴스 생성
    }

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
                val response = RetrofitClient.categoryApiService.getCategories()
                if (response.isSuccess) {
                    val categoryListResponse = response.result
                    _categories.postValue(categoryListResponse.categoryList.map { it.toCategory() }) // ✅ postValue()로 변경

                    Log.d("CategoryViewModel", "✅ LiveData postValue() 업데이트 완료!")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Error: ${e.message}") // ✅ postValue() 사용
            }
        }
    }


    fun updateCategoryOrderLocally(updatedList: List<Category>) {
        _categories.value = updatedList // 서버 요청 없이 UI만 업데이트 -  변경된 리스트를 LiveData에 반영
        Log.d("CategoryViewModel", "✅ 카테고리 순서 로컬 업데이트 완료")
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

                // Request Body 강제 출력
                println("Request Body: $request")

                val response = RetrofitClient.categoryApiService.addCategory(request)

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
                    // 삭제하려는 카테고리 ID 로그 출력
                    println("Deleting Category ID: ${categoryToDelete.id}")

                    val response = RetrofitClient.categoryApiService.deleteCategory(categoryToDelete.id)

                    // 서버 응답 확인
                    println("Delete Category Response: $response")

                    if (response.isSuccess) { // 커스텀 Response의 isSuccess 확인
                        val updatedList = _categories.value.orEmpty().filterNot { it.name == categoryName }
                        _categories.value = updatedList
                        fetchCategories() // 삭제 후 최신 데이터 다시 불러오기

                        botSheetViewModel.removeCategory(categoryName) // 바텀시트 ViewModel에도 삭제 반영
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
                    val response = RetrofitClient.categoryApiService.updateCategory(categoryToUpdate.id, request)

                    if (response.isSuccess) {
                        fetchCategories() // ✅ 수정 후 최신 데이터 다시 불러오기 (이것만 남기기)
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
