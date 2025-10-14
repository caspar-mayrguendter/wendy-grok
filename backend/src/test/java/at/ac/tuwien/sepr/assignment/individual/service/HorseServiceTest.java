package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
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
}
