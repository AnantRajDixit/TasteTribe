package com.tastetribe.dao;

import com.tastetribe.model.PasswordReset;
import com.tastetribe.model.SessionToken;
import java.util.Optional;

/** Data-access contract for login sessions and password-reset tokens. */
public interface SessionDao {

    void saveSession(SessionToken session);

    Optional<SessionToken> findSession(String token);

    void deleteSession(String token);

    void deleteSessionsForUser(String userId);

    void deleteSessionsForUserExcept(String userId, String keepToken);

    void saveResetToken(PasswordReset reset);

    Optional<PasswordReset> findResetToken(String token);

    void deleteResetToken(String token);
}
