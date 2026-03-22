package com.wad.monster.service;

import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.dto.GainExperienceRequest;
import com.wad.monster.dto.UpgradeSkillRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.model.Skill;
import com.wad.monster.repository.MonsterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonsterService {

    private final MonsterRepository monsterRepository;

    private static final Map<Integer, String> TEMPLATE_NAMES = Map.of(
            1, "Ifrit",
            2, "Leviathan",
            3, "Golem",
            4, "Phoenix"
    );

    public List<Monster> getAllMonsters() {
        return monsterRepository.findAll();
    }

    public List<Monster> getMonstersByOwner(String owner) {
        // FIXME: ca marche mais c'est pas opti quand y'a beaucoup de monstres
        List<Monster> monsters = monsterRepository.findByOwner(owner);
        monsters.forEach(this::normalizeSkills);
        return monsters;
    }

    public Monster getMonsterByIdAndOwner(String id, String owner) {
        Monster monster = monsterRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monstre introuvable"));
        normalizeSkills(monster);
        return monster;
    }

    public Optional<Monster> getMonsterById(String id) {
        Optional<Monster> monster = monsterRepository.findById(id);
        monster.ifPresent(this::normalizeSkills);
        return monster;
    }

    /**
     * Fix legacy monsters created before skill schema alignment.
     * Old invocation service sent baseDamage/multiplier fields that didn't map
     * to Skill's dmg/ratio.percent, leaving them at 0 in MongoDB.
     */
    private void normalizeSkills(Monster monster) {
        if (monster.getSkills() == null) return;
        boolean dirty = false;
        List<Skill> skills = monster.getSkills();
        for (int i = 0; i < skills.size(); i++) {
            Skill s = skills.get(i);
            if (s.getNum() <= 0) {
                s.setNum(i + 1);
                dirty = true;
            }
            if (s.getLvlMax() <= 0) {
                s.setLvlMax(5);
                dirty = true;
            }
            if (s.getLevel() <= 0) {
                s.setLevel(1);
                dirty = true;
            }
        }
        if (dirty) {
            monsterRepository.save(monster);
        }
    }

    // TODO: ajouter validation des stats max par rapport au template
    public Monster createMonster(CreateMonsterRequest request) {
        String name = request.getName();
        if (name == null || name.isBlank()) {
            name = TEMPLATE_NAMES.getOrDefault(request.getTemplateId(), "Monstre #" + request.getTemplateId());
        }

        List<Skill> skills = new ArrayList<>();
        if (request.getSkills() != null) {
            List<Skill> rawSkills = request.getSkills();
            for (int i = 0; i < rawSkills.size(); i++) {
                Skill skill = rawSkills.get(i);
                skill.setNum(i + 1);
                skill.setLevel(1);
                if (skill.getLvlMax() <= 0) {
                    skill.setLvlMax(5);
                }
                skills.add(skill);
            }
        }

        Monster monster = new Monster(
                null,
                request.getOwner(),
                request.getTemplateId(),
                name,
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

        System.out.println("monster created: " + monster.getName());
        return monsterRepository.save(monster);
    }

    public Monster gainExperience(String id, String owner, GainExperienceRequest request) {
        Monster monster = monsterRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monstre introuvable"));

        int amount = request.getAmount();
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le montant d'expérience doit être positif");
        }

        monster.setExperience(monster.getExperience() + amount);

        while (monster.getExperience() >= 100) {
            monster.setExperience(monster.getExperience() - 100);
            monster.setLevel(monster.getLevel() + 1);
            monster.setSkillPoints(monster.getSkillPoints() + 1);

            monster.setHp(monster.getHp() + 50);
            monster.setAtk(monster.getAtk() + 10);
            monster.setDef(monster.getDef() + 10);
            monster.setVit(monster.getVit() + 5);
        }

        return monsterRepository.save(monster);
    }

    public Monster upgradeSkill(String id, String owner, UpgradeSkillRequest request) {
        Monster monster = monsterRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monstre introuvable"));

        if (monster.getSkillPoints() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun points de compétence disponible");
        }

        Skill skill = monster.getSkills().stream()
                .filter(s -> s.getNum() == request.getSkillNum())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compétence non trouvée : " + request.getSkillNum()));

        if (skill.getLevel() >= skill.getLvlMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La compétence est déjà au niveau max");
        }

        skill.setLevel(skill.getLevel() + 1);
        monster.setSkillPoints(monster.getSkillPoints() - 1);

        return monsterRepository.save(monster);
    }

    public void deleteMonster(String id, String owner) {
        Monster monster = monsterRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monstre introuvable"));
        System.out.println("DEBUG: suppression monstre " + monster.getName());
        monsterRepository.delete(monster);
    }
}
