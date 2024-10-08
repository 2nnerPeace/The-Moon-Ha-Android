package com.innerpeace.themoonha.ui.fragment.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.SharedPreferencesManager
import com.innerpeace.themoonha.data.model.auth.LoginRequest
import com.innerpeace.themoonha.data.network.ApiClient
import com.innerpeace.themoonha.data.network.AuthService
import com.innerpeace.themoonha.data.network.AlimService
import com.innerpeace.themoonha.data.repository.AuthRepository
import com.innerpeace.themoonha.data.repository.AlimRepository
import com.innerpeace.themoonha.databinding.FragmentSignInBinding
import kotlinx.coroutines.launch

/**
 * 로그인 페이지 프레그먼트
 *
 * @author 손승완
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.10  	손승완       최초 생성
 * </pre>
 * @since 2024.09.10
 */
class SignInFragment : Fragment() {
    private var mBinding: FragmentSignInBinding? = null
    private val binding get() = mBinding!!
    private val authRepository = AuthRepository(ApiClient.getClient().create(AuthService::class.java))
    private val fcmRepository = AlimRepository(ApiClient.getClient().create(AlimService::class.java))
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSignInBinding.inflate(inflater, container, false)
        sharedPreferencesManager = SharedPreferencesManager(requireContext().applicationContext)
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUIBasedOnLoginState()

        binding.signInButton.setOnClickListener {
            val loginId = binding.inputLoginId.text.toString()
            val password = binding.inputPassword.text.toString()

            if (loginId.isNotEmpty() && password.isNotEmpty()) {
                login(loginId, password)
            } else {
                Toast.makeText(requireContext(), "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signOutButton.setOnClickListener {
            sharedPreferencesManager.clear()
            updateUIBasedOnLoginState() // 로그아웃 후 UI 업데이트
        }
    }

    private fun updateUIBasedOnLoginState() {
        if (sharedPreferencesManager.getIsLogin()) {
            binding.signOutButton.visibility = View.VISIBLE
            binding.signInButton.visibility = View.GONE
            binding.inputLoginId.visibility = View.GONE
            binding.inputPassword.visibility = View.GONE
        } else {
            binding.signOutButton.visibility = View.GONE
            binding.signInButton.visibility = View.VISIBLE
            binding.inputLoginId.visibility = View.VISIBLE
            binding.inputPassword.visibility = View.VISIBLE
        }
    }

    private fun login(loginId: String, password: String) {
        lifecycleScope.launch {
            val loginRequest = LoginRequest(username = loginId, password = password)
            val response = authRepository.fetchLogin(loginRequest)

            if (response != null && response.success) {
                sharedPreferencesManager.setIsLogin(true)
                Log.i("shared : ", sharedPreferencesManager.getIsLogin().toString())
                Log.i("shared : ", sharedPreferencesManager.getLoginToken().toString())
                // 로그인에 성공하면 FCM 토큰 저장
                getFcmToken()
                findNavController().navigate(R.id.action_signInFragment_to_fragment_lesson)
                updateUIBasedOnLoginState() // 로그인 후 UI 업데이트
            } else {
                Toast.makeText(requireContext(), "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // FCM 토큰 가져와서 저장
    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM Token", "FCM 토큰을 가져오지 못했습니다.", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result
            Log.d("FCM Token", "FCM Token: $fcmToken")

            sendTokenToServer(fcmToken)
        }
    }

    private fun sendTokenToServer(token: String) {
        lifecycleScope.launch {
            try {
                val response = fcmRepository.registerFcmToken(token)
                if (response != null) {
                    if (response.success) {
                        Log.d("FCM Token", "FCM 토큰이 서버에 성공적으로 전송되었습니다.")
                    } else {
                        Log.e("FCM Token", "서버로 FCM 토큰 전송 실패")
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM Token", "FCM 토큰 전송 중 오류 발생: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }
}
