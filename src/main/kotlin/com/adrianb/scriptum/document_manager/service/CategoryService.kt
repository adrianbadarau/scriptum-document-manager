package com.adrianb.scriptum.document_manager.service

import com.adrianb.scriptum.document_manager.domain.Category
import com.adrianb.scriptum.document_manager.repository.CategoryRepository
import org.slf4j.LoggerFactory

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

import java.util.Optional

/**
 * Service Implementation for managing [Category].
 */
@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a category.
     *
     * @param category the entity to save.
     * @return the persisted entity.
     */
    fun save(category: Category): Category {
        log.debug("Request to save Category : {}", category)
        return categoryRepository.save(category)
    }

    /**
     * Get all the categories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Category> {
        log.debug("Request to get all Categories")
        return categoryRepository.findAll(pageable)
    }

    /**
     * Get one category by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: String): Optional<Category> {
        log.debug("Request to get Category : {}", id)
        return categoryRepository.findById(id)
    }

    /**
     * Delete the category by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: String) {
        log.debug("Request to delete Category : {}", id)

        categoryRepository.deleteById(id)
    }
}
