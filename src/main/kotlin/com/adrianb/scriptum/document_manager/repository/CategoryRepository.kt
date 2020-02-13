package com.adrianb.scriptum.document_manager.repository

import com.adrianb.scriptum.document_manager.domain.Category
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data MongoDB repository for the [Category] entity.
 */
@Suppress("unused")
@Repository
interface CategoryRepository : MongoRepository<Category, String> {
}
