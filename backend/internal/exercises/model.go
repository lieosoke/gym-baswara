package exercises

type Exercise struct {
	ID            string `bson:"_id" json:"id"`
	Name          string `bson:"name" json:"name"`
	Description   string `bson:"description,omitempty" json:"description,omitempty"`
	Equipment     string `bson:"equipment,omitempty" json:"equipment,omitempty"`
	MovementType  string `bson:"movement_type,omitempty" json:"movement_type,omitempty"`
	Difficulty    string `bson:"difficulty,omitempty" json:"difficulty,omitempty"`
	PrimaryMuscle string `bson:"primary_muscle,omitempty" json:"primary_muscle,omitempty"`
	Instructions  string `bson:"instructions,omitempty" json:"instructions,omitempty"`
	ImageURL      string `bson:"image_url,omitempty" json:"image_url,omitempty"`
	IsSystem      bool   `bson:"is_system" json:"is_system"`
	UserID        string `bson:"user_id,omitempty" json:"user_id,omitempty"`
	CreatedAt     int64  `bson:"created_at" json:"created_at"`
	UpdatedAt     int64  `bson:"updated_at" json:"updated_at"`
}
