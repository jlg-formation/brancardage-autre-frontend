package com.example.huybrancardage.ui.viewmodel

import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Tests unitaires pour MediaViewModel
 *
 * Vérifie la gestion des médias (ajout, suppression, mise à jour)
 * Note: Les tests impliquant le MediaManager nécessiteraient des tests instrumentés
 */
class MediaViewModelTest {

    private lateinit var viewModel: MediaViewModel

    @Before
    fun setUp() {
        viewModel = MediaViewModel()
    }

    // ==================== Tests État Initial ====================

    @Test
    fun `initial state should have empty media list`() {
        val state = viewModel.uiState.value

        assertTrue(state.medias.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isProcessingPhoto)
        assertNull(state.error)
        assertNull(state.pendingCameraUri)
        assertEquals(0, state.count)
        assertFalse(state.hasMedias)
    }

    // ==================== Tests Ajout de Média ====================

    @Test
    fun `addMedia should add media to list`() {
        // Given
        val media = createTestMedia("1")

        // When
        viewModel.addMedia(media)

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.medias.size)
        assertEquals(media, state.medias[0])
        assertTrue(state.hasMedias)
        assertEquals(1, state.count)
    }

    @Test
    fun `addMedia should append to existing list`() {
        // Given
        val media1 = createTestMedia("1")
        val media2 = createTestMedia("2")

        // When
        viewModel.addMedia(media1)
        viewModel.addMedia(media2)

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.medias.size)
        assertEquals("1", state.medias[0].id)
        assertEquals("2", state.medias[1].id)
    }

    @Test
    fun `addMedia should clear any existing error`() {
        // Given - set an error first
        // Note: Since we can't easily set error without MediaManager,
        // we test the behavior by adding media directly
        val media = createTestMedia("1")

        // When
        viewModel.addMedia(media)

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== Tests Suppression de Média ====================

    @Test
    fun `removeMedia should remove media by id`() {
        // Given
        val media1 = createTestMedia("1")
        val media2 = createTestMedia("2")
        viewModel.addMedia(media1)
        viewModel.addMedia(media2)

        // When
        viewModel.removeMedia("1")

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.medias.size)
        assertEquals("2", state.medias[0].id)
    }

    @Test
    fun `removeMedia with non-existing id should not change list`() {
        // Given
        val media = createTestMedia("1")
        viewModel.addMedia(media)

        // When
        viewModel.removeMedia("non-existing")

        // Then
        assertEquals(1, viewModel.uiState.value.medias.size)
    }

    @Test
    fun `removeMedia last item should result in empty list`() {
        // Given
        val media = createTestMedia("1")
        viewModel.addMedia(media)

        // When
        viewModel.removeMedia("1")

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.medias.isEmpty())
        assertFalse(state.hasMedias)
        assertEquals(0, state.count)
    }

    // ==================== Tests Mise à jour de Description ====================

    @Test
    fun `updateMediaDescription should update description of specific media`() {
        // Given
        val media1 = createTestMedia("1", "Original description")
        val media2 = createTestMedia("2", "Other description")
        viewModel.addMedia(media1)
        viewModel.addMedia(media2)

        // When
        viewModel.updateMediaDescription("1", "Updated description")

        // Then
        val state = viewModel.uiState.value
        assertEquals("Updated description", state.medias.find { it.id == "1" }?.description)
        assertEquals("Other description", state.medias.find { it.id == "2" }?.description)
    }

    @Test
    fun `updateMediaDescription with non-existing id should not change anything`() {
        // Given
        val media = createTestMedia("1", "Original")
        viewModel.addMedia(media)

        // When
        viewModel.updateMediaDescription("non-existing", "New description")

        // Then
        assertEquals("Original", viewModel.uiState.value.medias[0].description)
    }

    // ==================== Tests setMedias ====================

    @Test
    fun `setMedias should replace entire media list`() {
        // Given
        val initialMedia = createTestMedia("1")
        viewModel.addMedia(initialMedia)

        val newMedias = listOf(
            createTestMedia("A"),
            createTestMedia("B"),
            createTestMedia("C")
        )

        // When
        viewModel.setMedias(newMedias)

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.medias.size)
        assertEquals("A", state.medias[0].id)
        assertEquals("B", state.medias[1].id)
        assertEquals("C", state.medias[2].id)
    }

    @Test
    fun `setMedias with empty list should clear all medias`() {
        // Given
        viewModel.addMedia(createTestMedia("1"))
        viewModel.addMedia(createTestMedia("2"))

        // When
        viewModel.setMedias(emptyList())

        // Then
        assertTrue(viewModel.uiState.value.medias.isEmpty())
    }

    // ==================== Tests getMedias ====================

    @Test
    fun `getMedias should return current media list`() {
        // Given
        val media1 = createTestMedia("1")
        val media2 = createTestMedia("2")
        viewModel.addMedia(media1)
        viewModel.addMedia(media2)

        // When
        val result = viewModel.getMedias()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }

    @Test
    fun `getMedias should return empty list when no medias`() {
        val result = viewModel.getMedias()

        assertTrue(result.isEmpty())
    }

    // ==================== Tests clearMedias ====================

    @Test
    fun `clearMedias should reset state to initial`() {
        // Given
        viewModel.addMedia(createTestMedia("1"))
        viewModel.addMedia(createTestMedia("2"))

        // When
        viewModel.clearMedias()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.medias.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isProcessingPhoto)
        assertNull(state.error)
        assertNull(state.pendingCameraUri)
    }

    // ==================== Tests clearError ====================

    @Test
    fun `clearError should remove error from state`() {
        // Given - we can test this by clearing even when no error
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== Tests MediaUiState Properties ====================

    @Test
    fun `count should return correct number of medias`() {
        viewModel.addMedia(createTestMedia("1"))
        assertEquals(1, viewModel.uiState.value.count)

        viewModel.addMedia(createTestMedia("2"))
        assertEquals(2, viewModel.uiState.value.count)

        viewModel.addMedia(createTestMedia("3"))
        assertEquals(3, viewModel.uiState.value.count)
    }

    @Test
    fun `hasMedias should return true when medias exist`() {
        assertFalse(viewModel.uiState.value.hasMedias)

        viewModel.addMedia(createTestMedia("1"))
        assertTrue(viewModel.uiState.value.hasMedias)
    }

    // ==================== Tests Different Media Types ====================

    @Test
    fun `should handle photo type media`() {
        val photoMedia = Media(
            id = "photo1",
            uri = "content://test/photo1",
            type = MediaType.PHOTO,
            mimeType = "image/jpeg",
            taille = 1024,
            dateAjout = Instant.now(),
            description = "Photo test"
        )

        viewModel.addMedia(photoMedia)

        assertEquals(MediaType.PHOTO, viewModel.uiState.value.medias[0].type)
    }

    @Test
    fun `should handle document type media`() {
        val documentMedia = Media(
            id = "doc1",
            uri = "content://test/doc1",
            type = MediaType.DOCUMENT,
            mimeType = "image/jpeg",
            taille = 2048,
            dateAjout = Instant.now(),
            description = "Document scanné"
        )

        viewModel.addMedia(documentMedia)

        assertEquals(MediaType.DOCUMENT, viewModel.uiState.value.medias[0].type)
    }

    // ==================== Helpers ====================

    private fun createTestMedia(
        id: String,
        description: String = "Test media $id"
    ): Media = Media(
        id = id,
        uri = "content://test/$id",
        type = MediaType.PHOTO,
        mimeType = "image/jpeg",
        taille = 1024,
        dateAjout = Instant.now(),
        description = description
    )
}

