package space.obminyashka.items_exchange.end2end;


import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import space.obminyashka.items_exchange.BasicControllerTest;
import space.obminyashka.items_exchange.dao.AdvertisementRepository;
import space.obminyashka.items_exchange.dto.AdvertisementFilterDto;
import space.obminyashka.items_exchange.dto.AdvertisementModificationDto;
import space.obminyashka.items_exchange.dto.AdvertisementTitleDto;
import space.obminyashka.items_exchange.model.enums.AgeRange;
import space.obminyashka.items_exchange.model.enums.Gender;
import space.obminyashka.items_exchange.model.enums.Season;
import space.obminyashka.items_exchange.util.AdvertisementDtoCreatingUtil;
import space.obminyashka.items_exchange.util.JsonConverter;
import space.obminyashka.items_exchange.util.ResponseMessagesHandler;
import space.obminyashka.items_exchange.util.MessageSourceUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static space.obminyashka.items_exchange.api.ApiKey.*;
import static space.obminyashka.items_exchange.util.AdvertisementDtoCreatingUtil.isResponseContainsExpectedResponse;
import static space.obminyashka.items_exchange.util.JsonConverter.asJsonString;
import static space.obminyashka.items_exchange.util.JsonConverter.jsonToObject;

@SpringBootTest
@DBRider
@AutoConfigureMockMvc
class AdvertisementFlowTest extends BasicControllerTest {

    private static final String VALID_ADV_ID = "65e3ee49-5927-40be-aafd-0461ce45f295";
    private static final String VALID_IMAGE_ID = "ebad2511-97c6-4221-a39f-a1b24a7d3251";
    private static final UUID INVALID_ID = UUID.randomUUID();
    private final AdvertisementRepository advertisementRepository;
    private final MockMultipartFile jpeg;

    @Autowired
    public AdvertisementFlowTest(MockMvc mockMvc, AdvertisementRepository advertisementRepository) throws IOException {
        super(mockMvc);
        this.advertisementRepository = advertisementRepository;
        jpeg = new MockMultipartFile("image", "test-image.jpeg", MediaType.IMAGE_JPEG_VALUE,
                Files.readAllBytes(Path.of("src/test/resources/image/test-image.jpeg")));
    }

    @ParameterizedTest
    @MethodSource("getSearchKeywords")
    @DataSet("database_init.yml")
    void findPaginated_shouldReturnSearchResults(String keyword, int expectedResultQuantity) throws Exception {
        int page = 0;
        int size = 12;

        MvcResult mvcResult = sendUriAndGetMvcResult(get(ADV_SEARCH_PAGINATED_REQUEST_PARAMS, keyword, page, size), status().isOk());
        final var totalElements = Stream.of(mvcResult.getResponse().getContentAsString().split(","))
                .filter(s -> s.startsWith("\"totalElements"))
                .map(s -> s.substring(s.length() - 1))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(0);

        assertEquals(expectedResultQuantity, totalElements);
    }

    private static Stream<Arguments> getSearchKeywords() {
        return Stream.of(
                Arguments.of("blouses description", 1), // full description matching
                Arguments.of("pajamas", 1), // full topic matching
                Arguments.of("description", 5), // partial description matching
                Arguments.of("ses", 2) // partial topic matching
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -2, 999})
    @DataSet("database_init.yml")
    void isOdd_ShouldReturnTrueForOddNumbers(long categorId) throws Exception {
        int page = 0;
        int size = 12;

        sendUriAndGetMvcResult(get(ADV_SEARCH_PAGINATED_BY_CATEGORY_ID, categorId, page, size), status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void findPaginatedAsThumbnails_shouldReturnSpecificAdvertisementTitleDto() throws Exception {
        int page = 2;
        int size = 1;
        sendUriAndGetResultAction(get(ADV_THUMBNAIL_PARAMS, page, size), status().isOk())
                .andExpect(jsonPath("$.content[0].advertisementId").value("4bd38c87-0f00-4375-bd8f-cd853f0eb9bd"))
                .andExpect(jsonPath("$.content[0].title").value("Dresses"))
                .andExpect(jsonPath("$.content[0].ownerName").value("admin"));
    }

    @Test
    @WithMockUser(username = "user")
    @DataSet("database_init.yml")
    void findPaginatedByCategoryId_shouldReturnPageResponse() throws Exception {
        long id = 1;
        int page = 0;
        int size = 12;
        sendUriAndGetResultAction(get(ADV_BY_CATEGORY_ID, id, page, size), status().isOk())
                .andExpect(jsonPath("$.content[0].advertisementId").value(VALID_ADV_ID))
                .andExpect(jsonPath("$.numberOfElements").value(advertisementRepository.count()));
    }

    @Test
    @WithMockUser(username = "user")
    @DataSet("database_init.yml")
    void findPaginatedAsThumbnails_shouldReturnPageProperQuantityOfAdvertisementWithoutRequestAdvertisement() throws Exception {
        UUID excludeAdvertisementId = UUID.fromString("65e3ee49-5927-40be-aafd-0461ce45f295");
        Long subcategoryId = 1l;
        sendUriAndGetResultAction(get(ADV_THUMBNAIL_RANDOM).queryParam("excludeAdvertisementId", excludeAdvertisementId.toString()).queryParam("subcategoryId", subcategoryId.toString()), status().isOk())
                .andExpect(jsonPath("$.size()").value(advertisementRepository.countByIdNotAndSubcategoryId(excludeAdvertisementId, subcategoryId)));
    }

    @Test
    @WithMockUser(username = "user")
    @DataSet("database_init.yml")
    void findPaginatedAsThumbnails_shouldReturnEmptyPage() throws Exception {
        UUID excludeAdvertisementId = UUID.fromString("65e3ee49-5927-40be-aafd-0461ce45f000");
        Long subcategoryId = 4l;
        sendUriAndGetResultAction(get(ADV_THUMBNAIL_RANDOM).queryParam("excludeAdvertisementId", excludeAdvertisementId.toString()).queryParam("subcategoryId", subcategoryId.toString()), status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @DisplayName("Should return all advertisements from DB as Page response")
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void findPaginatedAsThumbnails_shouldReturnPageThumbnailsResponse() throws Exception {
        sendUriAndGetResultAction(get(ADV_THUMBNAIL), status().isOk())
                .andExpect(jsonPath("$.numberOfElements").value(advertisementRepository.count()));
    }

    @Test
    @DisplayName("Should return all advertisements from DB (12 if there is more)")
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void findPaginatedAsThumbnails_shouldReturnProperQuantityOfAdvertisementsThumbnails() throws Exception {
        sendUriAndGetResultAction(get(ADV_THUMBNAIL_RANDOM), status().isOk())
                .andExpect(jsonPath("$.length()").value(advertisementRepository.count()));
    }

    @Test
    @DisplayName("Should return total size of existed advertisements")
    @DataSet("database_init.yml")
    void countAdvertisements_shouldReturnTotalAmount() throws Exception {
        final var count = advertisementRepository.count();
        final var mvcResult = sendUriAndGetMvcResult(get(ADV_TOTAL), status().isOk());
        assertEquals(String.valueOf(count), mvcResult.getResponse().getContentAsString());
    }

    @Test
    @DataSet("database_init.yml")
    void getAdvertisement_shouldReturnAdvertisementIfExists() throws Exception {
        sendUriAndGetResultAction(get(ADV_ID, VALID_ADV_ID), status().isOk())
                .andExpect(jsonPath("$.advertisementId").value(VALID_ADV_ID))
                .andExpect(jsonPath("$.topic").value("topic"))
                .andExpect(jsonPath("$.ownerName").value("super admin"))
                .andExpect(jsonPath("$.age").value("14+"))
                .andExpect(jsonPath("$.createdDate").value("01.01.2019"));
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void getAdvertisement_shouldReturnAdvertisementsIfAnyValueExists() throws Exception {
        AdvertisementFilterDto dto = AdvertisementFilterDto.builder()
                .season(Season.SUMMER)
                .gender(Gender.FEMALE)
                .age(AgeRange.FROM_10_TO_12)
                .build();

        MvcResult mvcResult = sendDtoAndGetMvcResult(post(ADV_FILTER), dto, status().isOk());

        String contentAsString = mvcResult.getResponse().getContentAsString();
        AdvertisementTitleDto[] advertisementDtos = JsonConverter.jsonToObject(contentAsString,
                AdvertisementTitleDto[].class);

        Assertions.assertAll(
                () -> assertEquals(1, advertisementDtos.length),
                () -> assertEquals(UUID.fromString("393f7bfb-cd0a-48e3-adb8-dd5b4c368f04"), advertisementDtos[0].getAdvertisementId()),
                () -> assertEquals("admin", advertisementDtos[0].getOwnerName())
        );
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "advertisement/create.yml", orderBy = {"created", "name"},
            ignoreCols = {"id", "default_photo", "created", "updated", "advertisement_id", "resource"})
    void createAdvertisement_shouldCreateValidAdvertisement() throws Exception {
        var nonExistDto = AdvertisementDtoCreatingUtil.createNonExistAdvertisementModificationDto();
        final var dtoJson = new MockMultipartFile("dto", "json", MediaType.APPLICATION_JSON_VALUE, asJsonString(nonExistDto).getBytes());
        final var contentJson = sendUriAndGetResultAction(multipart(ADV).file(jpeg).file(dtoJson), status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        assertEquals(nonExistDto, jsonToObject(contentJson, AdvertisementModificationDto.class));
        assertEquals(6, advertisementRepository.count());
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "advertisement/update.yml", orderBy = {"created", "name", "resource"}, ignoreCols = "updated")
    void updateAdvertisement_shouldUpdateExistedAdvertisement() throws Exception {
        AdvertisementModificationDto existDtoForUpdate =
                AdvertisementDtoCreatingUtil.createExistAdvertisementModificationDtoForUpdate();
        sendDtoAndGetResultAction(put(ADV_ID, existDtoForUpdate.getId()), existDtoForUpdate, status().isAccepted())
                .andExpect(jsonPath("$.description").value("new description"))
                .andExpect(jsonPath("$.topic").value("new topic"))
                .andExpect(jsonPath("$.wishesToExchange").value("BMW"));
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void createAdvertisement_shouldReturn400WhenInvalidLocationAndSubcategoryId() throws Exception {
        final var dto = AdvertisementDtoCreatingUtil.createNonExistAdvertisementModificationDto();

        dto.setLocationId(INVALID_ID);
        dto.setSubcategoryId(99L);
        final var dtoJson = new MockMultipartFile("dto", "json", MediaType.APPLICATION_JSON_VALUE, asJsonString(dto).getBytes());
        final var mvcResult = sendUriAndGetMvcResult(multipart(ADV).file(jpeg).file(dtoJson), status().isBadRequest());

        var validationLocationIdMessage = MessageSourceUtil.getMessageSource(ResponseMessagesHandler.ValidationMessage.INVALID_LOCATION_ID);
        var validationSubcategoryIdMessage = MessageSourceUtil.getMessageSource(ResponseMessagesHandler.ValidationMessage.INVALID_SUBCATEGORY_ID);
        Assertions.assertAll(
                () -> assertTrue(isResponseContainsExpectedResponse(validationLocationIdMessage, mvcResult)),
                () -> assertTrue(isResponseContainsExpectedResponse(validationSubcategoryIdMessage, mvcResult))
        );
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "advertisement/delete.yml", orderBy = {"created", "name"})
    void deleteAdvertisement_shouldDeleteExistedAdvertisement() throws Exception {
        sendUriAndGetMvcResult(delete(ADV_ID, VALID_ADV_ID), status().isOk());
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "advertisement/setDefaultImage.yml", orderBy = {"created", "name", "resource"},
            ignoreCols = {"created", "updated"})
    void setDefaultImage_success() throws Exception {
        sendUriAndGetMvcResult(post(ADV_DEFAULT_IMAGE, VALID_ADV_ID, VALID_IMAGE_ID), status().isOk());
    }


    @ParameterizedTest
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    @MethodSource("provideTestIds")
    void setDefaultImage_shouldReturn400WhenNotValidAdvertisementId(UUID firstId, UUID secondId) throws Exception {
        sendUriAndGetMvcResult(post(ADV_DEFAULT_IMAGE, firstId, secondId), status().isBadRequest());
    }

    private static Stream<Arguments> provideTestIds() {
        return Stream.of(
                Arguments.of(VALID_ADV_ID, INVALID_ID),
                Arguments.of(INVALID_ID, VALID_ADV_ID)
        );
    }
}
