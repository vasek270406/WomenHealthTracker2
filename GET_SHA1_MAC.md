# 🔑 Получение SHA-1 на macOS

## ❌ Проблема: "Unable to locate a Java Runtime"

Если вы видите эту ошибку, нужно установить Java или использовать альтернативный способ.

---

## ✅ Решение 1: Использовать Java из Android Studio (Рекомендуется)

Android Studio уже содержит Java. Используйте её:

### Способ A: Через Gradle панель в Android Studio

1. Откройте Android Studio
2. Справа найдите панель **Gradle**
3. Разверните: **app → Tasks → android**
4. Дважды кликните на **signingReport**
5. Внизу в панели **Run** найдите:
   ```
   Variant: debug
   SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
   ```
6. Скопируйте значение **SHA1**

### Способ B: Через Terminal в Android Studio

1. В Android Studio: **View → Tool Windows → Terminal**
2. Выполните:
```bash
./gradlew signingReport
```
3. Найдите SHA1 в выводе

---

## ✅ Решение 2: Использовать keytool напрямую

Если у вас есть Java (даже в Android Studio), можно использовать keytool:

### Найти Java в Android Studio:

```bash
# Обычно Java находится здесь:
/Applications/Android\ Studio.app/Contents/jbr/Contents/Home/bin/keytool
```

### Получить SHA-1:

```bash
/Applications/Android\ Studio.app/Contents/jbr/Contents/Home/bin/keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Найдите строку **SHA1:** и скопируйте значение.

---

## ✅ Решение 3: Установить Java через Homebrew

Если хотите установить Java отдельно:

```bash
# Установить Homebrew (если нет)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Установить Java
brew install openjdk@17

# Добавить в PATH (добавьте в ~/.zshrc)
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

После этого можно использовать `./gradlew signingReport`

---

## ✅ Решение 4: Через Android Studio UI (Самый простой!)

1. **Откройте Android Studio**
2. **File → Project Structure** (или нажмите `Cmd + ;`)
3. Перейдите в **Modules → app → Signing Configs**
4. Там будет показан SHA-1 для debug keystore

Или:

1. **Build → Generate Signed Bundle / APK**
2. Выберите **APK**
3. Нажмите **Next**
4. Выберите **debug** keystore (или создайте новый)
5. В окне будет показан SHA-1

---

## 📋 Что делать после получения SHA-1:

1. **Скопируйте SHA-1** (например: `A1:B2:C3:D4:E5:F6:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12`)

2. **Откройте Firebase Console:**
   - https://console.firebase.google.com/
   - Выберите проект **"womenht2"**

3. **Добавьте SHA-1:**
   - ⚙️ → **Project settings**
   - Прокрутите до **"Your apps"**
   - Найдите ваше Android приложение
   - Нажмите **"Add fingerprint"** (или иконку с плюсом)
   - Вставьте SHA-1
   - Нажмите **Save**

4. **Пересоберите проект:**
   - Build → Clean Project
   - Build → Rebuild Project

---

## 🎯 Самый быстрый способ:

**Используйте Gradle панель в Android Studio:**
1. Справа → Gradle
2. app → Tasks → android → signingReport
3. Дважды кликните
4. Скопируйте SHA1 из вывода

**Это не требует установки Java отдельно!**







