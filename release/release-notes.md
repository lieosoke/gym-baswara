# Gym Baswara - Release Notes

**Version:** 1.2.3
**versionCode:** 8

**Previous Version:** 1.2.2
**New Version:** 1.2.3

## Changes:
- Layar selalu menyala (Always On Display) selama Active Workout agar sesi latihan tidak terganggu.
- Aplikasi secara otomatis beralih ke Picture-in-Picture (PiP) mode ketika aplikasi di-*minimize* atau ditutup ke *homescreen* saat sedang sesi latihan.
- Fitur Catatan (Notes) untuk tiap set/variasi sekarang dibuat dinamis. Kolom hanya akan tampil jika ada catatan dari *routine*, atau jika pengguna menekan tombol "+ Tambah Catatan".
- Dropdown pilihan latihan pada tab Personal Record sekarang dapat difilter (diketik) (*Searchable Dropdown*).
- Personal Record kini hanya menampilkan daftar latihan yang sudah pernah dikerjakan (*completed workouts*) sehingga list tidak terlalu panjang.

## Bug Fixes:
- Memperbaiki versi aplikasi yang tidak terupdate menjadi `1.2.3`.
- Menghapus teks *placeholder* di fitur catatan yang mengganggu.
- Memperbaiki tampilan grafik perkembangan Personal Record sehingga *Max Weight* untuk setiap titik dapat dibaca langsung dengan jelas.

## Database Changes:
- Menambahkan query baru `getExercisedIds` di `WorkoutDao` untuk mengambil ID latihan yang pernah dimainkan tanpa mengubah struktur database.

## Migration:
- Tidak ada migrasi skema tabel yang dibutuhkan pada versi ini. `versionCode` dan `versionName` sudah dinaikkan.

## Data Safety:
- **Existing user data preserved**: Pembaruan ini aman dan tidak akan mereset atau menghapus data riwayat latihan, PR, ataupun rutinitas pengguna sebelumnya saat di-install menimpa versi terdahulu.
