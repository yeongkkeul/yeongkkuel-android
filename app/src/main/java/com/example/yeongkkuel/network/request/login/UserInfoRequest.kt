package com.example.yeongkkuel.network.request.login

data class UserInfoRequest(
    val nickName: String,
    val gender: String, //STUDENT,EMPLOYEE,HOMEMAKER,SELF_EMPLOYED,UNDECIDED
    val ageGroup: String, //UNDECIDED,TEENAGER,TWENTIES,THIRTIES,FORTIES,FIFTIES,SIXTIES_AND_ABOVE
    val job: String
)

