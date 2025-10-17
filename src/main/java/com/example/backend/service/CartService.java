package com.example.backend.service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.domain.entity.Cart;
import com.example.backend.domain.entity.CartItem;
import com.example.backend.domain.entity.Product;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.enums.CartStatus;
import com.example.backend.dto.cart.CartItemRequest;
import com.example.backend.dto.cart.CartResponse;
import com.example.backend.repository.CartItemRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
  private final CartRepository cartRepo;
  private final CartItemRepository itemRepo;
  private final ProductRepository productRepo;
  private final EntityManager em;

  public CartResponse getMyCart(int userId) {
    Cart cart = cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
        .orElseGet(() -> cartRepo.save(Cart.builder()
            .user(em.getReference(User.class, userId)) // <-- thay getReference
            .status(CartStatus.ACTIVE).build()));
    em.flush();
    em.refresh(cart); // totals cập nhật bởi trigger DB
    return map(cart);
  }

  public CartResponse addOrUpdateItem(int userId, CartItemRequest req) {
    Cart cart = getOrCreateActive(userId);
    Product p = productRepo.findById(req.getProductId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    CartItem item = cart.getItems().stream()
        .filter(i -> i.getProduct().getId().equals(p.getId()))
        .findFirst().orElse(null);

    if (item == null) {
      item = CartItem.builder()
          .cart(cart).product(p)
          .quantity(req.getQuantity())
          .unitPrice(p.getPrice()) // snapshot
          .currencyCode("VND").taxRate(BigDecimal.ZERO)
          .build();
      cart.getItems().add(item);
    } else {
      item.setQuantity(req.getQuantity());
    }
    cartRepo.save(cart);
    em.flush(); 
    em.refresh(cart);
    return map(cart);
  }

  public CartResponse updateItem(int userId, int itemId, Integer qty, Boolean isSelected) {
    CartItem it = itemRepo.findById(itemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

    if (!it.getCart().getUser().getId().equals(userId))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your cart item");

    if (qty != null) {
      if (qty < 1) // giữ logic: không cho 0, mời dùng remove API
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be >= 1");
      it.setQuantity(qty);
    }
    if (isSelected != null) it.setSelected(isSelected);
    itemRepo.save(it);

    Cart c = it.getCart();
    em.flush(); 
    em.refresh(c);
    return map(c);
  }

  public CartResponse removeItem(int userId, int itemId) {
    CartItem it = itemRepo.findById(itemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
    if (!it.getCart().getUser().getId().equals(userId))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your cart item");

    Cart c = it.getCart();
    itemRepo.delete(it);
    em.flush(); 
    em.refresh(c);
    return map(c);
  }

  public CartResponse updateShippingFee(int userId, BigDecimal fee) {
    Cart cart = getOrCreateActive(userId);
    cart.setShippingFee(fee == null ? BigDecimal.ZERO : fee.max(BigDecimal.ZERO));
    cartRepo.save(cart);
    em.flush(); 
    em.refresh(cart);
    return map(cart);
  }

  private Cart getOrCreateActive(int userId) {
    return cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
      .orElseGet(() -> cartRepo.save(Cart.builder()
        .user(em.getReference(User.class, userId)) // <-- thay getReference
        .status(CartStatus.ACTIVE).build()));
  }

private CartResponse map(Cart c) {
  return CartResponse.builder()
      .cartId(c.getId())
      .items(c.getItems().stream().map(ci -> {
        Product prod = ci.getProduct();
        return CartResponse.Item.builder()
            .cartItemId(ci.getId())
            .productId(prod != null ? prod.getId() : null)
            .productName(prod != null ? prod.getName() : null) 
            .imageUrl(prod != null ? prod.getImageUrl() : null) 
            .unitPrice(ci.getUnitPrice())
            .quantity(ci.getQuantity())
            .selected(ci.isSelected())
            .build();
      }).collect(Collectors.toList()))
      .subtotal(c.getSubtotal())
      .taxTotal(c.getTaxTotal())
      .shippingFee(c.getShippingFee())
      .grandTotal(c.getGrandTotal())
      .build();
  }
}
