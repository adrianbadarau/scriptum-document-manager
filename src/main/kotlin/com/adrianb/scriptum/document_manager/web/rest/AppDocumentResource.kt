package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.domain.AppDocument
import com.adrianb.scriptum.document_manager.service.AppDocumentService
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

private const val ENTITY_NAME = "documentmanagerAppDocument"
/**
 * REST controller for managing [com.adrianb.scriptum.document_manager.domain.AppDocument].
 */
@RestController
@RequestMapping("/api")
class AppDocumentResource(
    private val appDocumentService: AppDocumentService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /app-documents` : Create a new appDocument.
     *
     * @param appDocument the appDocument to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new appDocument, or with status `400 (Bad Request)` if the appDocument has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/app-documents")
    fun createAppDocument(@Valid @RequestBody appDocument: AppDocument): ResponseEntity<AppDocument> {
        log.debug("REST request to save AppDocument : {}", appDocument)
        if (appDocument.id != null) {
            throw BadRequestAlertException(
                "A new appDocument cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = appDocumentService.save(appDocument)
        return ResponseEntity.created(URI("/api/app-documents/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /app-documents` : Updates an existing appDocument.
     *
     * @param appDocument the appDocument to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated appDocument,
     * or with status `400 (Bad Request)` if the appDocument is not valid,
     * or with status `500 (Internal Server Error)` if the appDocument couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/app-documents")
    fun updateAppDocument(@Valid @RequestBody appDocument: AppDocument): ResponseEntity<AppDocument> {
        log.debug("REST request to update AppDocument : {}", appDocument)
        if (appDocument.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = appDocumentService.save(appDocument)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     appDocument.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /app-documents` : get all the appDocuments.
     *

     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the [ResponseEntity] with status `200 (OK)` and the list of appDocuments in body.
     */
    @GetMapping("/app-documents")    
    fun getAllAppDocuments(
        pageable: Pageable,
        @RequestParam(required = false, defaultValue = "false") eagerload: Boolean
    ) : ResponseEntity<MutableList<AppDocument>> {
        log.debug("REST request to get a page of AppDocuments")
        val page: Page<AppDocument> = if (eagerload) {
            appDocumentService.findAllWithEagerRelationships(pageable)
        } else {
            appDocumentService.findAll(pageable)
        }
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /app-documents/:id` : get the "id" appDocument.
     *
     * @param id the id of the appDocument to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the appDocument, or with status `404 (Not Found)`.
     */
    @GetMapping("/app-documents/{id}")
    fun getAppDocument(@PathVariable id: String): ResponseEntity<AppDocument> {
        log.debug("REST request to get AppDocument : {}", id)
        val appDocument = appDocumentService.findOne(id)
        return ResponseUtil.wrapOrNotFound(appDocument)
    }
    /**
     *  `DELETE  /app-documents/:id` : delete the "id" appDocument.
     *
     * @param id the id of the appDocument to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/app-documents/{id}")
    fun deleteAppDocument(@PathVariable id: String): ResponseEntity<Void> {
        log.debug("REST request to delete AppDocument : {}", id)
        appDocumentService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build()
    }
}
