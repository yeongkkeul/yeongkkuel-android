package com.example.yeongkkuel.presentation.botsheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.R
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.expenditure.DayExpenditureResponse
import com.example.yeongkkuel.presentation.home.category.data.Category
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseUpdateRequest
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseUpdateResponse
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar


class BotSheetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<BotSheetUiState>(BotSheetUiState.init())
    val uiState = _uiState.asStateFlow()

    private val statService = RetrofitClient.statService
    private val expenseApiService = RetrofitClient.expenseApiService

    private val _spendingHistoryList =
        MutableStateFlow<List<BotSheetUiState.Spending.History>>(emptyList())
    val spendingHistoryList = _spendingHistoryList.asStateFlow()

    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> get() = _categoryList
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            try {
                val response = expenseApiService.deleteExpense(expenseId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _deleteResult.postValue(true) // 삭제 성공
                } else {
                    _deleteResult.postValue(false) // 삭제 실패
                }
            } catch (e: Exception) {
                _deleteResult.postValue(false)
            }
        }
    }

    // 지출 내역 수정 페이지 - 날짜 수정시 그 날 카테고리에서 제거함
    fun removeExpenseFromCategory(expenseId: Int) {
        _uiState.update { prevState ->
            val updatedSpendingList = prevState.spendingList.map { category ->
                category.copy(
                    history = category.history.filter { it.id != expenseId }
                )
            }
            prevState.copy(spendingList = updatedSpendingList)
        }
    }

    // 지출 내역 추가 기능
    fun addExpenseToCategory(category: SpendingCategory, history: BotSheetUiState.Spending.History) {
        _uiState.update { prev ->
            val updatedList = prev.spendingList.toMutableList()

            // 기존 카테고리가 있는지 확인
            val categoryIndex = updatedList.indexOfFirst { it.kind == category }

            if (categoryIndex != -1) {
                // 기존 카테고리가 있으면 해당 카테고리에 내역 추가
                val existingCategory = updatedList[categoryIndex]

                val updatedCategory = existingCategory.copy(
                    history = existingCategory.history + history
                )

                updatedList[categoryIndex] = updatedCategory
            } else {
                // 기존 카테고리가 없으면 새로 추가 (색상 유지)
                updatedList.add(
                    BotSheetUiState.Spending(
                        categoryId = history.id,
                        kind = category,
                        color = prev.spendingList.find { it.kind == category }?.color ?: Colors.BLACK1,
                        plusIconResId = R.drawable.ic_plus_default,
                        history = listOf(history)
                    )
                )
            }

            prev.copy(spendingList = updatedList)
        }
        updateSpendingHistoryList()
    }

    fun updateBotSheetCategories(categories: List<Category>) {
        _categoryList.postValue(categories)

        val updatedSpendingList = categories.map { category ->
            BotSheetUiState.Spending(
                categoryId = category.id,
                kind = SpendingCategory.fromName(category.name),
                color = category.color,
                plusIconResId = R.drawable.ic_plus_default,
                history = emptyList() // 기본값 (필요에 따라 업데이트 가능)
            )
        }
        _uiState.update { prevState ->
            prevState.copy(spendingList = updatedSpendingList)
        }
    }


    private fun updateSpendingHistoryList() {
        val historyList = _uiState.value.spendingList.flatMap { it.history }
        _spendingHistoryList.value = historyList
    }

    // 카테고리 이동 기능
    fun moveCategory(fromPosition: Int, toPosition: Int) {
        val updateList = uiState.value.spendingList.toMutableList()
        val item = updateList.removeAt(fromPosition)
        updateList.add(toPosition, item)

        _uiState.update { prev ->
            prev.copy(spendingList = updateList)
        }
    }

    // 카테고리 추가 기능
    fun addCategory(category: Category) {
        val spendingCategory = SpendingCategory.CUSTOM(category.name)
        val categoryColor = category.color

        val plusIconResId = mapCategoryToIcon(categoryColor)

        _uiState.update { prev ->
            val updatedList = prev.spendingList.toMutableList().apply {
                add(
                    BotSheetUiState.Spending(
                        categoryId = category.id,
                        kind = spendingCategory,
                        color = categoryColor,
                        plusIconResId = plusIconResId,
                        history = emptyList()
                    )
                )
            }
            prev.copy(spendingList = updatedList)
        }
        // 추가된 카테고리를 _categoryList에 업데이트
        _categoryList.value = _categoryList.value.orEmpty() + category
    }

    private fun mapCategoryToIcon(color: Colors): Int {
        return R.drawable.ic_plus_default // 모든 아이콘은 동일한 XML을 사용
    }

    // 카테고리 제목, 색상 수정 후 바텀시트 업로드
    fun updateCategory(originalCategoryName: String, updatedCategory: Category) {
        val updatedCategoryColor = updatedCategory.color

        val updatedSpendingList = uiState.value.spendingList.map { spending ->
            if (spending.kind.name == originalCategoryName) {
                spending.copy(
                    kind = SpendingCategory.fromName(updatedCategory.name),
                    color = updatedCategoryColor
                )
            } else {
                spending
            }
        }

        // 기존 카테고리 리스트 업데이트
        _categoryList.value = _categoryList.value?.map { category ->
            if (category.name == originalCategoryName) {
                updatedCategory
            } else {
                category
            }
        }

        _uiState.update { prevState ->
            prevState.copy(spendingList = updatedSpendingList)
        }
    }

    // 카테고리 삭제 연동 기능
    fun removeCategory(categoryName: String) {
        _uiState.update { prev ->
            val updatedSpendingList = prev.spendingList.filter { it.kind.name != categoryName }
            prev.copy(spendingList = updatedSpendingList)
        }
    }

    // 일일 목표 지출 가져오기
    fun getDayTargetSpending() = viewModelScope.launch {
        try {
            val response = statService.getExpendituresDay()

            if (response.isSuccessful) { // HTTP 상태 코드 200~299
                response.body()?.let { body -> // body가 직접 만든 Response<T> 구조를 따름
                    if (body.isSuccess) {
                        _uiState.update { prev ->
                            prev.copy(targetSpending = body.result.dayTargetExpenditure)
                        }
                    } else if (body.code == "EXPENSE4005") { // 목표 지출액이 없을 경우
                        _uiState.update { prev ->
                            prev.copy(targetSpending = -1)
                        }
                    }
                }
            } else {
                // HTTP 400 에러 응답을 직접 처리
                val errorBody = response.errorBody()?.string() ?: "{}"
                val errorResponseType = object : TypeToken<Response<DayExpenditureResponse>>() {}.type
                val errorResponse = Gson().fromJson<Response<DayExpenditureResponse>>(errorBody, errorResponseType)


                if (errorResponse.code == "EXPENSE4005") { // 목표 지출액이 없을 경우
                    _uiState.update { prev ->
                        prev.copy(targetSpending = -1)
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    // 매개변수 없는 기본 함수
    fun getSpendingList() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        getSpendingList(year, month, day) // 기존 함수 호출
        getDayTargetSpending()
    }

    // 매개변수를 받는 기존 함수
    // 월별 지출 내역 가져오기
    fun getSpendingList(year: Int, month: Int, day: Int) = viewModelScope.launch {
        try {
            // yeongkkuelService를 통해 데이터 요청
            statService.getExpendituresMonthCategory(year, month, day).run {
                if (isSuccess) {
                    result.run {
                        val categories = categories.map { category ->
                            Category(
                                id = category.categoryId,
                                name = category.categoryName,
                                color = Colors.fromRGB(
                                    red = category.red,
                                    blue = category.blue,
                                    green = category.green
                                ) ?: Colors.RED1
                            )
                        }

                        // 바텀시트 UI 업데이트
                        updateBotSheetCategories(categories)

                        _uiState.update { prev ->
                            val updatedSpendingList = categories.map { category ->

                                val expenses = result.categories
                                    .find { it.categoryId == category.id }?.expenses ?: emptyList()

                                BotSheetUiState.Spending(
                                    categoryId = category.id,
                                    kind = SpendingCategory.fromName(category.name),
                                    color = category.color,
                                    plusIconResId = R.drawable.ic_plus_default,
                                    history = expenses.map { expense ->
                                        BotSheetUiState.Spending.History(
                                            id = expense.expenseId,
                                            name = expense.expenseName,
                                            price = expense.expenseAmount,
                                            imgExist = expense.imgExist
                                        )
                                    }
                                )
                            }
                            prev.copy(
                                spendingList = updatedSpendingList,
                                date = Calendar.getInstance().apply {
                                    set(year, month - 1, day)
                                }.time
                            )
                        }
                        updateSpendingHistoryList()
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    // 지출 내역 수정
    suspend fun updateExpense(expenseId: Int, request: ExpenseUpdateRequest, imageFile: MultipartBody.Part?): ExpenseUpdateResponse? {
        return try {
            // 1. 수정할 필드들을 하나의 Map으로 묶어 JSON으로 변환
            val updateMap = mapOf(
                "day" to request.day,
                "categoryId" to request.categoryId,
                "content" to request.content,
                "amount" to request.amount
            )
            val json = Gson().toJson(updateMap)
            val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            // 2. 수정 API 호출 (Retrofit 인터페이스는 수정된 버전을 사용)
            val response = expenseApiService.updateExpense(expenseId, requestBody, imageFile)
            if (response.isSuccessful) {
                val updateResponse = response.body()
                updateResponse?.let {
                    if (it.isSuccess) {
                        // 3. 성공 시 UI 상태 업데이트 (바텀시트 내 데이터 반영)
                        _uiState.update { prevState ->
                            val updatedSpendingList = prevState.spendingList.map { spending ->
                                if (spending.history.any { it.id == expenseId }) {
                                    val updatedHistory = spending.history.map { history ->
                                        if (history.id == expenseId) {
                                            history.copy(
                                                name = request.content,
                                                price = request.amount,
                                                imgExist = imageFile != null
                                            )
                                        } else {
                                            history
                                        }
                                    }
                                    spending.copy(history = updatedHistory)
                                } else {
                                    spending
                                }
                            }
                            prevState.copy(spendingList = updatedSpendingList)
                        }
                    }
                }
                updateResponse
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }




    fun moveExpenseToNewDate(expense: BotSheetUiState.Spending.History, newDate: String) {
        _uiState.update { prevState ->
            val oldSpendingList = prevState.spendingList.toMutableList()

            // ✅ 기존 날짜에서 해당 내역 제거 (새로운 리스트 생성)
            val updatedSpendingList = prevState.spendingList.toMutableList()

            // ✅ 기존 날짜에서 해당 내역 제거
            updatedSpendingList.forEachIndexed { index, spending ->
                if (spending.history.any { it.id == expense.id }) {
                    val newHistory = spending.history.filterNot { it.id == expense.id }
                    updatedSpendingList[index] = spending.copy(history = newHistory)
                }
            }

            // ✅ 새로운 날짜의 Spending을 찾아서 추가 (없으면 새로 생성)
            val targetSpendingIndex = updatedSpendingList.indexOfFirst { it.kind.name == "기타" }
            val updatedHistory = expense.copy() // ✅ 기존 데이터 유지한 채 새로운 내역 추가

            if (targetSpendingIndex != -1) {
                // ✅ 기존 카테고리에 추가 (copy()로 새로운 객체 생성)
                val updatedSpending = updatedSpendingList[targetSpendingIndex].copy(
                    history = updatedSpendingList[targetSpendingIndex].history + updatedHistory
                )
                updatedSpendingList[targetSpendingIndex] = updatedSpending
            } else {
                // ✅ 새로운 카테고리 생성 후 추가
                updatedSpendingList.add(
                    BotSheetUiState.Spending(
                        categoryId = -1, // 기본값
                        kind = SpendingCategory.fromName("기타"),
                        color = Colors.BLACK1,
                        plusIconResId = R.drawable.ic_plus_default,
                        history = listOf(updatedHistory)
                    )
                )
            }

            prevState.copy(spendingList = updatedSpendingList)
        }

        updateSpendingHistoryList() // ✅ 최신 데이터 반영
    }

    fun getCategoryList(): List<Category> {
        val categoryList = _uiState.value.spendingList.map { spending ->
            Category(
                id = spending.categoryId,
                name = spending.kind.name,
                color = spending.color
            )
        }
        return categoryList
    }

    fun addExpenseHistory(history: BotSheetUiState.Spending.History) {
        _spendingHistoryList.value = _spendingHistoryList.value + history
    }

}