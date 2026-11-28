package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Conversation;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	@EntityGraph(attributePaths = {"userLow", "userHigh"})
	Optional<Conversation> findByUserLowIdAndUserHighId(Long userLowId, Long userHighId);

	@EntityGraph(attributePaths = {"userLow", "userHigh"})
	@Query("select c from Conversation c where c.id = :id")
	Optional<Conversation> findWithUsersById(@Param("id") Long id);

	/** The row locked for writing: serialises read-marker and e-mail-marker updates of one conversation. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Conversation c where c.id = :id")
	Optional<Conversation> lockById(@Param("id") Long id);

	/** The user's conversations that have at least one message, most recent first. */
	@EntityGraph(attributePaths = {"userLow", "userHigh"})
	@Query("""
		select c from Conversation c
		where (c.userLow.id = :userId or c.userHigh.id = :userId) and c.lastMessageAt is not null
		order by c.lastMessageAt desc, c.id desc""")
	List<Conversation> findActiveFor(@Param("userId") Long userId);

	/**
	 * Conversation ids where a side has a message from the other side, unread and older than {@code cutoff}, and has
	 * read everything it was last e-mailed about (D-57). Returned as [conversationId, recipientId].
	 */
	@Query("""
		select c.id, c.userLow.id from Conversation c
		where c.lowNotifiedId <= c.lowLastReadId and exists (
			select 1 from ChatMessage m where m.conversation = c and m.sender = c.userHigh
			  and m.id > c.lowLastReadId and m.createdAt <= :cutoff)
		union
		select c.id, c.userHigh.id from Conversation c
		where c.highNotifiedId <= c.highLastReadId and exists (
			select 1 from ChatMessage m where m.conversation = c and m.sender = c.userLow
			  and m.id > c.highLastReadId and m.createdAt <= :cutoff)""")
	List<Object[]> findUnreadToNotify(@Param("cutoff") Instant cutoff);
}
