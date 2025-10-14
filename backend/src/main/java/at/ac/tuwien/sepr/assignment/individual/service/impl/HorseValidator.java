package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
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
    validateParents(null, horse.motherId(), horse.fatherId(), horse.dateOfBirth(), validationErrors);

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
    validateParents(horse.id(), horse.motherId(), horse.fatherId(), horse.dateOfBirth(), validationErrors);

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

  }

  /**
   * Validates parent assignments according to business rules.
   *
   * @param horseId the horse's own ID (can be null for new horses)
   * @param motherId the mother horse ID (can be null)
   * @param fatherId the father horse ID (can be null)
   * @param horseBirthDate the horse's birth date
   * @param validationErrors list to add validation errors to
   */
  private void validateParents(Long horseId, Long motherId, Long fatherId, LocalDate horseBirthDate, List<String> validationErrors) {
    // Check that parents exist if specified and validate their properties
    if (motherId != null) {
      try {
        var mother = horseDao.getById(motherId);
        if (mother.sex() != Sex.FEMALE) {
          validationErrors.add("Mother must be female");
        }
        // Check that horse is not its own mother
        if (horseId != null && horseId.equals(motherId)) {
          validationErrors.add("A horse cannot be its own mother");
        }
        // Check that mother is older than child
        if (mother.dateOfBirth().isAfter(horseBirthDate) || mother.dateOfBirth().isEqual(horseBirthDate)) {
          validationErrors.add("Mother must be born before the child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Mother horse with ID " + motherId + " does not exist");
      }
    }

    if (fatherId != null) {
      try {
        var father = horseDao.getById(fatherId);
        if (father.sex() != Sex.MALE) {
          validationErrors.add("Father must be male");
        }
        // Check that horse is not its own father
        if (horseId != null && horseId.equals(fatherId)) {
          validationErrors.add("A horse cannot be its own father");
        }
        // Check that father is older than child
        if (father.dateOfBirth().isAfter(horseBirthDate) || father.dateOfBirth().isEqual(horseBirthDate)) {
          validationErrors.add("Father must be born before the child");
        }
      } catch (NotFoundException e) {
        validationErrors.add("Father horse with ID " + fatherId + " does not exist");
      }
    }

    // Check that mother and father are different horses
    if (motherId != null && fatherId != null && motherId.equals(fatherId)) {
      validationErrors.add("Mother and father cannot be the same horse");
    }
  }

}
