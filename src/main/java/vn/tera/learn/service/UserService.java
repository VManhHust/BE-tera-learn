package vn.tera.learn.service;

import vn.tera.learn.dto.CurrentUserResponse;

public interface UserService {

    CurrentUserResponse getCurrentUser(Long userId);
}
