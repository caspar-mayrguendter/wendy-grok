package at.ac.tuwien.sepr.assignment.individual.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.tuwien.sepr.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepr.assignment.individual.dto.HorseListDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Integration tests for the Horse REST API endpoint.
 */
@ActiveProfiles({"test", "datagen"}) // Enables "test" Spring profile during test execution
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Sets up the MockMvc instance before each test.
   */
  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  /**
   * Tests retrieving all horses from the endpoint.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void gettingAllHorses() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult.size()).isGreaterThanOrEqualTo(1); // TODO: Adapt this to the exact number in the test data later
    assertThat(horseResult)
        .extracting(HorseListDto::id, HorseListDto::name)
        .contains(tuple(2001L, "Wendy"));
  }

  /**
   * Tests that accessing a nonexistent URL returns a 404 status.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/asdf123")
        ).andExpect(status().isNotFound());
  }

  /**
   * Tests that creating a horse with valid data via REST API works correctly.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void createHorseWithValidData() throws Exception {
    String horseJson = """
        {
          "name": "REST Test Horse",
          "description": "Created via REST API",
          "dateOfBirth": "2021-03-10",
          "sex": "FEMALE",
          "ownerId": null
        }
        """;

    byte[] response = mockMvc
        .perform(MockMvcRequestBuilders
            .post("/horses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(horseJson)
        ).andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsByteArray();

    HorseDetailDto createdHorse = objectMapper.readerFor(HorseDetailDto.class).readValue(response);
    assertThat(createdHorse).isNotNull();
    assertThat(createdHorse.name()).isEqualTo("REST Test Horse");
    assertThat(createdHorse.description()).isEqualTo("Created via REST API");
    assertThat(createdHorse.dateOfBirth()).isEqualTo(java.time.LocalDate.of(2021, 3, 10));
    assertThat(createdHorse.sex()).isEqualTo(at.ac.tuwien.sepr.assignment.individual.type.Sex.FEMALE);
  }

  /**
   * Tests that creating a horse with missing mandatory field returns 422 Unprocessable Entity.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void createHorseWithMissingNameReturns422() throws Exception {
    String horseJson = """
        {
          "description": "Missing name field",
          "dateOfBirth": "2021-03-10",
          "sex": "FEMALE",
          "ownerId": null
        }
        """;

    mockMvc
        .perform(MockMvcRequestBuilders
            .post("/horses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(horseJson)
        ).andExpect(status().isUnprocessableEntity());
  }

  /**
   * Tests that getting a horse by ID returns the correct horse.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void getHorseByIdReturnsCorrectHorse() throws Exception {
    // Get the first horse from the list to get its ID
    byte[] listResponse = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horses = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(listResponse).readAll();
    assertThat(horses).isNotEmpty();

    HorseListDto firstHorse = horses.getFirst();

    // Now get the detailed horse by ID
    byte[] detailResponse = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses/" + firstHorse.id())
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    HorseDetailDto horseDetail = objectMapper.readerFor(HorseDetailDto.class).readValue(detailResponse);
    assertThat(horseDetail).isNotNull();
    assertThat(horseDetail.id()).isEqualTo(firstHorse.id());
    assertThat(horseDetail.name()).isEqualTo(firstHorse.name());
  }

  /**
   * Tests that getting a nonexistent horse ID returns 404.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void getNonexistentHorseReturns404() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses/99999")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
  }
}
