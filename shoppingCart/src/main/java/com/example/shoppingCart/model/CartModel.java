package com.example.shoppingCart.model;

import lombok.Data;

@Data
public class CartModel {

    private Long userId;
    private Long productId;
    private String productName;
    private Double productPrice;
    private int quantity;

}
