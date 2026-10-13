package exercises

import (
	"errors"
	"net/http"
	"regexp"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gymbaswara/backend/middleware"
	"github.com/gymbaswara/backend/pkg/response"
	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

type Handler struct {
	coll *mongo.Collection
}

func RegisterHandlers(r *gin.RouterGroup, db *mongo.Database, secret string) {
	h := &Handler{coll: db.Collection("exercises")}
	auth := middleware.RequireAuth(secret)

	group := r.Group("/exercises")
	{
		group.GET("", h.List)
		group.GET("/:id", h.Get)
		group.POST("", auth, h.Create)
		group.PUT("/:id", auth, h.Update)
		group.DELETE("/:id", auth, h.Delete)
	}
}

func (h *Handler) List(c *gin.Context) {
	filter := bson.M{}

	if q := c.Query("q"); q != "" {
		filter["name"] = bson.M{"$regex": regexp.QuoteMeta(q), "$options": "i"}
	}
	if muscle := c.Query("muscle"); muscle != "" {
		filter["primary_muscle"] = muscle
	}

	findOpts := options.Find().SetSort(bson.D{{Key: "name", Value: 1}})
	if raw := c.Query("limit"); raw != "" {
		if n, err := strconv.ParseInt(raw, 10, 64); err == nil && n > 0 {
			findOpts.SetLimit(n)
		}
	}

	cursor, err := h.coll.Find(c.Request.Context(), filter, findOpts)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to fetch exercises")
		return
	}
	defer cursor.Close(c.Request.Context())

	items := make([]Exercise, 0)
	if err := cursor.All(c.Request.Context(), &items); err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to decode exercises")
		return
	}

	response.OK(c, items)
}

func (h *Handler) Get(c *gin.Context) {
	var item Exercise
	err := h.coll.FindOne(c.Request.Context(), bson.M{"_id": c.Param("id")}).Decode(&item)
	if errors.Is(err, mongo.ErrNoDocuments) {
		response.Error(c, http.StatusNotFound, "exercise not found")
		return
	}
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to fetch exercise")
		return
	}
	response.OK(c, item)
}

func (h *Handler) Create(c *gin.Context) {
	var item Exercise
	if err := c.ShouldBindJSON(&item); err != nil {
		response.Error(c, http.StatusBadRequest, "invalid request body")
		return
	}
	if item.Name == "" {
		response.Error(c, http.StatusBadRequest, "name is required")
		return
	}

	now := time.Now().UnixMilli()
	item.ID = bson.NewObjectID().Hex()
	item.IsSystem = false
	item.UserID = middleware.UserID(c)
	item.CreatedAt = now
	item.UpdatedAt = now

	if _, err := h.coll.InsertOne(c.Request.Context(), item); err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to create exercise")
		return
	}
	response.Created(c, item)
}

func (h *Handler) Update(c *gin.Context) {
	var item Exercise
	if err := c.ShouldBindJSON(&item); err != nil {
		response.Error(c, http.StatusBadRequest, "invalid request body")
		return
	}

	update := bson.M{
		"name":          item.Name,
		"description":   item.Description,
		"equipment":     item.Equipment,
		"movement_type": item.MovementType,
		"difficulty":    item.Difficulty,
		"primary_muscle": item.PrimaryMuscle,
		"instructions":  item.Instructions,
		"image_url":     item.ImageURL,
		"updated_at":    time.Now().UnixMilli(),
	}

	var updated Exercise
	err := h.coll.FindOneAndUpdate(
		c.Request.Context(),
		bson.M{"_id": c.Param("id")},
		bson.M{"$set": update},
		options.FindOneAndUpdate().SetReturnDocument(options.After),
	).Decode(&updated)
	if errors.Is(err, mongo.ErrNoDocuments) {
		response.Error(c, http.StatusNotFound, "exercise not found")
		return
	}
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to update exercise")
		return
	}
	response.OK(c, updated)
}

func (h *Handler) Delete(c *gin.Context) {
	res, err := h.coll.DeleteOne(c.Request.Context(), bson.M{"_id": c.Param("id")})
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to delete exercise")
		return
	}
	if res.DeletedCount == 0 {
		response.Error(c, http.StatusNotFound, "exercise not found")
		return
	}
	response.NoContent(c)
}
