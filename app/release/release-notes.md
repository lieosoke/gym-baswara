Version: 1.2.0
versionCode: 5

Previous Version: 1.1.2
New Version: 1.2.0

Changes:
- Added Background Workout Controls: implemented foreground service to keep active workout running in the background.

Bug Fixes:
- N/A

Database Changes:
- Added `syncStatus` column to `workouts` table.
- Database version updated to 6.

Migration:
- Added migration script from v5 to v6.

Data Safety:
- Existing user data preserved.
- Validated offline storage for active workouts.
