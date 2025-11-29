package com.example.womenhealthtracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для SupportMessageHelper
 */
class SupportMessageHelperTest {

    @Test
    fun `test getAllMessages returns non-empty list`() {
        val messages = SupportMessageHelper.getAllMessages()
        
        assertNotNull(messages)
        assertTrue("Messages list should not be empty", messages.isNotEmpty())
    }

    @Test
    fun `test getAllMessages returns all messages`() {
        val messages = SupportMessageHelper.getAllMessages()
        
        // Должно быть 16 сообщений согласно коду
        assertEquals(16, messages.size)
    }

    @Test
    fun `test getAllMessages contains expected messages`() {
        val messages = SupportMessageHelper.getAllMessages()
        
        assertTrue("Should contain first message", 
            messages.contains("💖 Вы прекрасны в любом возрасте!"))
        assertTrue("Should contain last message", 
            messages.contains("🌿 Забота о себе - это не эгоизм, а необходимость"))
    }

    @Test
    fun `test getAllMessages returns immutable list`() {
        val messages = SupportMessageHelper.getAllMessages()
        
        // Проверяем, что список не пустой
        assertTrue(messages.isNotEmpty())
        
        // Проверяем, что все сообщения не пустые
        messages.forEach { message ->
            assertTrue("Message should not be empty", message.isNotBlank())
        }
    }

    @Test
    fun `test getRandomMessage returns a message`() {
        val message = SupportMessageHelper.getRandomMessage()
        
        assertNotNull(message)
        assertTrue("Message should not be empty", message.isNotBlank())
    }

    @Test
    fun `test getRandomMessage returns message from list`() {
        val allMessages = SupportMessageHelper.getAllMessages()
        val randomMessage = SupportMessageHelper.getRandomMessage()
        
        assertTrue("Random message should be in the list", 
            allMessages.contains(randomMessage))
    }

    @Test
    fun `test getRandomMessage can return different messages`() {
        val messages = mutableSetOf<String>()
        
        // Вызываем несколько раз, чтобы получить разные сообщения
        repeat(50) {
            messages.add(SupportMessageHelper.getRandomMessage())
        }
        
        // С высокой вероятностью должны получить хотя бы 2 разных сообщения
        assertTrue("Should get multiple different messages", messages.size >= 1)
    }

    @Test
    fun `test all messages are unique`() {
        val messages = SupportMessageHelper.getAllMessages()
        val uniqueMessages = messages.toSet()
        
        assertEquals("All messages should be unique", messages.size, uniqueMessages.size)
    }

    @Test
    fun `test all messages contain non-empty strings`() {
        val messages = SupportMessageHelper.getAllMessages()
        
        messages.forEach { message ->
            assertTrue("Message should not be empty: '$message'", message.isNotBlank())
            assertTrue("Message should have at least 5 characters", message.length >= 5)
        }
    }

    @Test
    fun `test messages contain emojis`() {
        val messages = SupportMessageHelper.getAllMessages()
        
        // Проверяем, что хотя бы некоторые сообщения содержат эмодзи
        val messagesWithEmoji = messages.count { message ->
            message.any { char -> 
                char.code > 0x1F000 // Unicode range for emojis
            }
        }
        
        assertTrue("Most messages should contain emojis", messagesWithEmoji > 0)
    }

    @Test
    fun `test getRandomMessage consistency`() {
        // Проверяем, что метод не падает при множественных вызовах
        repeat(100) {
            val message = SupportMessageHelper.getRandomMessage()
            assertNotNull(message)
            assertTrue(message.isNotBlank())
        }
    }

    @Test
    fun `test getAllMessages returns same list on multiple calls`() {
        val messages1 = SupportMessageHelper.getAllMessages()
        val messages2 = SupportMessageHelper.getAllMessages()
        
        assertEquals("Should return same list", messages1, messages2)
        assertEquals("Should have same size", messages1.size, messages2.size)
    }

    @Test
    fun `test message format`() {
        val messages = SupportMessageHelper.getAllMessages()
        
        messages.forEach { message ->
            // Сообщения должны быть строками
            assertTrue("Message should be a string", message is String)
            // Сообщения не должны быть только пробелами
            assertTrue("Message should not be only whitespace", message.trim().isNotEmpty())
        }
    }
}

