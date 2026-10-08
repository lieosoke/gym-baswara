# Migration Notes

Version: 1.1.1
Database Version: 5

## Summary
No database schema changes introduced in this version.
Current database version remains at v5.
All existing migrations (v3->v4, v4->v5, v3->v5) have been verified and retained to support upgrades from older versions.

## Expected Behavior
- For users upgrading from v1.1.0 (which was on DB v5), no migration will run.
- For users upgrading from earlier testing builds on DB v3 or v4, the respective migrations will execute safely.
- No destructive fallback is enabled.
