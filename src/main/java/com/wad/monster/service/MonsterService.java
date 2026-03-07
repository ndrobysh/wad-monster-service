package com.wad.monster.service;

import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.model.MonsterType;
import com.wad.monster.model.Skill;
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

    public List<Monster> getMonstersByElementType(String elementType) {
        return monsterRepository.findByElementType(elementType);
    }

    public List<MonsterType> getAllTypes() {
        return List.of(MonsterType.values());
    }

    public Monster createMonster(CreateMonsterRequest request) {
        List<Skill> skills = request.getSkills() == null ? List.of() : request.getSkills()
                .stream()
                .map(skill -> {
                    skill.setLevel(1);
                    return skill;
                })
                .toList();

        Monster monster = new Monster(
                null,
                request.getOwner(),
                request.getTemplateId(),
                request.getName(),
                request.getElementType(),
                request.getHp(),
                request.getAtk(),
                request.getDef(),
                request.getVit(),
                1,
                0,
                0,
                skills
        );

        return monsterRepository.save(monster);
    }

    public List<Monster> getMonstersByOwner(String owner) {
        return monsterRepository.findByOwner(owner);
    }

    public Optional<Monster> gainExperience(String id, int amount) {
        return monsterRepository.findById(id).map(monster -> {
            monster.setExperience(monster.getExperience() + amount);

            while (monster.getExperience() >= 100 * monster.getLevel()) {
                monster.setExperience(monster.getExperience() - 100 * monster.getLevel());
                monster.setLevel(monster.getLevel() + 1);
                monster.setSkillPoints(monster.getSkillPoints() + 1);
            }

            return monsterRepository.save(monster);
        });
    }

    public Optional<Monster> upgradeSkill(String id, int skillNum) {
        return monsterRepository.findById(id).map(monster -> {
            if (monster.getSkillPoints() <= 0) {
                throw new IllegalStateException("Aucun point de compétence disponible");
            }

            Skill skill = monster.getSkills().stream()
                    .filter(s -> s.getNum() == skillNum)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Compétence introuvable : " + skillNum));

            if (skill.getLevel() >= skill.getLvlMax()) {
                throw new IllegalStateException("La compétence est déjà au niveau maximum");
            }

            skill.setLevel(skill.getLevel() + 1);
            monster.setSkillPoints(monster.getSkillPoints() - 1);

            return monsterRepository.save(monster);
        });
    }

    public boolean deleteMonster(String id) {
        if (!monsterRepository.existsById(id)) {
            return false;
        }
        monsterRepository.deleteById(id);
        return true;
    }
}
