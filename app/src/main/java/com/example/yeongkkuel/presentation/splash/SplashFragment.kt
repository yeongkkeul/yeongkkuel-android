package com.example.yeongkkuel.presentation.splash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import kotlinx.coroutines.*

class SplashFragment : Fragment() {



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

        val logo = view.findViewById<ImageView>(R.id.iv_logo)

        // 로고 애니메이션 실행
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        logo.startAnimation(slideUp)

        // 애니메이션 완료 후 LoginFragment로 이동
        logo.postDelayed({
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }, 1000) // 애니메이션 지속 시간과 동일하게 설정


    }


}