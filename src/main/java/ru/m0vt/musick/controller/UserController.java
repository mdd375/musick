package ru.m0vt.musick.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.m0vt.musick.dto.AddBalanceDTO;
import ru.m0vt.musick.model.*;
import ru.m0vt.musick.service.UserService;

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
@RequestMapping("/users")
@Tag(name = "Пользователи", description = "API для работы с пользователями и их данными")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
        summary = "Получение списка всех пользователей", 
        description = "Возвращает список всех пользователей в системе. Доступно только администраторам.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен. Требуются права администратора")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
        summary = "Получение пользователя по ID", 
        description = "Возвращает данные пользователя по указанному ID. Пользователь может получить только собственные данные.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Данные пользователя успешно получены"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен. Нельзя получить данные другого пользователя"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public ResponseEntity<User> getUserById(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }


    @Operation(
        summary = "Обновление данных пользователя", 
        description = "Обновляет данные пользователя по указанному ID. Пользователь может изменять только собственные данные.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Данные пользователя успешно обновлены"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные для обновления"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен. Нельзя изменять данные другого пользователя"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID пользователя") @PathVariable Long id, 
            @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
        summary = "Удаление пользователя", 
        description = "Удаляет пользователя по указанному ID. Пользователь может удалить только свой аккаунт.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен. Нельзя удалить другого пользователя")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(authentication, #id)")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Получение списка покупок текущего пользователя", 
        description = "Возвращает список всех покупок, сделанных текущим аутентифицированным пользователем.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список покупок успешно получен"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/me/purchases")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Purchase>> getUserPurchases(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserPurchases(authentication));
    }

    @Operation(
        summary = "Получение списка отзывов текущего пользователя", 
        description = "Возвращает список всех отзывов, оставленных текущим аутентифицированным пользователем.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список отзывов успешно получен"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/me/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Review>> getUserReviews(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserReviews(authentication));
    }

    @Operation(
        summary = "Получение списка подписок текущего пользователя", 
        description = "Возвращает список всех подписок на артистов текущего аутентифицированного пользователя.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список подписок успешно получен"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/me/subscriptions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Subscription>> getUserSubscriptions(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserSubscriptions(authentication));
    }

    @Operation(
        summary = "Подписка на артиста", 
        description = "Создает подписку текущего пользователя на указанного артиста.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Подписка успешно создана"),
        @ApiResponse(responseCode = "400", description = "Некорректный запрос (например, попытка подписаться на самого себя)"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "404", description = "Артист не найден"),
        @ApiResponse(responseCode = "409", description = "Подписка уже существует")
    })
    @PostMapping("/me/subscriptions/{artistId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Subscription> addUserSubscription(
        @Parameter(description = "ID артиста") @PathVariable Long artistId,
        Authentication authentication
    ) {
        return ResponseEntity.ok(userService.addUserSubscription(artistId, authentication));
    }
    
    @Operation(
        summary = "Пополнение баланса пользователя", 
        description = "Увеличивает баланс текущего пользователя на указанную сумму.",
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Баланс успешно пополнен"),
        @ApiResponse(responseCode = "400", description = "Некорректная сумма для пополнения"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping("/me/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> addBalance(
        @Valid @RequestBody AddBalanceDTO addBalanceDTO,
        Authentication authentication
    ) {
        return ResponseEntity.ok(userService.addBalance(addBalanceDTO, authentication));
    }
}
