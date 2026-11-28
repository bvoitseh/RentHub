package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.ChatMessage;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	/** A page of the conversation, newest first; {@code beforeId} = Long.MAX_VALUE for the latest messages. */
	@Query("""
		select m from ChatMessage m
		where m.conversation.id = :conversationId and m.id < :beforeId
		order by m.id desc""")
	List<ChatMessage> findPage(@Param("conversationId") Long conversationId, @Param("beforeId") long beforeId,
		Pageable pageable);

	@EntityGraph(attributePaths = {"conversation", "conversation.userLow", "conversation.userHigh"})
	@Query("select m from ChatMessage m where m.id = :id")
	Optional<ChatMessage> findWithConversationById(@Param("id") Long id);

	@Query("select coalesce(max(m.id), 0) from ChatMessage m where m.conversation.id = :conversationId")
	long findLastId(@Param("conversationId") Long conversationId);

	/** The newest message of each given conversation. */
	@Query("""
		select m from ChatMessage m
		where m.id in (select max(m2.id) from ChatMessage m2 where m2.conversation.id in :ids group by m2.conversation.id)""")
	List<ChatMessage> findLastOf(@Param("ids") Collection<Long> ids);

	/** Unread messages per conversation of the user: [conversationId, count] (messages from the other side only). */
	@Query("""
		select c.id, count(m) from ChatMessage m join m.conversation c
		where m.sender.id <> :userId and (
			(c.userLow.id = :userId and m.id > c.lowLastReadId)
			or (c.userHigh.id = :userId and m.id > c.highLastReadId))
		group by c.id""")
	List<Object[]> countUnreadByConversation(@Param("userId") Long userId);

	/** Unread messages of one side in one conversation, newer than {@code afterId}. */
	@Query("""
		select count(m) from ChatMessage m
		where m.conversation.id = :conversationId and m.sender.id <> :userId and m.id > :afterId""")
	long countUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId,
		@Param("afterId") long afterId);
}
