# 📱 SalesApp – PRM392 Project

Dự án môn PRM392 – Mobile Programming  
Android (Java, minSdk 21) + Spring Boot (Java) + PostgreSQL  

---

## ✅ Requirements

- Java **17.0.16**
- Gradle **9.1**
- PostgreSQL **18**
- IDE: IntelliJ IDEA / VS Code / Android Studio
- Extensions cần thiết (VS Code):
  - Gradle for Java
  - Extension Pack for Java
  - Spring Boot Dashboard

---

## ⚙️ Environment Setup

1. **Cài Java 17.0.16**
   - Download tại [Adoptium](https://adoptium.net/) hoặc trang chủ OpenJDK.
   - Sau khi cài:  
     ```bash
     java -version
     # openjdk 17.0.16 ...
     ```

2. **Cài Gradle 9.1**
   - Download tại [Gradle Releases](https://gradle.org/releases/).
   - Hoặc để tiện hơn: dùng Gradle Wrapper có sẵn trong repo (`./gradlew` hoặc `gradlew.bat`).

3. **Cài PostgreSQL 18**
   - Download tại [PostgreSQL](https://www.postgresql.org/download/).
   - Tạo database `salesapp`:
     ```sql
     CREATE DATABASE salesapp;
     ```
   - Ghi nhớ `username/password` để chỉnh trong `application.properties`.

4. **Cài đặt IDE Plugins**
   - `Gradle for Java`
   - `Extension Pack for Java`
   - `Spring Boot Dashboard`

---

## ▶️ Run Project

### Backend
- Mở repo trong **VS Code / IntelliJ**.  
- Sử dụng **Spring Boot Dashboard** để chạy `BackendApplication`.  
- API mặc định tại: [http://localhost:8080/api](http://localhost:8080/api)

### Android App
- Mở folder Android trong **Android Studio**.  
- Sync Gradle.  
- Chạy app trên **emulator** hoặc thiết bị thật.

---

## 👥 Working Rules (Team Workflow)

### 1. Branching
- `main` → code ổn định (chỉ merge khi review xong).
- - `develop` → nhánh tổng hợp code nhóm.  
- `feature/<tên-chức-năng>` → mỗi thành viên code trên nhánh riêng.  

### 2. Commit Convention
- `feat:` – thêm tính năng mới.  
- `fix:` – sửa bug.  
- `docs:` – cập nhật tài liệu/README.  
- `refactor:` – chỉnh code không đổi chức năng.  
- `test:` – thêm hoặc sửa test.  

### 3. Quy trình làm việc
1. **Pull code mới nhất** từ `develop` trước khi code:  
   ```bash
   git checkout develop
   git pull origin develop
2. **Tạo nhánh mới kèm tên feat** và check out:
   git checkout -b feat/aut
3. **Commit và push**
   git add .
   git commit -m "feat(auth): register & login with bcrypt and jwt"
   git push origin feat/auth
4. **Tạo Pull Request từ feat/.. -> develop**
5. **Review code nếu ổn thì merge**
6. **Sau khi release, mearge develop -> main**

📌 Notes

Mọi thành viên không push trực tiếp vào main.

Nếu gặp vấn đề môi trường → tham khảo [ChatGPT Setup Guide] hoặc trao đổi trong nhóm.
