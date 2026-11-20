package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.PublicProfileResponse;

/** FR-19: what anyone may see about a user. */
public interface PublicProfileService {

	PublicProfileResponse get(Long userId);
}
