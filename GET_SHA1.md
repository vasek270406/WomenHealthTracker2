# 🔑 Как получить SHA-1 Fingerprint

## Способ 1: Через Gradle (Самый простой)

### В Android Studio:

1. Откройте **Terminal** внизу (или View → Tool Windows → Terminal)
2. Выполните команду:
```bash
./gradlew signingReport
```
3. В выводе найдите:
```
Variant: debug
Config: debug
Store: ...
Alias: ...
SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
```
4. Скопируйте значение **SHA1** (всю строку с двоеточиями)

---

## Способ 2: Через keytool (macOS/Linux)

```bash
cd ~/.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Найдите строку **SHA1:** и скопируйте значение.

---

## Способ 3: Через Gradle панель в Android Studio

1. Справа откройте панель **Gradle**
2. Разверните: **app → Tasks → android**
3. Дважды кликните на **signingReport**
4. Внизу в панели **Run** найдите SHA1

---

## 📋 Что делать дальше:

1. **Скопируйте SHA-1** (например: `A1:B2:C3:D4:E5:F6:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12`)

2. **Откройте Firebase Console:**
   - https://console.firebase.google.com/
   - Выберите проект **"womenht2"**

3. **Добавьте SHA-1:**
   - ⚙️ → **Project settings**
   - Прокрутите до **"Your apps"**
   - Найдите ваше Android приложение
   - Нажмите **"Add fingerprint"**
   - Вставьте SHA-1
   - Нажмите **Save**

4. **Скачайте обновленный google-services.json** (если нужно)

5. **Пересоберите проект:**
   - Build → Clean Project
   - Build → Rebuild Project

---

**После этого ошибка DEVELOPER_ERROR должна исчезнуть!**







