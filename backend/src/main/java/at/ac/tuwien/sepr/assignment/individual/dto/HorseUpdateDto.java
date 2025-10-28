package at.ac.tuwien.sepr.assignment.individual.dto;

import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a Data Transfer Object (DTO) for updating an existing horse entry.
 * This record encapsulates all necessary details for modifying a horse.
 */
public record HorseUpdateDto(
    Long id,
    String name,
    String description,
    LocalDate dateOfBirth,
    Sex sex,
    Long ownerId,
    List<Long> parentIds
) {
}
