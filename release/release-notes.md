# Gym Baswara - Release Notes

**Version:** 1.3.1
**versionCode:** 10

**Previous Version:** 1.3.0 (versionCode: 9)
**New Version:** 1.3.1 (versionCode: 10)

## Changes:
- **Redesain Menyeluruh Apple Sporty UI (Apple Fitness Volt)**: Pembaruan total estetika antarmuka ke gaya Apple Human Interface Guidelines dan Apple Watch/Fitness+ activity styling dengan palet warna OLED Pitch Black (`#000000`), Apple Volt (`#D0FD3E`), Apple Crimson (`#FF375F`), dan Apple System Grays (`#1C1C1E`, `#2C2C2E`, `#8E8E93`).
- **Dashboard Home**: Header salam dinamis, kartu hero squircle 22dp dengan indikator sesi aktif, segmented pill filter, serta kartu metrik ringkasan dengan angka tabular 28sp tebal.
- **Active Workout Engine**: Digital Workout Bar dengan timer tabular, kartu set latihan dengan kolom terstruktur (SET, KG, REPS, STATUS), tombol centang taktil Volt, Live Activity floating rest timer, dan dukungan Picture-in-Picture (PiP).
- **Routines Module**: Daftar rutinitas dan editor rutinitas bergaya Apple Inset dengan target sentuh minimal 48dp x 48dp, pencarian gerakan instan, dan kontrol urutan latihan yang mulus.
- **Progress & Personal Record**: Grafik kurva Bezier bergaya Apple Health dengan gradient Volt dan penanda titik beban maksimal, selector gerakan dengan pencarian, dan badge pill rekor terbaik.
- **Profile & Settings**: Pengelompokan iOS Inset Grouped, pemilih satuan beban (kg/lb) segmented control, dan pengatur waktu istirahat digital.
- **Workout Flow**: Kartu mulai latihan cepat tanpa template (Quick Start), Live Activity banner sesi aktif, pratinjau sesi latihan, dan selebrasi selesai latihan bergaya Apple Fitness+.
- **Exercise Library**: Kolom pencarian inset, pill filter target otot dan peralatan, sticky category header, dan kartu detail panduan eksekusi gerakan.
- **Data & Utility**: Pelacak ukuran tubuh (berat, lemak, lingkar tubuh) ala Apple Health, kartu sinkronisasi Google Drive cloud backup, dan cadangan file lokal 100% offline.
- **Auth & Onboarding**: Desain form inset modern untuk masuk, daftar, dan pengenalan fitur langkah demi langkah dengan dynamic pagination pill.

## Bug Fixes:
- Memperbaiki keterbacaan kontras warna di seluruh aplikasi hingga rasio 14.5:1 (AAA) pada aksen Volt dan 13.8:1 (AAA) pada teks utama.
- Memperbesar seluruh target sentuh interaktif (tombol, ikon panah, menu opsi, hapus) menjadi minimal 48dp x 48dp untuk mencegah salah sentuh saat berolahraga.
- Menghilangkan glitch kedipan recomposition pada daftar latihan menggunakan penanda kunci stabil (stable keys) dan memoization.
- **Memperbaiki set latihan double/menambah sendiri pada sesi dari routine (mis. FULL BODY C)**: ID `workout_exercises` yang dipakai ulang lintas sesi menyebabkan set sesi lama menumpuk dan riwayat menampilkan nomor duplikat (`1,1,1,1,3,3`). Sekarang setiap sesi memakai ID baru, riwayat terduplikat di-dedup saat dibaca, dan prefill durasi Plank/Hold ikut terbawa.

## Database Changes:
- Tidak ada perubahan skema database pada versi ini. Struktur tabel, entitas, DAO, dan versi database Room tetap berada pada versi 10.

## Migration:
- Tidak ada migrasi tabel Room yang diperlukan. Seluruh data lama kompatibel secara langsung (Zero Breaking Migration).

## Data Safety:
- **Existing user data preserved**: Seluruh data riwayat latihan, sesi workout, rekor beban (PR), ukuran tubuh, rutinitas latihan, dan pengaturan pengguna dijamin 100% utuh dan tidak akan terhapus saat memasang update ini di atas versi sebelumnya.
