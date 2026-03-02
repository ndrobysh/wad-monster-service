package com.wad.monster.repository;

import com.wad.monster.model.Monster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonsterRepository extends MongoRepository<Monster, String> {

    List<Monster> findByElementType(String elementType);

    List<Monster> findByNameContainingIgnoreCase(String name);
}
