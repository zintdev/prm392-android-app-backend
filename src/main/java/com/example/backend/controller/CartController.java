package com.example.backend.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.cart.CartItemRequest;
import com.example.backend.dto.cart.CartResponse;
import com.example.backend.dto.cart.UpdateQuantityRequest;
import com.example.backend.dto.cart.UpdateShippingFeeRequest;
import com.example.backend.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getMyCart(HttpServletRequest request) {
        int userId = resolveUserId(request);
        return cartService.getMyCart(userId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.OK)
    public CartResponse addOrUpdateItem(HttpServletRequest request,
            @Valid @RequestBody CartItemRequest body) {
        int userId = resolveUserId(request);
        return cartService.addOrUpdateItem(userId, body);
    }

    @PatchMapping("/items/{itemId}")
    public CartResponse updateItem(HttpServletRequest request,
            @PathVariable("itemId") int itemId,
            @Valid @RequestBody UpdateQuantityRequest body) {
        int userId = resolveUserId(request);
        Integer qty = body.getQuantity();
        Boolean sel = body.getSelected();
        return cartService.updateItem(userId, itemId, qty, sel);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(HttpServletRequest request, @PathVariable("itemId") int itemId) {
        int userId = resolveUserId(request);
        cartService.removeItem(userId, itemId);
    }

    @PutMapping("/shipping-fee")
    public CartResponse updateShippingFee(HttpServletRequest request,
            @Valid @RequestBody UpdateShippingFeeRequest body) {
        int userId = resolveUserId(request);
        BigDecimal fee = body.getShippingFee();
        return cartService.updateShippingFee(userId, fee);
    }

    // DELETE /api/cart -> Clear toàn bộ items, trả về CartResponse rỗng
    @DeleteMapping
    public CartResponse clearCart(HttpServletRequest request) {
        int userId = resolveUserId(request);
        return cartService.clearCart(userId);
    }

    // PATCH /api/cart/items/select-all?selected=true|false
    @PatchMapping("/items/select-all")
    public CartResponse selectAll(HttpServletRequest request, @RequestParam("selected") boolean selected) {
        int userId = resolveUserId(request);
        return cartService.selectAll(userId, selected);
    }

    private int resolveUserId(HttpServletRequest request) {
        // 1) Dev fallback: header X-USER-ID
        String header = request.getHeader("X-USER-ID");
        System.out.println(request);
        if (header != null && !header.isBlank()) {
            return Integer.parseInt(header);
        }

        // 2) Lấy từ SecurityContext do JwtFilter set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            // a) JwtFilter set principal = userId dạng String
            if (principal instanceof String s) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException ignored) {
                }
            }

            // b) JwtFilter set principal = UserDetails
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                // giả định username là userId (ví dụ "1")
                try {
                    return Integer.parseInt(ud.getUsername());
                } catch (NumberFormatException ignored) {
                }
            }

            // c) JwtFilter set principal = CustomUserPrincipal có getId()
            try {
                var m = principal.getClass().getMethod("getId");
                Object id = m.invoke(principal);
                if (id != null)
                    return Integer.parseInt(String.valueOf(id));
            } catch (Exception ignored) {
            }

            // d) JwtFilter nhét userId vào details (Map hoặc object khác)
            Object details = auth.getDetails();
            if (details instanceof java.util.Map<?, ?> map) {
                Object id = map.get("userId");
                if (id != null)
                    return Integer.parseInt(String.valueOf(id));
            }
        }

        // 3) JwtFilter đặt attribute trên request (phòng hờ)
        Object attr = request.getAttribute("userId");
        if (attr != null) {
            return Integer.parseInt(String.valueOf(attr));
        }

        // 4) Không có gì -> 401
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Unauthenticated");
    }

}
