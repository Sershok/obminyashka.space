package space.obminyashka.items_exchange.util;

import org.springframework.test.web.servlet.MvcResult;
import space.obminyashka.items_exchange.dto.AdvertisementDto;
import space.obminyashka.items_exchange.dto.ImageDto;
import space.obminyashka.items_exchange.dto.LocationDto;
import space.obminyashka.items_exchange.model.enums.AgeRange;
import space.obminyashka.items_exchange.model.enums.DealType;
import space.obminyashka.items_exchange.model.enums.Gender;
import space.obminyashka.items_exchange.model.enums.I18n;
import space.obminyashka.items_exchange.model.enums.Season;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdvertisementDtoCreatingUtil {

    private static final ImageDto GIF = new ImageDto(0L, "test image gif".getBytes());
    private static final ImageDto JPEG = new ImageDto(1L, "test image jpeg".getBytes());
    private static final ImageDto PNG = new ImageDto(2L, "test image png".getBytes());
    private static final LocationDto KYIV = new LocationDto(0L, "Kyivska", "District", "Kyiv", I18n.EN);
    private static final LocationDto KHARKIV = new LocationDto(1L, "Kharkivska", "Kharkivska district", "Kharkiv", I18n.EN);
    private static final LocationDto ODESSA = new LocationDto(0L, "Odesska", "Odessa district", "Odessa", I18n.EN);
    private static final LocationDto CHANGEDLOCATION =
            new LocationDto(1L, "Kyivska", "New Vasyuki district", "New Vasyuki", I18n.EN);

    private static final LocationDto NOT_VALID_LOCATION =
            new LocationDto(2L, "b", "b", "b", I18n.EN);
    private static final String NOT_VALID_DESCRIPTION = createString(256);
    private static final String NOT_VALID_WISHES = createString(211);
    private static final String NOT_VALID_SIZE = createString(0);
    private static final String NOT_VALID_TOPIC = createString(2);

    public static AdvertisementDto createNonExistAdvertisementDto() {
        return getBuild(0L, "topic", "description", "hat",false, DealType.GIVEAWAY,
                KYIV, AgeRange.YOUNGER_THAN_1, Season.DEMI_SEASON, Gender.MALE, "M", 1L, Collections.singletonList(GIF));
    }

    public static AdvertisementDto createExistAdvertisementDto() {
        return getBuild(1L, "topic", "description", "shoes", true, DealType.EXCHANGE,
                KHARKIV, AgeRange.OLDER_THAN_14, Season.SUMMER, Gender.MALE, "40", 1L, Arrays.asList(JPEG, PNG));
    }

    public static AdvertisementDto createExistAdvertisementDtoForUpdateWithNewLocationChangedImagesAndSubcategory() {
        return getBuild(1L, "new topic", "new description", "BMW",true, DealType.EXCHANGE,
                ODESSA, AgeRange.OLDER_THAN_14, Season.SUMMER, Gender.FEMALE, "50", 2L, Collections.singletonList(JPEG));
    }

    public static AdvertisementDto createExistAdvertisementDtoForUpdateWithUpdatedLocationChangedImagesAndSubcategory() {
        return getBuild(1L, "new topic", "new description", "BMW",true, DealType.EXCHANGE,
                CHANGEDLOCATION, AgeRange.OLDER_THAN_14, Season.SUMMER, Gender.FEMALE, "50", 2L, Collections.singletonList(JPEG));
    }

    public static AdvertisementDto createExistAdvertisementDtoForUpdateWithNotValidFields() {
        return getBuild(1L, NOT_VALID_TOPIC, NOT_VALID_DESCRIPTION, NOT_VALID_WISHES, true, DealType.EXCHANGE,
                NOT_VALID_LOCATION, AgeRange.OLDER_THAN_14, Season.SUMMER, Gender.MALE, NOT_VALID_SIZE, 1L, Arrays.asList(JPEG, PNG));
    }

    private static AdvertisementDto getBuild(long aId, String topic, String description, String wishes, boolean offer,
                                             DealType exchange, LocationDto city, AgeRange age, Season season,
                                             Gender gender, String size, long subcatId, List<ImageDto> images) {
        return AdvertisementDto.builder()
                .id(aId)
                .topic(topic)
                .description(description)
                .wishesToExchange(wishes)
                .readyForOffers(offer)
                .dealType(exchange)
                .location(city)
                .age(age)
                .season(season)
                .gender(gender)
                .size(size)
                .subcategoryId(subcatId)
                .images(images)
                .build();
    }

    public static String createValidationMessage(String dtoFieldName, String dtoFieldValue, String minValidValue, String maxValidValue) {
        return MessageSourceUtil.getMessageSource("invalid.size")
                .replace("${validatedValue}",
                        "updateAdvertisement.dto." + dtoFieldName + ": " + dtoFieldValue)
                .replace("{min}", minValidValue)
                .replace("{max}", maxValidValue);
    }

    public static String createValidationMessage(String dtoFieldName, String dtoFieldValue, String maxValidValue) {
        return MessageSourceUtil.getMessageSource("invalid.max-size")
                .replace("${validatedValue}",
                        "updateAdvertisement.dto." + dtoFieldName + ": " + dtoFieldValue)
                .replace("{max}", maxValidValue);
    }

    public static boolean isResponseContainsExpectedResponse(String expectedResponse, MvcResult mvcResult) throws UnsupportedEncodingException {
        return mvcResult.getResponse().getContentAsString().contains(expectedResponse);
    }

    private static String createString(int quantityOfCharsInNewsString){
        return "x".repeat(Math.max(0, quantityOfCharsInNewsString));
    }
}
