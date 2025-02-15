package com.example.yeongkkuel.presentation.botsheet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.R
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.expenditure.DayExpenditureResponse
import com.example.yeongkkuel.presentation.home.category.data.Category
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseListResponse
import com.example.yeongkkuel.presentation.util.Colors
import com.example.yeongkkuel.presentation.util.SpendingCategory
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar


class BotSheetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<BotSheetUiState>(BotSheetUiState.init())
    val uiState = _uiState.asStateFlow()

    private val statService = RetrofitClient.statService
    private val expenseApiService = RetrofitClient.expenseApiService

    // ✅ LiveData → StateFlow로 일관된 상태 관리
    private val _spendingHistoryList =
        MutableStateFlow<List<BotSheetUiState.Spending.History>>(emptyList())
    val spendingHistoryList = _spendingHistoryList.asStateFlow()

    private val _categoryList = MutableLiveData<List<Category>>(emptyList()) // ✅ MutableLiveData 선언 추가
    val categoryList: LiveData<List<Category>> get() = _categoryList // ✅ LiveData로 접근
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> get() = _deleteResult

    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            try {
                val response = expenseApiService.deleteExpense(expenseId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _deleteResult.postValue(true) // ✅ 삭제 성공
                } else {
                    Log.e("BotSheetViewModel", "삭제 실패: ${response.body()?.message}")
                    _deleteResult.postValue(false) // ✅ 삭제 실패
                }
            } catch (e: Exception) {
                Log.e("BotSheetViewModel", "API 호출 중 오류 발생", e)
                _deleteResult.postValue(false)
            }
        }
    }

    private fun removeExpenseFromUi(expenseId: Int) {
        _uiState.update { prevState ->
            val updatedSpendingList = prevState.spendingList.map { spending ->
                val updatedHistory = spending.history.filterNot { it.id == expenseId }
                spending.copy(history = updatedHistory)
            }.filterNot { it.history.isEmpty() } // 🔥 내역이 비어 있으면 해당 카테리 삭제

            prevState.copy(spendingList = updatedSpendingList)
        }

        // ✅ 최신 지출 내역 반영
        updateSpendingHistoryList()
        Log.d("BotSheetViewModel", "📌 삭제됨: expenseId=$expenseId, 남은 지출 개수=${_spendingHistoryList.value.size}")
    }

    // 🔹 지출 내역 추가 기능
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

    private val mutex = Mutex()

    suspend fun updateBotSheetHistory(updatedExpense: BotSheetUiState.Spending.History) {
        Log.d("BotSheetViewModel", "🔄 updateBotSheetHistory() 실행됨: $updatedExpense")

        mutex.withLock {
            _uiState.update { prevState ->
                val updatedSpendingList = prevState.spendingList.map { spending ->
                    val updatedHistoryList = spending.history.map { history ->
                        if (history.id == updatedExpense.id) {
                            updatedExpense.copy() // ✅ 내용과 금액만 변경 (카테고리 변경 없음)
                        } else {
                            history
                        }
                    }
                    spending.copy(history = updatedHistoryList) // ✅ 카테고리 수정 없이 내용만 업데이트

                }
                Log.d("BotSheetViewModel", "✅ spendingList 업데이트 완료: $updatedSpendingList")
                prevState.copy(spendingList = updatedSpendingList)
            }

            // ✅ 추가된 내역을 반영하기 위해 `addExpenseToCategory()` 호출
            addExpenseToCategory(
                SpendingCategory.CUSTOM(updatedExpense.name),
                updatedExpense
            )
            addExpenseHistory(updatedExpense)
            updateSpendingHistoryList() // ✅ 최신 데이터 반영
        }
    }

    private fun Category.toBotSheetSpending(): BotSheetUiState.Spending {
        return BotSheetUiState.Spending(
            categoryId = this.id, // ✅ 기존 categoryId → id 로 변경
            kind = SpendingCategory.fromName(this.name), // ✅ 기존 categoryName → name 변경
            color = this.color, // ✅ 기존 Colors.RED1 → 서버에서 받은 색상 적용
            plusIconResId = R.drawable.ic_plus_default,
            history = emptyList() // ✅ 초기 history는 비워둠 (이후 업데이트 가능)
        )
    }

    fun updateBotSheetCategories(categories: List<Category>) {
        Log.d("BotSheetViewModel", "🚀 updateBotSheetCategories 실행됨! categories: $categories")

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
            Log.d("BotSheetViewModel", "✅ 바텀시트 UI 업데이트 완료! 카테고리 개수: ${updatedSpendingList.size}")
            prevState.copy(spendingList = updatedSpendingList)
        }
    }

    fun updateBotSheetData(response: ExpenseListResponse) {
        _uiState.update { prevState ->
            val updatedSpendingList = response.result.map { expense ->
                Log.d("BotSheetViewModel", "🚀 updateBotSheetData(): expenseId=${expense.id}, category=${expense.content}")

                BotSheetUiState.Spending(
                    categoryId = expense.id,
                    kind = SpendingCategory.fromName(expense.content),
                    color = Colors.RED1, // 서버에서 색상을 제공하는 경우 수정 필요
                    plusIconResId = R.drawable.ic_plus_default,
                    history = listOf(
                        BotSheetUiState.Spending.History(
                            id = expense.id,
                            name = expense.content,
                            price = expense.amount,
                            imgExist = expense.imageUrl?.isNotEmpty() ?: false // ✅ 안전한 처리
                        )
                    )
                )
            }
            prevState.copy(spendingList = updatedSpendingList)
        }
    }

    private fun updateSpendingHistoryList() {
        val historyList = _uiState.value.spendingList.flatMap { it.history }

        Log.d("BotSheetViewModel", "📌 updateSpendingHistoryList() 실행됨")
        Log.d("BotSheetViewModel", "📌 최신 spendingHistoryList: $historyList") // ✅ 최신 리스트 확인

        _spendingHistoryList.value = historyList // ✅ 최신 리스트로 갱신
    }

    // 🔹 카테고리 이동 기능
    fun moveCategory(fromPosition: Int, toPosition: Int) {
        val updateList = uiState.value.spendingList.toMutableList()
        val item = updateList.removeAt(fromPosition)
        updateList.add(toPosition, item)

        _uiState.update { prev ->
            prev.copy(spendingList = updateList)
        }
    }

    //지출 내역 삭제
    fun removeExpense(expenseName: String) {
        _uiState.update { prev ->
            val updatedList = prev.spendingList.map { spending ->
                spending.copy(history = spending.history.filterNot { it.name == expenseName })
            }.filterNot { it.history.isEmpty() } // 🔥 내역이 비어 있으면 해당 카테고리 제거

            prev.copy(spendingList = updatedList)
        }

        // 최신 내역 업데이트
        updateSpendingHistoryList()

        Log.d(
            "BotSheetViewModel",
            "📌 삭제됨: $expenseName, 남은 지출 개수: ${_spendingHistoryList.value.size}"
        )
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

                        // ✅ 추가: 바텀시트 UI 업데이트
                        updateBotSheetCategories(categories)

                        _uiState.update { prev ->
                            val updatedSpendingList = categories.map { category ->
                                // ✅ 카테고리 ID 기반으로 해당 카테고리의 지출 내역 찾기
                                val expenses = result.categories
                                    .find { it.categoryId == category.id }?.expenses ?: emptyList()

                                BotSheetUiState.Spending(
                                    categoryId = category.id, // ✅ 기존 1 → category.id 로 수정
                                    kind = SpendingCategory.fromName(category.name),
                                    color = category.color,
                                    plusIconResId = R.drawable.ic_plus_default,
                                    history = expenses.map { expense -> // ✅ expenses를 여기서 가져오기
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
                        updateSpendingHistoryList() // 최신 데이터 반영
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BotSheetViewModel", "🚨 getSpendingList() 오류: ${e.message}")
        }
    }



//    fun updateExpense(updatedExpense: BotSheetUiState.Spending.History) {
//        _spendingHistoryList.value = _spendingHistoryList.value.map { expense ->
//            if (expense.date == updatedExpense.date && expense.name == updatedExpense.name) {
//                updatedExpense // 기존 항목을 수정된 값으로 변경
//            } else {
//                expense
//            }
//        }
//    }

    fun getCategoryList(): List<Category> {
        val categoryList = _uiState.value.spendingList.map { spending ->
            Category(
                id = spending.categoryId,
                name = spending.kind.name,
                color = spending.color
            )
        }
        Log.d("BotSheetViewModel", "getCategoryList() 반환: $categoryList")
        Log.d("BotSheetViewModel", "📌 getCategoryList() 호출됨, 현재 카테고리 개수: ${_categoryList.value?.size ?: 0}")
        return categoryList
    }

    fun addExpenseHistory(history: BotSheetUiState.Spending.History) {
        _spendingHistoryList.value = _spendingHistoryList.value + history
    }

}