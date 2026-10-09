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
    version = 8,
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
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_exercises ADD COLUMN notes TEXT")
                db.execSQL("ALTER TABLE routine_exercise_cross_ref ADD COLUMN notes TEXT")
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE `routine_exercise_cross_ref_new` (`id` TEXT NOT NULL, `routineId` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `orderIndex` INTEGER NOT NULL, `notes` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`routineId`) REFERENCES `routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )
                """.trimIndent())
                
                db.execSQL("""
                    INSERT INTO `routine_exercise_cross_ref_new` (`id`, `routineId`, `exerciseId`, `orderIndex`, `notes`) SELECT `routineId` || '_' || `exerciseId`, `routineId`, `exerciseId`, `orderIndex`, `notes` FROM `routine_exercise_cross_ref`
                """.trimIndent())
                
                db.execSQL("DROP TABLE `routine_exercise_cross_ref`")
                
                db.execSQL("ALTER TABLE `routine_exercise_cross_ref_new` RENAME TO `routine_exercise_cross_ref`")
                
                db.execSQL("CREATE INDEX `index_routine_exercise_cross_ref_routineId` ON `routine_exercise_cross_ref` (`routineId`)")
                db.execSQL("CREATE INDEX `index_routine_exercise_cross_ref_exerciseId` ON `routine_exercise_cross_ref` (`exerciseId`)")
            }
        }
    }
}
