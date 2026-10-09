# Database Migration Notes (v5 -> v6)

1. **Table Modified**: `workouts`
2. **Column Added**: `syncStatus` (TEXT, NOT NULL, DEFAULT 'PENDING')
3. **Migration Logic**:
    ```kotlin
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workouts ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
        }
    }
    ```
4. **Data Safety Assured**: All previous records in `workouts` table are preserved. No destructive migrations.
