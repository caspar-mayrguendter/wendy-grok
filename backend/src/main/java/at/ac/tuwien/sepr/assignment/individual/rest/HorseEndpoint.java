package at.ac.tuwien.sepr.assignment.individual.rest;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing horse-related operations.
 * Provides endpoints for searching, retrieving, creating, updating, and deleting horses,
 * as well as fetching their family tree.
 */
@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/horses";

  private final HorseService service;

  @Autowired
  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  /**
   * Searches for horses based on the given search parameters.
   *
   * @param searchParameters the parameters to filter the horse search
   * @return a stream of {@link HorseListDto} matching the search criteria
   */
  @GetMapping
  public Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);
    return service.searchHorses(searchParameters);
  }

  /**
   * Retrieves the details of a horse by its ID.
   *
   * @param id the unique identifier of the horse
   * @return the detailed information of the requested horse
   * @throws ResponseStatusException if the horse is not found
   */
  @GetMapping("{id}")
  public HorseDetailDto getById(@PathVariable("id") long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * Creates a new horse entry in the system.
   *
   * @param toCreate the horse data to be created
   * @return the created horse details
   * @throws ValidationException if the input data is invalid
   * @throws ConflictException if a conflict occurs while creating the horse
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HorseDetailDto create(
      @RequestBody HorseCreateDto toCreate
  ) throws ValidationException, ConflictException {
    LOG.info("POST " + BASE_PATH);
    LOG.debug("Request body: {}", toCreate);
    return service.create(
        toCreate
    );
  }

  /**
   * Updates an existing horse entry in the system.
   *
   * @param id the ID of the horse to update
   * @param toUpdate the horse data to be updated
   * @return the updated horse details
   * @throws ValidationException if the input data is invalid
   * @throws ConflictException if a conflict occurs while updating the horse
   * @throws NotFoundException if the horse with the given ID does not exist
   */
  @PutMapping("{id}")
  public HorseDetailDto update(
      @PathVariable("id") long id,
      @RequestBody HorseUpdateDto toUpdate
  ) throws ValidationException, ConflictException, NotFoundException {
    LOG.info("PUT " + BASE_PATH + "/{}", id);
    LOG.debug("Request body: {}", toUpdate);

    // Ensure the ID in the path matches the ID in the body
    if (!toUpdate.id().equals(id)) {
      throw new ValidationException("ID mismatch between path and body", null);
    }

    return service.update(toUpdate);
  }

  /**
   * Deletes a horse from the system.
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the horse with the given ID does not exist
   */
  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") long id) throws NotFoundException {
    LOG.info("DELETE " + BASE_PATH + "/{}", id);
    service.delete(id);
  }

  /**
   * Logs client-side errors with relevant details.
   *
   * @param status  the HTTP status code of the error
   * @param message a brief message describing the error
   * @param e       the exception that occurred
   */
  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }

}
