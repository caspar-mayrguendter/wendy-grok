package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
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

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }

  }

}
