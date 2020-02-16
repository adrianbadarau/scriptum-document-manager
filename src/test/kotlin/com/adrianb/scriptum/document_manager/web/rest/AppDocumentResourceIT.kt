package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.DocumentmanagerApp
import com.adrianb.scriptum.document_manager.domain.AppDocument
import com.adrianb.scriptum.document_manager.domain.Category
import com.adrianb.scriptum.document_manager.repository.AppDocumentRepository
import com.adrianb.scriptum.document_manager.service.AppDocumentService
import com.adrianb.scriptum.document_manager.web.rest.errors.ExceptionTranslator

import kotlin.test.assertNotNull

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.Base64Utils
import org.springframework.validation.Validator


import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Integration tests for the [AppDocumentResource] REST controller.
 *
 * @see AppDocumentResource
 */
@SpringBootTest(classes = [DocumentmanagerApp::class])
class AppDocumentResourceIT {

    @Autowired
    private lateinit var appDocumentRepository: AppDocumentRepository

    @Mock
    private lateinit var appDocumentRepositoryMock: AppDocumentRepository

    @Mock
    private lateinit var appDocumentServiceMock: AppDocumentService

    @Autowired
    private lateinit var appDocumentService: AppDocumentService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restAppDocumentMockMvc: MockMvc

    private lateinit var appDocument: AppDocument

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val appDocumentResource = AppDocumentResource(appDocumentService)
        this.restAppDocumentMockMvc = MockMvcBuilders.standaloneSetup(appDocumentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        appDocumentRepository.deleteAll()
        appDocument = createEntity()
    }

    @Test
    @Throws(Exception::class)
    fun createAppDocument() {
        val databaseSizeBeforeCreate = appDocumentRepository.findAll().size

        // Create the AppDocument
        restAppDocumentMockMvc.perform(
            post("/api/app-documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(appDocument))
        ).andExpect(status().isCreated)

        // Validate the AppDocument in the database
        val appDocumentList = appDocumentRepository.findAll()
        assertThat(appDocumentList).hasSize(databaseSizeBeforeCreate + 1)
        val testAppDocument = appDocumentList[appDocumentList.size - 1]
        assertThat(testAppDocument.content).isEqualTo(DEFAULT_CONTENT)
        assertThat(testAppDocument.documentLink).isEqualTo(DEFAULT_DOCUMENT_LINK)
        assertThat(testAppDocument.blob).isEqualTo(DEFAULT_BLOB)
        assertThat(testAppDocument.blobContentType).isEqualTo(DEFAULT_BLOB_CONTENT_TYPE)
    }

    @Test
    fun createAppDocumentWithExistingId() {
        val databaseSizeBeforeCreate = appDocumentRepository.findAll().size

        // Create the AppDocument with an existing ID
        appDocument.id = "existing_id"

        // An entity with an existing ID cannot be created, so this API call must fail
        restAppDocumentMockMvc.perform(
            post("/api/app-documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(appDocument))
        ).andExpect(status().isBadRequest)

        // Validate the AppDocument in the database
        val appDocumentList = appDocumentRepository.findAll()
        assertThat(appDocumentList).hasSize(databaseSizeBeforeCreate)
    }


    @Test
    fun checkContentIsRequired() {
        val databaseSizeBeforeTest = appDocumentRepository.findAll().size
        // set the field null
        appDocument.content = null

        // Create the AppDocument, which fails.

        restAppDocumentMockMvc.perform(
            post("/api/app-documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(appDocument))
        ).andExpect(status().isBadRequest)

        val appDocumentList = appDocumentRepository.findAll()
        assertThat(appDocumentList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun checkDocumentLinkIsRequired() {
        val databaseSizeBeforeTest = appDocumentRepository.findAll().size
        // set the field null
        appDocument.documentLink = null

        // Create the AppDocument, which fails.

        restAppDocumentMockMvc.perform(
            post("/api/app-documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(appDocument))
        ).andExpect(status().isBadRequest)

        val appDocumentList = appDocumentRepository.findAll()
        assertThat(appDocumentList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun getAllAppDocuments() {
        // Initialize the database
        appDocumentRepository.save(appDocument)

        // Get all the appDocumentList
        restAppDocumentMockMvc.perform(get("/api/app-documents?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appDocument.id)))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].documentLink").value(hasItem(DEFAULT_DOCUMENT_LINK)))
            .andExpect(jsonPath("$.[*].blobContentType").value(hasItem(DEFAULT_BLOB_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].blob").value(hasItem(Base64Utils.encodeToString(DEFAULT_BLOB))))
    }
    
    @Suppress("unchecked")
    fun getAllAppDocumentsWithEagerRelationshipsIsEnabled() {
        val appDocumentResource = AppDocumentResource(appDocumentServiceMock)
        `when`(appDocumentServiceMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        val restAppDocumentMockMvc = MockMvcBuilders.standaloneSetup(appDocumentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build()

        restAppDocumentMockMvc.perform(get("/api/app-documents?eagerload=true"))
            .andExpect(status().isOk)

        verify(appDocumentServiceMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Suppress("unchecked")
    fun getAllAppDocumentsWithEagerRelationshipsIsNotEnabled() {
        val appDocumentResource = AppDocumentResource(appDocumentServiceMock)
            `when`(appDocumentServiceMock.findAllWithEagerRelationships(any())).thenReturn( PageImpl( mutableListOf()))
        val restAppDocumentMockMvc = MockMvcBuilders.standaloneSetup(appDocumentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build()

        restAppDocumentMockMvc.perform(get("/api/app-documents?eagerload=true"))
            .andExpect(status().isOk)

        verify(appDocumentServiceMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Test
    fun getAppDocument() {
        // Initialize the database
        appDocumentRepository.save(appDocument)

        val id = appDocument.id
        assertNotNull(id)

        // Get the appDocument
        restAppDocumentMockMvc.perform(get("/api/app-documents/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.documentLink").value(DEFAULT_DOCUMENT_LINK))
            .andExpect(jsonPath("$.blobContentType").value(DEFAULT_BLOB_CONTENT_TYPE))
            .andExpect(jsonPath("$.blob").value(Base64Utils.encodeToString(DEFAULT_BLOB)))
    }

    @Test
    fun getNonExistingAppDocument() {
        // Get the appDocument
        restAppDocumentMockMvc.perform(get("/api/app-documents/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    fun updateAppDocument() {
        // Initialize the database
        appDocumentService.save(appDocument)

        val databaseSizeBeforeUpdate = appDocumentRepository.findAll().size

        // Update the appDocument
        val id = appDocument.id
        assertNotNull(id)
        val updatedAppDocument = appDocumentRepository.findById(id).get()
        updatedAppDocument.content = UPDATED_CONTENT
        updatedAppDocument.documentLink = UPDATED_DOCUMENT_LINK
        updatedAppDocument.blob = UPDATED_BLOB
        updatedAppDocument.blobContentType = UPDATED_BLOB_CONTENT_TYPE

        restAppDocumentMockMvc.perform(
            put("/api/app-documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedAppDocument))
        ).andExpect(status().isOk)

        // Validate the AppDocument in the database
        val appDocumentList = appDocumentRepository.findAll()
        assertThat(appDocumentList).hasSize(databaseSizeBeforeUpdate)
        val testAppDocument = appDocumentList[appDocumentList.size - 1]
        assertThat(testAppDocument.content).isEqualTo(UPDATED_CONTENT)
        assertThat(testAppDocument.documentLink).isEqualTo(UPDATED_DOCUMENT_LINK)
        assertThat(testAppDocument.blob).isEqualTo(UPDATED_BLOB)
        assertThat(testAppDocument.blobContentType).isEqualTo(UPDATED_BLOB_CONTENT_TYPE)
    }

    @Test
    fun updateNonExistingAppDocument() {
        val databaseSizeBeforeUpdate = appDocumentRepository.findAll().size

        // Create the AppDocument

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppDocumentMockMvc.perform(
            put("/api/app-documents")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(appDocument))
        ).andExpect(status().isBadRequest)

        // Validate the AppDocument in the database
        val appDocumentList = appDocumentRepository.findAll()
        assertThat(appDocumentList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    fun deleteAppDocument() {
        // Initialize the database
        appDocumentService.save(appDocument)

        val databaseSizeBeforeDelete = appDocumentRepository.findAll().size

        val id = appDocument.id
        assertNotNull(id)

        // Delete the appDocument
        restAppDocumentMockMvc.perform(
            delete("/api/app-documents/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val appDocumentList = appDocumentRepository.findAll()
        assertThat(appDocumentList).hasSize(databaseSizeBeforeDelete - 1)
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
        fun createEntity(): AppDocument {
            val appDocument = AppDocument(
                content = DEFAULT_CONTENT,
                documentLink = DEFAULT_DOCUMENT_LINK,
                blob = DEFAULT_BLOB,
                blobContentType = DEFAULT_BLOB_CONTENT_TYPE
            )

            // Add required entity
            val category: Category
            category = CategoryResourceIT.createEntity()
            category.id = "fixed-id-for-tests"
            appDocument.category = category
            return appDocument
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(): AppDocument {
            val appDocument = AppDocument(
                content = UPDATED_CONTENT,
                documentLink = UPDATED_DOCUMENT_LINK,
                blob = UPDATED_BLOB,
                blobContentType = UPDATED_BLOB_CONTENT_TYPE
            )

            // Add required entity
            val category: Category
            category = CategoryResourceIT.createUpdatedEntity()
            category.id = "fixed-id-for-tests"
            appDocument.category = category
            return appDocument
        }
    }
}
