package com.adrianb.scriptum.document_manager.web.rest

import com.adrianb.scriptum.document_manager.domain.Tag
import com.adrianb.scriptum.document_manager.service.TagService
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

private const val ENTITY_NAME = "documentmanagerTag"
/**
 * REST controller for managing [com.adrianb.scriptum.document_manager.domain.Tag].
 */
@RestController
@RequestMapping("/api")
class TagResource(
    private val tagService: TagService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /tags` : Create a new tag.
     *
     * @param tag the tag to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new tag, or with status `400 (Bad Request)` if the tag has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/tags")
    fun createTag(@Valid @RequestBody tag: Tag): ResponseEntity<Tag> {
        log.debug("REST request to save Tag : {}", tag)
        if (tag.id != null) {
            throw BadRequestAlertException(
                "A new tag cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = tagService.save(tag)
        return ResponseEntity.created(URI("/api/tags/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /tags` : Updates an existing tag.
     *
     * @param tag the tag to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated tag,
     * or with status `400 (Bad Request)` if the tag is not valid,
     * or with status `500 (Internal Server Error)` if the tag couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/tags")
    fun updateTag(@Valid @RequestBody tag: Tag): ResponseEntity<Tag> {
        log.debug("REST request to update Tag : {}", tag)
        if (tag.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = tagService.save(tag)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     tag.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /tags` : get all the tags.
     *

     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of tags in body.
     */
    @GetMapping("/tags")    
    fun getAllTags(
        pageable: Pageable
    ) : ResponseEntity<MutableList<Tag>> {
        log.debug("REST request to get a page of Tags")
        val page = tagService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /tags/:id` : get the "id" tag.
     *
     * @param id the id of the tag to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the tag, or with status `404 (Not Found)`.
     */
    @GetMapping("/tags/{id}")
    fun getTag(@PathVariable id: String): ResponseEntity<Tag> {
        log.debug("REST request to get Tag : {}", id)
        val tag = tagService.findOne(id)
        return ResponseUtil.wrapOrNotFound(tag)
    }
    /**
     *  `DELETE  /tags/:id` : delete the "id" tag.
     *
     * @param id the id of the tag to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/tags/{id}")
    fun deleteTag(@PathVariable id: String): ResponseEntity<Void> {
        log.debug("REST request to delete Tag : {}", id)
        tagService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build()
    }
}
