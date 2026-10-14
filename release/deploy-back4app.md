# Deploy Backend GymBaswara ke Back4App Containers

Tutorial ini membuat backend Go tersedia online lewat HTTPS, sehingga aplikasi bisa signup/login dari jaringan mana saja.

Alur setelah selesai: **App Android -> https://<nama-app>.b4a.run -> MongoDB Atlas**.

Kenapa Back4App: free tier **tidak butuh kartu kredit**, mendukung Docker dari GitHub, dan bisa menyetel direktori root (`backend`).

---

## 1. Prasyarat

- Akun GitHub dan repo sudah ter-push (sudah: `lieosoke/gym-baswara`, branch `main`).
- Akun Back4App (gratis): https://www.back4app.com/signup
- Kredensial MongoDB Atlas:
  - Username: `yosraise_db_user`
  - Password: (lihat `backend/.env` di komputer Anda, jangan di-commit)
  - Host: `baswara.xbnjuaq.mongodb.net`
- Repo sudah berisi `backend/Dockerfile` (sudah di-commit).

---

## 2. Siapkan nilai rahasia

Anda butuh dua nilai ini untuk diisi di Back4App nanti. Keduanya **tidak** disimpan di repo.

### MONGODB_URI
Ambil dari `backend/.env` (baris `MONGODB_URI`). Bentuknya:

```
mongodb+srv://yosraise_db_user:PASSWORD_ANDA@baswara.xbnjuaq.mongodb.net/?retryWrites=true&w=majority
```

Catatan: jika password berisi karakter khusus (`@ : / ? # [ ] %`), harus di-URL-encode.

Cara cepat mendapatkan nilainya dari file:
```powershell
Select-String -Path D:\gymbaswara\backend\.env -Pattern '^MONGODB_URI='
```

### JWT_SECRET
Buat string acak panjang (minimal 32 karakter). Contoh perintah PowerShell:

```powershell
[System.Guid]::NewGuid().ToString('N') + [System.Guid]::NewGuid().ToString('N')
```

Salin hasilnya (64 karakter). Jangan pakai nilai contoh.

---

## 3. Izinkan Back4App mengakses Atlas

1. Buka https://cloud.mongodb.com
2. Pilih project -> menu kiri **Network Access**
3. Klik **Add IP Address**
4. Pilih **Allow Access from Anywhere** (isi `0.0.0.0/0`)
5. **Confirm**

Back4App memakai IP keluar yang tidak tetap, jadi `0.0.0.0/0` adalah cara paling praktis. Akses tetap aman karena Atlas masih mewajibkan username + password (URI).

---

## 4. Buat App di Back4App Containers

1. Login ke https://www.back4app.com
2. Pilih menu **Containers** (bukan Backend/Database).
3. Klik **Create new app** -> hubungkan akun GitHub jika diminta.
4. Pilih repo **`lieosoke/gym-baswara`**.
5. Isi konfigurasi deploy:
   - **App Name**: `gymbaswara-api`
   - **Branch**: `main`
   - **Root**: `backend` (penting, karena Dockerfile ada di `backend/`)
   - **Auto Deployment**: aktifkan (opsional, agar tiap push ke `main` otomatis deploy)
6. Tambahkan **Environment Variables** (huruf besar semua, hanya huruf/angka):
   - `MONGODB_URI` = nilai dari langkah 2
   - `MONGODB_DB` = `gymbaswara`
   - `JWT_SECRET` = nilai dari langkah 2
   - `JWT_TTL_HOURS` = `720`
7. Klik **Create App**.

Catatan penting:
- **Root harus `backend`.** Back4App membangun Docker dari folder root yang Anda pilih, dan mencari `Dockerfile` di sana.
- Pilih **plan Free** (tanpa kartu kredit). Back4App free: 0.25 CPU, 256 MB RAM, 100 GB transfer.
- Server sudah otomatis memakai port dari variabel `PORT` yang di-set Back4App, jadi tidak perlu diatur manual.

Setelah dibuat, Back4App otomatis build image dari Dockerfile dan menjalankan app. Proses build 2-5 menit.

---

## 5. Ambil URL app

1. Buka halaman **App Overview** app Anda.
2. Cari **App Domain/URL** (juga tersedia di menu **Actions** -> URL).
3. Bentuknya seperti (subdomain sering berisi akhiran acak):
   ```
   https://gymbaswaraapi-r1gfxmv6.b4a.run
   ```
4. Salin URL ini, dipakai di langkah 7.

---

## 6. Verifikasi

Ganti `<url>` dengan URL dari langkah 5.

1. Cek di browser:
   ```
   https://<url>/health
   ```
   Harus muncul: `{"status":"ok"}`

2. Uji signup lewat PowerShell:
   ```powershell
   $base = "https://<url>/api/v1"
   Invoke-RestMethod -Uri "$base/auth/register" -Method Post -ContentType 'application/json' `
     -Body (@{name='Tes';email="tes$(Get-Random)@example.com";password='rahasia123'} | ConvertTo-Json)
   ```
   Jika mengembalikan `token`, backend sudah benar.

Jika langkah 1 menampilkan `{"status":"ok"}`, backend siap dipakai aplikasi.

---

## 7. Arahkan aplikasi ke URL Back4App

URL dipakai di build **release** (`app/app/build.gradle.kts`):

```kotlin
buildConfigField("String", "BASE_URL", "\"https://<url>/\"")
```

Pastikan diakhiri `/`. Lalu build APK release:

```powershell
$env:JAVA_HOME="D:\gymbaswara\jdk-17.0.2"
& "D:\gymbaswara\app\gradle-8.7\bin\gradle.bat" :app:assembleRelease --no-daemon
```

APK ada di `app/app/build/outputs/apk/release/`.

Build release memakai HTTPS, jadi tidak perlu pengaturan cleartext.

---

## 8. Troubleshooting

| Gejala | Penyebab | Solusi |
|---|---|---|
| Build gagal: "Dockerfile not found" | Root salah | Set **Root** ke `backend` |
| Build gagal: `go.sum` tidak sinkron | File tidak ter-push | Pastikan `backend/go.mod` + `backend/go.sum` ada di repo, `git push` ulang |
| App berjalan tapi URL 502/502 Bad Gateway | Server tidak listen di `PORT` | Pastikan tidak override `PORT`; server sudah memakai `PORT` otomatis |
| Log server: `Failed to connect to MongoDB at mongodb://localhost:27017` | Env `MONGODB_URI` belum diisi di Back4App | Tambahkan env variables (langkah 4), lalu redeploy |
| Log server: `Failed to connect to MongoDB` | Atlas belum mengizinkan IP Back4App | Tambah `0.0.0.0/0` di Network Access (langkah 3) |
| Log server: `authentication failed` | Password salah / tidak di-URL-encode | Perbaiki `MONGODB_URI`, update env var di Back4App, redeploy |
| App Android: "Tidak dapat terhubung ke server" | `BASE_URL` salah atau app belum Live | Cek URL dari App Overview, pastikan build pakai APK release |
| Request pertama lambat | Container baru bangun | Normal; permintaan berikutnya cepat |

---

## 9. Catatan Free Tier

- Sumber daya terbatas: 0.25 CPU, 256 MB RAM, 100 GB transfer (regional US).
- Satu container per plan.
- Auto deploy: setiap `git push` ke `main` akan memicu build & deploy baru.

---

## 10. Update backend di kemudian hari

1. Ubah kode di `backend/`
2. `git add backend` lalu `git commit` dan `git push`
3. Back4App otomatis build dan deploy versi baru (jika Auto Deployment aktif).

---

## 11. Rujukan

- Panduan resmi: https://www.back4app.com/docs-containers
- Go di Back4App: https://www.back4app.com/docs-containers/run-a-go-container-app
