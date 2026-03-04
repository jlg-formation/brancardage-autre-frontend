package com.example.huybrancardage.data.remote.mapper

import com.example.huybrancardage.data.remote.dto.BrancardageRequestDto
import com.example.huybrancardage.data.remote.dto.BrancardageResponseDto
import com.example.huybrancardage.data.remote.dto.LocalisationDto
import com.example.huybrancardage.data.remote.dto.MediaDto
import com.example.huybrancardage.data.remote.dto.MediaUploadResponseDto
import com.example.huybrancardage.domain.model.BrancardageRequest
import com.example.huybrancardage.domain.model.BrancardageResponse
import com.example.huybrancardage.domain.model.Destination
import com.example.huybrancardage.domain.model.Localisation
import com.example.huybrancardage.domain.model.Media
import com.example.huybrancardage.domain.model.MediaType
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.domain.model.Sexe
import com.example.huybrancardage.domain.model.StatutBrancardage
import java.time.Instant
import java.time.LocalDate

/**
 * Mapper pour les objets Brancardage entre DTO et Domain
 */
object BrancardageMapper {

    /**
     * Convertit une requête domain en DTO
     */
    fun toDto(request: BrancardageRequest): BrancardageRequestDto {
        return BrancardageRequestDto(
            patientId = request.patientId,
            depart = toLocalisationDto(request.depart),
            destinationId = request.destinationId,
            mediaIds = request.mediaIds,
            commentaire = request.commentaire
        )
    }

    /**
     * Convertit une localisation domain en DTO
     */
    fun toLocalisationDto(localisation: Localisation): LocalisationDto {
        return LocalisationDto(
            latitude = localisation.latitude,
            longitude = localisation.longitude,
            description = localisation.description,
            batiment = localisation.batiment,
            etage = localisation.etage,
            chambre = localisation.chambre
        )
    }

    /**
     * Convertit une réponse DTO en domain
     */
    fun toDomain(dto: BrancardageResponseDto): BrancardageResponse {
        return BrancardageResponse(
            id = dto.id,
            statut = parseStatut(dto.statut),
            dateCreation = parseInstant(dto.dateCreation),
            patient = Patient(
                id = dto.patient.id,
                ipp = dto.patient.ipp,
                nom = dto.patient.nom,
                prenom = dto.patient.prenom,
                dateNaissance = LocalDate.now(), // Valeur par défaut car non fournie dans le résumé
                sexe = Sexe.MASCULIN
            ),
            depart = toLocalisation(dto.depart),
            destination = Destination(
                id = dto.destination.id,
                nom = dto.destination.nom,
                batiment = dto.destination.batiment,
                etage = dto.destination.etage,
                etageLibelle = dto.destination.etageLibelle,
                frequente = dto.destination.frequente
            ),
            medias = dto.medias.map { toMedia(it) },
            commentaire = dto.commentaire
        )
    }

    /**
     * Convertit un MediaDto en Media domain
     */
    private fun toMedia(dto: MediaDto): Media {
        return Media(
            id = dto.id,
            uri = dto.url,
            type = parseMediaType(dto.type),
            mimeType = dto.mimeType,
            taille = dto.taille,
            dateAjout = dto.dateUpload?.let { parseInstant(it) } ?: Instant.now(),
            description = dto.description
        )
    }

    /**
     * Convertit une réponse d'upload en Media domain
     */
    fun toMedia(dto: MediaUploadResponseDto): Media {
        return Media(
            id = dto.id,
            uri = dto.url,
            type = parseMediaType(dto.type),
            mimeType = dto.mimeType,
            taille = dto.taille,
            dateAjout = dto.dateUpload?.let { parseInstant(it) } ?: Instant.now(),
            description = dto.description
        )
    }

    /**
     * Convertit une LocalisationDto en Localisation domain
     */
    private fun toLocalisation(dto: LocalisationDto): Localisation {
        return Localisation(
            latitude = dto.latitude,
            longitude = dto.longitude,
            description = dto.description,
            batiment = dto.batiment,
            etage = dto.etage,
            chambre = dto.chambre
        )
    }

    /**
     * Parse un statut string en enum
     */
    private fun parseStatut(statut: String): StatutBrancardage {
        return try {
            StatutBrancardage.valueOf(statut)
        } catch (e: Exception) {
            StatutBrancardage.EN_ATTENTE
        }
    }

    /**
     * Parse un type de média string en enum
     */
    private fun parseMediaType(type: String): MediaType {
        return try {
            MediaType.valueOf(type)
        } catch (e: Exception) {
            MediaType.PHOTO
        }
    }

    /**
     * Parse une date ISO en Instant
     */
    private fun parseInstant(dateString: String): Instant {
        return try {
            Instant.parse(dateString)
        } catch (e: Exception) {
            Instant.now()
        }
    }
}

