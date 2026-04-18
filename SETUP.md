# Инструкция по первому запуску

## Важные шаги перед запуском

### 1. Настройка Gradle Wrapper

В командной строке выполните:

```bash
cd "d:\ЗЯ трекер"
gradle wrapper
```

Или откройте проект в Android Studio - он автоматически создаст wrapper.

### 2. Проверьте путь к SDK

Файл `local.properties` должен указывать на правильный путь к Android SDK.
Если у вас другой путь, измените его.

### 3. Откройте в Android Studio

1. Запустите Android Studio
2. File → Open → Выберите папку `d:\ЗЯ трекер`
3. Дождитесь синхронизации Gradle
4. Запустите приложение (Shift+F10)

### 4. Или используйте командную строку

```bash
cd "d:\ЗЯ трекер"
gradlew.bat assembleDebug
```

APK файл будет здесь:
`app\build\outputs\apk\debug\app-debug.apk`
