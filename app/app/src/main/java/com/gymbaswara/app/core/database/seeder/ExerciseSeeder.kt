package com.gymbaswara.app.core.database.seeder

import android.content.Context
import com.gymbaswara.app.core.database.entity.ExerciseEntity
import org.json.JSONArray
import java.util.UUID

object ExerciseSeeder {
    fun getInitialExercises(context: Context): List<ExerciseEntity> {
        val now = System.currentTimeMillis()
        val exercises = mutableListOf<ExerciseEntity>()
        
        try {
            val inputStream = context.assets.open("exercises.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val primaryMuscle = obj.getString("primaryMuscle")
                val equipment = obj.getString("equipment")
                
                exercises.add(
                    ExerciseEntity(
                        id = UUID.nameUUIDFromBytes(name.toByteArray()).toString(),
                        name = name,
                        description = null,
                        equipment = equipment,
                        movementType = "Isotonic",
                        difficulty = "Beginner",
                        primaryMuscle = primaryMuscle,
                        instructions = null,
                        imageUrl = null,
                        isSystem = true,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback if parsing fails
            exercises.add(
                ExerciseEntity(
                    id = UUID.nameUUIDFromBytes("Bench Press".toByteArray()).toString(),
                    name = "Bench Press",
                    description = null,
                    equipment = "Barbell",
                    movementType = "Isotonic",
                    difficulty = "Beginner",
                    primaryMuscle = "Chest",
                    instructions = null,
                    imageUrl = null,
                    isSystem = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        return exercises
    }
}
