package com.gymbaswara.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymbaswara.app.core.database.dao.ExerciseDao
import com.gymbaswara.app.core.database.dao.WorkoutDao
import com.gymbaswara.app.core.database.entity.ExerciseEntity
import com.gymbaswara.app.core.database.entity.WorkoutEntity
import com.gymbaswara.app.core.database.entity.WorkoutExerciseEntity
import com.gymbaswara.app.core.database.entity.WorkoutSetEntity
import com.gymbaswara.app.core.database.entity.RoutineEntity
import com.gymbaswara.app.core.database.entity.RoutineExerciseCrossRef

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        RoutineEntity::class,
        RoutineExerciseCrossRef::class
    ],
    version = 6,
    exportSchema = false
)
abstract class GymBaswaraDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun routineDao(): com.gymbaswara.app.core.database.dao.RoutineDao
    
    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workouts ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
            }
        }
    }
}
