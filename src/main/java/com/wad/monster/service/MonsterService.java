package com.wad.monster.service;

import com.wad.monster.model.Monster;
import com.wad.monster.model.MonsterType;
import com.wad.monster.repository.MonsterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonsterService {

    private final MonsterRepository monsterRepository;

    public List<Monster> getAllMonsters() {
        return monsterRepository.findAll();
    }

    public Optional<Monster> getMonsterById(String id) {
        return monsterRepository.findById(id);
    }

    public List<Monster> getMonstersByType(MonsterType type) {
        return monsterRepository.findByType(type);
    }

    public List<MonsterType> getAllTypes() {
        return List.of(MonsterType.values());
    }
}
