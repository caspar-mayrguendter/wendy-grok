package at.ac.tuwien.sepr.assignment.individual.dto;

/**
 * Represents a Data Transfer Object (DTO) for parent information in horse relationships.
 */
public record ParentDto(
    HorseListDto horse,
    String relationship  // "mother" or "father"
) {
}
