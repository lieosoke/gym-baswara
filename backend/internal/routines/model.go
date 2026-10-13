package routines

type RoutineExercise struct {
	ID         string `bson:"id" json:"id"`
	ExerciseID string `bson:"exercise_id" json:"exercise_id"`
	OrderIndex int    `bson:"order_index" json:"order_index"`
	Notes      string `bson:"notes,omitempty" json:"notes,omitempty"`
}

type Routine struct {
	ID          string            `bson:"_id" json:"id"`
	UserID      string            `bson:"user_id" json:"user_id"`
	Name        string            `bson:"name" json:"name"`
	Description string            `bson:"description,omitempty" json:"description,omitempty"`
	Exercises   []RoutineExercise `bson:"exercises" json:"exercises"`
	CreatedAt   int64             `bson:"created_at" json:"created_at"`
	UpdatedAt   int64             `bson:"updated_at" json:"updated_at"`
}
