package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

const UserIDKey = "userID"

// RequireAuth validates the "Authorization: Bearer <token>" header and stores
// the token subject (user id) in the request context.
func RequireAuth(secret string) gin.HandlerFunc {
	return func(c *gin.Context) {
		header := c.GetHeader("Authorization")
		if header == "" || !strings.HasPrefix(header, "Bearer ") {
			abortUnauthorized(c, "missing or invalid authorization header")
			return
		}

		tokenString := strings.TrimSpace(strings.TrimPrefix(header, "Bearer "))
		token, err := jwt.Parse(tokenString, func(t *jwt.Token) (any, error) {
			if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, jwt.ErrSignatureInvalid
			}
			return []byte(secret), nil
		})
		if err != nil || !token.Valid {
			abortUnauthorized(c, "invalid or expired token")
			return
		}

		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok {
			abortUnauthorized(c, "invalid token claims")
			return
		}

		sub, _ := claims["sub"].(string)
		if sub == "" {
			abortUnauthorized(c, "token missing subject")
			return
		}

		c.Set(UserIDKey, sub)
		c.Next()
	}
}

// UserID returns the authenticated user id stored by RequireAuth, or "".
func UserID(c *gin.Context) string {
	if v, ok := c.Get(UserIDKey); ok {
		if s, ok := v.(string); ok {
			return s
		}
	}
	return ""
}

func abortUnauthorized(c *gin.Context, message string) {
	c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{
		"data": gin.H{"success": false},
		"meta": gin.H{"error": message},
	})
}
