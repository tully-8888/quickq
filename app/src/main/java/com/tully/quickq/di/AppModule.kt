package com.tully.quickq.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tully.quickq.data.api.QuickQApiService
import com.tully.quickq.data.constants.Constants
import com.tully.quickq.data.local.QuickQDatabase
import com.tully.quickq.data.repository.InterviewRepositoryImpl
import com.tully.quickq.data.repository.JobRepositoryImpl
import com.tully.quickq.data.repository.UserRepositoryImpl
import com.tully.quickq.domain.repository.InterviewRepository
import com.tully.quickq.domain.repository.JobRepository
import com.tully.quickq.domain.repository.UserRepository
import com.tully.quickq.presentation.viewmodel.AppViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    
    // Network
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    single {
        GsonBuilder()
            .setLenient()
            .create()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl(Constants.QUICKQ_BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .build()
    }
    
    single {
        get<Retrofit>().create(QuickQApiService::class.java)
    }
    
    // SharedPreferences
    single { 
        get<Context>().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE) 
    }
    
    // Room Database
    single {
        Room.databaseBuilder(
            get<Context>(),
            QuickQDatabase::class.java,
            "quickq_database"
        ).build()
    }
    
    // DAOs
    single { get<QuickQDatabase>().jobDao() }
    
    // Repositories
    single<JobRepository> {
        JobRepositoryImpl(
            quickQApi = get(),
            gson = get(),
            jobDao = get()
        )
    }
    
    single<InterviewRepository> {
        InterviewRepositoryImpl(
            quickQApi = get(),
            jobRepository = get(),
            sharedPreferences = get(),
            gson = get()
        )
    }
    
    single<UserRepository> { 
        UserRepositoryImpl(get()) 
    }
    
    // ViewModels
    viewModel { AppViewModel(get(), get(), get()) }
} 