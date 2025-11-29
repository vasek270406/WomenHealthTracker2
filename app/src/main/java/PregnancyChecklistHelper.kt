package com.example.womenhealthtracker

/**
 * Вспомогательный класс для чек-листа беременности
 */
object PregnancyChecklistHelper {
    
    /**
     * Получить стандартный чек-лист для триместра
     */
    fun getChecklistForTrimester(trimester: Int): List<PregnancyChecklistItem> {
        return when (trimester) {
            1 -> getFirstTrimesterChecklist()
            2 -> getSecondTrimesterChecklist()
            3 -> getThirdTrimesterChecklist()
            else -> emptyList()
        }
    }
    
    /**
     * Чек-лист первого триместра
     */
    private fun getFirstTrimesterChecklist(): List<PregnancyChecklistItem> {
        return listOf(
            PregnancyChecklistItem("1_1", "Встать на учет в женской консультации", 1),
            PregnancyChecklistItem("1_2", "Сдать общий анализ крови и мочи", 1),
            PregnancyChecklistItem("1_3", "Первый скрининг (11-13 недель)", 1),
            PregnancyChecklistItem("1_4", "Начать принимать витамины для беременных", 1),
            PregnancyChecklistItem("1_5", "Исключить алкоголь и курение", 1),
            PregnancyChecklistItem("1_6", "Ограничить кофеин", 1),
            PregnancyChecklistItem("1_7", "Сообщить работодателю о беременности", 1)
        )
    }
    
    /**
     * Чек-лист второго триместра
     */
    private fun getSecondTrimesterChecklist(): List<PregnancyChecklistItem> {
        return listOf(
            PregnancyChecklistItem("2_1", "Второй скрининг (18-21 неделя)", 2),
            PregnancyChecklistItem("2_2", "Глюкозотолерантный тест (24-28 недель)", 2),
            PregnancyChecklistItem("2_3", "Начать курсы для будущих родителей", 2),
            PregnancyChecklistItem("2_4", "Начать готовить детскую комнату", 2),
            PregnancyChecklistItem("2_5", "Выбрать роддом", 2),
            PregnancyChecklistItem("2_6", "Купить одежду для беременных", 2),
            PregnancyChecklistItem("2_7", "Начать планировать декретный отпуск", 2)
        )
    }
    
    /**
     * Чек-лист третьего триместра
     */
    private fun getThirdTrimesterChecklist(): List<PregnancyChecklistItem> {
        return listOf(
            PregnancyChecklistItem("3_1", "Третий скрининг (30-34 недели)", 3),
            PregnancyChecklistItem("3_2", "Собрать сумку в роддом", 3),
            PregnancyChecklistItem("3_3", "Выбрать имя для малыша", 3),
            PregnancyChecklistItem("3_4", "Подготовить документы для роддома", 3),
            PregnancyChecklistItem("3_5", "Купить детские вещи первой необходимости", 3),
            PregnancyChecklistItem("3_6", "Установить детское кресло в машину", 3),
            PregnancyChecklistItem("3_7", "Подготовить дом к приезду малыша", 3),
            PregnancyChecklistItem("3_8", "Обсудить план родов с врачом", 3)
        )
    }
    
    /**
     * Получить все пункты чек-листа
     */
    fun getAllChecklistItems(): List<PregnancyChecklistItem> {
        return getFirstTrimesterChecklist() + 
               getSecondTrimesterChecklist() + 
               getThirdTrimesterChecklist()
    }
}







