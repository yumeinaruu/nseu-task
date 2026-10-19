package com.yumeinaruu.nseutask.mapper;

import com.yumeinaruu.nseutask.dto.UserResponse;
import com.yumeinaruu.nseutask.dto.UserShortResponse;
import com.yumeinaruu.nseutask.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public UserShortResponse toUserShortResponse(User user) {
        return new UserShortResponse(user.getId(), user.getUsername());
    }

}
