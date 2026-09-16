package vn.tera.learn.service;

import vn.tera.learn.dto.TokenPair;
import vn.tera.learn.entity.User;

public interface AuthService {
    TokenPair refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);

    TokenPair generateTokenPair(User user);
}
