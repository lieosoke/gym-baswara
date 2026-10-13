package database

import (
	"context"
	"log"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

func EnsureIndexes(ctx context.Context, db *mongo.Database) error {
	specs := map[string][]mongo.IndexModel{
		"users": {
			{Keys: bson.D{{Key: "email", Value: 1}}, Options: options.Index().SetUnique(true)},
		},
		"workouts": {
			{Keys: bson.D{{Key: "user_id", Value: 1}, {Key: "started_at", Value: -1}}},
			{Keys: bson.D{{Key: "user_id", Value: 1}, {Key: "updated_at", Value: 1}}},
		},
		"routines": {
			{Keys: bson.D{{Key: "user_id", Value: 1}, {Key: "created_at", Value: -1}}},
			{Keys: bson.D{{Key: "user_id", Value: 1}, {Key: "updated_at", Value: 1}}},
		},
		"body_metrics": {
			{Keys: bson.D{{Key: "user_id", Value: 1}, {Key: "recorded_at", Value: -1}}},
			{Keys: bson.D{{Key: "user_id", Value: 1}, {Key: "updated_at", Value: 1}}},
		},
		"exercises": {
			{Keys: bson.D{{Key: "name", Value: 1}}},
			{Keys: bson.D{{Key: "user_id", Value: 1}}},
		},
	}

	for collection, models := range specs {
		if _, err := db.Collection(collection).Indexes().CreateMany(ctx, models); err != nil {
			return err
		}
		log.Printf("indexes ensured for collection %q", collection)
	}
	return nil
}
