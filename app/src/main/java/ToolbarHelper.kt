package com.example.womenhealthtracker

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.ViewGroup
import android.widget.LinearLayout

object ToolbarHelper {
    /**
     * Настройка Toolbar с кнопкой назад для Activity
     * Если Toolbar уже есть в layout (с id R.id.toolbar), использует его
     * Иначе создает Toolbar программно и добавляет в корневой контейнер
     */
    fun setupToolbar(activity: AppCompatActivity) {
        // Сначала пытаемся найти Toolbar в layout
        val existingToolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        
        if (existingToolbar != null) {
            // Toolbar уже есть в layout, просто настраиваем его
            activity.setSupportActionBar(existingToolbar)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        } else {
            // Toolbar нет в layout, создаем программно
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            val rootLayout = rootView.getChildAt(0) as? LinearLayout
            
            if (rootLayout != null && rootLayout.orientation == LinearLayout.VERTICAL) {
                val toolbar = Toolbar(activity)
                toolbar.id = R.id.toolbar
                toolbar.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    activity.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) + 
                    activity.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 2
                )
                toolbar.setBackgroundColor(activity.resources.getColor(android.R.color.white, null))
                
                // Добавляем Toolbar в начало контейнера
                rootLayout.addView(toolbar, 0)
                
                activity.setSupportActionBar(toolbar)
                activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                activity.supportActionBar?.setDisplayShowTitleEnabled(false)
            }
        }
    }
}






