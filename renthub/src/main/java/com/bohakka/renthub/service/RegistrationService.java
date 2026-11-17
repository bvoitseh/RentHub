package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.RegisterRequest;
import com.bohakka.renthub.entity.User;

/** FR-1 / FR-1a: creates a renter or owner account and triggers the verification e-mail. */
public interface RegistrationService {

	User register(RegisterRequest request, String clientIp);
}
