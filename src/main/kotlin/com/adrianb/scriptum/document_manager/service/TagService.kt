package com.adrianb.scriptum.document_manager.service

import com.adrianb.scriptum.document_manager.domain.Tag
import com.adrianb.scriptum.document_manager.repository.TagRepository
import org.slf4j.LoggerFactory

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

import java.util.Optional

/**
 * Service Implementation for managing [Tag].
 */
@Service
class TagService(
    private val tagRepository: TagRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a tag.
     *
     * @param tag the entity to save.
     * @return the persisted entity.
     */
    fun save(tag: Tag): Tag {
        log.debug("Request to save Tag : {}", tag)
        return tagRepository.save(tag)
    }

    /**
     * Get all the tags.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<Tag> {
        log.debug("Request to get all Tags")
        return tagRepository.findAll(pageable)
    }

    /**
     * Get one tag by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: String): Optional<Tag> {
        log.debug("Request to get Tag : {}", id)
        return tagRepository.findById(id)
    }

    /**
     * Delete the tag by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: String) {
        log.debug("Request to delete Tag : {}", id)

        tagRepository.deleteById(id)
    }
}
