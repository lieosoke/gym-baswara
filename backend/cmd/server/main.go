package main

import (
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/gymbaswara/backend/internal/exercises"
	"github.com/gymbaswara/backend/internal/routines"
	"github.com/gymbaswara/backend/internal/sync"
	"github.com/gymbaswara/backend/internal/workouts"
)

func main() {
	r := gin.Default()

	// Health check endpoint
	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status": "ok",
		})
	})

	apiV1 := r.Group("/api/v1")
	{
		exercises.RegisterHandlers(apiV1)
		routines.RegisterHandlers(apiV1)
		workouts.RegisterHandlers(apiV1)
		sync.RegisterHandlers(apiV1)
	}

	log.Println("Starting Gym Baswara backend server on :8080...")
	if err := r.Run(":8080"); err != nil {
		log.Fatalf("Failed to run server: %v", err)
	}
}
