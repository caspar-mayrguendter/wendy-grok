package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseUpdateDto;
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
    List<HorseListDto> horses = horseService.allHorses()
        .toList();

    assertThat(horses.size()).isGreaterThanOrEqualTo(1); // TODO: Adapt to exact number of test data entries

    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.FEMALE));
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
        null // no owner
    );

    HorseDetailDto createdHorse = horseService.create(horseToCreate);

    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.name()).isEqualTo("Test Horse");
    assertThat(createdHorse.description()).isEqualTo("A beautiful test horse");
    assertThat(createdHorse.dateOfBirth()).isEqualTo(LocalDate.of(2020, 5, 15));
    assertThat(createdHorse.sex()).isEqualTo(Sex.MALE);
    assertThat(createdHorse.owner()).isNull();
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
        null // no owner
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
        null
    );

    HorseDetailDto createdHorse = horseService.create(horseToCreate);

    // Now update the horse
    HorseUpdateDto horseToUpdate = new HorseUpdateDto(
        createdHorse.id(),
        "Updated Horse Name",
        "Updated description",
        LocalDate.of(2021, 6, 15),
        Sex.FEMALE,
        null
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
        null
    );

    HorseDetailDto createdHorse = horseService.create(horseToCreate);

    // Now try to update with invalid data
    HorseUpdateDto horseToUpdate = new HorseUpdateDto(
        createdHorse.id(),
        null, // missing name
        "Updated description",
        LocalDate.of(2021, 6, 15),
        Sex.FEMALE,
        null
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> horseService.update(horseToUpdate))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Validation of horse for update failed")
        .hasMessageContaining("Horse name is mandatory");
  }
}
