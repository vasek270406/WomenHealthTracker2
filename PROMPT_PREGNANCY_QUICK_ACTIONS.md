# 🚀 ПРОМПТ: Добавление панели быстрых действий для режима беременности

## 📋 Задача

Добавить на главный экран (`CalendarActivity`) в режиме беременности (`UserGoal.PREGNANCY`) панель быстрых действий с тремя кнопками:

1. **🏥 Визит к врачу** → `DoctorVisitActivity`
2. **📝 Симптомы** → `PregnancySymptomsActivity`
3. **📸 УЗИ** → `UltrasoundActivity`

## 🎯 Требования

### 1. Визуальное расположение
- Панель должна отображаться **только в режиме беременности** (`UserGoal.PREGNANCY`)
- Разместить панель **после виджета размера ребенка** (BabySizeWidget) и **перед информацией о цикле**
- Использовать `GridLayout` или `LinearLayout` с горизонтальной ориентацией
- 3 кнопки в ряд (или вертикально, если экран узкий)

### 2. Дизайн кнопок
- Каждая кнопка должна быть в виде карточки (`CardView`)
- Размер: примерно 100-120dp в ширину и высоту
- Фон: белый с закругленными углами (12dp)
- Тень: `cardElevation="2dp"`
- Внутри карточки:
  - **Иконка** (emoji или ImageView) - размер 48dp
  - **Текст** под иконкой - размер 14sp, цвет #000000
  - Центрирование по вертикали и горизонтали

### 3. Кнопки и их действия

#### 🏥 Визит к врачу
- **Иконка**: 🏥 или `@android:drawable/ic_menu_agenda`
- **Текст**: "Визит к врачу"
- **Действие**: Открыть `DoctorVisitActivity`

#### 📝 Симптомы
- **Иконка**: 📝 или `@android:drawable/ic_menu_edit`
- **Текст**: "Симптомы"
- **Действие**: Открыть `PregnancySymptomsActivity` с текущей датой

#### 📸 УЗИ
- **Иконка**: 📸 или `@android:drawable/ic_menu_camera`
- **Текст**: "УЗИ"
- **Действие**: Открыть `UltrasoundActivity`

### 4. Условное отображение
- Панель должна быть **скрыта** для других режимов (`CYCLE_TRACKING`, `PREGNANCY_PLANNING`, `MENOPAUSE`)
- Использовать `android:visibility="gone"` или программно управлять видимостью

## 📝 Шаги реализации

### Шаг 1: Обновить `activity_calendar.xml`

Добавить панель быстрых действий в layout:

```xml
<!-- Панель быстрых действий для беременности -->
<LinearLayout
    android:id="@+id/pregnancyQuickActionsContainer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center"
    android:padding="16dp"
    android:visibility="gone">

    <!-- Визит к врачу -->
    <androidx.cardview.widget.CardView
        android:id="@+id/doctorVisitCard"
        android:layout_width="0dp"
        android:layout_height="120dp"
        android:layout_weight="1"
        android:layout_margin="8dp"
        app:cardCornerRadius="12dp"
        app:cardElevation="2dp"
        android:backgroundTint="#FFFFFF"
        android:clickable="true"
        android:focusable="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🏥"
                android:textSize="48sp"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Визит к врачу"
                android:textColor="#000000"
                android:textSize="14sp"
                android:gravity="center"
                android:fontFamily="sans-serif" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- Симптомы -->
    <androidx.cardview.widget.CardView
        android:id="@+id/symptomsCard"
        android:layout_width="0dp"
        android:layout_height="120dp"
        android:layout_weight="1"
        android:layout_margin="8dp"
        app:cardCornerRadius="12dp"
        app:cardElevation="2dp"
        android:backgroundTint="#FFFFFF"
        android:clickable="true"
        android:focusable="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="📝"
                android:textSize="48sp"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Симптомы"
                android:textColor="#000000"
                android:textSize="14sp"
                android:gravity="center"
                android:fontFamily="sans-serif" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- УЗИ -->
    <androidx.cardview.widget.CardView
        android:id="@+id/ultrasoundCard"
        android:layout_width="0dp"
        android:layout_height="120dp"
        android:layout_weight="1"
        android:layout_margin="8dp"
        app:cardCornerRadius="12dp"
        app:cardElevation="2dp"
        android:backgroundTint="#FFFFFF"
        android:clickable="true"
        android:focusable="true">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="📸"
                android:textSize="48sp"
                android:layout_marginBottom="8dp" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="УЗИ"
                android:textColor="#000000"
                android:textSize="14sp"
                android:gravity="center"
                android:fontFamily="sans-serif" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

</LinearLayout>
```

### Шаг 2: Обновить `CalendarActivity.kt`

Добавить метод для настройки панели быстрых действий:

```kotlin
private fun setupPregnancyQuickActions() {
    val quickActionsContainer = findViewById<LinearLayout>(R.id.pregnancyQuickActionsContainer)
    val doctorVisitCard = findViewById<androidx.cardview.widget.CardView>(R.id.doctorVisitCard)
    val symptomsCard = findViewById<androidx.cardview.widget.CardView>(R.id.symptomsCard)
    val ultrasoundCard = findViewById<androidx.cardview.widget.CardView>(R.id.ultrasoundCard)
    
    val goal = userPreferences.getSelectedGoal()
    
    // Показываем панель только в режиме беременности
    if (goal == UserGoal.PREGNANCY) {
        quickActionsContainer?.visibility = android.view.View.VISIBLE
        
        // Визит к врачу
        doctorVisitCard?.setOnClickListener {
            val intent = Intent(this, DoctorVisitActivity::class.java)
            startActivity(intent)
        }
        
        // Симптомы
        symptomsCard?.setOnClickListener {
            val intent = Intent(this, PregnancySymptomsActivity::class.java)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            intent.putExtra("date", dateFormat.format(Date()))
            startActivity(intent)
        }
        
        // УЗИ
        ultrasoundCard?.setOnClickListener {
            val intent = Intent(this, UltrasoundActivity::class.java)
            startActivity(intent)
        }
    } else {
        quickActionsContainer?.visibility = android.view.View.GONE
    }
}
```

Вызвать этот метод в `onCreate()` или `updateCycleInfo()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ... существующий код ...
    setupPregnancyQuickActions()
}
```

## ✅ Ожидаемый результат

1. В режиме беременности на главном экране появляется панель с 3 кнопками
2. Каждая кнопка открывает соответствующий экран
3. В других режимах панель скрыта
4. Дизайн соответствует общему стилю приложения

## 🔧 Дополнительные улучшения (опционально)

1. **Анимация при нажатии**: Добавить ripple effect для карточек
2. **Счетчики**: Показать количество сохраненных визитов/симптомов на карточках
3. **Последний визит**: Показать дату последнего визита к врачу
4. **Адаптивность**: Для узких экранов расположить кнопки вертикально

## 📍 Расположение в коде

- **Layout**: `app/src/main/res/layout/activity_calendar.xml`
- **Activity**: `app/src/main/java/CalendarActivity.kt`
- **Целевые Activity**: 
  - `app/src/main/java/DoctorVisitActivity.kt`
  - `app/src/main/java/PregnancySymptomsActivity.kt`
  - `app/src/main/java/UltrasoundActivity.kt`

---

**Cursor, реализуй эту панель быстрых действий для режима беременности!** 🚀







