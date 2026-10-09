# Migration Notes v1.2.0

## Database Version
- Old Version: 5
- New Version: 6

## Changes
- `workouts` table: Added column `syncStatus` (TEXT, NOT NULL, DEFAULT 'PENDING')

## Migration Strategy
The `MIGRATION_5_6` object was provided to Room Database builder.
It executes the following SQL:
```sql
ALTER TABLE workouts ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'
```

## Data Safety
- No tables were dropped.
- No destructive migration was configured.
- Existing user workouts are preserved and their `syncStatus` will default to `PENDING`.
