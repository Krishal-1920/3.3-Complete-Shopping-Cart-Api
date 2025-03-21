package com.example.shoppingCart.controller;

import com.example.shoppingCart.mapper.CartMapper;
import com.example.shoppingCart.model.CartModel;
import com.example.shoppingCart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;


    @PostMapping("/addToCart/{userId}")
    public List<CartModel> addMultipleToCart(@PathVariable Long userId, @RequestBody List<CartModel> cartModels) {
        return cartService.addProductToCart(userId, cartModels);
    }


    @PutMapping("/updateCart/{userId}")
    public ResponseEntity<?> updateUserCart(
            @PathVariable Long userId,
            @RequestBody Map<String, List<Long>> requestBody) {

        // Extract the list of productIds
        List<Long> productIds = requestBody.get("productId");

        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Product list is empty or missing!");
        }
        List<CartModel> updatedCartList = cartService.updateUserCart(userId, productIds);

        return ResponseEntity.ok(updatedCartList);
    }
}