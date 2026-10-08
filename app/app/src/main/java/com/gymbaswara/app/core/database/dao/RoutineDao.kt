package com.gymbaswara.app.core.database.dao

import androidx.room.*
import com.gymbaswara.app.core.database.entity.RoutineEntity
import com.gymbaswara.app.core.database.entity.RoutineExerciseCrossRef
import com.gymbaswara.app.core.database.entity.RoutineWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(crossRefs: List<RoutineExerciseCrossRef>)

    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineWithExercises>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId")
    fun getRoutineById(routineId: String): Flow<RoutineWithExercises?>

    @Query("SELECT * FROM routine_exercise_cross_ref WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getCrossRefsByRoutineId(routineId: String): Flow<List<RoutineExerciseCrossRef>>

    @Query("SELECT * FROM routine_exercise_cross_ref ORDER BY orderIndex ASC")
    fun getAllCrossRefs(): Flow<List<RoutineExerciseCrossRef>>

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: String)

    @Query("DELETE FROM routine_exercise_cross_ref WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: String)

    @Transaction
    suspend fun insertRoutineWithExercises(routine: RoutineEntity, exercises: List<RoutineExerciseCrossRef>) {
        insertRoutine(routine)
        deleteRoutineExercises(routine.id) // Clear old exercises if editing
        insertRoutineExercises(exercises)
    }
}
