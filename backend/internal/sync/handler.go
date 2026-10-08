package sync

import (
	"net/http"
	"github.com/gin-gonic/gin"
)

func RegisterHandlers(r *gin.RouterGroup) {
	group := r.Group("/sync")
	{
		group.GET("/pull", PullData)
		group.POST("/push", PushData)
	}
}

func PullData(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"data": gin.H{}, "meta": gin.H{}})
}

func PushData(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"data": gin.H{}, "meta": gin.H{}})
}
