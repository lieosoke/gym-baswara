package sync

import (
	"context"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gymbaswara/backend/internal/bodymetrics"
	"github.com/gymbaswara/backend/internal/routines"
	"github.com/gymbaswara/backend/internal/workouts"
	"github.com/gymbaswara/backend/middleware"
	"github.com/gymbaswara/backend/pkg/response"
)

const syncTimeout = 10 * time.Second

type PushRequest struct {
	Workouts           []workouts.Workout   `json:"workouts"`
	Routines           []routines.Routine   `json:"routines"`
	BodyMetrics        []bodymetrics.Metric `json:"body_metrics"`
	DeletedRoutines    []string             `json:"deleted_routines"`
	DeletedBodyMetrics []string             `json:"deleted_body_metrics"`
}

type Handler struct {
	workoutRepo *workouts.Repository
	routineRepo *routines.Repository
	metricRepo  *bodymetrics.Repository
}

func RegisterHandlers(
	r *gin.RouterGroup,
	workoutRepo *workouts.Repository,
	routineRepo *routines.Repository,
	metricRepo *bodymetrics.Repository,
	secret string,
) {
	h := &Handler{
		workoutRepo: workoutRepo,
		routineRepo: routineRepo,
		metricRepo:  metricRepo,
	}
	group := r.Group("/sync", middleware.RequireAuth(secret))
	{
		group.POST("/push", h.Push)
		group.GET("/pull", h.Pull)
	}
}

func (h *Handler) Push(c *gin.Context) {
	var req PushRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "invalid request body")
		return
	}

	userID := middleware.UserID(c)
	ctx, cancel := context.WithTimeout(c.Request.Context(), syncTimeout)
	defer cancel()

	for i := range req.Workouts {
		w := &req.Workouts[i]
		if w.ID == "" {
			continue
		}
		w.UserID = userID
		for j := range w.Exercises {
			if w.Exercises[j].OrderIndex == 0 {
				w.Exercises[j].OrderIndex = j
			}
		}
		if err := h.workoutRepo.Upsert(ctx, w); err != nil {
			response.Error(c, http.StatusInternalServerError, "failed to sync workout")
			return
		}
	}

	for i := range req.Routines {
		r := &req.Routines[i]
		if r.ID == "" {
			continue
		}
		r.UserID = userID
		for j := range r.Exercises {
			r.Exercises[j].OrderIndex = j
		}
		if err := h.routineRepo.Upsert(ctx, r); err != nil {
			response.Error(c, http.StatusInternalServerError, "failed to sync routine")
			return
		}
	}

	for i := range req.BodyMetrics {
		m := &req.BodyMetrics[i]
		if m.ID == "" {
			continue
		}
		m.UserID = userID
		if err := h.metricRepo.Upsert(ctx, m); err != nil {
			response.Error(c, http.StatusInternalServerError, "failed to sync body metric")
			return
		}
	}

	for _, id := range req.DeletedRoutines {
		if id == "" {
			continue
		}
		if _, err := h.routineRepo.Delete(ctx, userID, id); err != nil {
			response.Error(c, http.StatusInternalServerError, "failed to delete routine")
			return
		}
	}

	for _, id := range req.DeletedBodyMetrics {
		if id == "" {
			continue
		}
		if _, err := h.metricRepo.Delete(ctx, userID, id); err != nil {
			response.Error(c, http.StatusInternalServerError, "failed to delete body metric")
			return
		}
	}

	count := len(req.Workouts) + len(req.Routines) + len(req.BodyMetrics) +
		len(req.DeletedRoutines) + len(req.DeletedBodyMetrics)
	response.OK(c, gin.H{"success": true, "count": count})
}

func (h *Handler) Pull(c *gin.Context) {
	var since int64
	if raw := c.Query("since"); raw != "" {
		if n, err := strconv.ParseInt(raw, 10, 64); err == nil {
			since = n
		}
	}

	userID := middleware.UserID(c)
	ctx, cancel := context.WithTimeout(c.Request.Context(), syncTimeout)
	defer cancel()

	workoutItems, err := h.workoutRepo.ListByUser(ctx, userID, since)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to pull workouts")
		return
	}

	routineItems, err := h.routineRepo.ListByUser(ctx, userID, since)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to pull routines")
		return
	}

	metricItems, err := h.metricRepo.ListByUser(ctx, userID, since)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to pull body metrics")
		return
	}

	response.OK(c, gin.H{
		"workouts":     workoutItems,
		"routines":     routineItems,
		"body_metrics": metricItems,
		"server_time":  time.Now().UnixMilli(),
	})
}
