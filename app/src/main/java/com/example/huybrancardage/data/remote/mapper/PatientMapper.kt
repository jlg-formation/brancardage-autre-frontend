package com.example.huybrancardage.data.remote.mapper

import com.example.huybrancardage.data.remote.dto.AlerteMedicaleDto
import com.example.huybrancardage.data.remote.dto.PatientDto
import com.example.huybrancardage.domain.model.AlerteMedicale
import com.example.huybrancardage.domain.model.Patient
import com.example.huybrancardage.domain.model.Sexe
import com.example.huybrancardage.domain.model.TypeAlerte
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Mapper pour convertir les DTOs Patient en modèles domain
 */
object PatientMapper {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Convertit un PatientDto en Patient domain
     */
    fun toDomain(dto: PatientDto): Patient {
        return Patient(
            id = dto.id,
            ipp = dto.ipp,
            nom = dto.nom,
            prenom = dto.prenom,
            dateNaissance = LocalDate.parse(dto.dateNaissance, dateFormatter),
            sexe = Sexe.fromCode(dto.sexe),
            numeroSecuriteSociale = dto.numeroSecuriteSociale,
            chambre = dto.chambre,
            service = dto.service,
            batiment = dto.batiment,
            etage = dto.etage,
            alertesMedicales = dto.alertesMedicales?.map { toDomain(it) } ?: emptyList()
        )
    }

    /**
     * Convertit une AlerteMedicaleDto en AlerteMedicale domain
     */
    private fun toDomain(dto: AlerteMedicaleDto): AlerteMedicale {
        return AlerteMedicale(
            type = TypeAlerte.entries.find { it.name == dto.type } ?: TypeAlerte.AUTRE,
            titre = dto.titre,
            description = dto.description
        )
    }

    /**
     * Convertit une liste de PatientDto en liste de Patient domain
     */
    fun toDomainList(dtos: List<PatientDto>): List<Patient> {
        return dtos.map { toDomain(it) }
    }
}

