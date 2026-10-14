package com.tastetribe.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO contract — the core of the data-access layer and the clearest
 * generics demonstration in the project: every DAO works with any entity type
 * {@code T} and any identifier type {@code ID}.
 *
 * <p>Specialised DAOs extend this interface with entity-specific queries. Services
 * depend on these interfaces (never on the JDBC implementations) — polymorphism
 * makes it trivial to swap the persistence technology later.</p>
 */
public interface GenericDao<T, ID> {

    /** Load one entity by its primary key. */
    Optional<T> findById(ID id);

    /** Load all entities (bounded — call specialised queries for pagination). */
    List<T> findAll();

    /** Persist a new entity. */
    void save(T entity);

    /** Remove an entity by primary key. */
    void deleteById(ID id);
}
