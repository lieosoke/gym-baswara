package workouts

import (
	"net/http"
	"github.com/gin-gonic/gin"
)

func RegisterHandlers(r *gin.RouterGroup) {
	group := r.Group("/workouts")
	{
		group.GET("", GetWorkouts)
		group.GET("/:id", GetWorkout)
		group.POST("", CreateWorkout)
		group.PUT("/:id", UpdateWorkout)
		group.DELETE("/:id", DeleteWorkout)
	}
}

func GetWorkouts(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"data": []string{}, "meta": gin.H{}})
}

func GetWorkout(c *gin.Context) {
	id := c.Param("id")
	c.JSON(http.StatusOK, gin.H{"data": gin.H{"id": id}, "meta": gin.H{}})
}

func CreateWorkout(c *gin.Context) {
	c.JSON(http.StatusCreated, gin.H{"data": gin.H{}, "meta": gin.H{}})
}

func UpdateWorkout(c *gin.Context) {
	id := c.Param("id")
	c.JSON(http.StatusOK, gin.H{"data": gin.H{"id": id}, "meta": gin.H{}})
}

func DeleteWorkout(c *gin.Context) {
	c.JSON(http.StatusNoContent, nil)
}
