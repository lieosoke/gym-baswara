package auth

import (
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gymbaswara/backend/internal/users"
	"github.com/gymbaswara/backend/middleware"
	"github.com/gymbaswara/backend/pkg/response"
	"golang.org/x/crypto/bcrypt"
)

type Handler struct {
	repo   *users.Repository
	secret string
	ttl    time.Duration
}

func RegisterHandlers(r *gin.RouterGroup, repo *users.Repository, secret string, ttl time.Duration) {
	h := &Handler{repo: repo, secret: secret, ttl: ttl}
	group := r.Group("/auth")
	{
		group.POST("/register", h.Register)
		group.POST("/login", h.Login)
		group.GET("/me", middleware.RequireAuth(secret), h.Me)
	}
}

type credentialsRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
	Name     string `json:"name"`
}

func (h *Handler) Register(c *gin.Context) {
	var req credentialsRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "invalid request body")
		return
	}

	email := strings.ToLower(strings.TrimSpace(req.Email))
	if email == "" || len(req.Password) < 6 {
		response.Error(c, http.StatusBadRequest, "email is required and password must be at least 6 characters")
		return
	}

	ctx := c.Request.Context()

	exists, err := h.repo.ExistsByEmail(ctx, email)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to check account")
		return
	}
	if exists {
		response.Error(c, http.StatusConflict, "email already registered")
		return
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to secure password")
		return
	}

	u := &users.User{
		Email:        email,
		Name:         strings.TrimSpace(req.Name),
		PasswordHash: string(hash),
	}
	if err := h.repo.Create(ctx, u); err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to create account")
		return
	}

	token, err := GenerateToken(h.secret, u.ID, h.ttl)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to issue token")
		return
	}

	response.Created(c, gin.H{"token": token, "user": u})
}

func (h *Handler) Login(c *gin.Context) {
	var req credentialsRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		response.Error(c, http.StatusBadRequest, "invalid request body")
		return
	}

	email := strings.ToLower(strings.TrimSpace(req.Email))
	if email == "" || req.Password == "" {
		response.Error(c, http.StatusBadRequest, "email and password are required")
		return
	}

	ctx := c.Request.Context()

	u, err := h.repo.FindByEmail(ctx, email)
	if err != nil {
		response.Error(c, http.StatusUnauthorized, "invalid email or password")
		return
	}

	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(req.Password)); err != nil {
		response.Error(c, http.StatusUnauthorized, "invalid email or password")
		return
	}

	token, err := GenerateToken(h.secret, u.ID, h.ttl)
	if err != nil {
		response.Error(c, http.StatusInternalServerError, "failed to issue token")
		return
	}

	response.OK(c, gin.H{"token": token, "user": u})
}

func (h *Handler) Me(c *gin.Context) {
	u, err := h.repo.FindByID(c.Request.Context(), middleware.UserID(c))
	if err != nil {
		response.Error(c, http.StatusNotFound, "account not found")
		return
	}
	response.OK(c, gin.H{"user": u})
}
