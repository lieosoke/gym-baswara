package workouts

import (
	"context"
	"errors"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

var ErrNotFound = errors.New("workout not found")

type Repository struct {
	coll *mongo.Collection
}

func NewRepository(db *mongo.Database) *Repository {
	return &Repository{coll: db.Collection("workouts")}
}

// Upsert stores a workout keyed by its client-generated id. It never writes
// _id through $set (Mongo rejects that), the id lives only in the filter.
func (r *Repository) Upsert(ctx context.Context, w *Workout) error {
	now := time.Now().UnixMilli()
	if w.CreatedAt == 0 {
		w.CreatedAt = now
	}
	w.UpdatedAt = now

	update := bson.M{"$set": bson.M{
		"user_id":          w.UserID,
		"routine_id":       w.RoutineID,
		"name":             w.Name,
		"started_at":       w.StartedAt,
		"completed_at":     w.CompletedAt,
		"duration_seconds": w.DurationSeconds,
		"total_volume":     w.TotalVolume,
		"status":           w.Status,
		"exercises":        w.Exercises,
		"created_at":       w.CreatedAt,
		"updated_at":       w.UpdatedAt,
	}}

	_, err := r.coll.UpdateOne(
		ctx,
		bson.M{"_id": w.ID},
		update,
		options.UpdateOne().SetUpsert(true),
	)
	return err
}

func (r *Repository) ListByUser(ctx context.Context, userID string, since int64) ([]Workout, error) {
	filter := bson.M{"user_id": userID}
	if since > 0 {
		filter["updated_at"] = bson.M{"$gt": since}
	}

	cursor, err := r.coll.Find(ctx, filter, options.Find().SetSort(bson.D{{Key: "started_at", Value: -1}}))
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	items := make([]Workout, 0)
	if err := cursor.All(ctx, &items); err != nil {
		return nil, err
	}
	return items, nil
}

func (r *Repository) FindByID(ctx context.Context, userID, id string) (*Workout, error) {
	var w Workout
	err := r.coll.FindOne(ctx, bson.M{"_id": id, "user_id": userID}).Decode(&w)
	if errors.Is(err, mongo.ErrNoDocuments) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &w, nil
}

func (r *Repository) Delete(ctx context.Context, userID, id string) (bool, error) {
	res, err := r.coll.DeleteOne(ctx, bson.M{"_id": id, "user_id": userID})
	if err != nil {
		return false, err
	}
	return res.DeletedCount > 0, nil
}
