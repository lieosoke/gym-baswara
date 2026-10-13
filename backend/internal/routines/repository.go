package routines

import (
	"context"
	"errors"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

var ErrNotFound = errors.New("routine not found")

type Repository struct {
	coll *mongo.Collection
}

func NewRepository(db *mongo.Database) *Repository {
	return &Repository{coll: db.Collection("routines")}
}

// Upsert stores a routine keyed by its client id. created_at is written only on
// insert so edits keep the original creation time; the id lives in the filter
// only because Mongo rejects updating _id through $set.
func (r *Repository) Upsert(ctx context.Context, item *Routine) error {
	now := time.Now().UnixMilli()
	createdAt := item.CreatedAt
	if createdAt == 0 {
		createdAt = now
	}
	item.CreatedAt = createdAt
	item.UpdatedAt = now

	update := bson.M{
		"$set": bson.M{
			"user_id":     item.UserID,
			"name":        item.Name,
			"description": item.Description,
			"exercises":   item.Exercises,
			"updated_at":  item.UpdatedAt,
		},
		"$setOnInsert": bson.M{"created_at": createdAt},
	}

	_, err := r.coll.UpdateOne(
		ctx,
		bson.M{"_id": item.ID},
		update,
		options.UpdateOne().SetUpsert(true),
	)
	return err
}

func (r *Repository) ListByUser(ctx context.Context, userID string, since int64) ([]Routine, error) {
	filter := bson.M{"user_id": userID}
	if since > 0 {
		filter["updated_at"] = bson.M{"$gt": since}
	}

	cursor, err := r.coll.Find(ctx, filter, options.Find().SetSort(bson.D{{Key: "created_at", Value: -1}}))
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	items := make([]Routine, 0)
	if err := cursor.All(ctx, &items); err != nil {
		return nil, err
	}
	return items, nil
}

func (r *Repository) FindByID(ctx context.Context, userID, id string) (*Routine, error) {
	var item Routine
	err := r.coll.FindOne(ctx, bson.M{"_id": id, "user_id": userID}).Decode(&item)
	if errors.Is(err, mongo.ErrNoDocuments) {
		return nil, ErrNotFound
	}
	if err != nil {
		return nil, err
	}
	return &item, nil
}

func (r *Repository) Delete(ctx context.Context, userID, id string) (bool, error) {
	res, err := r.coll.DeleteOne(ctx, bson.M{"_id": id, "user_id": userID})
	if err != nil {
		return false, err
	}
	return res.DeletedCount > 0, nil
}
