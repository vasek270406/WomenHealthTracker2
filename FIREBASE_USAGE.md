# Руководство по использованию Firebase в приложении

## 📋 Обзор

В приложении интегрированы следующие сервисы Firebase:
- **Firebase Authentication** - для авторизации пользователей
- **Cloud Firestore** - для хранения данных пользователей и данных по дням

## 🔧 Что было сделано

### 1. Добавлены зависимости
- `firebase-auth-ktx` - для работы с аутентификацией
- `firebase-firestore-ktx` - для работы с базой данных

### 2. Созданы вспомогательные классы

#### `FirebaseAuthHelper`
Класс для работы с Firebase Authentication:
- `signUp()` - регистрация нового пользователя
- `signIn()` - вход пользователя
- `signOut()` - выход пользователя
- `getCurrentUser()` - получение текущего пользователя
- `isUserLoggedIn()` - проверка авторизации

#### `FirestoreHelper`
Класс для работы с Cloud Firestore:
- `saveUserProfile()` - сохранение профиля пользователя
- `getUserProfile()` - получение профиля пользователя
- `saveDayData()` - сохранение данных дня
- `getDayData()` - получение данных дня
- `getDayDataForPeriod()` - получение данных за период
- `getAllDatesWithData()` - получение всех дат с данными

### 3. Интегрировано в активности

#### `SignUpActivity`
- Регистрация через Firebase Authentication
- Автоматическое создание профиля в Firestore
- Сохранение данных в SharedPreferences для обратной совместимости

#### `LoginActivity`
- Вход через Firebase Authentication
- Загрузка профиля из Firestore
- Синхронизация данных с SharedPreferences

#### `AccountActivity`
- Выход из Firebase Authentication при выходе из аккаунта

## 📊 Структура данных в Firestore

### Коллекция `users`
```
users/
  {userId}/
    name: String
    age: Int
    cycleLength: Int
    menstruationLength: Int
    goals: String
    lastPeriodStart: String
    periodDates: List<String>
    onboardingCompleted: Boolean
    notificationPeriod: Boolean
    notificationFertile: Boolean
    notificationDaily: Boolean
    notificationWater: Boolean
```

### Подколлекция `days`
```
users/
  {userId}/
    days/
      {date}/
        date: String
        mood: Int
        symptoms: List<Map>
        weight: Float?
        temperature: Float?
        sleepHours: Float?
        waterIntake: Float?
        energy: Int?
        libido: Int?
        sexualActivity: Boolean
        vitamins: Boolean
        notes: String
```

## 🚀 Как использовать

### Регистрация пользователя
```kotlin
val authHelper = FirebaseAuthHelper(this)
authHelper.signUp(
    email = "user@example.com",
    password = "password123",
    name = "Имя пользователя",
    onSuccess = { user ->
        // Пользователь успешно зарегистрирован
    },
    onError = { error ->
        // Обработка ошибки
    }
)
```

### Вход пользователя
```kotlin
val authHelper = FirebaseAuthHelper(this)
authHelper.signIn(
    email = "user@example.com",
    password = "password123",
    onSuccess = { user ->
        // Пользователь успешно вошел
    },
    onError = { error ->
        // Обработка ошибки
    }
)
```

### Сохранение данных дня
```kotlin
val firestoreHelper = FirestoreHelper()
val userId = FirebaseAuthHelper(this).getCurrentUserId() ?: return

val dayData = DayData(
    date = "2024-11-18",
    mood = 3,
    symptoms = listOf(...),
    // ... другие поля
)

firestoreHelper.saveDayData(
    userId = userId,
    dayData = dayData,
    onSuccess = {
        // Данные успешно сохранены
    },
    onError = { error ->
        // Обработка ошибки
    }
)
```

### Получение данных дня
```kotlin
val firestoreHelper = FirestoreHelper()
val userId = FirebaseAuthHelper(this).getCurrentUserId() ?: return

firestoreHelper.getDayData(
    userId = userId,
    date = "2024-11-18",
    onSuccess = { dayData ->
        // Использовать dayData
    },
    onError = { error ->
        // Обработка ошибки
    }
)
```

## 🔄 Синхронизация данных

Приложение использует гибридный подход:
- **SharedPreferences** - для быстрого доступа к данным локально
- **Firestore** - для синхронизации между устройствами и резервного копирования

При входе пользователя данные автоматически синхронизируются из Firestore в SharedPreferences.

## ⚙️ Настройка в Firebase Console

1. **Включить Authentication:**
   - Перейти в Firebase Console → Authentication
   - Включить метод "Email/Password"

2. **Настроить Firestore:**
   - Перейти в Firebase Console → Firestore Database
   - Создать базу данных в режиме "Production" или "Test"
   - Настроить правила безопасности (пример ниже)

### Правила безопасности Firestore (для тестирования)
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

## 🔒 Безопасность

- Все данные пользователя доступны только самому пользователю (по userId)
- Требуется авторизация для доступа к данным
- Пароли хранятся в зашифрованном виде в Firebase Authentication

## 📝 Следующие шаги

1. **Миграция существующих данных:**
   - Создать функцию для переноса данных из SharedPreferences в Firestore
   - Выполнить миграцию при первом входе после обновления

2. **Синхронизация данных по дням:**
   - Обновить методы сохранения данных дня для использования Firestore
   - Добавить автоматическую синхронизацию при изменении данных

3. **Офлайн-режим:**
   - Включить кэширование Firestore для работы без интернета
   - Синхронизация при восстановлении соединения

4. **Дополнительные функции:**
   - Push-уведомления (Firebase Cloud Messaging)
   - Хранение файлов (Firebase Storage)
   - Аналитика (Firebase Analytics - уже подключен)







