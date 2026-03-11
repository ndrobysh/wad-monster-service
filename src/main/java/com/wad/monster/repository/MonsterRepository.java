package com.wad.monster.repository;

import com.wad.monster.model.Monster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonsterRepository extends MongoRepository<Monster, String> {

    List<Monster> findByElementType(String elementType);

    List<Monster> findByNameContainingIgnoreCase(String name);

    List<Monster> findByOwner(String owner);

    Optional<Monster> findByIdAndOwner(String id, String owner);
}
