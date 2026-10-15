package com.gymbaswara.app.data.di

import com.gymbaswara.app.data.repository.ExerciseRepositoryImpl
import com.gymbaswara.app.domain.repository.ExerciseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: com.gymbaswara.app.data.repository.WorkoutRepositoryImpl
    ): com.gymbaswara.app.domain.repository.WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        routineRepositoryImpl: com.gymbaswara.app.data.repository.RoutineRepositoryImpl
    ): com.gymbaswara.app.domain.repository.RoutineRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: com.gymbaswara.app.data.repository.AuthRepositoryImpl
    ): com.gymbaswara.app.domain.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindBodyMetricRepository(
        bodyMetricRepositoryImpl: com.gymbaswara.app.data.repository.BodyMetricRepositoryImpl
    ): com.gymbaswara.app.domain.repository.BodyMetricRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        backupRepositoryImpl: com.gymbaswara.app.data.repository.BackupRepositoryImpl
    ): com.gymbaswara.app.domain.repository.BackupRepository
}
