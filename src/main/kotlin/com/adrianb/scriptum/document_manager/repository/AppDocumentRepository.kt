package com.adrianb.scriptum.document_manager.repository

import com.adrianb.scriptum.document_manager.domain.AppDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

import java.util.Optional

/**
 * Spring Data MongoDB repository for the [AppDocument] entity.
 */
@Repository
interface AppDocumentRepository : MongoRepository<AppDocument, String> {

    @Query("{}")
    fun findAllWithEagerRelationships(pageable: Pageable): Page<AppDocument>

    @Query("{}")
    fun findAllWithEagerRelationships(): MutableList<AppDocument>

    @Query("{'id': ?0}")
    fun findOneWithEagerRelationships(id: String): Optional<AppDocument>
}
