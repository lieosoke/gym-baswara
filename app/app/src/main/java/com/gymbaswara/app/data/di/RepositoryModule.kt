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
}
