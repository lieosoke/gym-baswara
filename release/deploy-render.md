# Deploy Backend GymBaswara ke Render (Opsi B)

Tutorial ini membuat backend Go tersedia online lewat HTTPS, sehingga aplikasi bisa signup/login dari jaringan mana saja.

Alur setelah selesai: **App Android -> https://<nama>.onrender.com -> MongoDB Atlas**.

---

## 1. Prasyarat

- Akun GitHub dan repo sudah ter-push (sudah: `lieosoke/gym-baswara`, branch `main`).
- Akun Render (gratis): https://render.com
- Kredensial MongoDB Atlas:
  - Username: `yosraise_db_user`
  - Password: (lihat `backend/.env` di komputer Anda, jangan di-commit)
  - Host: `baswara.xbnjuaq.mongodb.net`
- Repo sudah berisi `render.yaml` dan `backend/Dockerfile` (sudah di-commit).

---

## 2. Siapkan nilai rahasia

Anda butuh dua nilai ini untuk diisi di Render nanti. Keduanya **tidak** disimpan di repo.

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

## 3. Izinkan Render mengakses Atlas

1. Buka https://cloud.mongodb.com
2. Pilih project -> menu kiri **Network Access**
3. Klik **Add IP Address**
4. Pilih **Allow Access from Anywhere** (isi `0.0.0.0/0`)
5. **Confirm**

Render memakai IP keluar yang tidak tetap, jadi `0.0.0.0/0` adalah cara paling praktis. Akses tetap aman karena Atlas masih mewajibkan username + password (URI).

---

## 4. Deploy di Render lewat Blueprint

1. Login ke https://dashboard.render.com
2. Klik **New +** (kanan atas) -> **Blueprint**
3. Hubungkan akun GitHub jika belum, lalu pilih repo **`lieosoke/gym-baswara`**
4. Render membaca `render.yaml` dan menampilkan service **`gymbaswara-api`**
5. Render meminta nilai variabel yang ditandai rahasia (karena `sync: false`):
   - **MONGODB_URI** -> tempel nilai dari langkah 2
   - **JWT_SECRET** -> tempel nilai dari langkah 2
   - `MONGODB_DB` (`gymbaswara`) dan `JWT_TTL_HOURS` (`720`) sudah terisi otomatis
6. Klik **Apply** / **Create**
7. Tunggu proses build (sekitar 2-5 menit). Status berubah menjadi **Live**.

Konfigurasi yang dipakai (dari `render.yaml`):
- Runtime: Docker, konteks `backend/`
- Plan: Free
- Health check: `/health`

---

## 5. Verifikasi

Ganti `<nama>` sesuai nama service Anda (default `gymbaswara-api`).

1. Cek di browser:
   ```
   https://gymbaswara-api.onrender.com/health
   ```
   Harus muncul: `{"status":"ok"}`

2. Uji signup lewat PowerShell:
   ```powershell
   $base = "https://gymbaswara-api.onrender.com/api/v1"
   Invoke-RestMethod -Uri "$base/auth/register" -Method Post -ContentType 'application/json' `
     -Body (@{name='Tes';email="tes$(Get-Random)@example.com";password='rahasia123'} | ConvertTo-Json)
   ```
   Jika mengembalikan `token`, backend sudah benar.

Jika langkah 1 menampilkan `{"status":"ok"}`, backend siap dipakai aplikasi.

---

## 6. Arahkan aplikasi ke URL Render

URL dipakai di build **release** (`app/app/build.gradle.kts`):

```kotlin
buildConfigField("String", "BASE_URL", "\"https://gymbaswara-api.onrender.com/\"")
```

- Jika nama service Render Anda **sama** (`gymbaswara-api`), nilainya sudah benar.
- Jika nama berbeda, ubah ke `https://<nama-anda>.onrender.com/` (harus diakhiri `/`).

Lalu build APK release:

```powershell
$env:JAVA_HOME="D:\gymbaswara\jdk-17.0.2"
& "D:\gymbaswara\app\gradle-8.7\bin\gradle.bat" :app:assembleRelease --no-daemon
```

APK ada di `app/app/build/outputs/apk/release/`.

Build release memakai HTTPS, jadi tidak perlu pengaturan cleartext.

---

## 7. Kalau nama service sudah cocok, lewati langkah ini

Hanya jika ingin mengganti nama service: ubah `name:` di `render.yaml`, commit, push, lalu Render akan menyesuaikan. URL ikut berubah.

---

## 8. Troubleshooting

| Gejala | Penyebab | Solusi |
|---|---|---|
| `/health` 404 | Service belum ter-deploy atau nama/URL salah | Cek dashboard Render, pastikan status Live, salin URL yang benar |
| Build gagal di Render | `go.sum` tidak sinkron atau file tidak ter-push | Pastikan `backend/go.mod` + `backend/go.sum` ada di repo; jalankan `git push` ulang |
| Log server: `Failed to connect to MongoDB` | Atlas belum mengizinkan IP Render | Tambah `0.0.0.0/0` di Network Access (langkah 3) |
| Log server: `authentication failed` | Password salah / tidak di-URL-encode | Perbaiki `MONGODB_URI`, update env var di Render |
| App: "Tidak dapat terhubung ke server" | `BASE_URL` salah atau backend tidur | Cek URL, tunggu cold start, pastikan pakai APK release |
| Request pertama lambat (30-60 detik) | Free tier Render tidur saat idle | Normal; permintaan berikutnya cepat |

---

## 9. Catatan Free Tier

- Service tidur setelah sekitar 15 menit tanpa aktivitas. Permintaan pertama membangunkan kembali (cold start 30-60 detik).
- Kuota bulanan terbatas (jam instans). Untuk pemakaian ringan umumnya cukup.
- Setiap `git push` ke `main` akan men-trigger deploy otomatis (`autoDeploy: true`).

---

## 10. Update backend di kemudian hari

1. Ubah kode di `backend/`
2. `git add backend render.yaml` lalu `git commit` dan `git push`
3. Render otomatis build dan deploy versi baru.
