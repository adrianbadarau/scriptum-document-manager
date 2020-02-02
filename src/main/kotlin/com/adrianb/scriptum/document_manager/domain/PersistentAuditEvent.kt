package com.adrianb.scriptum.document_manager.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import javax.validation.constraints.NotNull
import java.io.Serializable
import java.time.Instant

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Document(collection = "jhi_persistent_audit_event")
class PersistentAuditEvent(

    @Id
    @Field("event_id")
    var id: String? = null,

    @field:NotNull
    var principal: String? = null,

    @Field("event_date")
    var auditEventDate: Instant? = null,

    @Field("event_type")
    var auditEventType: String? = null,

    var data: MutableMap<String, String?> = mutableMapOf()

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersistentAuditEvent) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "PersistentAuditEvent{" +
        "principal='" + principal + '\'' +
        ", auditEventDate=" + auditEventDate +
        ", auditEventType='" + auditEventType + '\'' +
        '}'

    companion object {
        private const val serialVersionUID = 1L
    }
}
