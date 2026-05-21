package com.appsdeveloperblog.users.graphql;

import com.appsdeveloperblog.users.graphql.input.UserInput;
import com.appsdeveloperblog.users.model.Role;
import com.appsdeveloperblog.users.model.User;
import com.appsdeveloperblog.users.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UsersGraphQLController {

    private final UserService userService;

    public UsersGraphQLController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public UserPage users(@Argument Integer page, @Argument Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        var result = userService.getAllUsers(pageNum, pageSize);
        return new UserPage(result.getContent(), (int) result.getTotalElements(), result.getTotalPages());
    }

    @QueryMapping
    public User user(@Argument Long id) {
        return userService.getUser(id);
    }

    @MutationMapping
    public User createUser(@Argument UserInput input) {
        var user = new User();
        user.setName(input.name());
        user.setAge(input.age());
        user.setEmailId(input.emailId());
        user.setDepartment(input.department());
        user.setBaseLocation(input.baseLocation());
        user.setPassword(input.password());
        if (input.role() != null) {
            user.setRole(Role.valueOf(input.role()));
        }
        return userService.createUser(user);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument UserInput input) {
        var user = new User();
        user.setName(input.name());
        user.setAge(input.age());
        user.setEmailId(input.emailId());
        user.setDepartment(input.department());
        user.setBaseLocation(input.baseLocation());
        if (input.role() != null) {
            user.setRole(Role.valueOf(input.role()));
        }
        return userService.updateUser(id, user);
    }

    @MutationMapping
    public boolean deleteUser(@Argument Long id) {
        userService.deleteUser(id);
        return true;
    }
}
