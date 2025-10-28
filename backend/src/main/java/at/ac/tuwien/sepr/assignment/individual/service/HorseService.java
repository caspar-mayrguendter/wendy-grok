package at.ac.tuwien.sepr.assignment.individual.service;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {
  /**
   * Searches for horses based on the given search criteria.
   * If no search criteria are provided, returns all horses.
   *
   * @param searchParameters the search criteria to filter horses
   * @return stream of horses matching the search criteria
   */
  Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters);

  /**
   * Creates a horse with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to create
   * @return the created horse
   * @throws ValidationException if the horse could not be created because the data given is in itself incorrect (description too long, no name, …)
   * @throws ConflictException if the update data given for the horse is in conflict the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto create(
      HorseCreateDto horse
  ) throws ValidationException, ConflictException;

  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Get the family tree for a horse with the given ID.
   * This includes the horse and all its ancestors up to the specified maximum generations.
   *
   * @param id the ID of the horse to get the family tree for
   * @param maxGenerations the maximum number of generations to include (1-10)
   * @return the family tree of the horse
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   * @throws ValidationException if maxGenerations is not between 1 and 10
   */
  HorseFamilyTreeDto getFamilyTree(long id, int maxGenerations) throws NotFoundException, ValidationException;

  /**
   * Updates a horse with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws ValidationException if the horse could not be updated because the data given is in itself incorrect (description too long, no name, …)
   * @throws ConflictException if the update data given for the horse is in conflict the data currently in the system (owner does not exist, …)
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto update(
      HorseUpdateDto horse
  ) throws ValidationException, ConflictException, NotFoundException;

  /**
   * Searches for potential parent horses based on name, limiting results to 5 candidates.
   * Used for parent selection when creating or updating horses.
   *
   * @param name the name to search for (partial match, case-insensitive)
   * @return stream of up to 5 horses matching the name search
   */
  Stream<HorseListDto> searchParents(String name);

  /**
   * Deletes the horse with the given ID from the persistent data store.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  void delete(long id) throws NotFoundException;
}
