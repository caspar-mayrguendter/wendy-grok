package at.ac.tuwien.sepr.assignment.individual.service.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.dto.ParentDto;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepr.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepr.assignment.individual.persistence.ParentDao;
import at.ac.tuwien.sepr.assignment.individual.service.HorseService;
import at.ac.tuwien.sepr.assignment.individual.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
  private final ParentDao parentDao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;


  @Autowired
  public HorseServiceImpl(HorseDao dao,
                          ParentDao parentDao,
                          HorseMapper mapper,
                          HorseValidator validator,
                          OwnerService ownerService) {
    this.dao = dao;
    this.parentDao = parentDao;
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
    var newHorse = dao.create(horse);

    // Set parent relationships if provided
    if (horse.parentIds() != null && !horse.parentIds().isEmpty()) {
      parentDao.setParents(newHorse.id(), horse.parentIds());
    }

    var ownerMap = ownerMapForSingleId(newHorse.ownerId());
    var parentList = parentListForHorseId(newHorse.id());
    return mapper.entityToDetailDto(
        newHorse,
        ownerMap,
        parentList);
  }

  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    var ownerMap = ownerMapForSingleId(horse.ownerId());
    var parentList = parentListForHorseId(horse.id());
    return mapper.entityToDetailDto(
        horse,
        ownerMap,
        parentList);
  }

  @Override
  public HorseFamilyTreeDto getFamilyTree(long id, int maxGenerations) throws NotFoundException, ValidationException {
    LOG.trace("getFamilyTree({}, {})", id, maxGenerations);

    // Validate maxGenerations parameter
    if (maxGenerations < 1 || maxGenerations > 10) {
      throw new ValidationException("Maximum generations must be between 1 and 10", null);
    }

    // Get the root horse
    Horse rootHorse = dao.getById(id);

    // Build the family tree recursively
    return buildFamilyTreeNode(rootHorse, maxGenerations, 0);
  }

  /**
   * Recursively builds a family tree node for the given horse.
   * This method loads all ancestors up to the specified maximum generations.
   *
   * @param horse the horse to build the tree node for
   * @param maxGenerations the maximum number of generations to include
   * @param currentGeneration the current generation level (0 = root)
   * @return the family tree node for the horse
   */
  private HorseFamilyTreeDto buildFamilyTreeNode(Horse horse, int maxGenerations, int currentGeneration) {
    // Stop recursion if we've reached the maximum generations
    if (currentGeneration >= maxGenerations) {
      return null;
    }

    // Load parents recursively
    var parentRelationships = parentDao.getParentsByHorseId(horse.id());

    // Separate parents by sex (assuming at most one male and one female parent)
    HorseFamilyTreeDto mother = null;
    HorseFamilyTreeDto father = null;

    for (var parentRel : parentRelationships) {
      try {
        Horse parentHorse = dao.getById(parentRel.parentId());
        var parentTreeNode = buildFamilyTreeNode(parentHorse, maxGenerations, currentGeneration + 1);

        if (parentHorse.sex() == Sex.FEMALE) {
          mother = parentTreeNode;
        } else if (parentHorse.sex() == Sex.MALE) {
          father = parentTreeNode;
        }
      } catch (NotFoundException e) {
        // If parent is not found, continue without it (shouldn't happen in a consistent DB)
        LOG.warn("Parent horse {} not found for horse {}", parentRel.parentId(), horse.id());
      }
    }

    return new HorseFamilyTreeDto(
        horse.id(),
        horse.name(),
        horse.dateOfBirth(),
        horse.sex().toString(),
        mother,
        father
    );
  }

  @Override
  public HorseDetailDto update(
      HorseUpdateDto horse
  ) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("update({})", horse);
    validator.validateForUpdate(horse);
    var updatedHorse = dao.update(horse);

    // Update parent relationships if provided
    if (horse.parentIds() != null) {
      parentDao.setParents(updatedHorse.id(), horse.parentIds());
    }

    var ownerMap = ownerMapForSingleId(updatedHorse.ownerId());
    var parentList = parentListForHorseId(updatedHorse.id());
    return mapper.entityToDetailDto(
        updatedHorse,
        ownerMap,
        parentList);
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

  private List<ParentDto> parentListForHorseId(long horseId) {
    try {
      var parentRelationships = parentDao.getParentsByHorseId(horseId);

      if (parentRelationships.isEmpty()) {
        return Collections.emptyList();
      }

      List<ParentDto> parentList = new ArrayList<>();
      for (var parentRel : parentRelationships) {
        try {
          Horse parentHorse = dao.getById(parentRel.parentId());
          Map<Long, OwnerDto> ownerMap = ownerMapForSingleId(parentHorse.ownerId());
          HorseListDto parentDto = mapper.entityToListDto(parentHorse, ownerMap != null ? ownerMap : Collections.emptyMap());

          String relationship = parentHorse.sex() == Sex.FEMALE ? "mother" : "father";
          parentList.add(new ParentDto(parentDto, relationship));
        } catch (NotFoundException e) {
          throw new FatalException("Parent horse %d not found".formatted(parentRel.parentId()));
        }
      }
      return parentList;
    } catch (Exception e) {
      throw new FatalException("Error loading parent horses", e);
    }
  }

  @Override
  public Stream<HorseListDto> searchParents(String name) {
    LOG.trace("searchParents({})", name);

    if (name == null || name.isBlank()) {
      return Stream.empty();
    }

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

    // Filter by name (case-insensitive partial match) and limit to 5 results
    return horses.stream()
        .filter(horse -> {
          if (horse.name() == null) {
            return false;
          }
          return horse.name().toLowerCase().contains(name.toLowerCase());
        })
        .limit(5)
        .map(horse -> mapper.entityToListDto(horse, allOwnerMap));
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);

    // Delete parent relationships first (both as child and as parent)
    parentDao.deleteParentsByHorseId(id);
    parentDao.deleteChildrenByParentId(id);

    dao.delete(id);
  }

}
