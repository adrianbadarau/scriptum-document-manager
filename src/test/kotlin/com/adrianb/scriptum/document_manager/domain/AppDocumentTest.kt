package com.adrianb.scriptum.document_manager.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.adrianb.scriptum.document_manager.web.rest.equalsVerifier

class AppDocumentTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(AppDocument::class)
        val appDocument1 = AppDocument()
        appDocument1.id = "id1"
        val appDocument2 = AppDocument()
        appDocument2.id = appDocument1.id
        assertThat(appDocument1).isEqualTo(appDocument2)
        appDocument2.id = "id2"
        assertThat(appDocument1).isNotEqualTo(appDocument2)
        appDocument1.id = null
        assertThat(appDocument1).isNotEqualTo(appDocument2)
    }
}
