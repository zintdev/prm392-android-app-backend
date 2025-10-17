package com.example.backend.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.CartItem;

import jakarta.transaction.Transactional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

 Optional<CartItem> findByCart_IdAndProduct_Id(Integer cartId, Integer productId);

    @EntityGraph(attributePaths = "product")
    List<CartItem> findByCart_Id(Integer cartId);

    @Transactional
    void deleteByCart_Id(Integer cartId);

    @Modifying
    @Transactional
    @Query("UPDATE CartItem ci SET ci.selected = :selected WHERE ci.cart.id = :cartId")
    int updateSelectedByCartId(@Param("cartId") Integer cartId, @Param("selected") boolean selected);

}