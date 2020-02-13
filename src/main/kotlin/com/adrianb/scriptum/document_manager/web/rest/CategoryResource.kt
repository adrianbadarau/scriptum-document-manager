package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.domain.Category
import com.adrianb.scriptum.document_manager.service.CategoryService
import com.adrianb.scriptum.document_manager.web.rest.errors.BadRequestAlertException

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid
import java.net.URI
import java.net.URISyntaxException

private const val ENTITY_NAME = "documentmanagerCategory"
/**
 * REST controller for managing [com.adrianb.scriptum.document_manager.domain.Category].
 */
@RestController
@RequestMapping("/api")
class CategoryResource(
    private val categoryService: CategoryService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /categories` : Create a new category.
     *
     * @param category the category to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new category, or with status `400 (Bad Request)` if the category has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/categories")
    fun createCategory(@Valid @RequestBody category: Category): ResponseEntity<Category> {
        log.debug("REST request to save Category : {}", category)
        if (category.id != null) {
            throw BadRequestAlertException(
                "A new category cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = categoryService.save(category)
        return ResponseEntity.created(URI("/api/categories/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /categories` : Updates an existing category.
     *
     * @param category the category to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated category,
     * or with status `400 (Bad Request)` if the category is not valid,
     * or with status `500 (Internal Server Error)` if the category couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/categories")
    fun updateCategory(@Valid @RequestBody category: Category): ResponseEntity<Category> {
        log.debug("REST request to update Category : {}", category)
        if (category.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = categoryService.save(category)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     category.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /categories` : get all the categories.
     *

     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of categories in body.
     */
    @GetMapping("/categories")    
    fun getAllCategories(
        pageable: Pageable
    ) : ResponseEntity<MutableList<Category>> {
        log.debug("REST request to get a page of Categories")
        val page = categoryService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /categories/:id` : get the "id" category.
     *
     * @param id the id of the category to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the category, or with status `404 (Not Found)`.
     */
    @GetMapping("/categories/{id}")
    fun getCategory(@PathVariable id: String): ResponseEntity<Category> {
        log.debug("REST request to get Category : {}", id)
        val category = categoryService.findOne(id)
        return ResponseUtil.wrapOrNotFound(category)
    }
    /**
     *  `DELETE  /categories/:id` : delete the "id" category.
     *
     * @param id the id of the category to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/categories/{id}")
    fun deleteCategory(@PathVariable id: String): ResponseEntity<Void> {
        log.debug("REST request to delete Category : {}", id)
        categoryService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build()
    }
}
