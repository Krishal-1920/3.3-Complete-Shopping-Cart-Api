package com.example.shoppingCart.service;

import com.example.shoppingCart.entity.Cart;
import com.example.shoppingCart.entity.Product;
import com.example.shoppingCart.entity.User;
import com.example.shoppingCart.mapper.CartMapper;
import com.example.shoppingCart.model.CartModel;
import com.example.shoppingCart.repository.CartRepository;
import com.example.shoppingCart.repository.ProductRepository;
import com.example.shoppingCart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;


    // Add Items To User Cart
    @Transactional
    public List<CartModel> addProductToCart(Long userId, List<CartModel> cartModels) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Available"));

        List<CartModel> responseList = new ArrayList<>();

        for (CartModel cartModel : cartModels) {
            Product product = productRepository.findById(cartModel.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product Not Found with ID: " + cartModel.getProductId()));

            Cart cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(cartModel.getQuantity());

            Cart savedCart = cartRepository.save(cart);

            CartModel response = cartMapper.cartToCartModel(savedCart);
            if (response == null) {
                throw new RuntimeException("CartMapper returned null! Check the mapping configuration.");
            }
            response.setProductName(product.getProductName());
            response.setProductPrice(Double.valueOf(product.getProductPrice()));

            responseList.add(response);
        }
        return responseList;
    }


    // Update User Cart
    @Transactional
    public List<CartModel> updateUserCart(Long userId, List<Long> productIds) {

        // Fetch User
        User checkingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found with ID: " + userId));

        // Fetch existing cart items
        List<Cart> existingItems = cartRepository.findByUser(checkingUser);

        // Map existing cart items by productId
        Map<Long, Cart> existingCartMap = existingItems.stream()
                .collect(Collectors.toMap(cart -> cart.getProduct().getProductId(), cart -> cart));

        // Convert to Set for easier lookup
        Set<Long> incomingProductIds = new HashSet<>(productIds);

        // DELETE items not present in incoming list
        for (Cart existingCart : existingItems) {
            Long existingProductId = existingCart.getProduct().getProductId();
            if (!incomingProductIds.contains(existingProductId)) {
                cartRepository.delete(existingCart);
            }
        }

        List<Cart> updatedCart = new ArrayList<>();

        // ADD and UPDATE incoming productIds
        for (Long productId : incomingProductIds) {

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product Not Found with ID: " + productId));

            if (existingCartMap.containsKey(productId)) {
                Cart existingCart = existingCartMap.get(productId);
                existingCart.setQuantity(existingCart.getQuantity());
                updatedCart.add(cartRepository.save(existingCart));
            } else {
                Cart newCart = new Cart();
                newCart.setUser(checkingUser);
                newCart.setProduct(product);
                newCart.setQuantity(1);
                updatedCart.add(cartRepository.save(newCart));
            }
        }

        List<CartModel> cartModels = new ArrayList<>();

        for (Cart cart : updatedCart) {
            CartModel cartModel = cartMapper.cartToCartModel(cart);

            // Set extra details from the product (optional)
            cartModel.setProductName(cart.getProduct().getProductName());
            cartModel.setProductPrice(Double.valueOf(cart.getProduct().getProductPrice()));
            cartModels.add(cartModel);
        }
        return cartModels;
    }

}