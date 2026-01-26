package com.wad.monster.repository;

import com.wad.monster.model.Monster;
import com.wad.monster.model.MonsterType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonsterRepository extends MongoRepository<Monster, String> {
    
    List<Monster> findByType(MonsterType type);
    
    List<Monster> findByNameContainingIgnoreCase(String name);
}
