package com.adrianb.scriptum.document_manager.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.DataStore
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStreamReader
import java.io.Serializable
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

@Service
class GoogleDriveService() {
    companion object {
        val HTTP_TRANSPORT = NetHttpTransport()
        val SCOPES = listOf(DriveScopes.DRIVE)
        val JSON_FACTORY = JacksonFactory.getDefaultInstance()
    }

    @Value("\${google.oauth.callback.uri}")
    private lateinit var callbackUri: String

    @Value("\${google.secret.key.path}")
    private lateinit var secretKey: Resource

    @Value("\${google.credentials.folder.path}")
    private lateinit var credentialsFolder: Resource

    private lateinit var flow: GoogleAuthorizationCodeFlow

    private lateinit var dataStoreFactory: FileDataStoreFactory

    private val drive: Drive by lazy {
        Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials()).setApplicationName("Scriptum-Document-Manager").build()
    }

    val userID = "DUMMY_USER"

    @PostConstruct
    fun postConstruct() {
        val secrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(secretKey.inputStream))
        dataStoreFactory = FileDataStoreFactory(credentialsFolder.file)
        flow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES).setDataStoreFactory(dataStoreFactory).build()
    }

    fun authenticate(): String {
        val url = flow.newAuthorizationUrl()
        return url.setRedirectUri(callbackUri).setAccessType("offline").build()
    }

    fun exchangeCodeForTokens(code: String) {
        val tokenResponse = flow.newTokenRequest(code).setRedirectUri(callbackUri).execute()
        flow.createAndStoreCredential(tokenResponse, userID)
    }

    fun logOut(request: HttpServletRequest) {
        dataStoreFactory.getDataStore<Serializable>(credentialsFolder.filename).clear()
    }

    fun getCredentials(): Credential? {
        return flow.loadCredential(userID)
    }

    fun isAuthenticated(): Boolean {
        getCredentials()?.let {
            return it.refreshToken()
        }
        return false
    }

    fun uploadFile(file: MultipartFile) {
        val tempFile = File("/temp", file.originalFilename!!)
        file.transferTo(tempFile)
        val content = FileContent(file.contentType, tempFile)
        val toDrive = com.google.api.services.drive.model.File()
        toDrive.name = file.originalFilename
        drive.files().create(toDrive, content).setFields("id").execute()

    }

}
