package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import java.util.List;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Validator for horse-related operations, ensuring that all horse data meets the required constraints.
 */
@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao horseDao;

  public HorseValidator(HorseDao horseDao) {
    this.horseDao = horseDao;
  }


  /**
   * Validates a horse before creation, ensuring all fields meet constraints and checking for conflicts.
   *
   * @param horse the {@link HorseCreateDto} to validate
   * @throws ValidationException if validation fails
   * @throws ConflictException   if conflicts with existing data are detected
   */
  public void validateForCreate(
       HorseCreateDto horse
  ) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    // Validate name (mandatory)
    if (horse.name() == null || horse.name().isBlank()) {
      validationErrors.add("Horse name is mandatory");
    }

    // Validate dateOfBirth (mandatory)
    if (horse.dateOfBirth() == null) {
      validationErrors.add("Horse birthdate is mandatory");
    }

    // Validate sex (mandatory)
    if (horse.sex() == null) {
      validationErrors.add("Horse gender is mandatory");
    }

    // Validate description (optional)
    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    // Validate parents
    validateParents(null, horse.parentIds(), horse.dateOfBirth(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }

  }

  /**
   * Validates a horse before update, ensuring all fields meet constraints and checking for conflicts.
   * Similar to validateForCreate but allows for ID validation.
   *
   * @param horse the {@link HorseUpdateDto} to validate
   * @throws ValidationException if validation fails
   * @throws ConflictException   if conflicts with existing data are detected
   */
  public void validateForUpdate(
       HorseUpdateDto horse
  ) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    // Validate ID
    if (horse.id() == null || horse.id() <= 0) {
      validationErrors.add("Horse ID must be a positive number");
    }

    // Validate name (mandatory)
    if (horse.name() == null || horse.name().isBlank()) {
      validationErrors.add("Horse name is mandatory");
    }

    // Validate dateOfBirth (mandatory)
    if (horse.dateOfBirth() == null) {
      validationErrors.add("Horse birthdate is mandatory");
    }

    // Validate sex (mandatory)
    if (horse.sex() == null) {
      validationErrors.add("Horse gender is mandatory");
    }

    // Validate description (optional)
    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    // Validate parents
    validateParents(horse.id(), horse.parentIds(), horse.dateOfBirth(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

  }

  /**
   * Validates parent assignments according to business rules.
   * A horse can have up to 2 parents, and parents must have different genders.
   *
   * @param horseId the horse's own ID (can be null for new horses)
   * @param parentIds the list of parent horse IDs (can be null or empty)
   * @param horseBirthDate the horse's birth date
   * @param validationErrors list to add validation errors to
   */
  private void validateParents(Long horseId, List<Long> parentIds, LocalDate horseBirthDate, List<String> validationErrors) {
    if (parentIds == null || parentIds.isEmpty()) {
      return; // No parents to validate
    }

    // Check that we don't have more than 2 parents
    if (parentIds.size() > 2) {
      validationErrors.add("A horse cannot have more than 2 parents");
      return;
    }

    // Track parent sexes to ensure different genders
    Sex parent1Sex = null;
    Sex parent2Sex = null;

    for (int i = 0; i < parentIds.size(); i++) {
      Long parentId = parentIds.get(i);
      if (parentId == null) {
        validationErrors.add("Parent ID cannot be null");
        continue;
      }

      try {
        var parent = horseDao.getById(parentId);

        // Check that horse is not its own parent
        if (horseId != null && horseId.equals(parentId)) {
          validationErrors.add("A horse cannot be its own parent");
        }

        // Check that parent is older than child
        if (parent.dateOfBirth().isAfter(horseBirthDate) || parent.dateOfBirth().isEqual(horseBirthDate)) {
          validationErrors.add("Parent must be born before the child");
        }

        // Track parent sexes
        if (i == 0) {
          parent1Sex = parent.sex();
        } else if (i == 1) {
          parent2Sex = parent.sex();
        }

      } catch (NotFoundException e) {
        validationErrors.add("Parent horse with ID " + parentId + " does not exist");
      }
    }

    // Check that parents have different genders (if we have 2 parents)
    if (parentIds.size() == 2 && parent1Sex != null && parent2Sex != null) {
      if (parent1Sex == parent2Sex) {
        validationErrors.add("Parents must have different genders");
      }
    }

    // Check for duplicate parent IDs
    if (parentIds.size() == 2 && parentIds.get(0).equals(parentIds.get(1))) {
      validationErrors.add("A horse cannot have the same horse as both parents");
    }
  }

}
