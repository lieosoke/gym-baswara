package bodymetrics

type Metric struct {
	ID             string   `bson:"_id" json:"id"`
	UserID         string   `bson:"user_id" json:"user_id"`
	WeightKg       *float64 `bson:"weight_kg,omitempty" json:"weight_kg,omitempty"`
	BodyFatPercent *float64 `bson:"body_fat_percent,omitempty" json:"body_fat_percent,omitempty"`
	ChestCm        *float64 `bson:"chest_cm,omitempty" json:"chest_cm,omitempty"`
	WaistCm        *float64 `bson:"waist_cm,omitempty" json:"waist_cm,omitempty"`
	ArmCm          *float64 `bson:"arm_cm,omitempty" json:"arm_cm,omitempty"`
	ThighCm        *float64 `bson:"thigh_cm,omitempty" json:"thigh_cm,omitempty"`
	Notes          string   `bson:"notes,omitempty" json:"notes,omitempty"`
	RecordedAt     int64    `bson:"recorded_at" json:"recorded_at"`
	CreatedAt      int64    `bson:"created_at" json:"created_at"`
	UpdatedAt      int64    `bson:"updated_at" json:"updated_at"`
}
