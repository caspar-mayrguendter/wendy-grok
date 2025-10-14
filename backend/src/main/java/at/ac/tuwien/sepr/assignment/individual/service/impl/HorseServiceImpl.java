package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link HorseService} for handling image storage and retrieval.
 */
@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;


  @Autowired
  public HorseServiceImpl(HorseDao dao,
                          HorseMapper mapper,
                          HorseValidator validator,
                          OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters) {
    LOG.trace("searchHorses({})", searchParameters);
    var horses = dao.getAll();

    // Get all owners first to handle owner name filtering
    var allOwnerIds = horses.stream()
        .map(Horse::ownerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    final Map<Long, OwnerDto> allOwnerMap;
    try {
      allOwnerMap = !allOwnerIds.isEmpty() ? ownerService.getAllById(allOwnerIds) : Map.of();
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }

    // Apply search filters using AND logic and limit
    final Integer limit = searchParameters.limit();
    var filteredHorseList = horses.stream()
        .filter(horse -> matchesSearchCriteria(horse, searchParameters, allOwnerMap))
        .limit(limit != null && limit > 0 ? limit : Integer.MAX_VALUE)
        .toList();
    return filteredHorseList.stream()
        .map(horse -> mapper.entityToListDto(horse, allOwnerMap));
  }

  /**
   * Checks if a horse matches the given search criteria using AND logic.
   * All non-null criteria must match for the horse to be included.
   *
   * @param horse the horse to check
   * @param searchParameters the search criteria
   * @param ownerMap map of owner IDs to owner DTOs
   * @return true if the horse matches all criteria, false otherwise
   */
  private boolean matchesSearchCriteria(Horse horse, HorseSearchDto searchParameters, Map<Long, OwnerDto> ownerMap) {
    // Name filter (case-insensitive partial match)
    if (searchParameters.name() != null && !searchParameters.name().isBlank()) {
      if (horse.name() == null
          || !horse.name().toLowerCase().contains(searchParameters.name().toLowerCase())) {
        return false;
      }
    }

    // Description filter (case-insensitive partial match)
    if (searchParameters.description() != null && !searchParameters.description().isBlank()) {
      if (horse.description() == null
          || !horse.description().toLowerCase().contains(searchParameters.description().toLowerCase())) {
        return false;
      }
    }

    // Born before filter (horse must be born before the given date)
    if (searchParameters.bornBefore() != null) {
      if (horse.dateOfBirth() == null || !horse.dateOfBirth().isBefore(searchParameters.bornBefore())) {
        return false;
      }
    }

    // Sex filter (exact match)
    if (searchParameters.sex() != null) {
      if (horse.sex() != searchParameters.sex()) {
        return false;
      }
    }

    // Owner name filter (case-insensitive partial match on owner's name)
    if (searchParameters.ownerName() != null && !searchParameters.ownerName().isBlank()) {
      if (horse.ownerId() == null) {
        return false; // Horse has no owner, but we're searching for owner name
      }
      OwnerDto owner = ownerMap.get(horse.ownerId());
      if (owner == null) {
        return false; // Owner not found
      }
      String fullOwnerName = (owner.firstName() + " " + owner.lastName()).toLowerCase();
      if (!fullOwnerName.contains(searchParameters.ownerName().toLowerCase())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public HorseDetailDto create(
      HorseCreateDto horse
  ) throws ValidationException, ConflictException {
    LOG.trace("create({})", horse);
    validator.validateForCreate(horse);
    var newHorse = dao.create(
        horse
    );
    var ownerMap = ownerMapForSingleId(newHorse.ownerId());
    var parentMap = parentMapForIds(newHorse.motherId(), newHorse.fatherId());
    return mapper.entityToDetailDto(
        newHorse,
        ownerMap,
        parentMap);
  }

  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    var ownerMap = ownerMapForSingleId(horse.ownerId());
    var parentMap = parentMapForIds(horse.motherId(), horse.fatherId());
    return mapper.entityToDetailDto(
        horse,
        ownerMap,
        parentMap);
  }

  @Override
  public HorseDetailDto update(
      HorseUpdateDto horse
  ) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("update({})", horse);
    validator.validateForUpdate(horse);
    var updatedHorse = dao.update(horse);
    var ownerMap = ownerMapForSingleId(updatedHorse.ownerId());
    var parentMap = parentMapForIds(updatedHorse.motherId(), updatedHorse.fatherId());
    return mapper.entityToDetailDto(
        updatedHorse,
        ownerMap,
        parentMap);
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null
          ? null
          : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  private Map<Long, HorseListDto> parentMapForIds(Long motherId, Long fatherId) {
    try {
      Set<Long> parentIds = Stream.of(motherId, fatherId)
          .filter(Objects::nonNull)
          .collect(Collectors.toUnmodifiableSet());

      if (parentIds.isEmpty()) {
        return Collections.emptyMap();
      }

      // For now, we'll create basic HorseListDto objects from the parent IDs
      // In a real implementation, you'd want to fetch the full horse data
      Map<Long, HorseListDto> parentMap = new HashMap<>();
      for (Long parentId : parentIds) {
        try {
          Horse parentHorse = dao.getById(parentId);
          Map<Long, OwnerDto> ownerMap = ownerMapForSingleId(parentHorse.ownerId());
          HorseListDto parentDto = mapper.entityToListDto(parentHorse, ownerMap != null ? ownerMap : Collections.emptyMap());
          parentMap.put(parentId, parentDto);
        } catch (NotFoundException e) {
          throw new FatalException("Parent horse %d not found".formatted(parentId));
        }
      }
      return parentMap;
    } catch (Exception e) {
      throw new FatalException("Error loading parent horses", e);
    }
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);
    dao.delete(id);
  }

}
