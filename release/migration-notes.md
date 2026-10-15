# Migration Notes - Gym Baswara 1.3.2

**Schema Version Changes**: None (No schema version increment)
**Room Database Version**: Tetap versi 10 (`GymBaswaraDatabase.kt`, Room version 10)
**Previous Version**: 1.3.1 (versionCode: 10)
**New Version**: 1.3.2 (versionCode: 11)

## Detail:
Tidak ada perubahan skema database pada pembaruan versi 1.3.2 ini. Perbaikan crash backup/Drive dilakukan murni di konfigurasi ProGuard (keep rules DTO JSON, tanpa perubahan kode runtime), tanpa `DROP TABLE`, tanpa hapus data, dan tanpa migrasi Room baru. Perbaikan set double dari 1.3.1 tetap berlaku.

Karena seluruh entitas (`ExerciseEntity`, `WorkoutEntity`, `WorkoutExerciseEntity`, `WorkoutSetEntity`, `RoutineEntity`, `RoutineExerciseCrossRef`, `BodyMetricEntity`), tabel Room, dan DAO tidak mengalami penambahan kolom atau perubahan tipe data, tidak diperlukan skrip `Migration` Room baru.

## Jaminan Keamanan Data (Safe Update):
1. **Application ID Tetap**: `com.gymbaswara.app` (tidak berubah).
2. **Signing Key Konsisten**: Menggunakan keystore produksi resmi `gymbaswara_production.jks`.
3. **Data User 100% Persisten**: Update ini dapat di-install langsung menimpa (upgrade) aplikasi versi 1.2.3 atau versi sebelumnya tanpa kehilangan satu pun riwayat latihan, catatan PR, data rutinitas, atau ukuran tubuh pengguna.
