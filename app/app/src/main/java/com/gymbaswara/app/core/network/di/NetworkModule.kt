package com.gymbaswara.app.core.network.di

import com.gymbaswara.app.core.network.GymBaswaraApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.gymbaswara.app.BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGymBaswaraApi(retrofit: Retrofit): GymBaswaraApi {
        return retrofit.create(GymBaswaraApi::class.java)
    }
}
