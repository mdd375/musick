package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.*;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.service.AlbumService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список альбомов успешно получен")
    })
    @GetMapping
    public ResponseEntity<List<Album>> getAllAlbums() {
        return ResponseEntity.ok(albumService.getAllAlbums());
    }

    @Operation(
        summary = "Получение альбома по ID",
        description = "Возвращает информацию об альбоме по указанному ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Альбом успешно найден"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Album> getAlbumById(
        @Parameter(description = "ID альбома") @PathVariable Long id
    ) {
        Album album = albumService.getAlbumById(id);
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Альбом успешно создан"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные для создания альбома"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль ARTIST или ADMIN)")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ResponseEntity<Album> createAlbum(
        @Valid @RequestBody AlbumCreateDTO albumDTO,
        Authentication authentication
    ) {
        return ResponseEntity.ok(albumService.createAlbum(albumDTO, authentication));
    }

    @Operation(
        summary = "Обновление альбома",
        description = "Обновляет информацию об альбоме. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Альбом успешно обновлен"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные для обновления альбома"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав (должен быть владельцем альбома)"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id)")
    public ResponseEntity<Album> updateAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long id, 
        @Valid @RequestBody Album album
    ) {
        Album updatedAlbum = albumService.updateAlbum(id, album);
        if (updatedAlbum == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedAlbum);
    }

    @Operation(
        summary = "Удаление альбома",
        description = "Удаляет альбом по указанному ID. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Альбом успешно удален"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав (должен быть владельцем альбома)"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #id)")
    public ResponseEntity<Void> deleteAlbum(@Parameter(description = "ID альбома") @PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Покупка альбома",
        description = "Создает запись о покупке альбома текущим пользователем.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Альбом успешно куплен"),
        @ApiResponse(responseCode = "400", description = "Ошибка при покупке альбома"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав для покупки альбома"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
    @PostMapping("/{albumId}/purchase")
    @PreAuthorize("@securityService.canPurchaseAlbum(authentication)")
    public ResponseEntity<Purchase> purchaseAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        Authentication authentication
    ) {
        return ResponseEntity.ok(albumService.purchaseAlbum(albumId, authentication));
    }

    @Operation(
        summary = "Добавление тега к альбому",
        description = "Добавляет тег к альбому. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Тег успешно добавлен к альбому"),
        @ApiResponse(responseCode = "400", description = "Некорректный тег"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав (должен быть владельцем альбома)"),
        @ApiResponse(responseCode = "404", description = "Альбом или тег не найден")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Тег успешно удален из альбома"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав (должен быть владельцем альбома)"),
        @ApiResponse(responseCode = "404", description = "Альбом или тег не найден")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список отзывов успешно получен"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Отзыв успешно добавлен"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные для отзыва"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав для написания отзыва"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
    @PostMapping("/{albumId}/reviews")
    @PreAuthorize("@securityService.canWriteReview(authentication)")
    public ResponseEntity<Review> addReviewToAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Valid @RequestBody ReviewCreateDTO reviewDTO,
        Authentication authentication
    ) {
        return ResponseEntity.ok(albumService.addReviewToAlbum(
            albumId,
            reviewDTO,
            authentication
        ));
    }

    @Operation(
        summary = "Получение треков альбома",
        description = "Возвращает список всех треков для указанного альбома"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список треков успешно получен"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
    @GetMapping("/{albumId}/tracks")
    public ResponseEntity<List<Track>> getTracksForAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId
    ) {
        return ResponseEntity.ok(albumService.getTracksForAlbum(albumId));
    }

    @Operation(
        summary = "Добавление трека к альбому",
        description = "Добавляет новый трек к альбому. Доступно только владельцу альбома и администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Трек успешно добавлен к альбому"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные для трека"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав (должен быть владельцем альбома)"),
        @ApiResponse(responseCode = "404", description = "Альбом не найден")
    })
    @PostMapping("/{albumId}/tracks")
    @PreAuthorize("@securityService.isAlbumOwner(authentication, #albumId)")
    public ResponseEntity<Track> addTrackToAlbum(
        @Parameter(description = "ID альбома") @PathVariable Long albumId,
        @Valid @RequestBody TrackCreateDTO trackDTO
    ) {
        return ResponseEntity.ok(albumService.addTrackToAlbum(albumId, trackDTO));
    }
}
