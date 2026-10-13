package users

// User is the account document stored in the "users" collection.
type User struct {
	ID           string `bson:"_id" json:"id"`
	Email        string `bson:"email" json:"email"`
	Name         string `bson:"name" json:"name"`
	PasswordHash string `bson:"password_hash" json:"-"`
	CreatedAt    int64  `bson:"created_at" json:"created_at"`
	UpdatedAt    int64  `bson:"updated_at" json:"updated_at"`
}
