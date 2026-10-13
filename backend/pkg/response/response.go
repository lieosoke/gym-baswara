package response

import "github.com/gin-gonic/gin"

// Envelope keeps every endpoint on the same shape the Android client already
// expects: a "data" payload plus a "meta" object that carries errors.
func OK(c *gin.Context, data any) {
	c.JSON(200, gin.H{"data": data, "meta": gin.H{}})
}

func Created(c *gin.Context, data any) {
	c.JSON(201, gin.H{"data": data, "meta": gin.H{}})
}

func NoContent(c *gin.Context) {
	c.Status(204)
}

func Error(c *gin.Context, status int, message string) {
	c.JSON(status, gin.H{"data": gin.H{"success": false}, "meta": gin.H{"error": message}})
}
