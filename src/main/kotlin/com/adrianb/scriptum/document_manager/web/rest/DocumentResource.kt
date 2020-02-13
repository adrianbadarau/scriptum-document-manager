package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.domain.Document
import com.adrianb.scriptum.document_manager.service.DocumentService
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

private const val ENTITY_NAME = "documentmanagerDocument"
/**
 * REST controller for managing [com.adrianb.scriptum.document_manager.domain.Document].
 */
@RestController
@RequestMapping("/api")
class DocumentResource(
    private val documentService: DocumentService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /documents` : Create a new document.
     *
     * @param document the document to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new document, or with status `400 (Bad Request)` if the document has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/documents")
    fun createDocument(@Valid @RequestBody document: Document): ResponseEntity<Document> {
        log.debug("REST request to save Document : {}", document)
        if (document.id != null) {
            throw BadRequestAlertException(
                "A new document cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = documentService.save(document)
        return ResponseEntity.created(URI("/api/documents/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /documents` : Updates an existing document.
     *
     * @param document the document to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated document,
     * or with status `400 (Bad Request)` if the document is not valid,
     * or with status `500 (Internal Server Error)` if the document couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/documents")
    fun updateDocument(@Valid @RequestBody document: Document): ResponseEntity<Document> {
        log.debug("REST request to update Document : {}", document)
        if (document.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = documentService.save(document)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     document.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /documents` : get all the documents.
     *

     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of documents in body.
     */
    @GetMapping("/documents")    
    fun getAllDocuments(
        pageable: Pageable
    ) : ResponseEntity<MutableList<Document>> {
        log.debug("REST request to get a page of Documents")
        val page = documentService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /documents/:id` : get the "id" document.
     *
     * @param id the id of the document to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the document, or with status `404 (Not Found)`.
     */
    @GetMapping("/documents/{id}")
    fun getDocument(@PathVariable id: String): ResponseEntity<Document> {
        log.debug("REST request to get Document : {}", id)
        val document = documentService.findOne(id)
        return ResponseUtil.wrapOrNotFound(document)
    }
    /**
     *  `DELETE  /documents/:id` : delete the "id" document.
     *
     * @param id the id of the document to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/documents/{id}")
    fun deleteDocument(@PathVariable id: String): ResponseEntity<Void> {
        log.debug("REST request to delete Document : {}", id)
        documentService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build()
    }
}
