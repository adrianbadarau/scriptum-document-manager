package com.adrianb.scriptum.document_manager.service

import com.adrianb.scriptum.document_manager.domain.AppDocument
import com.adrianb.scriptum.document_manager.repository.AppDocumentRepository
import org.slf4j.LoggerFactory

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

import java.util.Optional

/**
 * Service Implementation for managing [AppDocument].
 */
@Service
class AppDocumentService(
    private val appDocumentRepository: AppDocumentRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a appDocument.
     *
     * @param appDocument the entity to save.
     * @return the persisted entity.
     */
    fun save(appDocument: AppDocument): AppDocument {
        log.debug("Request to save AppDocument : {}", appDocument)
        return appDocumentRepository.save(appDocument)
    }

    /**
     * Get all the appDocuments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<AppDocument> {
        log.debug("Request to get all AppDocuments")
        return appDocumentRepository.findAll(pageable)
    }

    /**
     * Get all the appDocuments with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    fun findAllWithEagerRelationships(pageable: Pageable) =
        appDocumentRepository.findAllWithEagerRelationships(pageable)


    /**
     * Get one appDocument by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: String): Optional<AppDocument> {
        log.debug("Request to get AppDocument : {}", id)
        return appDocumentRepository.findOneWithEagerRelationships(id)
    }

    /**
     * Delete the appDocument by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: String) {
        log.debug("Request to delete AppDocument : {}", id)

        appDocumentRepository.deleteById(id)
    }
}
