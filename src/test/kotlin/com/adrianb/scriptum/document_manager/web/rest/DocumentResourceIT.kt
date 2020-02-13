package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.DocumentmanagerApp
import com.adrianb.scriptum.document_manager.domain.Document
import com.adrianb.scriptum.document_manager.repository.DocumentRepository
import com.adrianb.scriptum.document_manager.service.DocumentService
import com.adrianb.scriptum.document_manager.web.rest.errors.ExceptionTranslator

import kotlin.test.assertNotNull

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.Base64Utils
import org.springframework.validation.Validator


import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Integration tests for the [DocumentResource] REST controller.
 *
 * @see DocumentResource
 */
@SpringBootTest(classes = [DocumentmanagerApp::class])
class DocumentResourceIT {

    @Autowired
    private lateinit var documentRepository: DocumentRepository

    @Autowired
    private lateinit var documentService: DocumentService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restDocumentMockMvc: MockMvc

    private lateinit var document: Document

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val documentResource = DocumentResource(documentService)
        this.restDocumentMockMvc = MockMvcBuilders.standaloneSetup(documentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        documentRepository.deleteAll()
        document = createEntity()
    }

    @Test
    @Throws(Exception::class)
    fun createDocument() {
        val databaseSizeBeforeCreate = documentRepository.findAll().size

        // Create the Document
        restDocumentMockMvc.perform(
            post("/api/documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(document))
        ).andExpect(status().isCreated)

        // Validate the Document in the database
        val documentList = documentRepository.findAll()
        assertThat(documentList).hasSize(databaseSizeBeforeCreate + 1)
        val testDocument = documentList[documentList.size - 1]
        assertThat(testDocument.content).isEqualTo(DEFAULT_CONTENT)
        assertThat(testDocument.documentLink).isEqualTo(DEFAULT_DOCUMENT_LINK)
        assertThat(testDocument.blob).isEqualTo(DEFAULT_BLOB)
        assertThat(testDocument.blobContentType).isEqualTo(DEFAULT_BLOB_CONTENT_TYPE)
    }

    @Test
    fun createDocumentWithExistingId() {
        val databaseSizeBeforeCreate = documentRepository.findAll().size

        // Create the Document with an existing ID
        document.id = "existing_id"

        // An entity with an existing ID cannot be created, so this API call must fail
        restDocumentMockMvc.perform(
            post("/api/documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(document))
        ).andExpect(status().isBadRequest)

        // Validate the Document in the database
        val documentList = documentRepository.findAll()
        assertThat(documentList).hasSize(databaseSizeBeforeCreate)
    }


    @Test
    fun checkContentIsRequired() {
        val databaseSizeBeforeTest = documentRepository.findAll().size
        // set the field null
        document.content = null

        // Create the Document, which fails.

        restDocumentMockMvc.perform(
            post("/api/documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(document))
        ).andExpect(status().isBadRequest)

        val documentList = documentRepository.findAll()
        assertThat(documentList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun checkDocumentLinkIsRequired() {
        val databaseSizeBeforeTest = documentRepository.findAll().size
        // set the field null
        document.documentLink = null

        // Create the Document, which fails.

        restDocumentMockMvc.perform(
            post("/api/documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(document))
        ).andExpect(status().isBadRequest)

        val documentList = documentRepository.findAll()
        assertThat(documentList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun getAllDocuments() {
        // Initialize the database
        documentRepository.save(document)

        // Get all the documentList
        restDocumentMockMvc.perform(get("/api/documents?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(document.id)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].documentLink").value(hasItem(DEFAULT_DOCUMENT_LINK)))
            .andExpect(jsonPath("$.[*].blobContentType").value(hasItem(DEFAULT_BLOB_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].blob").value(hasItem(Base64Utils.encodeToString(DEFAULT_BLOB))))
    }
    
    @Test
    fun getDocument() {
        // Initialize the database
        documentRepository.save(document)

        val id = document.id
        assertNotNull(id)

        // Get the document
        restDocumentMockMvc.perform(get("/api/documents/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.documentLink").value(DEFAULT_DOCUMENT_LINK))
            .andExpect(jsonPath("$.blobContentType").value(DEFAULT_BLOB_CONTENT_TYPE))
            .andExpect(jsonPath("$.blob").value(Base64Utils.encodeToString(DEFAULT_BLOB)))
    }

    @Test
    fun getNonExistingDocument() {
        // Get the document
        restDocumentMockMvc.perform(get("/api/documents/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    fun updateDocument() {
        // Initialize the database
        documentService.save(document)

        val databaseSizeBeforeUpdate = documentRepository.findAll().size

        // Update the document
        val id = document.id
        assertNotNull(id)
        val updatedDocument = documentRepository.findById(id).get()
        updatedDocument.content = UPDATED_CONTENT
        updatedDocument.documentLink = UPDATED_DOCUMENT_LINK
        updatedDocument.blob = UPDATED_BLOB
        updatedDocument.blobContentType = UPDATED_BLOB_CONTENT_TYPE

        restDocumentMockMvc.perform(
            put("/api/documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedDocument))
        ).andExpect(status().isOk)

        // Validate the Document in the database
        val documentList = documentRepository.findAll()
        assertThat(documentList).hasSize(databaseSizeBeforeUpdate)
        val testDocument = documentList[documentList.size - 1]
        assertThat(testDocument.content).isEqualTo(UPDATED_CONTENT)
        assertThat(testDocument.documentLink).isEqualTo(UPDATED_DOCUMENT_LINK)
        assertThat(testDocument.blob).isEqualTo(UPDATED_BLOB)
        assertThat(testDocument.blobContentType).isEqualTo(UPDATED_BLOB_CONTENT_TYPE)
    }

    @Test
    fun updateNonExistingDocument() {
        val databaseSizeBeforeUpdate = documentRepository.findAll().size

        // Create the Document

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentMockMvc.perform(
            put("/api/documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(document))
        ).andExpect(status().isBadRequest)

        // Validate the Document in the database
        val documentList = documentRepository.findAll()
        assertThat(documentList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    fun deleteDocument() {
        // Initialize the database
        documentService.save(document)

        val databaseSizeBeforeDelete = documentRepository.findAll().size

        val id = document.id
        assertNotNull(id)

        // Delete the document
        restDocumentMockMvc.perform(
            delete("/api/documents/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val documentList = documentRepository.findAll()
        assertThat(documentList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_CONTENT = "AAAAAAAAAA"
        private const val UPDATED_CONTENT = "BBBBBBBBBB"

        private const val DEFAULT_DOCUMENT_LINK = "AAAAAAAAAA"
        private const val UPDATED_DOCUMENT_LINK = "BBBBBBBBBB"

        private val DEFAULT_BLOB: ByteArray = createByteArray(1, "0")
        private val UPDATED_BLOB: ByteArray = createByteArray(1, "1")
        private const val DEFAULT_BLOB_CONTENT_TYPE: String = "image/jpg"
        private const val UPDATED_BLOB_CONTENT_TYPE: String = "image/png"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(): Document {
            val document = Document(
                content = DEFAULT_CONTENT,
                documentLink = DEFAULT_DOCUMENT_LINK,
                blob = DEFAULT_BLOB,
                blobContentType = DEFAULT_BLOB_CONTENT_TYPE
            )

            return document
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(): Document {
            val document = Document(
                content = UPDATED_CONTENT,
                documentLink = UPDATED_DOCUMENT_LINK,
                blob = UPDATED_BLOB,
                blobContentType = UPDATED_BLOB_CONTENT_TYPE
            )

            return document
        }
    }
}
