package com.adrianb.scriptum.document_manager.repository

import com.adrianb.scriptum.document_manager.domain.Document
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data MongoDB repository for the [Document] entity.
 */
@Suppress("unused")
@Repository
interface DocumentRepository : MongoRepository<Document, String> {
}
