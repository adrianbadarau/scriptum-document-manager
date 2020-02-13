package com.adrianb.scriptum.document_manager.service

import com.adrianb.scriptum.document_manager.domain.Document
import com.adrianb.scriptum.document_manager.repository.DocumentRepository
import org.slf4j.LoggerFactory

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

import java.util.Optional

/**
 * Service Implementation for managing [Document].
 */
@Service
class DocumentService(
    private val documentRepository: DocumentRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a document.
     *
     * @param document the entity to save.
     * @return the persisted entity.
     */
    fun save(document: Document): Document {
        log.debug("Request to save Document : {}", document)
        return documentRepository.save(document)
    }

    /**
     * Get all the documents.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Document> {
        log.debug("Request to get all Documents")
        return documentRepository.findAll(pageable)
    }

    /**
     * Get one document by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: String): Optional<Document> {
        log.debug("Request to get Document : {}", id)
        return documentRepository.findById(id)
    }

    /**
     * Delete the document by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: String) {
        log.debug("Request to delete Document : {}", id)

        documentRepository.deleteById(id)
    }
}
