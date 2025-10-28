package at.ac.tuwien.sepr.assignment.individual.persistence;

import at.ac.tuwien.sepr.assignment.individual.entity.Parent;
import java.util.List;

/**
 * Data Access Object for horse parent relationships.
 * Implements access functionality to the application's persistent data store regarding horse parent relationships.
 */
public interface ParentDao {

  /**
   * Get all parent relationships for a horse.
   *
   * @param horseId the ID of the horse
   * @return a list of parent relationships for the horse
   */
  List<Parent> getParentsByHorseId(long horseId);

  /**
   * Get all children relationships for a horse (horses where this horse is a parent).
   *
   * @param parentId the ID of the parent horse
   * @return a list of parent relationships where this horse is the parent
   */
  List<Parent> getChildrenByParentId(long parentId);

  /**
   * Set the parent relationships for a horse.
   * This replaces all existing parent relationships for the horse.
   *
   * @param horseId the ID of the horse
   * @param parentIds the list of parent IDs (must contain 0, 1, or 2 IDs)
   */
  void setParents(long horseId, List<Long> parentIds);

  /**
   * Delete all parent relationships for a horse.
   *
   * @param horseId the ID of the horse
   */
  void deleteParentsByHorseId(long horseId);

  /**
   * Delete all parent relationships where the given horse is a parent.
   *
   * @param parentId the ID of the parent horse
   */
  void deleteChildrenByParentId(long parentId);
}
