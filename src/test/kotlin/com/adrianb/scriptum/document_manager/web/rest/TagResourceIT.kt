package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.DocumentmanagerApp
import com.adrianb.scriptum.document_manager.domain.Tag
import com.adrianb.scriptum.document_manager.repository.TagRepository
import com.adrianb.scriptum.document_manager.service.TagService
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
 * Integration tests for the [TagResource] REST controller.
 *
 * @see TagResource
 */
@SpringBootTest(classes = [DocumentmanagerApp::class])
class TagResourceIT {

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var tagService: TagService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restTagMockMvc: MockMvc

    private lateinit var tag: Tag

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val tagResource = TagResource(tagService)
        this.restTagMockMvc = MockMvcBuilders.standaloneSetup(tagResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        tagRepository.deleteAll()
        tag = createEntity()
    }

    @Test
    @Throws(Exception::class)
    fun createTag() {
        val databaseSizeBeforeCreate = tagRepository.findAll().size

        // Create the Tag
        restTagMockMvc.perform(
            post("/api/tags")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(tag))
        ).andExpect(status().isCreated)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeCreate + 1)
        val testTag = tagList[tagList.size - 1]
        assertThat(testTag.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    fun createTagWithExistingId() {
        val databaseSizeBeforeCreate = tagRepository.findAll().size

        // Create the Tag with an existing ID
        tag.id = "existing_id"

        // An entity with an existing ID cannot be created, so this API call must fail
        restTagMockMvc.perform(
            post("/api/tags")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(tag))
        ).andExpect(status().isBadRequest)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeCreate)
    }


    @Test
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = tagRepository.findAll().size
        // set the field null
        tag.name = null

        // Create the Tag, which fails.

        restTagMockMvc.perform(
            post("/api/tags")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(tag))
        ).andExpect(status().isBadRequest)

        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun getAllTags() {
        // Initialize the database
        tagRepository.save(tag)

        // Get all the tagList
        restTagMockMvc.perform(get("/api/tags?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tag.id)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
    }
    
    @Test
    fun getTag() {
        // Initialize the database
        tagRepository.save(tag)

        val id = tag.id
        assertNotNull(id)

        // Get the tag
        restTagMockMvc.perform(get("/api/tags/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
    }

    @Test
    fun getNonExistingTag() {
        // Get the tag
        restTagMockMvc.perform(get("/api/tags/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    fun updateTag() {
        // Initialize the database
        tagService.save(tag)

        val databaseSizeBeforeUpdate = tagRepository.findAll().size

        // Update the tag
        val id = tag.id
        assertNotNull(id)
        val updatedTag = tagRepository.findById(id).get()
        updatedTag.name = UPDATED_NAME

        restTagMockMvc.perform(
            put("/api/tags")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedTag))
        ).andExpect(status().isOk)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
        val testTag = tagList[tagList.size - 1]
        assertThat(testTag.name).isEqualTo(UPDATED_NAME)
    }

    @Test
    fun updateNonExistingTag() {
        val databaseSizeBeforeUpdate = tagRepository.findAll().size

        // Create the Tag

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTagMockMvc.perform(
            put("/api/tags")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(tag))
        ).andExpect(status().isBadRequest)

        // Validate the Tag in the database
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    fun deleteTag() {
        // Initialize the database
        tagService.save(tag)

        val databaseSizeBeforeDelete = tagRepository.findAll().size

        val id = tag.id
        assertNotNull(id)

        // Delete the tag
        restTagMockMvc.perform(
            delete("/api/tags/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val tagList = tagRepository.findAll()
        assertThat(tagList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(): Tag {
            val tag = Tag(
                name = DEFAULT_NAME
            )

            return tag
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(): Tag {
            val tag = Tag(
                name = UPDATED_NAME
            )

            return tag
        }
    }
}
