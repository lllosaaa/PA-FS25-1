package ch.zhaw.pa_fs25.di

import ch.zhaw.pa_fs25.data.remote.SwissNextGenApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    fun provideSwissNextGenApi(): SwissNextGenApi {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // ✅ Mockoon on Android emulator
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SwissNextGenApi::class.java)

    }
}
