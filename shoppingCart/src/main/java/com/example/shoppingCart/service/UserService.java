package com.example.shoppingCart.service;

import com.example.shoppingCart.entity.Product;
import com.example.shoppingCart.entity.Role;
import com.example.shoppingCart.entity.User;
import com.example.shoppingCart.entity.UserRole;
import com.example.shoppingCart.enums.ProductCategory;
import com.example.shoppingCart.mapper.ProductMapper;
import com.example.shoppingCart.mapper.RoleMapper;
import com.example.shoppingCart.mapper.UserMapper;
import com.example.shoppingCart.model.ProductModel;
import com.example.shoppingCart.model.RoleModel;
import com.example.shoppingCart.model.UserModel;
import com.example.shoppingCart.repository.ProductRepository;
import com.example.shoppingCart.repository.RoleRepository;
import com.example.shoppingCart.repository.UserRepository;
import com.example.shoppingCart.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserRoleRepository userRoleRepository;

    private final RoleRepository roleRepository;

    private final RoleMapper roleMapper;

    @Autowired
    private ProductMapper productMapper;


    // Creating New User
    @Transactional
    public UserModel addCustomer(UserModel userModel) {

        User addUser = userMapper.userModelToUser(userModel); // Converted to User Entity

        // Save user to get ID
        addUser = userRepository.save(addUser);

        // Extract Roles From Model
        List<Long> roleIdsFromModel = userModel.getRoles().stream().map(r -> r.getRoleId()).toList();

        // Finding all matching Roles From DB
        List<Role> roleInDb = roleRepository.findAllByRoleIdIn(roleIdsFromModel); // In is used for finding the roles inside the DB

        // Extract Roles Id that exists in Database
        List<Long> roleIdsInDb = roleInDb.stream().map(r -> r.getRoleId()).toList();

        List<Long> invalidRoles = new ArrayList<>();

        // Checking for invalid Roles
        for(Long roleId : roleIdsFromModel){
            if(!roleIdsInDb.contains(roleId)){
                invalidRoles.add(roleId);
            }
        }

        if(!invalidRoles.isEmpty()){throw new IllegalArgumentException("Invalid role ID: " + invalidRoles + ". Allowed role IDs are 1, 2, and 3.");}

        // Filter Valid Roles
        List<Role> saveRoles = roleInDb.stream().filter(r -> roleIdsFromModel.contains(r.getRoleId())).toList();

        // Saving Roles
        for(Role role : saveRoles){
            UserRole userRole = new UserRole();
            userRole.setUser(addUser);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }

        UserModel userModelToReturn = userMapper.userToUserModel(addUser);
        List<UserRole> byUserUserId = userRoleRepository.findByUserUserId(addUser.getUserId());
        List<RoleModel> roleList = new ArrayList<>();
        byUserUserId.forEach(ur -> roleList.add(roleMapper.rolesToRolesModel(ur.getRole())));

        userModelToReturn.setRoles(roleList);
        return userModelToReturn;
    }


    // Deleting the user By Id
    public String deleteUserById(Long id) {
        Optional<User> userModel = userRepository.findById(id);
        if (userModel.isPresent()) {
            userRepository.deleteById(id);
            return "User deleted successfully.";
        } else {
            return "User not found.";
        }
    }


    // Updating User Role
    @Transactional
    public UserModel updateUser(Long id, UserModel userModel) {

        User userEntity = userMapper.userModelToUser(userModel);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException());

        existingUser.setUserId(id);

        User savedUser = userRepository.save(existingUser);

        // Getting Role Id

        // Fetching Role Id
        List<Long> incomingRoleIdsFromModel = userModel.getRoles().stream().map(u -> u.getRoleId()).distinct().toList();

        // Roles From Db
        List<Role> roleInDb = roleRepository.findAllByRoleIdIn(incomingRoleIdsFromModel);

        List<Long> roleIdsInDb = roleInDb.stream()
                .map(r -> r.getRoleId())
                .toList();


        // Fetch Existing Roles from user Database

        List<UserRole> existingRoles = userRoleRepository.findByUserUserId((id));
        List<Long> existingRoleIds = existingRoles.stream()
                .map(r -> r.getRole().getRoleId())
                .toList();

        // Determine Roles To Remove

        List<Long> removeRoleIds = new ArrayList<>();

        for(Long roleId : existingRoleIds){
            if(!incomingRoleIdsFromModel.contains(roleId)){
                removeRoleIds.add(roleId);
            }
        }
        /*List<UserRole> deletedRoles = userRoleRepository.findByRoleRoleIdInAndUserUserId(removeRoleIds, id);*/

        if(!removeRoleIds.isEmpty()){
            userRoleRepository.deleteByRoleRoleIdInAndUserUserId(removeRoleIds, id);
        }

        // Roles To Add
        List<Long> nonAllocateRoleIds = incomingRoleIdsFromModel.stream()
                .filter(roleId -> !existingRoleIds.contains(roleId))  // Compare incoming IDs against existing ones
                .toList();  // Collect all matching roleIds into a list

        List<Long> invalidRoleIds = new ArrayList<>();
        if (!nonAllocateRoleIds.isEmpty()) {
            for(Long roleId : nonAllocateRoleIds) {
                if(!roleIdsInDb.contains(roleId)) {
                    invalidRoleIds.add(roleId);
                }
            }
        }

        if (!invalidRoleIds.isEmpty()) {
            throw new RuntimeException("Invalid Roles" + invalidRoleIds);
        }

        List<Role> updatedRoles = roleInDb.stream().filter(rd -> nonAllocateRoleIds.contains(rd.getRoleId())).toList();

        for (Role role : updatedRoles) {
            UserRole updatedUser = new UserRole();
            updatedUser.setUser(savedUser);
            updatedUser.setRole(role);
            userRoleRepository.save(updatedUser);
        }

        UserModel updatedUserModel = userMapper.userToUserModel(savedUser);

        // Updated List
        List<UserRole> updatedUserRoles = userRoleRepository.findByUserUserId(id);

        List<RoleModel> updatedRoleModels = updatedUserRoles.stream()
                .map(userRole -> roleMapper.rolesToRolesModel(userRole.getRole()))
                .toList();

        updatedUserModel.setRoles(updatedRoleModels);

        return updatedUserModel;
    }


    // Find Product By Category
    public List<ProductModel> findProductByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        categoryName = categoryName.trim().toUpperCase();
        ProductCategory closestCategory = getBestMatchingCategory(categoryName);

        if (closestCategory != null) {
            List<Product> products = productRepository.findByProductCategory(closestCategory);
            return products.stream()
                    .map(productMapper::productToProductModel)
                    .toList();
        }

        return Collections.emptyList();
    }


    // Find the best matching category using both Levenshtein Distance and Partial Match
    private ProductCategory getBestMatchingCategory(String inputCategory) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        ProductCategory bestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (ProductCategory category : ProductCategory.values()) {
            String categoryName = category.name();

            if (categoryName.replace("_", "").contains(inputCategory.replace("_", ""))) {
                return category;
            }

            int distance = levenshtein.apply(categoryName, inputCategory);
            if (distance < minDistance && distance <= 3) {
                minDistance = distance;
                bestMatch = category;
            }
        }
        return bestMatch;
    }

}
