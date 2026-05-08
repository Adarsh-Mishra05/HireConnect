package com.hireconnect.auth.service;

import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.UserCredential;

public interface RefreshTokenService {

    RefreshToken createOrUpdateRefreshToken(UserCredential user);

    RefreshToken verifyRefreshToken(String token);
}