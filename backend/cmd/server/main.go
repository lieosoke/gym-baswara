package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gymbaswara/backend/config"
	"github.com/gymbaswara/backend/database"
	"github.com/gymbaswara/backend/internal/auth"
	"github.com/gymbaswara/backend/internal/bodymetrics"
	"github.com/gymbaswara/backend/internal/exercises"
	"github.com/gymbaswara/backend/internal/routines"
	"github.com/gymbaswara/backend/internal/sync"
	"github.com/gymbaswara/backend/internal/users"
	"github.com/gymbaswara/backend/internal/workouts"
)

func main() {
	cfg := config.Load()
	ctx := context.Background()

	client, err := database.Connect(ctx, cfg.MongoURI)
	if err != nil {
		log.Fatalf("Failed to connect to MongoDB at %s: %v", cfg.MongoURI, err)
	}
	defer func() {
		if err := client.Disconnect(context.Background()); err != nil {
			log.Printf("Failed to disconnect MongoDB: %v", err)
		}
	}()

	db := client.Database(cfg.MongoDBName)
	if err := database.EnsureIndexes(ctx, db); err != nil {
		log.Fatalf("Failed to ensure MongoDB indexes: %v", err)
	}

	userRepo := users.NewRepository(db)
	workoutRepo := workouts.NewRepository(db)
	routineRepo := routines.NewRepository(db)
	metricRepo := bodymetrics.NewRepository(db)

	r := gin.Default()

	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	apiV1 := r.Group("/api/v1")
	{
		auth.RegisterHandlers(apiV1, userRepo, cfg.JWTSecret, time.Duration(cfg.JWTTTLHours)*time.Hour)
		exercises.RegisterHandlers(apiV1, db, cfg.JWTSecret)
		routines.RegisterHandlers(apiV1, routineRepo, cfg.JWTSecret)
		bodymetrics.RegisterHandlers(apiV1, metricRepo, cfg.JWTSecret)
		workouts.RegisterHandlers(apiV1, workoutRepo, cfg.JWTSecret)
		sync.RegisterHandlers(apiV1, workoutRepo, routineRepo, metricRepo, cfg.JWTSecret)
	}

	srv := &http.Server{
		Addr:    ":" + cfg.Port,
		Handler: r,
	}

	go func() {
		log.Printf("Starting Gym Baswara backend server on :%s...", cfg.Port)
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatalf("Failed to run server: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down server...")

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(shutdownCtx); err != nil {
		log.Fatalf("Server forced to shutdown: %v", err)
	}
	log.Println("Server exited cleanly")
}
