Version: 1.1.2
versionCode: 4

Previous Version: 1.1.1
New Version: 1.1.2

Changes:
- HOTFIX: Fixed a critical database cascade deletion bug that caused custom routine exercises to disappear upon app restart.

Bug Fixes:
- Fixed `OnConflictStrategy.REPLACE` in `ExerciseDao` which triggered `ON DELETE CASCADE` on `RoutineExerciseCrossRef`.

Database Changes:
- Validasi Room schema version 5
- No structural changes in this release.

Migration:
- Tested existing migrations (v3 -> v4, v4 -> v5, v3 -> v5).

Data Safety:
- Routine data is now 100% persistent across app restarts and updates.
