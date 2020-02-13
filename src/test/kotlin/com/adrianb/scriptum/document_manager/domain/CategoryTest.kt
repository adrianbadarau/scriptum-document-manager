package com.adrianb.scriptum.document_manager.domain

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import com.adrianb.scriptum.document_manager.web.rest.equalsVerifier

class CategoryTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Category::class)
        val category1 = Category()
        category1.id = "id1"
        val category2 = Category()
        category2.id = category1.id
        assertThat(category1).isEqualTo(category2)
        category2.id = "id2"
        assertThat(category1).isNotEqualTo(category2)
        category1.id = null
        assertThat(category1).isNotEqualTo(category2)
    }
}
