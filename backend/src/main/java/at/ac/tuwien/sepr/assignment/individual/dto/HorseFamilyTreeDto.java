package at.ac.tuwien.sepr.assignment.individual.dto;

import java.time.LocalDate;

/**
 * DTO for representing a horse in a family tree structure.
 * This includes the horse's basic information and references to its parents.
 */
public record HorseFamilyTreeDto(
    Long id,
    String name,
    LocalDate dateOfBirth,
    String sex,
    HorseFamilyTreeDto mother,
    HorseFamilyTreeDto father
) {}
