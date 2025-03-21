package com.example.shoppingCart.repository;

import com.example.shoppingCart.entity.Cart;
import com.example.shoppingCart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUser_UserIdAndProduct_ProductIdIn(Long userId, List<Long> productIds);

    // Find By User
    List<Cart> findByUser(User user);

    List<Cart> findByUserUserId(Long userId);

    // Delete multiple cart items by userId and productIds
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user.userId = :userId AND c.product.productId IN :productIds")
    void deleteByUserIdAndProductIds(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);

}