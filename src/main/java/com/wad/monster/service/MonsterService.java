package com.wad.monster.service;

import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.dto.GainExperienceRequest;
import com.wad.monster.dto.UpgradeSkillRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.model.Skill;
import com.wad.monster.repository.MonsterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MonsterService {

    private final MonsterRepository monsterRepository;

    private static final int HP_PER_LEVEL = 50;
    private static final int ATK_PER_LEVEL = 10;
    private static final int DEF_PER_LEVEL = 10;
    private static final int VIT_PER_LEVEL = 5;
    private static final int XP_TO_LEVEL_UP = 100;

    private static final String[] DEFAULT_NAMES = {
            "", "Ifrit", "Sylphid", "Kraken", "Leviathan"
    };

    public MonsterService(MonsterRepository monsterRepository) {
        this.monsterRepository = monsterRepository;
    }

    public List<Monster> getAllMonsters() {
        return monsterRepository.findAll();
    }

    public List<Monster> getMonstersByOwner(String owner) {
        return monsterRepository.findByOwner(owner);
    }

    public Monster getMonsterByIdAndOwner(String monsterId, String owner) {
        return monsterRepository.findByIdAndOwner(monsterId, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Monstre non trouvé ou ne vous appartient pas"));
    }

    public Monster createMonster(CreateMonsterRequest request) {
        Monster monster = new Monster();
        monster.setOwner(request.getOwner());
        monster.setTemplateId(request.getTemplateId());
        monster.setElementType(request.getElementType());
        monster.setHp(request.getHp());
        monster.setAtk(request.getAtk());
        monster.setDef(request.getDef());
        monster.setVit(request.getVit());
        monster.setLevel(1);
        monster.setExperience(0);
        monster.setSkillPoints(0);

        if (request.getName() != null && !request.getName().isBlank()) {
            monster.setName(request.getName());
        } else {
            int tid = request.getTemplateId();
            monster.setName(tid > 0 && tid < DEFAULT_NAMES.length
                    ? DEFAULT_NAMES[tid] : "Monster #" + tid);
        }

        List<Skill> skills = request.getSkills();
        if (skills != null) {
            for (Skill skill : skills) { skill.setLevel(1); }
        }
        monster.setSkills(skills);

        return monsterRepository.save(monster);
    }

    public Monster gainExperience(String monsterId, String owner, GainExperienceRequest request) {
        Monster monster = getMonsterByIdAndOwner(monsterId, owner);
        if (request.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le montant d'XP doit être positif");
        }
        monster.setExperience(monster.getExperience() + request.getAmount());
        while (monster.getExperience() >= XP_TO_LEVEL_UP) {
            monster.setExperience(monster.getExperience() - XP_TO_LEVEL_UP);
            levelUp(monster);
        }
        return monsterRepository.save(monster);
    }

    private void levelUp(Monster monster) {
        monster.setLevel(monster.getLevel() + 1);
        monster.setHp(monster.getHp() + HP_PER_LEVEL);
        monster.setAtk(monster.getAtk() + ATK_PER_LEVEL);
        monster.setDef(monster.getDef() + DEF_PER_LEVEL);
        monster.setVit(monster.getVit() + VIT_PER_LEVEL);
        monster.setSkillPoints(monster.getSkillPoints() + 1);
    }

    public Monster upgradeSkill(String monsterId, String owner, UpgradeSkillRequest request) {
        Monster monster = getMonsterByIdAndOwner(monsterId, owner);
        if (monster.getSkillPoints() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pas de points de compétence disponibles");
        }
        Skill targetSkill = monster.getSkills().stream()
                .filter(s -> s.getNum() == request.getSkillNum()).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Compétence n°" + request.getSkillNum() + " non trouvée"));
        if (targetSkill.getLevel() >= targetSkill.getLvlMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La compétence n°" + request.getSkillNum() + " est déjà au niveau max");
        }
        targetSkill.setLevel(targetSkill.getLevel() + 1);
        monster.setSkillPoints(monster.getSkillPoints() - 1);
        return monsterRepository.save(monster);
    }

    public void deleteMonster(String monsterId, String owner) {
        Monster monster = getMonsterByIdAndOwner(monsterId, owner);
        monsterRepository.delete(monster);
    }
}
