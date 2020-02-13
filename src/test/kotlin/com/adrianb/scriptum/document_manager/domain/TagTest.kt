package com.adrianb.scriptum.document_manager.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.adrianb.scriptum.document_manager.web.rest.equalsVerifier

class TagTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Tag::class)
        val tag1 = Tag()
        tag1.id = "id1"
        val tag2 = Tag()
        tag2.id = tag1.id
        assertThat(tag1).isEqualTo(tag2)
        tag2.id = "id2"
        assertThat(tag1).isNotEqualTo(tag2)
        tag1.id = null
        assertThat(tag1).isNotEqualTo(tag2)
    }
}
