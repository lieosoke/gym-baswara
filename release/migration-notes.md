# Migration Notes - Gym Baswara 1.2.3

**Schema Version Changes**: None (No schema version increment)
**Room Database Version**: Tetap sama (2.6.1 config, version tidak berubah di entitas)

## Detail:
Tidak ada perubahan skema database pada update ini. Kami hanya menambahkan metode query baru pada `WorkoutDao` untuk mengambil daftar `exerciseId` dari `workout_exercises` yang memiliki `workoutId` dengan status `completed`.

Karena struktur tabel dan kolom tetap sama, tidak ada skrip `Migration` Room yang diperlukan. Data lama pengguna dijamin utuh (safe update).
