# Migration Notes - Gym Baswara 1.3.1

**Schema Version Changes**: None (No schema version increment)
**Room Database Version**: Tetap versi 10 (`GymBaswaraDatabase.kt`, Room version 10)
**Previous Version**: 1.3.0 (versionCode: 9)
**New Version**: 1.3.1 (versionCode: 10)

## Detail:
Tidak ada perubahan skema database pada pembaruan versi 1.3.1 ini. Perbaikan bug set double dilakukan murni di lapisan logika (ID sesi baru per workout + dedup riwayat di memori), tanpa `DROP TABLE`, tanpa hapus data, dan tanpa migrasi Room baru. Perubahan UI/UX Apple Fitness Volt dari 1.3.0 tetap berlaku.

Karena seluruh entitas (`ExerciseEntity`, `WorkoutEntity`, `WorkoutExerciseEntity`, `WorkoutSetEntity`, `RoutineEntity`, `RoutineExerciseCrossRef`, `BodyMetricEntity`), tabel Room, dan DAO tidak mengalami penambahan kolom atau perubahan tipe data, tidak diperlukan skrip `Migration` Room baru.

## Jaminan Keamanan Data (Safe Update):
1. **Application ID Tetap**: `com.gymbaswara.app` (tidak berubah).
2. **Signing Key Konsisten**: Menggunakan keystore produksi resmi `gymbaswara_production.jks`.
3. **Data User 100% Persisten**: Update ini dapat di-install langsung menimpa (upgrade) aplikasi versi 1.2.3 atau versi sebelumnya tanpa kehilangan satu pun riwayat latihan, catatan PR, data rutinitas, atau ukuran tubuh pengguna.
