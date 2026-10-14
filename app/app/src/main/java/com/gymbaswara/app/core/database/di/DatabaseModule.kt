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

    private const val DB_META_PREFS = "gymbaswara_db_meta"
    private const val KEY_EXERCISE_SEED_VERSION = "exercise_seed_version"

    // Bump this whenever assets/exercises.json changes so existing installs pick up
    // the new exercises exactly once instead of re-seeding on every database open.
    private const val EXERCISE_SEED_VERSION = 1

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

        val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val seedPrefs = context.getSharedPreferences(DB_META_PREFS, Context.MODE_PRIVATE)
        val seeding = java.util.concurrent.atomic.AtomicBoolean(false)

        // Seeding and de-duplication are idempotent, but they are I/O and write heavy.
        // Run them only when the seed version changes (first install or exercise update),
        // never on every database open.
        fun ensureExercisesSeeded() {
            if (seedPrefs.getInt(KEY_EXERCISE_SEED_VERSION, 0) == EXERCISE_SEED_VERSION) return
            if (!seeding.compareAndSet(false, true)) return
            seedScope.launch {
                try {
                    provider.get().insertExercises(ExerciseSeeder.getInitialExercises(context))
                    provider.get().deduplicateAllExercises()
                    seedPrefs.edit()
                        .putInt(KEY_EXERCISE_SEED_VERSION, EXERCISE_SEED_VERSION)
                        .apply()
                } finally {
                    seeding.set(false)
                }
            }
        }

        return Room.databaseBuilder(
            context,
            GymBaswaraDatabase::class.java,
            "gymbaswara.db"
        )
        .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_3_5, GymBaswaraDatabase.MIGRATION_5_6, GymBaswaraDatabase.MIGRATION_6_7, GymBaswaraDatabase.MIGRATION_7_8, GymBaswaraDatabase.MIGRATION_8_9, GymBaswaraDatabase.MIGRATION_9_10)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                ensureExercisesSeeded()
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                ensureExercisesSeeded()
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

    @Provides
    fun provideBodyMetricDao(database: GymBaswaraDatabase): com.gymbaswara.app.core.database.dao.BodyMetricDao {
        return database.bodyMetricDao()
    }
}
