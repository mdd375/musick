package ru.m0vt.musick.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.AlbumBriefDTO;
import ru.m0vt.musick.dto.ArtistCreateDTO;
import ru.m0vt.musick.dto.ArtistCreateResponseDTO;
import ru.m0vt.musick.dto.ArtistUpdateDTO;
import ru.m0vt.musick.model.Artist;
import ru.m0vt.musick.model.Review;
import ru.m0vt.musick.model.Subscription;
import ru.m0vt.musick.service.ArtistService;

@RestController
@RequestMapping("/artists")
@Tag(
    name = "Артисты",
    description = "API для работы с артистами, их альбомами и подписчиками"
)
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @Operation(
        summary = "Получение списка всех артистов",
        description = "Возвращает список всех артистов, зарегистрированных в системе"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Список артистов успешно получен"
            ),
        }
    )
    @GetMapping
    public ResponseEntity<List<Artist>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    @Operation(
        summary = "Получение артиста по ID",
        description = "Возвращает информацию об артисте по указанному ID"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Артист успешно найден"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Артист не найден"
            ),
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Artist> getArtistById(
        @Parameter(description = "ID артиста") @PathVariable Long id
    ) {
        Artist artist = artistService.getArtistById(id);
        if (artist == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(artist);
    }

    @Operation(
        summary = "Создание профиля артиста",
        description = "Создает профиль артиста для текущего пользователя, обновляет роль пользователя и возвращает новый JWT токен. Доступно только пользователям и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Профиль артиста успешно создан, роль обновлена, возвращен новый JWT токен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для создания профиля артиста"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "У пользователя уже есть профиль артиста"
            ),
        }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ArtistCreateResponseDTO> createArtist(
        @Valid @RequestBody ArtistCreateDTO artistDTO,
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            artistService.createArtist(artistDTO, authentication)
        );
    }

    @Operation(
        summary = "Обновление профиля артиста",
        description = "Обновляет информацию о профиле артиста. Доступно только владельцу профиля и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Профиль артиста успешно обновлен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для обновления профиля"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем профиля)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Профиль артиста не найден"
            ),
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize(
        "@securityService.isSameUser(authentication, @securityService.getArtistUserId(#id))"
    )
    public ResponseEntity<Artist> updateArtist(
        @Parameter(description = "ID артиста") @PathVariable Long id,
        @Valid @RequestBody ArtistUpdateDTO artistUpdateDTO
    ) {
        Artist updatedArtist = artistService.updateArtistProfile(
            id,
            artistUpdateDTO
        );
        return ResponseEntity.ok(updatedArtist);
    }

    @Operation(
        summary = "Удаление профиля артиста",
        description = "Удаляет профиль артиста. Доступно только владельцу профиля и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "204",
                description = "Профиль артиста успешно удален"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем профиля)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Профиль артиста не найден"
            ),
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public ResponseEntity<Void> deleteArtist(
        @Parameter(description = "ID артиста") @PathVariable Long id
    ) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Получение альбомов артиста",
        description = "Возвращает список всех альбомов указанного артиста"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Список альбомов успешно получен"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Артист не найден"
            ),
        }
    )
    @GetMapping("/{artistId}/albums")
    public ResponseEntity<List<AlbumBriefDTO>> getArtistAlbums(
        @Parameter(description = "ID артиста") @PathVariable Long artistId
    ) {
        return ResponseEntity.ok(artistService.getArtistAlbums(artistId));
    }

    @Operation(
        summary = "Получение подписчиков артиста",
        description = "Возвращает список всех подписчиков артиста. Доступно только самому артисту и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Список подписчиков успешно получен"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем профиля)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Артист не найден"
            ),
        }
    )
    @GetMapping("/{artistId}/subscribers")
    @PreAuthorize("@securityService.isSameUser(authentication, #artistId)")
    public ResponseEntity<List<Subscription>> getArtistSubscribers(
        @Parameter(description = "ID артиста") @PathVariable Long artistId
    ) {
        return ResponseEntity.ok(artistService.getArtistSubscribers(artistId));
    }

    @Operation(
        summary = "Получение отзывов на альбом артиста",
        description = "Возвращает список всех отзывов на указанный альбом артиста"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Список отзывов успешно получен"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Артист или альбом не найден"
            ),
        }
    )
    @GetMapping("/{artistId}/albums/{albumId}/reviews")
    public ResponseEntity<List<Review>> getAlbumReviews(
        @Parameter(description = "ID артиста") @PathVariable Long artistId,
        @Parameter(description = "ID альбома") @PathVariable Long albumId
    ) {
        return ResponseEntity.ok(artistService.getAlbumReviews(albumId));
    }

    @Operation(
        summary = "Подписка на артиста",
        description = "Создает подписку текущего пользователя на указанного артиста.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Подписка успешно создана"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректный запрос (например, попытка подписаться на самого себя)"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "404",
                description = "Артист не найден"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Подписка уже существует"
            ),
        }
    )
    @PostMapping("/{artistId}/subscribe")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    public ResponseEntity<Subscription> subscribeToArtist(
        @Parameter(description = "ID артиста") @PathVariable Long artistId,
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            artistService.subscribeToArtist(artistId, authentication)
        );
    }

    @Operation(
        summary = "Отписка от артиста",
        description = "Удаляет подписку текущего пользователя на указанного артиста.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Подписка успешно удалена"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "404",
                description = "Артист не найден или подписка не существует"
            ),
        }
    )
    @DeleteMapping("/{artistId}/unsubscribe")
    @PreAuthorize("hasAnyRole('USER', 'ARTIST', 'ADMIN')")
    public ResponseEntity<Map<String, Boolean>> unsubscribeFromArtist(
        @Parameter(description = "ID артиста") @PathVariable Long artistId,
        Authentication authentication
    ) {
        boolean result = artistService.unsubscribeFromArtist(
            artistId,
            authentication
        );
        return ResponseEntity.ok(Map.of("success", result));
    }
}
