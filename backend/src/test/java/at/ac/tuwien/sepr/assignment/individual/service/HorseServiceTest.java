package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for {@link HorseService}.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
public class HorseServiceTest {

  @Autowired
  HorseService horseService;

  /**
   * Tests whether retrieving all stored horses returns the expected number and specific entries.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {
    List<HorseListDto> horses = horseService.searchHorses(new HorseSearchDto(null, null, null, null, null, null))
        .toList();

    assertThat(horses.size()).isGreaterThanOrEqualTo(1); // TODO: Adapt to exact number of test data entries

    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(2001L, Sex.FEMALE));
  }

  /**
   * Tests that creating a horse with valid data works correctly.
   *
   * @throws Exception if the creation fails
   */
  @Test
  public void createHorseWithValidData() throws Exception {
    HorseCreateDto horseToCreate = new HorseCreateDto(
        "Test Horse",
        "A beautiful test horse",
        LocalDate.of(2020, 5, 15),
        Sex.MALE,
        null, // no owner
        List.of() // no parents
    );

    HorseDetailDto createdHorse = horseService.create(horseToCreate);

    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.name()).isEqualTo("Test Horse");
    assertThat(createdHorse.description()).isEqualTo("A beautiful test horse");
    assertThat(createdHorse.dateOfBirth()).isEqualTo(LocalDate.of(2020, 5, 15));
    assertThat(createdHorse.sex()).isEqualTo(Sex.MALE);
    assertThat(createdHorse.owner()).isNull();
    assertThat(createdHorse.parents()).isEmpty();
    assertThat(createdHorse.id()).isNotNull();
  }

  /**
   * Tests that creating a horse with missing mandatory field (name) throws ValidationException.
   */
  @Test
  public void createHorseWithMissingNameThrowsValidationException() {
    HorseCreateDto horseToCreate = new HorseCreateDto(
        null, // missing name
        "A beautiful test horse",
        LocalDate.of(2020, 5, 15),
        Sex.MALE,
        null, // no owner
        List.of() // no parents
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> horseService.create(horseToCreate))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Validation of horse for create failed")
        .hasMessageContaining("Horse name is mandatory");
  }

  /**
   * Tests that updating a horse with valid data works correctly.
   *
   * @throws Exception if the update fails
   */
  @Test
  public void updateHorseWithValidData() throws Exception {
    // First create a horse
    HorseCreateDto horseToCreate = new HorseCreateDto(
        "Original Horse",
        "Original description",
        LocalDate.of(2020, 1, 1),
        Sex.MALE,
        null,
        List.of()
    );

    HorseDetailDto createdHorse = horseService.create(horseToCreate);

    // Now update the horse
    HorseUpdateDto horseToUpdate = new HorseUpdateDto(
        createdHorse.id(),
        "Updated Horse Name",
        "Updated description",
        LocalDate.of(2021, 6, 15),
        Sex.FEMALE,
        null,
        List.of()
    );

    HorseDetailDto updatedHorse = horseService.update(horseToUpdate);

    assertThat(updatedHorse).isNotNull();
    assertThat(updatedHorse.id()).isEqualTo(createdHorse.id());
    assertThat(updatedHorse.name()).isEqualTo("Updated Horse Name");
    assertThat(updatedHorse.description()).isEqualTo("Updated description");
    assertThat(updatedHorse.dateOfBirth()).isEqualTo(LocalDate.of(2021, 6, 15));
    assertThat(updatedHorse.sex()).isEqualTo(Sex.FEMALE);
    assertThat(updatedHorse.owner()).isNull();
  }

  /**
   * Tests that updating a horse with missing mandatory field (name) throws ValidationException.
   *
   * @throws Exception if the creation fails
   */
  @Test
  public void updateHorseWithMissingNameThrowsValidationException() throws Exception {
    // First create a horse
    HorseCreateDto horseToCreate = new HorseCreateDto(
        "Horse to Update",
        "Description",
        LocalDate.of(2020, 1, 1),
        Sex.MALE,
        null,
        List.of()
    );

    HorseDetailDto createdHorse = horseService.create(horseToCreate);

    // Now try to update with invalid data
    HorseUpdateDto horseToUpdate = new HorseUpdateDto(
        createdHorse.id(),
        null, // missing name
        "Updated description",
        LocalDate.of(2021, 6, 15),
        Sex.FEMALE,
        null,
        List.of()
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> horseService.update(horseToUpdate))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Validation of horse for update failed")
        .hasMessageContaining("Horse name is mandatory");
  }

  /**
   * Tests that creating a horse with valid parents works correctly.
   *
   * @throws Exception if the creation fails
   */
  @Test
  public void createHorseWithValidParents() throws Exception {
    // First create parent horses
    HorseCreateDto motherDto = new HorseCreateDto(
        "Mother Horse",
        "A female parent horse",
        LocalDate.of(2010, 1, 1),
        Sex.FEMALE,
        null,
        List.of()
    );
    HorseDetailDto mother = horseService.create(motherDto);

    HorseCreateDto fatherDto = new HorseCreateDto(
        "Father Horse",
        "A male parent horse",
        LocalDate.of(2008, 1, 1),
        Sex.MALE,
        null,
        List.of()
    );
    HorseDetailDto father = horseService.create(fatherDto);

    // Now create a horse with these parents
    HorseCreateDto horseToCreate = new HorseCreateDto(
        "Child Horse",
        "Horse with parents",
        LocalDate.of(2022, 6, 15),
        Sex.MALE,
        null,
        List.of(mother.id(), father.id())
    );

    HorseDetailDto createdHorse = horseService.create(horseToCreate);

    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.name()).isEqualTo("Child Horse");
    assertThat(createdHorse.parents()).hasSize(2);
    // Check that we have both mother and father
    assertThat(createdHorse.parents())
        .anyMatch(parent -> parent.relationship().equals("mother") && parent.horse().name().equals("Mother Horse"));
    assertThat(createdHorse.parents())
        .anyMatch(parent -> parent.relationship().equals("father") && parent.horse().name().equals("Father Horse"));
  }

  /**
   * Tests that creating a horse with invalid parents (same horse as mother and father) throws ValidationException.
   */
  @Test
  public void createHorseWithInvalidParentsSameHorse() {
    // Create a parent horse
    HorseCreateDto parentDto = new HorseCreateDto(
        "Parent Horse",
        "A parent horse",
        LocalDate.of(2010, 1, 1),
        Sex.FEMALE,
        null,
        List.of()
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      HorseDetailDto parent = horseService.create(parentDto);

      // Try to create a horse with the same horse as both parents
      HorseCreateDto horseToCreate = new HorseCreateDto(
          "Invalid Child",
          "Horse with invalid parents",
          LocalDate.of(2022, 6, 15),
          Sex.MALE,
          null,
          List.of(parent.id(), parent.id()) // same ID for both
      );

      horseService.create(horseToCreate);
    })
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Validation of horse for create failed")
        .hasMessageContaining("A horse cannot have the same horse as both parents");
  }

  /**
   * Tests that creating a horse with parents of the same gender throws ValidationException.
   */
  @Test
  public void createHorseWithInvalidParentsSameGender() {
    // Create two parent horses of the same gender
    HorseCreateDto parent1Dto = new HorseCreateDto(
        "Parent 1",
        "A female parent",
        LocalDate.of(2010, 1, 1),
        Sex.FEMALE,
        null,
        List.of()
    );

    HorseCreateDto parent2Dto = new HorseCreateDto(
        "Parent 2",
        "Another female parent",
        LocalDate.of(2009, 1, 1),
        Sex.FEMALE,
        null,
        List.of()
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      HorseDetailDto parent1 = horseService.create(parent1Dto);
      HorseDetailDto parent2 = horseService.create(parent2Dto);

      // Try to create a horse with two parents of the same gender
      HorseCreateDto horseToCreate = new HorseCreateDto(
          "Invalid Child",
          "Horse with same gender parents",
          LocalDate.of(2022, 6, 15),
          Sex.MALE,
          null,
          List.of(parent1.id(), parent2.id())
      );

      horseService.create(horseToCreate);
    })
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Validation of horse for create failed")
        .hasMessageContaining("Parents must have different genders");
  }

  /**
   * Tests that creating a horse with more than 2 parents throws ValidationException.
   */
  @Test
  public void createHorseWithTooManyParents() {
    // Create three parent horses
    HorseCreateDto parent1Dto = new HorseCreateDto(
        "Parent 1",
        "A female parent",
        LocalDate.of(2010, 1, 1),
        Sex.FEMALE,
        null,
        List.of()
    );

    HorseCreateDto parent2Dto = new HorseCreateDto(
        "Parent 2",
        "A male parent",
        LocalDate.of(2009, 1, 1),
        Sex.MALE,
        null,
        List.of()
    );

    HorseCreateDto parent3Dto = new HorseCreateDto(
        "Parent 3",
        "Another parent",
        LocalDate.of(2008, 1, 1),
        Sex.FEMALE,
        null,
        List.of()
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      HorseDetailDto parent1 = horseService.create(parent1Dto);
      HorseDetailDto parent2 = horseService.create(parent2Dto);
      HorseDetailDto parent3 = horseService.create(parent3Dto);

      // Try to create a horse with three parents
      HorseCreateDto horseToCreate = new HorseCreateDto(
          "Invalid Child",
          "Horse with too many parents",
          LocalDate.of(2022, 6, 15),
          Sex.MALE,
          null,
          List.of(parent1.id(), parent2.id(), parent3.id())
      );

      horseService.create(horseToCreate);
    })
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Validation of horse for create failed")
        .hasMessageContaining("A horse cannot have more than 2 parents");
  }

  /**
   * Tests that searching with no criteria returns all horses.
   */
  @Test
  public void searchWithNoCriteriaReturnsAllHorses() {
    List<HorseListDto> horses = horseService.searchHorses(new HorseSearchDto(null, null, null, null, null, null))
        .toList();

    assertThat(horses.size()).isGreaterThanOrEqualTo(1);
  }

  /**
   * Tests that searching by name returns correct results.
   */
  @Test
  public void searchByNameReturnsMatchingHorses() throws ValidationException, ConflictException {
    // First, create a horse with a specific name
    HorseCreateDto horseDto = new HorseCreateDto(
        "SearchTest Horse",
        "A horse for search testing",
        LocalDate.of(2020, 5, 15),
        Sex.FEMALE,
        null,
        List.of()
    );

    HorseDetailDto createdHorse = horseService.create(horseDto);

    // Search by partial name
    List<HorseListDto> searchResults = horseService.searchHorses(new HorseSearchDto("Search", null, null, null, null, null))
        .toList();

    assertThat(searchResults)
        .isNotEmpty()
        .anyMatch(horse -> horse.id().equals(createdHorse.id()));
  }

  /**
   * Tests that searching with non-matching criteria returns no results.
   */
  @Test
  public void searchWithNonMatchingCriteriaReturnsEmpty() {
    List<HorseListDto> searchResults = horseService.searchHorses(new HorseSearchDto("NonExistentHorseName12345", null, null, null, null, null))
        .toList();

    assertThat(searchResults).isEmpty();
  }

  /**
   * Tests that searching by sex filters correctly.
   */
  @Test
  public void searchBySexReturnsOnlyMatchingSex() {
    List<HorseListDto> maleHorses = horseService.searchHorses(new HorseSearchDto(null, null, null, Sex.MALE, null, null))
        .toList();

    List<HorseListDto> femaleHorses = horseService.searchHorses(new HorseSearchDto(null, null, null, Sex.FEMALE, null, null))
        .toList();

    // All male horses should be male
    maleHorses.forEach(horse -> assertThat(horse.sex()).isEqualTo(Sex.MALE));

    // All female horses should be female
    femaleHorses.forEach(horse -> assertThat(horse.sex()).isEqualTo(Sex.FEMALE));
  }
}
