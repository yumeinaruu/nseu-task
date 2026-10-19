package com.yumeinaruu.nseutask.security;

import com.yumeinaruu.nseutask.entity.User;
import java.util.Optional;

public interface JwtService {

    String generateToken(User user);

    Optional<String> findUsernameByToken(String token);

}
