package com.gymbaswara.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "session")

data class Session(
    val token: String?,
    val userId: String?,
    val email: String?,
    val name: String?
) {
    val isLoggedIn: Boolean get() = !token.isNullOrBlank()
}

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val NAME = stringPreferencesKey("name")
    }

    @Volatile
    private var cachedToken: String? = null

    val session: Flow<Session> = context.sessionDataStore.data.map { prefs ->
        Session(
            token = prefs[Keys.TOKEN],
            userId = prefs[Keys.USER_ID],
            email = prefs[Keys.EMAIL],
            name = prefs[Keys.NAME]
        )
    }

    // The OkHttp interceptor runs on a background thread; the token is cached in
    // memory after the first read so we do not hit DataStore on every request.
    fun tokenBlocking(): String? {
        cachedToken?.let { return it }
        val token = runBlocking { context.sessionDataStore.data.first()[Keys.TOKEN] }
        cachedToken = token
        return token
    }

    suspend fun save(token: String, userId: String, email: String, name: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = userId
            prefs[Keys.EMAIL] = email
            prefs[Keys.NAME] = name
        }
        cachedToken = token
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
        cachedToken = null
    }
}
