package com.example.womenhealthtracker

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthHelper(private val context: Context) {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    // Регистрация нового пользователя
    fun signUp(
        email: String,
        password: String,
        name: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            onError("Email и пароль не могут быть пустыми")
            return
        }
        
        if (password.length < 6) {
            onError("Пароль должен содержать минимум 6 символов")
            return
        }
        
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            // Обновляем профиль пользователя с именем (необязательно, но полезно)
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            
                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { updateTask ->
                                    // Вызываем onSuccess независимо от результата обновления профиля
                                    // так как пользователь уже создан
                                    onSuccess(user)
                                }
                                .addOnFailureListener {
                                    // Даже если обновление профиля не удалось, пользователь создан
                                    onSuccess(user)
                                }
                        } else {
                            onError("Пользователь не найден после регистрации")
                        }
                    } else {
                        val exception = task.exception
                        // Логируем полную информацию об ошибке для отладки
                        android.util.Log.e("FirebaseAuthHelper", "Registration error", exception)
                        android.util.Log.d("FirebaseAuthHelper", "Exception type: ${exception?.javaClass?.name}")
                        
                        val errorMessage = when {
                            exception is FirebaseAuthException -> {
                                val errorCode = exception.errorCode
                                android.util.Log.d("FirebaseAuthHelper", "Error code: '$errorCode', Message: '${exception.message}'")
                                
                                // СТРОГАЯ проверка: только точные коды ошибок Firebase
                                when (errorCode) {
                                    // Точный код ошибки для существующего email (без дополнительных проверок)
                                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                                        "Пользователь с таким email уже зарегистрирован"
                                    }
                                    "ERROR_INVALID_EMAIL" -> {
                                        "Некорректный email адрес"
                                    }
                                    "ERROR_WEAK_PASSWORD" -> {
                                        "Пароль слишком слабый. Используйте минимум 6 символов"
                                    }
                                    "ERROR_NETWORK_REQUEST_FAILED" -> {
                                        "Проверьте подключение к интернету"
                                    }
                                    "ERROR_OPERATION_NOT_ALLOWED" -> {
                                        "Операция не разрешена. Обратитесь к администратору"
                                    }
                                    else -> {
                                        // Для всех остальных ошибок показываем общее сообщение
                                        // НЕ показываем сообщение о существующем пользователе
                                        val msg = exception.localizedMessage ?: exception.message ?: "Ошибка регистрации"
                                        android.util.Log.d("FirebaseAuthHelper", "Unknown error code, showing: $msg")
                                        msg
                                    }
                                }
                            }
                            exception?.message?.contains("network", ignoreCase = true) == true -> {
                                "Проверьте подключение к интернету"
                            }
                            else -> {
                                val msg = exception?.localizedMessage ?: exception?.message ?: "Ошибка регистрации"
                                android.util.Log.d("FirebaseAuthHelper", "Non-FirebaseAuthException, showing: $msg")
                                msg
                            }
                        }
                        onError(errorMessage)
                    }
                }
        } catch (e: Exception) {
            onError("Ошибка: ${e.message ?: "Неизвестная ошибка"}")
        }
    }
    
    // Вход пользователя
    fun signIn(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            onError("Email и пароль не могут быть пустыми")
            return
        }
        
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            onSuccess(user)
                        } else {
                            onError("Пользователь не найден")
                        }
                    } else {
                        val exception = task.exception
                        val errorMessage = when {
                            exception?.message?.contains("network") == true -> "Проверьте подключение к интернету"
                            exception?.message?.contains("password") == true -> "Неверный email или пароль"
                            exception?.message?.contains("user") == true -> "Пользователь не найден"
                            else -> exception?.message ?: "Ошибка входа"
                        }
                        onError(errorMessage)
                    }
                }
                .addOnFailureListener { e ->
                    val errorMessage = when {
                        e.message?.contains("network") == true -> "Проверьте подключение к интернету"
                        e.message?.contains("password") == true -> "Неверный email или пароль"
                        else -> e.message ?: "Ошибка входа"
                    }
                    onError(errorMessage)
                }
        } catch (e: Exception) {
            onError("Ошибка: ${e.message ?: "Неизвестная ошибка"}")
        }
    }
    
    // Выход пользователя
    fun signOut() {
        auth.signOut()
    }
    
    // Получить текущего пользователя
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    // Проверить, авторизован ли пользователь
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    // Получить email текущего пользователя
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    
    // Получить имя текущего пользователя
    fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }
    
    // Получить ID текущего пользователя
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}

