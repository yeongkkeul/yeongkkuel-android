package com.example.yeongkkuel.presentation.base

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.ActivityMainBinding
import com.example.yeongkkuel.presentation.statbotsheet.StatBotSheetViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val statBotSheetViewModel: StatBotSheetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        // 스플래시 화면 설정
        val splashScreen = this.installSplashScreen()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.iconView.animate()
                .translationY(-splashScreenView.iconView.height.toFloat())
                .setDuration(10)
                .withEndAction {
                    splashScreenView.remove()
                }
                .start()
        }

        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 스플래시 화면 종료 조건 설정 (예: 데이터 초기화 완료)
        splashScreen.setKeepOnScreenCondition {
            // 앱 초기화 작업이 완료될 때까지 유지
            checkInitialization()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment

        val navController = navHostFragment.navController

        // BottomNavigationView 설정
        binding.bottomNavi.setupWithNavController(navController)

        // BottomNavigationView 아이템 선택 리스너 설정
        binding.bottomNavi.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }

                R.id.navigation_chat -> {
                    navController.navigate(R.id.navigation_chat)
                    true
                }

                R.id.navigation_stat -> {
                    navController.navigate(R.id.navigation_stat)
                    true
                }

                R.id.navigation_my -> {
                    navController.navigate(R.id.navigation_my)
                    true
                }

                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 바텀네비게이션 뷰 숨김 처리 - 스플래시, 로그인
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_splash, R.id.navigation_login, R.id.navigation_signup -> hideBottomNavigation(true)
                else -> hideBottomNavigation(false)
            }
        }
    }

    private fun hideBottomNavigation(state: Boolean) {
        if (state) binding.bottomNavi.visibility = View.GONE else binding.bottomNavi.visibility =
            View.VISIBLE
    }
    private fun checkInitialization(): Boolean {
        return false // false를 반환하면 스플래시 화면 종료
    }
}