package com.innerpeace.themoonha.ui.activity.common

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.SharedPreferencesManager
import com.innerpeace.themoonha.data.network.ApiClient
import com.innerpeace.themoonha.databinding.ActivityMainBinding
import com.innerpeace.themoonha.ui.fragment.live.LiveMyLessonListFragment
import com.innerpeace.themoonha.ui.fragment.live.LiveOnAirListFragment
import com.innerpeace.themoonha.ui.fragment.lounge.LoungeHomeFragment
import com.kakao.sdk.common.KakaoSdk

/**
 * 메인 액티비티
 * @author 김진규
 * @since 2024.08.23
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.23  	김진규       최초 생성
 * 2024.08.24  	조희정       툴바, 네비게이션바 기능 추가
 * 2024.09.05  	김진규       네비게이션바 숨기기, 보이기 기능 추가
 * </pre>
 */
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FCM
        FirebaseApp.initializeApp(this)

        ApiClient.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        KakaoSdk.init(this, resources.getString(R.string.kakao_app_key))

        // 상단 툴바 설정
        setSupportActionBar(binding.toolbar)

        // 뒤로 가기 버튼 활성화
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        supportActionBar?.setHomeAsUpIndicator(drawable)

        // 하단 네비게이션바 설정
        navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
        binding.bottomNavigationView.setupWithNavController(navController)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val sharedPreferencesManager = SharedPreferencesManager(this)
        val isLoggedIn = sharedPreferencesManager.getIsLogin()

        Log.i("메인 액티비티: 로그인 상태", isLoggedIn.toString())

        if (!isLoggedIn) {
            navController.navigate(R.id.signInFragment)
            hideBottomNavigation() // 로그인 페이지에서는 네비게이션 바 숨기기
            hideToolbar() // 필요에 따라 툴바도 숨기기
        }

        // FCM 페이지 이동 설정
        handleIntent(intent)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            handleIntent(it)
        }
    }

    // 알림 인텐트를 처리하고, NavController로 Fragment 전환
    private fun handleIntent(intent: Intent) {
        val targetFragment = intent.getStringExtra("Fragment")

        if (targetFragment != null) {
            when (targetFragment) {
                "loungeHomeFragment" -> {
                    val loungeId = intent.getLongExtra("loungeId", -1)
                    if (loungeId != -1L) {
                        val bundle = Bundle().apply { putLong("loungeId", loungeId) }
                        navController.navigate(R.id.action_global_loungeHomeFragment, bundle)
                    }
                }
                "liveFragment" -> {
                    navController.navigate(R.id.action_global_myLiveLesson)
                }
            }
        }
    }

    // 툴바 제목 설정
    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    // 툴바 숨기기
    fun hideToolbar() {
        binding.toolbar.visibility = View.GONE
    }

    // 툴바 보이기
    fun showToolbar() {
        binding.toolbar.visibility = View.VISIBLE
    }

    fun showBottomNavigation() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    // 툴바 메뉴 보이기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_toolbar, menu)
        return true
    }

    // 툴바 뒤로가기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 툴바 메뉴
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    // 네비게이션 바 숨기기
    fun hideNavigationBar() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    // 네비게이션 바 보이기
    fun showNavigationBar() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }
}