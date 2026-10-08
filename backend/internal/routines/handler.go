package routines

import (
	"net/http"
	"github.com/gin-gonic/gin"
)

func RegisterHandlers(r *gin.RouterGroup) {
	group := r.Group("/routines")
	{
		group.GET("", GetRoutines)
		group.GET("/:id", GetRoutine)
		group.POST("", CreateRoutine)
		group.PUT("/:id", UpdateRoutine)
		group.DELETE("/:id", DeleteRoutine)
	}
}

func GetRoutines(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"data": []string{}, "meta": gin.H{}})
}

func GetRoutine(c *gin.Context) {
	id := c.Param("id")
	c.JSON(http.StatusOK, gin.H{"data": gin.H{"id": id}, "meta": gin.H{}})
}

func CreateRoutine(c *gin.Context) {
	c.JSON(http.StatusCreated, gin.H{"data": gin.H{}, "meta": gin.H{}})
}

func UpdateRoutine(c *gin.Context) {
	id := c.Param("id")
	c.JSON(http.StatusOK, gin.H{"data": gin.H{"id": id}, "meta": gin.H{}})
}

func DeleteRoutine(c *gin.Context) {
	c.JSON(http.StatusNoContent, nil)
}
