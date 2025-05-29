package ru.m0vt.musick.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.AlbumBriefDTO;
import ru.m0vt.musick.dto.AlbumCreateDTO;
import ru.m0vt.musick.dto.AlbumDetailDTO;
import ru.m0vt.musick.dto.AlbumUpdateDTO;
import ru.m0vt.musick.dto.ReviewCreateDTO;
import ru.m0vt.musick.dto.TrackCreateDTO;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.service.AlbumService;

@RestController
@RequestMapping("/albums")
@Tag(name = "Альбомы", description = "API для работы с музыкальными альбомами")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @Operation(
        summary = "Получение всех альбомов",
        description = "Возвращает список всех альбомов, доступных в системе"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Список альбомов успешно получен"
            ),
        }
    )
    @GetMapping
    public ResponseEntity<List<AlbumBriefDTO>> getAllAlbums() {
        return ResponseEntity.ok(albumService.getAllAlbums());
    }

    @Operation(
        summary = "Получение альбома по ID",
        description = "Возвращает информацию об альбоме по указанному ID"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Альбом успешно найден"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDetailDTO> getAlbumById(
        @Parameter(description = "ID альбома") @PathVariable Long id
    ) {
        AlbumDetailDTO album = albumService.getAlbumById(id);
        if (album == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(album);
    }

    @Operation(
        summary = "Создание нового альбома",
        description = "Создает новый альбом для текущего артиста. Доступно только артистам и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Альбом успешно создан"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для создания альбома"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (требуется роль ARTIST или ADMIN)"
            ),
        }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<AlbumDetailDTO> createAlbum(
        @Valid @RequestBody AlbumCreateDTO albumDTO,
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            albumService.createAlbum(albumDTO, authentication)
        );
    }

    @Operation(
        summary = "Обновление альбома",
        description = "Обновляет информацию об альбоме. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Альбом успешно обновлен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для обновления альбома"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем альбома)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
        }
    )
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id)")
    public ResponseEntity<AlbumDetailDTO> updateAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long id,
        @Valid @RequestBody AlbumUpdateDTO albumUpdateDTO
    ) {
        AlbumDetailDTO updatedAlbum = albumService.updateAlbum(
            id,
            albumUpdateDTO
        );
        return ResponseEntity.ok(updatedAlbum);
    }

    @Operation(
        summary = "Удаление альбома",
        description = "Удаляет альбом по указанному ID. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "204",
                description = "Альбом успешно удален"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем альбома)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
        }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id)")
    public ResponseEntity<Void> deleteAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long id
    ) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Покупка альбома",
        description = "Создает запись о покупке альбома текущим пользователем. Снимает деньги с баланса покупателя и добавляет их на счет артиста.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Альбом успешно куплен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка при покупке альбома или попытка артиста купить собственный альбом"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "402",
                description = "Недостаточно денег на балансе пользователя"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав для покупки альбома"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Альбом уже куплен данным пользователем"
            ),
        }
    )
    @PostMapping("/{albumId}/purchase")
    @PreAuthorize("@securityService.canPurchaseAlbum(authentication)")
    public ResponseEntity<Purchase> purchaseAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            albumService.purchaseAlbum(albumId, authentication)
        );
    }

    @Operation(
        summary = "Добавление тега к альбому",
        description = "Добавляет тег к альбому. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Тег успешно добавлен к альбому"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректный тег"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем альбома)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом или тег не найден"
            ),
        }
    )
    @PostMapping("/{albumId}/tags")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public ResponseEntity<AlbumTag> addTagToAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Parameter(description = "Название тега") @RequestBody String tag
    ) {
        return ResponseEntity.ok(albumService.addTagToAlbum(albumId, tag));
    }

    @Operation(
        summary = "Удаление тега из альбома",
        description = "Удаляет тег из альбома. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "204",
                description = "Тег успешно удален из альбома"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем альбома)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом или тег не найден"
            ),
        }
    )
    @DeleteMapping("/{albumId}/tags/{tagId}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public ResponseEntity<Void> removeTagFromAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Parameter(description = "ID тега") @PathVariable Long tagId
    ) {
        albumService.removeTagFromAlbum(albumId, tagId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Получение отзывов для альбома",
        description = "Возвращает список всех отзывов для указанного альбома"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Список отзывов успешно получен"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
        }
    )
    @GetMapping("/{albumId}/reviews")
    public ResponseEntity<List<Review>> getReviewsForAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId
    ) {
        return ResponseEntity.ok(albumService.getReviewsForAlbum(albumId));
    }

    @Operation(
        summary = "Добавление отзыва к альбому",
        description = "Добавляет новый отзыв к альбому от имени текущего пользователя.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Отзыв успешно добавлен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для отзыва"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав для написания отзыва"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
        }
    )
    @PostMapping("/{albumId}/reviews")
    @PreAuthorize("@securityService.canWriteReview(authentication)")
    public ResponseEntity<Review> addReviewToAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Valid @RequestBody ReviewCreateDTO reviewDTO,
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            albumService.addReviewToAlbum(albumId, reviewDTO, authentication)
        );
    }

    @Operation(
        summary = "Получение треков альбома",
        description = "Возвращает список всех треков для указанного альбома"
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Список треков успешно получен"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
        }
    )
    @GetMapping("/{albumId}/tracks")
    public ResponseEntity<List<Track>> getTracksForAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId
    ) {
        return ResponseEntity.ok(albumService.getTracksForAlbum(albumId));
    }

    @Operation(
        summary = "Добавление трека к альбому",
        description = "Добавляет новый трек в конец альбома. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Трек успешно добавлен к альбому"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для трека"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем альбома)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом не найден"
            ),
        }
    )
    @PostMapping("/{albumId}/tracks")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public ResponseEntity<Track> addTrackToAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Valid @RequestBody TrackCreateDTO trackDTO
    ) {
        return ResponseEntity.ok(
            albumService.addTrackToAlbum(albumId, trackDTO)
        );
    }

    @Operation(
        summary = "Удаление трека из альбома",
        description = "Удаляет трек из альбома по указанной позиции. Позиции остальных треков будут пересчитаны. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Трек успешно удален из альбома"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для удаления"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем альбома)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом или трек не найден"
            ),
        }
    )
    @DeleteMapping("/{albumId}/tracks/{position}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public ResponseEntity<List<Track>> removeTrackFromAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Parameter(
            description = "Позиция трека в альбоме"
        ) @PathVariable Integer position
    ) {
        return ResponseEntity.ok(
            albumService.removeTrackFromAlbum(albumId, position)
        );
    }

    @Operation(
        summary = "Перемещение трека в альбоме",
        description = "Перемещает трек в альбоме на новую позицию. Позиции остальных треков будут пересчитаны. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(
        {
            @ApiResponse(
                responseCode = "200",
                description = "Трек успешно перемещен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректные данные для перемещения"
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(
                responseCode = "403",
                description = "Недостаточно прав (должен быть владельцем альбома)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Альбом или трек не найден"
            ),
        }
    )
    @PutMapping("/{albumId}/tracks/{currentPosition}/move/{newPosition}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public ResponseEntity<List<Track>> moveTrackPosition(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Parameter(
            description = "Текущая позиция трека"
        ) @PathVariable Integer currentPosition,
        @Parameter(
            description = "Новая позиция трека"
        ) @PathVariable Integer newPosition
    ) {
        return ResponseEntity.ok(
            albumService.moveTrackPosition(
                albumId,
                currentPosition,
                newPosition
            )
        );
    }
}
