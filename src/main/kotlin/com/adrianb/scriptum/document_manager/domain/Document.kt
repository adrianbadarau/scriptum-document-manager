package com.adrianb.scriptum.document_manager.domain

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
 * A Document.
 */
@Document(collection = "jhi_document")
class Document(

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
    var blobContentType: String? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Document) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Document{" +
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
