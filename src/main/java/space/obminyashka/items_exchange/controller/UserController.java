package space.obminyashka.items_exchange.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import space.obminyashka.items_exchange.api.ApiKey;
import space.obminyashka.items_exchange.dto.*;
import space.obminyashka.items_exchange.exception.DataConflictException;
import space.obminyashka.items_exchange.exception.InvalidDtoException;
import space.obminyashka.items_exchange.model.User;
import space.obminyashka.items_exchange.service.AdvertisementService;
import space.obminyashka.items_exchange.service.ImageService;
import space.obminyashka.items_exchange.service.UserService;
import space.obminyashka.items_exchange.util.PatternHandler;
import space.obminyashka.items_exchange.util.ResponseMessagesHandler;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

import static space.obminyashka.items_exchange.util.MessageSourceUtil.getMessageSource;
import static space.obminyashka.items_exchange.util.MessageSourceUtil.getParametrizedMessageSource;
import static space.obminyashka.items_exchange.util.ResponseMessagesHandler.ValidationMessage.*;

@RestController
@Tag(name = "User")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    @Value(ResponseMessagesHandler.ValidationMessage.INCORRECT_PASSWORD)
    public String incorrectPassword;
    private static final int MAX_CHILDREN_AMOUNT = 10;

    private final UserService userService;
    private final ImageService imageService;
    private final AdvertisementService advService;

    @GetMapping(value = ApiKey.USER_MY_INFO, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Find a registered requested user's data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    public ResponseEntity<UserDto> getPersonalInfo(@Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.of(userService.findByUsername(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @PutMapping(value = ApiKey.USER_MY_INFO, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Operation(summary = "Update a registered requested user's data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String updateUserInfo(@Valid @RequestBody UserUpdateDto userUpdateDto, @Parameter(hidden = true) Authentication authentication) {
        return userService.update(userUpdateDto, getUser(authentication.getName()));
    }

    @GetMapping(value = ApiKey.USER_MY_ADV, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a registered requested user's data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    @ResponseStatus(HttpStatus.OK)
    public List<AdvertisementTitleDto> getCreatedAdvertisements(@Parameter(hidden = true) Authentication authentication) {
        return advService.findAllByUsername(authentication.getName());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @PutMapping(value = ApiKey.USER_SERVICE_CHANGE_PASSWORD, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Operation(summary = "Update a user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String updateUserPassword(@Valid @RequestBody UserChangePasswordDto userChangePasswordDto,
                                     @Parameter(hidden = true) Authentication authentication) throws InvalidDtoException {
        User user = findUserByValidCredentials(authentication, userChangePasswordDto.getOldPassword());

        return userService.updateUserPassword(userChangePasswordDto, user);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @PutMapping(value = ApiKey.USER_SERVICE_CHANGE_EMAIL, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Operation(summary = "Update a user email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @ApiResponse(responseCode = "409", description = "CONFLICT")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String updateUserEmail(@Parameter(name = "email", description = "New email", example = "username@example.com")
                                  @Email(regexp = PatternHandler.EMAIL, message = "{" + INVALID_EMAIL + "}")
                                  @RequestParam String email,
                                  @Parameter(hidden = true) Authentication authentication) throws DataConflictException {
        User user = getUser(authentication.getName());
        checkEmailUniqueAndNotUsed(user.getEmail(), email);
        user.setEmail(email);
        userService.update(user);

        return getMessageSource(ResponseMessagesHandler.PositiveMessage.CHANGED_USER_EMAIL);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @DeleteMapping(value = ApiKey.USER_SERVICE_DELETE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Operation(summary = "Delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String selfDeleteRequest(@Valid @RequestBody UserDeleteFlowDto userDeleteFlowDto, @Parameter(hidden = true) Authentication authentication)
            throws InvalidDtoException {
        User user = findUserByValidCredentials(authentication, userDeleteFlowDto.getPassword());
        userService.selfDeleteRequest(user);

        return getParametrizedMessageSource(ResponseMessagesHandler.PositiveMessage.DELETE_ACCOUNT,
                userService.calculateDaysBeforeCompleteRemove(user.getUpdated()));
    }

    @PreAuthorize("hasRole('SELF_REMOVING')")
    @PutMapping(value = ApiKey.USER_SERVICE_RESTORE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Operation(summary = "Restore user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String makeAccountActiveAgain(@Valid @RequestBody UserDeleteFlowDto userDeleteFlowDto, @Parameter(hidden = true) Authentication authentication)
            throws InvalidDtoException {
        User user = findUserByValidCredentials(authentication, userDeleteFlowDto.getPassword());
        userService.makeAccountActiveAgain(user.getUsername());

        return getMessageSource(ResponseMessagesHandler.PositiveMessage.ACCOUNT_ACTIVE_AGAIN);
    }

    @GetMapping(value = ApiKey.USER_CHILD, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Find a registered requested user's children data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    @ResponseStatus(HttpStatus.OK)
    public List<ChildDto> getChildren(@Parameter(hidden = true) Authentication authentication) {
        return userService.getChildren(getUser(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @PutMapping(value = ApiKey.USER_CHILD, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update child data for a registered requested user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.OK)
    public List<ChildDto> updateChildren(@Size(max = MAX_CHILDREN_AMOUNT, message = "{" + ResponseMessagesHandler.ExceptionMessage.CHILDREN_AMOUNT + "}")
                                         @RequestBody List<@Valid ChildDto> childrenDto,
                                         @Parameter(hidden = true) Authentication authentication) {
        final User user = getUser(authentication.getName());
        return userService.updateChildren(user, childrenDto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @PostMapping(value = ApiKey.USER_SERVICE_CHANGE_AVATAR, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set a new user's avatar image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "ACCEPTED"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN"),
            @ApiResponse(responseCode = "406", description = "NOT ACCEPTABLE"),
            @ApiResponse(responseCode = "415", description = "UNSUPPORTED MEDIA TYPE")})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, byte[]> updateUserAvatar(@RequestPart MultipartFile image, @Parameter(hidden = true) Authentication authentication) {
        User user = getUser(authentication.getName());
        byte[] newAvatarImage = imageService.scale(image);
        userService.setUserAvatar(newAvatarImage, user);
        return Map.of("avatarImage", newAvatarImage);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @DeleteMapping(ApiKey.USER_SERVICE_CHANGE_AVATAR)
    @Operation(summary = "Remove a user's avatar image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN")})
    @ResponseStatus(HttpStatus.OK)
    public void removeAvatar(@Parameter(hidden = true) Authentication authentication) {
        userService.removeUserAvatarFor(authentication.getName());
    }

    private User findUserByValidCredentials(Authentication authentication, String password) throws InvalidDtoException {
        User user = getUser(authentication.getName());
        if (!userService.isPasswordMatches(user, password)) {
            throw new InvalidDtoException(getMessageSource(incorrectPassword));
        }
        return user;
    }

    private User getUser(String username) {
        return userService.findByUsernameOrEmail(username).orElseThrow(() -> new UsernameNotFoundException(
                getMessageSource(ResponseMessagesHandler.ExceptionMessage.USER_NOT_FOUND)));
    }

    private void checkEmailUniqueAndNotUsed(String currentEmail, String newEmail) throws DataConflictException {
        if (currentEmail.equals(newEmail)) {
            throw new DataConflictException(getMessageSource(ResponseMessagesHandler.ExceptionMessage.EMAIL_OLD));
        }
        if (userService.existsByEmail(newEmail)) {
            throw new DataConflictException(getMessageSource(
                    ResponseMessagesHandler.ValidationMessage.DUPLICATE_EMAIL));
        }
    }
}
