package com.innerpeace.themoonha.data.network

import android.content.Context
import com.innerpeace.themoonha.Constants.url
import com.innerpeace.themoonha.SharedPreferencesManager
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.CookieManager

object ApiClient {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    fun getClient(): Retrofit = createRetrofit()

    fun init(context: Context) {
        sharedPreferencesManager = SharedPreferencesManager(context)
    }

    private fun createClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(CookieManager()))
            .addInterceptor(CookieInterceptor(sharedPreferencesManager))
            .addInterceptor(RequestInterceptor(sharedPreferencesManager))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(createClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

class CookieInterceptor(private val sharedPreferencesManager: SharedPreferencesManager) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val cookies = response.headers("Set-Cookie")
        if (cookies.isNotEmpty()) {
            var accessToken: String? = null
            var refreshToken: String? = null

            for (cookie in cookies) {
                val parsedCookie = cookie.split(";").map { it.trim() }

                for (keyValue in parsedCookie) {
                    when {
                        keyValue.startsWith("accessToken=") -> accessToken = keyValue.substringAfter("accessToken=")
                        keyValue.startsWith("refreshToken=") -> refreshToken =
                            keyValue.substringAfter("refreshToken=")
                    }
                }
            }

            if (accessToken != null && refreshToken != null) {
                sharedPreferencesManager.setLoginToken(accessToken, refreshToken)
            }
        }

        return response
    }
}

class RequestInterceptor(private val sharedPreferencesManager: SharedPreferencesManager) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        val (accessToken, refreshToken) = sharedPreferencesManager.getLoginToken()

        if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Cookie", "accessToken=$accessToken; refreshToken=$refreshToken")
        } else {
            sharedPreferencesManager.clear()
            sharedPreferencesManager.setIsLogin(false)
        }

        return chain.proceed(requestBuilder.build())
    }
}
