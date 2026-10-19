package com.example.keilotechnologystore

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Representa los diferentes estados de la autenticación.
 */
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val user: FirebaseUser?) : AuthState
    data class Error(val message: String) : AuthState
}

/**
 * ViewModel para gestionar el registro, inicio de sesión y estado del usuario con Firebase Authentication.
 */
class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Obtiene el usuario de Firebase actual si existe.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    init {
        // Si el usuario ya tiene una sesión activa al iniciar el ViewModel, actualizamos el estado a Success.
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser)
        }
    }

    /**
     * Registra un nuevo usuario con correo y contraseña en Firebase Auth.
     */
    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("El correo electrónico y la contraseña no pueden estar vacíos.")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(auth.currentUser)
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Error desconocido al registrar el usuario."
                    )
                }
            }
    }

    /**
     * Inicia sesión de un usuario existente con correo y contraseña en Firebase Auth.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("El correo electrónico y la contraseña no pueden estar vacíos.")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(auth.currentUser)
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Error desconocido al iniciar sesión."
                    )
                }
            }
    }

    /**
     * Cierra la sesión del usuario actual y reinicia el estado.
     */
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    /**
     * Restablece el estado de autenticación a Idle.
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
