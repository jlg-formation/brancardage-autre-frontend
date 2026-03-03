package com.example.huybrancardage.data.remote

/**
 * Wrapper pour les résultats d'appels réseau
 */
sealed class NetworkResult<out T> {
    /**
     * Succès avec données
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * Erreur avec message
     */
    data class Error(
        val code: String,
        val message: String,
        val httpCode: Int? = null
    ) : NetworkResult<Nothing>()

    /**
     * Chargement en cours
     */
    data object Loading : NetworkResult<Nothing>()

    /**
     * Vérifie si le résultat est un succès
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Récupère les données si succès, null sinon
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Récupère les données si succès, valeur par défaut sinon
     */
    fun getOrDefault(default: @UnsafeVariance T): T = getOrNull() ?: default
}

