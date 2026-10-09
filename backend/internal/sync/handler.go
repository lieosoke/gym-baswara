package sync

import (
	"context"
	"net/http"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

type WorkoutExerciseDto struct {
	ID         string          `json:"id" bson:"_id"`
	ExerciseID string          `json:"exercise_id" bson:"exercise_id"`
	Notes      string          `json:"notes" bson:"notes"`
	Sets       []WorkoutSetDto `json:"sets" bson:"sets"`
}

type WorkoutSetDto struct {
	ID          string  `json:"id" bson:"_id"`
	SetNumber   int     `json:"set_number" bson:"set_number"`
	Weight      float64 `json:"weight" bson:"weight"`
	Reps        int     `json:"reps" bson:"reps"`
	IsCompleted bool    `json:"is_completed" bson:"is_completed"`
}

type WorkoutDto struct {
	ID              string               `json:"id" bson:"_id"`
	Name            string               `json:"name" bson:"name"`
	StartedAt       int64                `json:"started_at" bson:"started_at"`
	CompletedAt     *int64               `json:"completed_at" bson:"completed_at"`
	DurationSeconds int                  `json:"duration_seconds" bson:"duration_seconds"`
	TotalVolume     float64              `json:"total_volume" bson:"total_volume"`
	Status          string               `json:"status" bson:"status"`
	Exercises       []WorkoutExerciseDto `json:"exercises" bson:"exercises"`
}

type SyncRequest struct {
	Workouts []WorkoutDto `json:"workouts"`
}

type Handler struct {
	db *mongo.Database
}

func RegisterHandlers(r *gin.RouterGroup, db *mongo.Database) {
	h := &Handler{db: db}
	group := r.Group("/sync")
	{
		group.POST("/push", h.PushData)
	}
}

func (h *Handler) PushData(c *gin.Context) {
	var req SyncRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"data": gin.H{"success": false}, "meta": gin.H{"error": err.Error()}})
		return
	}

	if h.db != nil {
		collection := h.db.Collection("workouts")
		
		for _, w := range req.Workouts {
			filter := bson.M{"_id": w.ID}
			update := bson.M{"$set": w}
			opts := options.UpdateOne().SetUpsert(true)
			
			_, err := collection.UpdateOne(context.Background(), filter, update, opts)
			if err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{"data": gin.H{"success": false}, "meta": gin.H{"error": "Failed to sync workout"}})
				return
			}
		}
	}

	c.JSON(http.StatusOK, gin.H{"data": gin.H{"success": true}, "meta": gin.H{}})
}
