package space.obminyashka.items_exchange.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import space.obminyashka.items_exchange.api.ApiKey;
import space.obminyashka.items_exchange.dto.LocationDto;
import space.obminyashka.items_exchange.dto.LocationsRequest;
import space.obminyashka.items_exchange.service.LocationService;
import space.obminyashka.items_exchange.util.ResponseMessagesHandler;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static space.obminyashka.items_exchange.config.SecurityConfig.HAS_ROLE_ADMIN;
import static space.obminyashka.items_exchange.util.MessageSourceUtil.getExceptionMessageSourceWithAdditionalInfo;

@RestController
@Tag(name = "Location")
@RequiredArgsConstructor
@Validated
@Slf4j
public class LocationController {
    private final LocationService locationService;

    @GetMapping(value = ApiKey.LOCATION_ALL, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all of existed locations.")
    @ResponseStatus(HttpStatus.OK)
    public List<LocationDto> getAllLocations() {
        return locationService.findAll();
    }

    @GetMapping(value = ApiKey.LOCATION_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get an existed location by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    public ResponseEntity<LocationDto> getLocation(@PathVariable("location_id") UUID id) {
        return ResponseEntity.of(locationService.getById(id));
    }

    @PreAuthorize(HAS_ROLE_ADMIN)
    @PostMapping(value = ApiKey.LOCATION, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Save a new Location", description = "ADMIN ONLY")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CREATED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    public ResponseEntity<LocationDto> createLocation(@Valid @RequestBody LocationDto locationDto) {
        return new ResponseEntity<>(locationService.save(locationDto), HttpStatus.CREATED);
    }

    @PreAuthorize(HAS_ROLE_ADMIN)
    @PutMapping(value = ApiKey.LOCATION_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an existed Location", description = "ADMIN ONLY")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "ACCEPTED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    public ResponseEntity<LocationDto> updateLocation(@PathVariable("location_id") UUID locationId,
                                                      @Valid @RequestBody LocationDto locationDto) {
        locationDto.setId(locationId);
        return locationService.existsById(locationId) ?
                new ResponseEntity<>(locationService.update(locationDto), HttpStatus.ACCEPTED) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize(HAS_ROLE_ADMIN)
    @DeleteMapping(ApiKey.LOCATION)
    @Operation(summary = "Delete existed Locations by their IDs", description = "ADMIN ONLY")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.OK)
    public void deleteLocations(@RequestParam("ids") List<UUID> locationIds) {
        List<LocationDto> locations = locationService.findByIds(locationIds);
        if (locationIds.size() != locations.size()) {
            final String strIds = getNonExistingLocationsIds(locationIds, locations);

            log.warn("Received nonexistent IDs {}", strIds);
            throw new IllegalIdentifierException(
                    getExceptionMessageSourceWithAdditionalInfo(ResponseMessagesHandler.ExceptionMessage.ILLEGAL_ID, strIds));
        }
        locationService.removeById(locationIds);
    }

    private static String getNonExistingLocationsIds(List<UUID> locationIds, List<LocationDto> locations) {
        final var foundLocationsIdsToBeRemoved = locations.stream()
                .map(LocationDto::getId)
                .toList();

        return locationIds.stream()
                .filter(Predicate.not(foundLocationsIdsToBeRemoved::contains))
                .map(String::valueOf)
                .collect(Collectors.joining(", ", ": ", ""));
    }

    @PreAuthorize(HAS_ROLE_ADMIN)
    @PostMapping(value = ApiKey.LOCATIONS_INIT, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Operation(summary = "Setting up locations from request", description = "ADMIN ONLY")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> createLocationsInitFile(@RequestBody LocationsRequest locationsRequest) {
        try {
            return new ResponseEntity<>(locationService.createParsedLocationsFile(locationsRequest.rawLocations), HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
