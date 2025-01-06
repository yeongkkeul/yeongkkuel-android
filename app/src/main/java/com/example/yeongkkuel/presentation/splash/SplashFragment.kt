package com.example.yeongkkuel.presentation.splash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import kotlinx.coroutines.*

class SplashFragment : Fragment() {


    // 스플래시 화면 표시 시간
    private val splashScreenDuration = 2000L
    private var job: Job? = null // 코루틴 작업 관리

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SplashFragment", "onViewCreated")

        job = CoroutineScope(Dispatchers.Main).launch {
            delay(splashScreenDuration)
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }

}