package exercises

import (
	"net/http"
	"github.com/gin-gonic/gin"
)

func RegisterHandlers(r *gin.RouterGroup) {
	group := r.Group("/exercises")
	{
		group.GET("", GetExercises)
		group.GET("/:id", GetExercise)
		group.POST("", CreateExercise)
		group.PUT("/:id", UpdateExercise)
		group.DELETE("/:id", DeleteExercise)
	}
}

func GetExercises(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"data": []string{}, "meta": gin.H{}})
}

func GetExercise(c *gin.Context) {
	id := c.Param("id")
	c.JSON(http.StatusOK, gin.H{"data": gin.H{"id": id}, "meta": gin.H{}})
}

func CreateExercise(c *gin.Context) {
	c.JSON(http.StatusCreated, gin.H{"data": gin.H{}, "meta": gin.H{}})
}

func UpdateExercise(c *gin.Context) {
	id := c.Param("id")
	c.JSON(http.StatusOK, gin.H{"data": gin.H{"id": id}, "meta": gin.H{}})
}

func DeleteExercise(c *gin.Context) {
	c.JSON(http.StatusNoContent, nil)
}
