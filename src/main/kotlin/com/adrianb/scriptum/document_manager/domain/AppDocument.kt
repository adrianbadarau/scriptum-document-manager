package com.adrianb.scriptum.document_manager.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DBRef
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

import java.io.Serializable

/**
 * A AppDocument.
 */
@Document(collection = "jhi_document")
class AppDocument(

    @Id
    var id: String? = null,

    @get: NotNull
    @Field("content")
    var content: String? = null,

    @get: NotNull
    @Field("document_link")
    var documentLink: String? = null,

    @Field("blob")
    var blob: ByteArray? = null,

    @Field("blob_content_type")
    var blobContentType: String? = null,

    @DBRef
    @Field("category")
    @JsonIgnoreProperties("documents")
    var category: Category? = null,

    @DBRef
    @Field("tags")
    var tags: MutableSet<Tag> = mutableSetOf()

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {

    fun addTags(tag: Tag): AppDocument {
        this.tags.add(tag)
        return this
    }

    fun removeTags(tag: Tag): AppDocument {
        this.tags.remove(tag)
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppDocument) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "AppDocument{" +
        "id=$id" +
        ", content='$content'" +
        ", documentLink='$documentLink'" +
        ", blob='$blob'" +
        ", blobContentType='$blobContentType'" +
        "}"


    companion object {
        private const val serialVersionUID = 1L
    }
}
