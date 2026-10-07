package com.tastetribe.dao;

import com.tastetribe.model.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Data-access contract for {@link User} rows. */
public interface UserDao extends GenericDao<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /** Login helper: matches username OR email in one query. */
    Optional<User> findByIdentifier(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void updateProfile(String id, String name, String bio, String avatarUrl);

    void updatePassword(String id, String passwordHash);

    long countAll();

    List<User> findRecent(int limit);

    List<User> search(String query, int limit);

    /** Batch lookup used when denormalising author info into reviews/comments. */
    Map<String, User> findAllByIds(Collection<String> ids);

    void delete(String id);

    @Override
    default void deleteById(String id) {
        delete(id);
    }
}
