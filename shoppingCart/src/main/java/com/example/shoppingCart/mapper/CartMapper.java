package com.example.shoppingCart.mapper;

import com.example.shoppingCart.entity.Cart;
import com.example.shoppingCart.model.CartModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    // Entity To Model
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    @Mapping(source = "product.productPrice", target = "productPrice")
    CartModel cartToCartModel(Cart cart);

    // Model To Entity
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Cart cartModelToCart(CartModel cartModel);

}
