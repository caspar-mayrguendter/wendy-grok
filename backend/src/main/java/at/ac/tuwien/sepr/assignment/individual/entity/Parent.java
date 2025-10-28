package at.ac.tuwien.sepr.assignment.individual.entity;

/**
 * Represents a parent relationship between horses in the persistent data store.
 * A horse can have up to two parents (mother and father).
 */
public record Parent(
    Long horseId,
    Long parentId
) {
}
