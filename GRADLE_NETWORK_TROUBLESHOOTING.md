# Устранение проблем с сетью в Gradle

## Проблема
Gradle не может загрузить зависимости из-за ошибок сети:
- `UnknownHostException: dl.google.com`
- `UnknownHostException: repo.maven.apache.org`

## Решения

### 1. Проверка интернет-соединения

Убедитесь, что у вас есть доступ к интернету:
```bash
ping google.com
ping dl.google.com
```

### 2. Проверка DNS

Если ping не работает, попробуйте использовать альтернативные DNS серверы:
- Google DNS: `8.8.8.8` и `8.8.4.4`
- Cloudflare DNS: `1.1.1.1` и `1.0.0.1`

### 3. Настройка прокси (если требуется)

Если вы используете прокси-сервер, добавьте настройки в `gradle.properties`:

```properties
systemProp.http.proxyHost=your.proxy.host
systemProp.http.proxyPort=8080
systemProp.http.proxyUser=username
systemProp.http.proxyPassword=password

systemProp.https.proxyHost=your.proxy.host
systemProp.https.proxyPort=8080
systemProp.https.proxyUser=username
systemProp.https.proxyPassword=password
```

### 4. Использование альтернативных репозиториев

В файле `settings.gradle.kts` уже добавлены альтернативные репозитории (Aliyun mirrors). Если они не работают, можно попробовать другие:

#### Для пользователей в Китае:
- Aliyun Maven (уже добавлен)
- Tencent Maven
- Huawei Maven

#### Для других регионов:
- JitPack: `maven { url = uri("https://jitpack.io") }`
- Sonatype: `maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }`

### 5. Очистка кэша Gradle

Иногда помогает очистка кэша:

```bash
# В терминале Android Studio или командной строке:
./gradlew clean --refresh-dependencies

# Или удалите кэш вручную:
rm -rf ~/.gradle/caches/
```

### 6. Использование офлайн-режима (если зависимости уже загружены)

Если зависимости уже были загружены ранее, можно использовать офлайн-режим:

В `gradle.properties` добавьте:
```properties
org.gradle.offline=true
```

### 7. Проверка файрвола и антивируса

Убедитесь, что файрвол или антивирус не блокируют доступ Gradle к интернету.

### 8. Использование VPN

Если проблема связана с географическими ограничениями, попробуйте использовать VPN.

## Шаги для применения исправлений

1. **Синхронизация проекта:**
   - В Android Studio: **File** → **Sync Project with Gradle Files**

2. **Очистка проекта:**
   - **Build** → **Clean Project**

3. **Пересборка:**
   - **Build** → **Rebuild Project**

4. **Если проблема сохраняется:**
   - Закройте Android Studio
   - Удалите папку `.gradle` в корне проекта (если есть)
   - Удалите папку `build` в модуле `app`
   - Откройте Android Studio и синхронизируйте проект снова

## Дополнительная информация

- Проверьте логи Gradle для более детальной информации об ошибке
- Убедитесь, что версия Gradle совместима с вашей версией Android Studio
- Проверьте, что в `settings.gradle.kts` правильно настроены репозитории

## Контакты для поддержки

Если проблема не решается, проверьте:
- [Gradle Issues](https://github.com/gradle/gradle/issues)
- [Android Studio Help](https://developer.android.com/studio/intro)

