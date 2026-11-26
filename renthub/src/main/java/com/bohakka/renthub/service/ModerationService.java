package com.bohakka.renthub.service;

import com.bohakka.renthub.dto.AdminEquipmentResponse;
import com.bohakka.renthub.dto.AdminUserResponse;
import com.bohakka.renthub.dto.ComplaintResponse;
import com.bohakka.renthub.dto.ModerationResult;
import com.bohakka.renthub.dto.PageResponse;
import com.bohakka.renthub.entity.ComplaintStatus;
import java.util.Set;

/** FR-20, FR-22: the admin's complaints, users and listings; callers are ADMIN only (NFR-11). */
public interface ModerationService {

	PageResponse<ComplaintResponse> complaints(Set<ComplaintStatus> statuses, int page);

	ComplaintResponse resolve(Long adminId, Long complaintId);

	ComplaintResponse reject(Long adminId, Long complaintId);

	PageResponse<AdminUserResponse> users(String query, int page);

	ModerationResult blockUser(Long userId);

	ModerationResult unblockUser(Long userId);

	PageResponse<AdminEquipmentResponse> equipment(String query, int page);

	ModerationResult blockEquipment(Long equipmentId);

	ModerationResult unblockEquipment(Long equipmentId);
}
