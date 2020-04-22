package com.adrianb.scriptum.document_manager.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.docs.v1.Docs
import com.google.api.services.docs.v1.model.ParagraphElement
import com.google.api.services.docs.v1.model.StructuralElement
import com.google.api.services.drive.DriveScopes
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.InputStreamReader
import javax.annotation.PostConstruct

@Service
class GoogleDocsService {
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

    val userID = "DUMMY_USER"

    private val docs: Docs by lazy {
        Docs.Builder(GoogleDriveService.HTTP_TRANSPORT, GoogleDriveService.JSON_FACTORY, getCredentials()).setApplicationName("Scriptum-Document-Manager").build()
    }

    @PostConstruct
    fun postConstruct() {
        val secrets = GoogleClientSecrets.load(GoogleDriveService.JSON_FACTORY, InputStreamReader(secretKey.inputStream))
        dataStoreFactory = FileDataStoreFactory(credentialsFolder.file)
        flow = GoogleAuthorizationCodeFlow.Builder(GoogleDriveService.HTTP_TRANSPORT, GoogleDriveService.JSON_FACTORY, secrets, GoogleDriveService.SCOPES).setDataStoreFactory(dataStoreFactory).build()
    }

    fun getCredentials(): Credential? {
        return flow.loadCredential(userID)
    }

    private fun readParagraphElement(element: ParagraphElement): String {
        return element?.textRun?.content ?: ""
    }

    private fun readStructuralElements(elements: List<StructuralElement>): String {
        val builder = StringBuilder()
        elements.forEach { structuralElement ->
            structuralElement?.paragraph?.elements?.forEach {
                builder.append(readParagraphElement(it))
            }
            structuralElement?.table?.tableRows?.forEach { tableRow ->
                tableRow.tableCells.forEach {
                    builder.append(it.content)
                }
            }
            structuralElement?.tableOfContents?.let {
                builder.append(readStructuralElements(it.content))
            }
        }
        return builder.toString()
    }

    fun readDocumentById(docId: String): String {
        val document = docs.documents().get(docId).execute()
        return readStructuralElements(document.body.content)
    }

}
