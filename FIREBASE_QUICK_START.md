# ⚡ Быстрый старт: Настройка Firebase за 5 минут

## 🎯 Минимальные шаги для запуска

### 1️⃣ Включить Authentication (2 минуты)

```
Firebase Console → Authentication → Sign-in method → Email/Password → Enable → Save
```

### 2️⃣ Создать Firestore (2 минуты)

```
Firestore Database → Create database → Test mode → Выбрать регион → Enable
```

### 3️⃣ Настроить правила безопасности (1 минута)

```
Firestore Database → Rules → Вставить код ниже → Publish
```

**Код правил (копируйте БЕЗ слова "javascript" в начале!):**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /days/{date} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

---

## ✅ Готово!

Теперь можно запускать приложение и тестировать регистрацию.

📖 **Подробная инструкция:** см. файл `FIREBASE_SETUP_GUIDE.md`

