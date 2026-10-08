package com.gymbaswara.app.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymbaswara.app.core.database.GymBaswaraDatabase
import com.gymbaswara.app.core.database.seeder.ExerciseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        provider: Provider<com.gymbaswara.app.core.database.dao.ExerciseDao>
    ): GymBaswaraDatabase {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }
        val MIGRATION_3_5 = object : androidx.room.migration.Migration(3, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        return Room.databaseBuilder(
            context,
            GymBaswaraDatabase::class.java,
            "gymbaswara.db"
        )
        .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_3_5)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    provider.get().insertExercises(ExerciseSeeder.getInitialExercises(context))
                }
            }
        }).build()
    }

    @Provides
    fun provideExerciseDao(database: GymBaswaraDatabase): com.gymbaswara.app.core.database.dao.ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    fun provideWorkoutDao(database: GymBaswaraDatabase): com.gymbaswara.app.core.database.dao.WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideRoutineDao(database: GymBaswaraDatabase): com.gymbaswara.app.core.database.dao.RoutineDao {
        return database.routineDao()
    }
}
