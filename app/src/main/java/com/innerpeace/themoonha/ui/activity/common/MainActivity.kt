package com.innerpeace.themoonha.ui.activity.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.databinding.ActivityMainBinding

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
 * </pre>
 */
class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단 툴바 설정
        setSupportActionBar(binding.toolbar)

        // 뒤로 가기 버튼 활성화
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)

        // 하단 네비게이션바 설정
        val navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
        binding.bottomNavigationView.setupWithNavController(navController)
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
}