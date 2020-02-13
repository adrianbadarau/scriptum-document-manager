package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.DocumentmanagerApp
import com.adrianb.scriptum.document_manager.domain.Category
import com.adrianb.scriptum.document_manager.repository.CategoryRepository
import com.adrianb.scriptum.document_manager.service.CategoryService
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
 * Integration tests for the [CategoryResource] REST controller.
 *
 * @see CategoryResource
 */
@SpringBootTest(classes = [DocumentmanagerApp::class])
class CategoryResourceIT {

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var categoryService: CategoryService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restCategoryMockMvc: MockMvc

    private lateinit var category: Category

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val categoryResource = CategoryResource(categoryService)
        this.restCategoryMockMvc = MockMvcBuilders.standaloneSetup(categoryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        categoryRepository.deleteAll()
        category = createEntity()
    }

    @Test
    @Throws(Exception::class)
    fun createCategory() {
        val databaseSizeBeforeCreate = categoryRepository.findAll().size

        // Create the Category
        restCategoryMockMvc.perform(
            post("/api/categories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isCreated)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate + 1)
        val testCategory = categoryList[categoryList.size - 1]
        assertThat(testCategory.name).isEqualTo(DEFAULT_NAME)
    }

    @Test
    fun createCategoryWithExistingId() {
        val databaseSizeBeforeCreate = categoryRepository.findAll().size

        // Create the Category with an existing ID
        category.id = "existing_id"

        // An entity with an existing ID cannot be created, so this API call must fail
        restCategoryMockMvc.perform(
            post("/api/categories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isBadRequest)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate)
    }


    @Test
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = categoryRepository.findAll().size
        // set the field null
        category.name = null

        // Create the Category, which fails.

        restCategoryMockMvc.perform(
            post("/api/categories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isBadRequest)

        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    fun getAllCategories() {
        // Initialize the database
        categoryRepository.save(category)

        // Get all the categoryList
        restCategoryMockMvc.perform(get("/api/categories?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.id)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
    }
    
    @Test
    fun getCategory() {
        // Initialize the database
        categoryRepository.save(category)

        val id = category.id
        assertNotNull(id)

        // Get the category
        restCategoryMockMvc.perform(get("/api/categories/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
    }

    @Test
    fun getNonExistingCategory() {
        // Get the category
        restCategoryMockMvc.perform(get("/api/categories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    fun updateCategory() {
        // Initialize the database
        categoryService.save(category)

        val databaseSizeBeforeUpdate = categoryRepository.findAll().size

        // Update the category
        val id = category.id
        assertNotNull(id)
        val updatedCategory = categoryRepository.findById(id).get()
        updatedCategory.name = UPDATED_NAME

        restCategoryMockMvc.perform(
            put("/api/categories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(updatedCategory))
        ).andExpect(status().isOk)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
        val testCategory = categoryList[categoryList.size - 1]
        assertThat(testCategory.name).isEqualTo(UPDATED_NAME)
    }

    @Test
    fun updateNonExistingCategory() {
        val databaseSizeBeforeUpdate = categoryRepository.findAll().size

        // Create the Category

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(
            put("/api/categories")
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isBadRequest)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    fun deleteCategory() {
        // Initialize the database
        categoryService.save(category)

        val databaseSizeBeforeDelete = categoryRepository.findAll().size

        val id = category.id
        assertNotNull(id)

        // Delete the category
        restCategoryMockMvc.perform(
            delete("/api/categories/{id}", id)
                .accept(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeDelete - 1)
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
        fun createEntity(): Category {
            val category = Category(
                name = DEFAULT_NAME
            )

            return category
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(): Category {
            val category = Category(
                name = UPDATED_NAME
            )

            return category
        }
    }
}
