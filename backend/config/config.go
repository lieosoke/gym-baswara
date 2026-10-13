package config

import (
	"os"
	"strconv"
	"strings"
)

type Config struct {
	Port        string
	MongoURI    string
	MongoDBName string
	JWTSecret   string
	JWTTTLHours int
}

func Load() Config {
	loadDotEnv(".env")

	return Config{
		Port:        getEnv("PORT", "8080"),
		MongoURI:    getEnv("MONGODB_URI", "mongodb://localhost:27017"),
		MongoDBName: getEnv("MONGODB_DB", "gymbaswara"),
		JWTSecret:   getEnv("JWT_SECRET", "dev-insecure-secret-change-in-production"),
		JWTTTLHours: getEnvInt("JWT_TTL_HOURS", 720),
	}
}

// loadDotEnv reads a local .env file (if present) and sets variables only when
// they are not already defined in the environment. Environment variables always
// win, so deployments can override the file. Secrets therefore stay out of git.
func loadDotEnv(path string) {
	data, err := os.ReadFile(path)
	if err != nil {
		return
	}

	for _, line := range strings.Split(string(data), "\n") {
		line = strings.TrimSpace(line)
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}

		key, value, ok := strings.Cut(line, "=")
		if !ok {
			continue
		}

		key = strings.TrimSpace(key)
		value = strings.Trim(strings.TrimSpace(value), `"'`)
		if key == "" {
			continue
		}

		if _, exists := os.LookupEnv(key); !exists {
			_ = os.Setenv(key, value)
		}
	}
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func getEnvInt(key string, fallback int) int {
	if v := os.Getenv(key); v != "" {
		if n, err := strconv.Atoi(v); err == nil {
			return n
		}
	}
	return fallback
}
