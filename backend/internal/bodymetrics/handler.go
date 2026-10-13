package bodymetrics

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/gymbaswara/backend/middleware"
	"github.com/gymbaswara/backend/pkg/response"
	"go.mongodb.org/mongo-driver/v2/bson"
)

type Handler struct {
	repo *Repository
}

func RegisterHandlers(r *gin.RouterGroup, repo *Repository, secret string) {
	h := &Handler{repo: repo}
	auth := middleware.RequireAuth(secret)

	group := r.Group("/body-metrics", auth)
	{
		group.GET("", h.List)
		group.GET("/:id", h.Get)
		group.POST("", h.Create)
		group.PUT("/:id", h.Update)
		group.DELETE("/:id", h.Delete)
	}
}

func (h *Handler) List(c *gin.Context) {
	var since int64
	if raw := c.Query("since"); raw != "" {
		if n, err := strconv.ParseInt(raw, 10, 64); err == nil {
			since = n
		}
	}

	items, err := h.repo.ListByUser(c.Request.Context(), middleware.UserID(c), since)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to fetch body metrics")
		return
	}
	response.OK(c, items)
}

func (h *Handler) Get(c *gin.Context) {
	item, err := h.repo.FindByID(c.Request.Context(), middleware.UserID(c), c.Param("id"))
	if errors.Is(err, ErrNotFound) {
		response.Error(c, http.StatusNotFound, "body metric not found")
		return
	}
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to fetch body metric")
		return
	}
	response.OK(c, item)
}

func (h *Handler) Create(c *gin.Context) {
	var item Metric
	if err := c.ShouldBindJSON(&item); err != nil {
		response.Error(c, http.StatusBadRequest, "invalid request body")
		return
	}

	item.ID = bson.NewObjectID().Hex()
	item.UserID = middleware.UserID(c)
	if err := h.repo.Upsert(c.Request.Context(), &item); err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to create body metric")
		return
	}
	response.Created(c, item)
}

func (h *Handler) Update(c *gin.Context) {
	var item Metric
	if err := c.ShouldBindJSON(&item); err != nil {
		response.Error(c, http.StatusBadRequest, "invalid request body")
		return
	}

	item.ID = c.Param("id")
	item.UserID = middleware.UserID(c)
	if err := h.repo.Upsert(c.Request.Context(), &item); err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to update body metric")
		return
	}
	response.OK(c, item)
}

func (h *Handler) Delete(c *gin.Context) {
	deleted, err := h.repo.Delete(c.Request.Context(), middleware.UserID(c), c.Param("id"))
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to delete body metric")
		return
	}
	if !deleted {
		response.Error(c, http.StatusNotFound, "body metric not found")
		return
	}
	response.NoContent(c)
}
