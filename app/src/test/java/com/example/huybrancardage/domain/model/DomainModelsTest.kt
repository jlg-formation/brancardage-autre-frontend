package com.example.huybrancardage.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests unitaires pour les modèles de domaine
 * Vérifie les propriétés calculées et la logique métier
 */
class DomainModelsTest {

    // ==================== Tests Patient ====================

    @Test
    fun `Patient nomComplet should be prenom nom`() {
        val patient = createTestPatient(nom = "DUPONT", prenom = "Jean")

        assertEquals("Jean DUPONT", patient.nomComplet)
    }

    @Test
    fun `Patient initiales should be first letters uppercase`() {
        val patient = createTestPatient(nom = "DUPONT", prenom = "Jean")

        assertEquals("JD", patient.initiales)
    }

    @Test
    fun `Patient initiales with lowercase names should still be uppercase`() {
        val patient = createTestPatient(nom = "dupont", prenom = "jean")

        assertEquals("JD", patient.initiales)
    }

    @Test
    fun `Patient age should be calculated from dateNaissance`() {
        // Patient born on 1990-01-01 should be approximately 36 in 2026
        val patient = createTestPatient(dateNaissance = LocalDate.of(1990, 1, 1))

        // Age depends on current date but should be around 36
        assertTrue(patient.age in 35..37)
    }

    @Test
    fun `Patient localisationFormattee with service and chambre`() {
        val patient = createTestPatient(service = "Cardiologie", chambre = "101")

        assertEquals("Cardiologie - 101", patient.localisationFormattee)
    }

    @Test
    fun `Patient localisationFormattee with only service`() {
        val patient = createTestPatient(service = "Cardiologie", chambre = null)

        assertEquals("Cardiologie", patient.localisationFormattee)
    }

    @Test
    fun `Patient localisationFormattee with only chambre`() {
        val patient = createTestPatient(service = null, chambre = "101")

        assertEquals("101", patient.localisationFormattee)
    }

    // ==================== Tests Sexe ====================

    @Test
    fun `Sexe fromCode M should return MASCULIN`() {
        assertEquals(Sexe.MASCULIN, Sexe.fromCode("M"))
    }

    @Test
    fun `Sexe fromCode F should return FEMININ`() {
        assertEquals(Sexe.FEMININ, Sexe.fromCode("F"))
    }

    @Test
    fun `Sexe fromCode lowercase should work`() {
        assertEquals(Sexe.MASCULIN, Sexe.fromCode("m"))
        assertEquals(Sexe.FEMININ, Sexe.fromCode("f"))
    }

    @Test
    fun `Sexe fromCode unknown should default to MASCULIN`() {
        assertEquals(Sexe.MASCULIN, Sexe.fromCode("X"))
        assertEquals(Sexe.MASCULIN, Sexe.fromCode(""))
    }

    @Test
    fun `Sexe libelle should be correct`() {
        assertEquals("Homme", Sexe.MASCULIN.libelle)
        assertEquals("Femme", Sexe.FEMININ.libelle)
    }

    // ==================== Tests Localisation ====================

    @Test
    fun `Localisation isValid with coordinates`() {
        val loc = Localisation(latitude = 48.8566, longitude = 2.3522)

        assertTrue(loc.isValid)
    }

    @Test
    fun `Localisation isValid with description`() {
        val loc = Localisation(description = "Accueil principal")

        assertTrue(loc.isValid)
    }

    @Test
    fun `Localisation isValid with batiment`() {
        val loc = Localisation(batiment = "A")

        assertTrue(loc.isValid)
    }

    @Test
    fun `Localisation isValid false when empty`() {
        val loc = Localisation()

        assertFalse(loc.isValid)
    }

    @Test
    fun `Localisation descriptionFormattee with description`() {
        val loc = Localisation(description = "Accueil principal")

        assertEquals("Accueil principal", loc.descriptionFormattee)
    }

    @Test
    fun `Localisation descriptionFormattee with batiment etage chambre`() {
        val loc = Localisation(batiment = "A", etage = 2, chambre = "201")

        assertEquals("Bâtiment A - Étage 2, Chambre 201", loc.descriptionFormattee)
    }

    @Test
    fun `Localisation descriptionFormattee with RDC`() {
        val loc = Localisation(batiment = "A", etage = 0)

        assertEquals("Bâtiment A - RDC", loc.descriptionFormattee)
    }

    @Test
    fun `Localisation detailsFormattes with etage and chambre`() {
        val loc = Localisation(etage = 3, chambre = "305")

        assertEquals("Étage 3, Chambre 305", loc.detailsFormattes)
    }

    @Test
    fun `Localisation detailsFormattes with RDC`() {
        val loc = Localisation(etage = 0, chambre = "001")

        assertEquals("RDC, Chambre 001", loc.detailsFormattes)
    }

    // ==================== Tests Destination ====================

    @Test
    fun `Destination localisationFormattee with etageLibelle`() {
        val destination = Destination(
            id = "D001",
            nom = "Radiologie",
            batiment = "A",
            etage = 0,
            etageLibelle = "Rez-de-chaussée"
        )

        assertEquals("Bâtiment A - Rez-de-chaussée", destination.localisationFormattee)
    }

    @Test
    fun `Destination localisationFormattee without etageLibelle uses RDC`() {
        val destination = Destination(
            id = "D001",
            nom = "Radiologie",
            batiment = "A",
            etage = 0
        )

        assertEquals("Bâtiment A - RDC", destination.localisationFormattee)
    }

    @Test
    fun `Destination localisationFormattee with etage number`() {
        val destination = Destination(
            id = "D001",
            nom = "Chirurgie",
            batiment = "B",
            etage = 3
        )

        assertEquals("Bâtiment B - Étage 3", destination.localisationFormattee)
    }

    // ==================== Tests Media ====================

    @Test
    fun `Media should have correct type PHOTO`() {
        val media = Media(
            id = "M001",
            uri = "content://test",
            type = MediaType.PHOTO,
            mimeType = "image/jpeg"
        )

        assertEquals(MediaType.PHOTO, media.type)
    }

    @Test
    fun `Media should have correct type DOCUMENT`() {
        val media = Media(
            id = "M001",
            uri = "content://test",
            type = MediaType.DOCUMENT,
            mimeType = "image/jpeg"
        )

        assertEquals(MediaType.DOCUMENT, media.type)
    }

    // ==================== Tests AlerteMedicale ====================

    @Test
    fun `AlerteMedicale ALLERGIE type`() {
        val alerte = AlerteMedicale(
            type = TypeAlerte.ALLERGIE,
            titre = "Pénicilline",
            description = "Allergie sévère"
        )

        assertEquals(TypeAlerte.ALLERGIE, alerte.type)
        assertEquals("Pénicilline", alerte.titre)
        assertEquals("Allergie sévère", alerte.description)
    }

    @Test
    fun `AlerteMedicale without description`() {
        val alerte = AlerteMedicale(
            type = TypeAlerte.PRECAUTION,
            titre = "Attention"
        )

        assertEquals(null, alerte.description)
    }

    @Test
    fun `TypeAlerte should have all expected values`() {
        val types = TypeAlerte.entries

        assertEquals(4, types.size)
        assertTrue(types.contains(TypeAlerte.ALLERGIE))
        assertTrue(types.contains(TypeAlerte.PRECAUTION))
        assertTrue(types.contains(TypeAlerte.ISOLEMENT))
        assertTrue(types.contains(TypeAlerte.AUTRE))
    }

    // ==================== Helpers ====================

    private fun createTestPatient(
        id: String = "P001",
        ipp: String = "123456789",
        nom: String = "TEST",
        prenom: String = "Patient",
        dateNaissance: LocalDate = LocalDate.of(1990, 1, 1),
        sexe: Sexe = Sexe.MASCULIN,
        service: String? = "Test Service",
        chambre: String? = "101"
    ): Patient = Patient(
        id = id,
        ipp = ipp,
        nom = nom,
        prenom = prenom,
        dateNaissance = dateNaissance,
        sexe = sexe,
        numeroSecuriteSociale = null,
        chambre = chambre,
        service = service,
        batiment = "A",
        etage = 1,
        alertesMedicales = emptyList()
    )
}

