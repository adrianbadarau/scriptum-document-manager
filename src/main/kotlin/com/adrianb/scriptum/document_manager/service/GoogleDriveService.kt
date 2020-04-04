package com.adrianb.scriptum.document_manager.service

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import javax.annotation.PostConstruct

@Service
class GoogleDriveService() {
    companion object {
        val HTTP_TRANSPORT = NetHttpTransport()
        val SCOPES = listOf(DriveScopes.DRIVE)
        val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    }

    @Value("\${google.oauth.callback.uri}")
    lateinit var callbackUri: String

    @Value("\${google.secret.key.path}")
    lateinit var secretKey: Resource

    @Value("\${google.oauth.callback.uri}")
    lateinit var credentialsFolder: Resource

    lateinit var flow: GoogleAuthorizationCodeFlow

    val userID = "DUMMY_USER"

    @PostConstruct
    fun postConstruct() {
        val secrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(secretKey.inputStream))
        flow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES).setDataStoreFactory(
            FileDataStoreFactory(credentialsFolder.file)
        ).build()
    }

    fun authenticate(): String {
        val url = flow.newAuthorizationUrl()
        return url.setRedirectUri(callbackUri).setAccessType("offline").build()
    }

    fun exchangeCodeForTokens(code: String) {
        val tokenResponse = flow.newTokenRequest(code).setRedirectUri(callbackUri).execute()
        flow.createAndStoreCredential(tokenResponse, userID)
    }

}
