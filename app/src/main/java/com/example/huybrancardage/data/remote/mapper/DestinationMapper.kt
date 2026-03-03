package com.example.huybrancardage.data.remote.mapper

import com.example.huybrancardage.data.remote.dto.DestinationDto
import com.example.huybrancardage.domain.model.Destination

/**
 * Mapper pour convertir les DTOs Destination en modèles domain
 */
object DestinationMapper {

    /**
     * Convertit un DestinationDto en Destination domain
     */
    fun toDomain(dto: DestinationDto): Destination {
        return Destination(
            id = dto.id,
            nom = dto.nom,
            batiment = dto.batiment,
            etage = dto.etage,
            etageLibelle = dto.etageLibelle,
            frequente = dto.frequente
        )
    }

    /**
     * Convertit une liste de DestinationDto en liste de Destination domain
     */
    fun toDomainList(dtos: List<DestinationDto>): List<Destination> {
        return dtos.map { toDomain(it) }
    }
}

