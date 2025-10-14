package at.ac.tuwien.sepr.assignment.individual.service;

import static org.assertj.core.api.Assertions.assertThat;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepr.assignment.individual.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Test class for OwnerService.
 */
@SpringBootTest
@ActiveProfiles({"test", "datagen"})
@Sql({"/sql/createSchema.sql", "/sql/insertData.sql"})
public class OwnerServiceTest {

  @Autowired
  OwnerService ownerService;

  /**
   * Tests that creating an owner with valid data works correctly.
   */
  @Test
  public void createOwnerWithValidDataWorks() throws ValidationException {
    OwnerCreateDto ownerToCreate = new OwnerCreateDto(
        "John",
        "Doe",
        "john.doe@example.com"
    );

    OwnerDto createdOwner = ownerService.create(ownerToCreate);

    assertThat(createdOwner.firstName()).isEqualTo("John");
    assertThat(createdOwner.lastName()).isEqualTo("Doe");
    assertThat(createdOwner.email()).isEqualTo("john.doe@example.com");
    assertThat(createdOwner.id()).isNotNull();
    assertThat(createdOwner.id()).isGreaterThan(0);
  }

  /**
   * Tests that creating an owner with null email works (email is optional).
   */
  @Test
  public void createOwnerWithNullEmailWorks() throws ValidationException {
    OwnerCreateDto ownerToCreate = new OwnerCreateDto(
        "Jane",
        "Smith",
        null
    );

    OwnerDto createdOwner = ownerService.create(ownerToCreate);

    assertThat(createdOwner.firstName()).isEqualTo("Jane");
    assertThat(createdOwner.lastName()).isEqualTo("Smith");
    assertThat(createdOwner.email()).isNull();
    assertThat(createdOwner.id()).isNotNull();
  }

  /**
   * Tests that creating an owner with missing first name throws ValidationException.
   */
  @Test
  public void createOwnerWithMissingFirstNameThrowsValidationException() {
    OwnerCreateDto ownerToCreate = new OwnerCreateDto(
        null, // missing first name
        "Doe",
        "john.doe@example.com"
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      ownerService.create(ownerToCreate);
    })
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Owner first name is mandatory");
  }

  /**
   * Tests that creating an owner with blank first name throws ValidationException.
   */
  @Test
  public void createOwnerWithBlankFirstNameThrowsValidationException() {
    OwnerCreateDto ownerToCreate = new OwnerCreateDto(
        "", // blank first name
        "Doe",
        "john.doe@example.com"
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      ownerService.create(ownerToCreate);
    })
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Owner first name is mandatory");
  }

  /**
   * Tests that creating an owner with missing last name throws ValidationException.
   */
  @Test
  public void createOwnerWithMissingLastNameThrowsValidationException() {
    OwnerCreateDto ownerToCreate = new OwnerCreateDto(
        "John",
        null, // missing last name
        "john.doe@example.com"
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      ownerService.create(ownerToCreate);
    })
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Owner last name is mandatory");
  }

  /**
   * Tests that creating an owner with blank last name throws ValidationException.
   */
  @Test
  public void createOwnerWithBlankLastNameThrowsValidationException() {
    OwnerCreateDto ownerToCreate = new OwnerCreateDto(
        "John",
        "", // blank last name
        "john.doe@example.com"
    );

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
      ownerService.create(ownerToCreate);
    })
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Owner last name is mandatory");
  }
}
