package com.bohakka.renthub.service.impl;

import com.bohakka.renthub.dto.VideoResponse;
import com.bohakka.renthub.entity.Equipment;
import com.bohakka.renthub.event.StoredObjectUploaded;
import com.bohakka.renthub.event.StoredObjectsRemoved;
import com.bohakka.renthub.exception.BusinessRuleException;
import com.bohakka.renthub.repository.EquipmentRepository;
import com.bohakka.renthub.service.ObjectStorage;
import com.bohakka.renthub.service.VideoService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link VideoService} (D-52): the file is checked by size (≤ 50 MB) and by its leading bytes (MP4 / WebM), then
 * streamed to the object storage; a new video replaces the old one, whose file is removed after the commit. Storage
 * clean-up after a rollback is event-driven, as for photos.
 */
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

	public static final int MAX_MEGABYTES = 50;
	private static final long MAX_BYTES = DataSize.ofMegabytes(MAX_MEGABYTES).toBytes();

	private final EquipmentRepository equipment;
	private final EquipmentAccess access;
	private final ObjectStorage storage;
	private final EquipmentMapper mapper;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	@Override
	@Transactional
	public VideoResponse upload(Long ownerId, Long equipmentId, MultipartFile file) {
		Equipment listing = access.owned(ownerId, equipmentId);
		if (file == null || file.isEmpty()) {
			throw new BusinessRuleException("video.empty", "video.empty");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BusinessRuleException("video.too_large", "video.too_large", MAX_MEGABYTES);
		}
		try (InputStream in = new BufferedInputStream(file.getInputStream())) {
			in.mark(VideoFormat.HEADER_BYTES);
			byte[] header = in.readNBytes(VideoFormat.HEADER_BYTES);
			in.reset();
			VideoFormat format = VideoFormat.detect(header)
				.orElseThrow(() -> new BusinessRuleException("video.unsupported_type", "video.unsupported_type"));
			String key = "equipment/%d/%s.%s".formatted(listing.getId(), UUID.randomUUID(), format.extension());
			String previous = listing.replaceVideo(key, format.contentType(), clock.instant());
			storage.put(key, in, file.getSize(), format.contentType());
			events.publishEvent(new StoredObjectUploaded(key));
			if (previous != null) {
				events.publishEvent(new StoredObjectsRemoved(List.of(previous)));
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		equipment.flush();
		return mapper.video(listing);
	}

	@Override
	@Transactional
	public void delete(Long ownerId, Long equipmentId) {
		Equipment listing = access.owned(ownerId, equipmentId);
		String key = listing.removeVideo(clock.instant());
		events.publishEvent(new StoredObjectsRemoved(List.of(key)));
		equipment.flush();
	}
}
