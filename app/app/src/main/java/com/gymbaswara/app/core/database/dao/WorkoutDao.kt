package com.gymbaswara.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gymbaswara.app.core.database.entity.WorkoutEntity
import com.gymbaswara.app.core.database.entity.WorkoutExerciseEntity
import com.gymbaswara.app.core.database.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

data class ActivityStats(
    val sessionCount: Int,
    val totalVolume: Double
)

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSets(sets: List<WorkoutSetEntity>)

    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    fun getWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT COUNT(id) as sessionCount, COALESCE(SUM(totalVolume), 0.0) as totalVolume FROM workouts WHERE startedAt >= :startTime AND status = 'completed'")
    fun getStatsSince(startTime: Long): Flow<ActivityStats>

    @Transaction
    suspend fun insertWorkoutWithDetails(workout: WorkoutEntity, exercises: List<WorkoutExerciseEntity>, sets: List<WorkoutSetEntity>) {
        insertWorkout(workout)
        insertWorkoutExercises(exercises)
        insertWorkoutSets(sets)
    }

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workoutExerciseId = we.id
        INNER JOIN workouts w ON we.workoutId = w.id
        WHERE we.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY w.createdAt DESC, ws.weight DESC, ws.reps DESC
        LIMIT 1
    """)
    suspend fun getLastPerformance(exerciseId: String): WorkoutSetEntity?

    @Query("""
        SELECT ws.* FROM workout_sets ws
        WHERE ws.workoutExerciseId = (
            SELECT we.id FROM workout_exercises we
            INNER JOIN workouts w ON we.workoutId = w.id
            WHERE we.exerciseId = :exerciseId
            ORDER BY w.createdAt DESC
            LIMIT 1
        )
        ORDER BY ws.setNumber ASC
    """)
    suspend fun getLastPerformanceSets(exerciseId: String): List<WorkoutSetEntity>
}
