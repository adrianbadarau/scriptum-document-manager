package com.adrianb.scriptum.document_manager.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.adrianb.scriptum.document_manager.web.rest.equalsVerifier

class DocumentTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Document::class)
        val document1 = Document()
        document1.id = "id1"
        val document2 = Document()
        document2.id = document1.id
        assertThat(document1).isEqualTo(document2)
        document2.id = "id2"
        assertThat(document1).isNotEqualTo(document2)
        document1.id = null
        assertThat(document1).isNotEqualTo(document2)
    }
}
