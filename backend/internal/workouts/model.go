package workouts

type WorkoutSet struct {
	ID          string  `bson:"id" json:"id"`
	SetNumber   int     `bson:"set_number" json:"set_number"`
	Weight      float64 `bson:"weight" json:"weight"`
	Reps        int     `bson:"reps" json:"reps"`
	IsCompleted bool    `bson:"is_completed" json:"is_completed"`
}

type WorkoutExercise struct {
	ID         string       `bson:"id" json:"id"`
	ExerciseID string       `bson:"exercise_id" json:"exercise_id"`
	OrderIndex int          `bson:"order_index" json:"order_index"`
	Notes      string       `bson:"notes,omitempty" json:"notes,omitempty"`
	Sets       []WorkoutSet `bson:"sets" json:"sets"`
}

type Workout struct {
	ID              string            `bson:"_id" json:"id"`
	UserID          string            `bson:"user_id" json:"user_id"`
	RoutineID       string            `bson:"routine_id,omitempty" json:"routine_id,omitempty"`
	Name            string            `bson:"name" json:"name"`
	StartedAt       int64             `bson:"started_at" json:"started_at"`
	CompletedAt     *int64            `bson:"completed_at" json:"completed_at"`
	DurationSeconds int               `bson:"duration_seconds" json:"duration_seconds"`
	TotalVolume     float64           `bson:"total_volume" json:"total_volume"`
	Status          string            `bson:"status" json:"status"`
	Exercises       []WorkoutExercise `bson:"exercises" json:"exercises"`
	CreatedAt       int64             `bson:"created_at" json:"created_at"`
	UpdatedAt       int64             `bson:"updated_at" json:"updated_at"`
}
