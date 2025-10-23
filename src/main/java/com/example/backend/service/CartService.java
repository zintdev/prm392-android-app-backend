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
import com.example.backend.repository.StoreInventoryRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
  private final CartRepository cartRepo;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final EntityManager em;
  private final StoreInventoryRepository storeInventoryRepository;

  public CartResponse getMyCart(int userId) {
    Cart cart = getOrCreateActive(userId);
    em.flush();
    // Áp dụng rule ship theo subtotal hiện tại rồi recalc lại totals (grand_total
    // phụ thuộc ship)
    applyShippingRuleAndRecalc(cart.getId());
    em.refresh(cart);
    return map(cart);
  }

  public CartResponse addOrUpdateItem(int userId, CartItemRequest req) {
    if (req.getQuantity() == 0 || req.getQuantity() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be >= 1");
    }
    Cart cart = getOrCreateActive(userId);
    Product p = productRepository.findById(req.getProductId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    // tìm item hiện có
    CartItem item = cartItemRepository
        .findByCart_IdAndProduct_Id(cart.getId(), p.getId())
        .orElse(null);

    // tổng tồn kho của sản phẩm
    Integer available = productRepository.findQuantityById(p.getId());

    // requested = qty hiện có trong giỏ (nếu có) + qty người dùng yêu cầu
    int currentQty = (item == null ? 0 : item.getQuantity());
    int requestedQty = currentQty + req.getQuantity();

    if (available == 0) {
      // hết hàng: nếu item đã có → xóa; nếu chưa có → báo 409
      if (item != null) {
        cartItemRepository.delete(item);
        em.flush();
        applyShippingRuleAndRecalc(cart.getId());
        em.refresh(cart);
        return map(cart);
      }
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Out of stock");
    }

    int finalQty = Math.min(requestedQty, available);
    if (item == null) {
      item = CartItem.builder()
          .cart(cart)
          .product(p)
          .quantity(finalQty)
          .unitPrice(p.getPrice())
          .currencyCode("VND")
          .taxRate(BigDecimal.ZERO)
          .selected(true)
          .build();
    } else {
      item.setQuantity(finalQty);
    }

    cartItemRepository.save(item);
    em.flush();
    applyShippingRuleAndRecalc(cart.getId());
    em.refresh(cart);
    return map(cart);
  }

  public CartResponse updateItem(int userId, int itemId, Integer qty, Boolean isSelected) {
    CartItem it = cartItemRepository.findById(itemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

    if (!it.getCart().getUser().getId().equals(userId))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your cart item");

    // if (qty != null) {
    //   if (qty < 1)
    //     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be >= 1");

    //   Integer available = storeInventoryRepository.sumQuantityByProductId(it.getProduct().getId());
    //   if (available == null)
    //     available = 0;

    //   if (available == 0) {
    //     // hết hàng: xóa item luôn
    //     int cartId = it.getCart().getId();
    //     cartItemRepository.delete(it);
    //     em.flush();
    //     applyShippingRuleAndRecalc(cartId);
    //     Cart c = cartRepo.findById(cartId).orElseThrow();
    //     em.refresh(c);
    //     return map(c);
    //   }

    //   int finalQty = Math.min(qty, available);
    //   it.setQuantity(finalQty);
    // }

    if (isSelected != null)
      it.setSelected(isSelected);

    cartItemRepository.save(it);
    em.flush();
    applyShippingRuleAndRecalc(it.getCart().getId());
    em.refresh(it.getCart());
    return map(it.getCart());
  }

  public CartResponse removeItem(int userId, int itemId) {
    CartItem it = cartItemRepository.findById(itemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
    if (!it.getCart().getUser().getId().equals(userId))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your cart item");

    int cartId = it.getCart().getId();
    cartItemRepository.delete(it);
    em.flush();
    applyShippingRuleAndRecalc(cartId);
    Cart c = cartRepo.findById(cartId).orElseThrow(); // đảm bảo entity managed
    em.refresh(c);
    return map(c);
  }

  public CartResponse updateShippingFee(int userId, BigDecimal fee) {
    Cart cart = getOrCreateActive(userId);
    cart.setShippingFee(fee == null ? BigDecimal.ZERO : fee.max(BigDecimal.ZERO));
    cartRepo.save(cart);
    em.flush();
    // Sau khi đổi ship fee, cần recalc grand_total
    em.createNativeQuery("SELECT recalc_cart_totals(:cid)")
        .setParameter("cid", cart.getId())
        .getSingleResult();
    em.refresh(cart);
    return map(cart);
  }

  private Cart getOrCreateActive(int userId) {
    return cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
        .orElseGet(() -> {
          try {
            Cart c = Cart.builder()
                .user(em.getReference(User.class, userId))
                .status(CartStatus.ACTIVE)
                .build();
            return cartRepo.save(c);
          } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Có thể do unique index (uq_active_cart_per_user) đua → refetch
            return cartRepo.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> ex);
          }
        });
  }

  public CartResponse clearCart(int userId) {
    Cart cart = getOrCreateActive(userId);
    cartItemRepository.deleteByCart_Id(cart.getId());
    em.flush();
    applyShippingRuleAndRecalc(cart.getId());
    em.refresh(cart);
    return map(cart);
  }

  public CartResponse selectAll(int userId, boolean selected) {
    Cart cart = getOrCreateActive(userId);
    cartItemRepository.updateSelectedByCartId(cart.getId(), selected);
    em.flush();
    applyShippingRuleAndRecalc(cart.getId());
    em.refresh(cart);
    return map(cart);
  }

  private CartResponse map(Cart c) {
    return CartResponse.builder()
        .cartId(c.getId())
        .status(c.getStatus().name())
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

  private void applyShippingRuleAndRecalc(int cartId) {
    // cập nhật shipping_fee theo subtotal hiện tại
    em.createNativeQuery("""
            UPDATE carts c
            SET shipping_fee = CASE WHEN c.subtotal >= 500000 THEN 0 ELSE 30000 END
            WHERE c.cart_id = :cid
        """).setParameter("cid", cartId).executeUpdate();

    // gọi function trong DB để tính lại totals
    em.createNativeQuery("SELECT recalc_cart_totals(:cid)")
        .setParameter("cid", cartId)
        .getSingleResult();
  }

}
