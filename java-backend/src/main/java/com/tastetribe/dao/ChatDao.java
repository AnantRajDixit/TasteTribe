package com.tastetribe.dao;

import com.tastetribe.model.ChatMessage;
import java.util.List;

/** Data-access contract for persisted AI conversation messages. */
public interface ChatDao {

    void save(ChatMessage message);

    /** The newest {@code limit} messages of a session, oldest first. */
    List<ChatMessage> findRecent(String sessionId, String userId, int limit);
}
