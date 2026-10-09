package com.gymbaswara.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.gymbaswara.app.core.database.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getExerciseById(id: String): Flow<ExerciseEntity?>

    @Upsert
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("""
        UPDATE workout_exercises 
        SET exerciseId = :targetId 
        WHERE exerciseId IN (SELECT id FROM exercises WHERE name = :name AND id != :targetId)
    """)
    suspend fun remapWorkoutExercises(name: String, targetId: String)

    @Query("""
        UPDATE routine_exercise_cross_ref 
        SET exerciseId = :targetId 
        WHERE exerciseId IN (SELECT id FROM exercises WHERE name = :name AND id != :targetId)
    """)
    suspend fun remapRoutineExercises(name: String, targetId: String)

    @Query("""
        DELETE FROM exercises 
        WHERE name = :name AND id != :targetId
    """)
    suspend fun deleteDuplicateExercises(name: String, targetId: String)

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesOnce(): List<ExerciseEntity>

    @androidx.room.Transaction
    suspend fun deduplicateAllExercises() {
        val allExercises = getAllExercisesOnce()
        val groups = allExercises.groupBy { it.name }
        
        for ((name, exercises) in groups) {
            if (exercises.size > 1) {
                val primary = exercises.find { it.isSystem } ?: exercises.minByOrNull { it.createdAt } ?: exercises.first()
                remapWorkoutExercises(name, primary.id)
                remapRoutineExercises(name, primary.id)
                deleteDuplicateExercises(name, primary.id)
            }
        }
    }
}
