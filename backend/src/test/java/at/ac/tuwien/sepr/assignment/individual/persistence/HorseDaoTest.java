package at.ac.tuwien.sepr.assignment.individual.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Horse;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.type.Sex;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for {@link HorseDao}, ensuring database operations function correctly.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile to load test data
@SpringBootTest
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;

  /**
   * Tests that retrieving all stored horses returns at least one entry
   * and verifies that a specific horse exists in the test dataset.
   */
  @Test
  public void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.getAll();
    assertThat(horses.size()).isGreaterThanOrEqualTo(1); // TODO adapt to exact number of elements in test data later
    assertThat(horses)
        .extracting(Horse::id, Horse::name)
        .contains(tuple(-1L, "Wendy"));
  }

  /**
   * Tests that creating a horse with valid data works correctly.
   */
  @Test
  public void createHorseWithValidData() {
    HorseCreateDto horseToCreate = new HorseCreateDto(
        "DAO Test Horse",
        "Created via DAO",
        LocalDate.of(2022, 7, 20),
        Sex.MALE,
        null,
        List.of()
    );

    Horse createdHorse = horseDao.create(horseToCreate);

    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.id()).isNotNull();
    assertThat(createdHorse.name()).isEqualTo("DAO Test Horse");
    assertThat(createdHorse.description()).isEqualTo("Created via DAO");
    assertThat(createdHorse.dateOfBirth()).isEqualTo(LocalDate.of(2022, 7, 20));
    assertThat(createdHorse.sex()).isEqualTo(Sex.MALE);
    assertThat(createdHorse.ownerId()).isNull();
  }

  /**
   * Tests that getting a horse by ID returns the correct horse.
   *
   * @throws NotFoundException if the horse is not found
   */
  @Test
  public void getByIdReturnsCorrectHorse() throws NotFoundException {
    // Create a horse first to ensure we have one to retrieve
    HorseCreateDto horseToCreate = new HorseCreateDto(
        "GetById Test Horse",
        null,
        LocalDate.of(2023, 1, 15),
        Sex.FEMALE,
        null,
        List.of()
    );

    Horse createdHorse = horseDao.create(horseToCreate);

    // Now retrieve it by ID
    Horse retrievedHorse = horseDao.getById(createdHorse.id());

    assertThat(retrievedHorse).isNotNull();
    assertThat(retrievedHorse.id()).isEqualTo(createdHorse.id());
    assertThat(retrievedHorse.name()).isEqualTo("GetById Test Horse");
    assertThat(retrievedHorse.description()).isNull();
    assertThat(retrievedHorse.dateOfBirth()).isEqualTo(LocalDate.of(2023, 1, 15));
    assertThat(retrievedHorse.sex()).isEqualTo(Sex.FEMALE);
  }

  /**
   * Tests that getting a horse by nonexistent ID throws NotFoundException.
   */
  @Test
  public void getByIdWithNonexistentIdThrowsNotFoundException() {
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> horseDao.getById(99999L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("No horse with ID 99999 found");
  }
}
