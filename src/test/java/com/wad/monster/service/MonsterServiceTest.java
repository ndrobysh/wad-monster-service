package com.wad.monster.service;

import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.dto.GainExperienceRequest;
import com.wad.monster.dto.UpgradeSkillRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.model.Ratio;
import com.wad.monster.model.Skill;
import com.wad.monster.repository.MonsterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonsterServiceTest {

    @Mock
    private MonsterRepository monsterRepository;

    @InjectMocks
    private MonsterService monsterService;

    private Monster sample;
    private final String OWNER = "player1";
    private final String ID = "abc123";

    @BeforeEach
    void setUp() {
        sample = new Monster();
        sample.setId(ID);
        sample.setOwner(OWNER);
        sample.setTemplateId(1);
        sample.setName("Ifrit");
        sample.setElementType("fire");
        sample.setHp(1200);
        sample.setAtk(450);
        sample.setDef(300);
        sample.setVit(85);
        sample.setLevel(1);
        sample.setExperience(0);
        sample.setSkillPoints(0);
        sample.setSkills(new ArrayList<>(Arrays.asList(
                new Skill("Flamme", 1, 125, new Ratio("atk", 25), 0, 1, 5),
                new Skill("Explosion", 2, 250, new Ratio("atk", 27.5), 2, 1, 7),
                new Skill("Inferno", 3, 425, new Ratio("atk", 40), 5, 1, 5)
        )));
    }

    @Test
    void testGetMonstersByOwner() {
        when(monsterRepository.findByOwner(OWNER)).thenReturn(List.of(sample));
        List<Monster> result = monsterService.getMonstersByOwner(OWNER);
        assertEquals(1, result.size());
        assertEquals("fire", result.get(0).getElementType());
        assertEquals("Ifrit", result.get(0).getName());
    }

    @Test
    void testGetMonstersVide() {
        when(monsterRepository.findByOwner(OWNER)).thenReturn(List.of());
        assertTrue(monsterService.getMonstersByOwner(OWNER).isEmpty());
    }

    @Test
    void testGetMonsterParId() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        Monster m = monsterService.getMonsterByIdAndOwner(ID, OWNER);
        assertEquals(1200, m.getHp());
        assertEquals("fire", m.getElementType());
    }

    @Test
    void testGetMonsterInexistant() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> monsterService.getMonsterByIdAndOwner(ID, OWNER));
    }

    @Test
    void testCreationMonstreDefaut() {
        CreateMonsterRequest req = new CreateMonsterRequest();
        req.setOwner(OWNER);
        req.setTemplateId(1);
        req.setElementType("fire");
        req.setHp(1200); req.setAtk(450); req.setDef(300); req.setVit(85);
        req.setSkills(new ArrayList<>(List.of(new Skill("Flamme", 1, 125, new Ratio("atk", 25), 0, 0, 5))));
        when(monsterRepository.save(any(Monster.class))).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.createMonster(req);
        assertEquals("Ifrit", m.getName());
        assertEquals(1, m.getLevel());
        assertEquals(0, m.getExperience());
        assertEquals(0, m.getSkillPoints());
        for (var s : m.getSkills()) { assertEquals(1, s.getLevel()); }
    }

    @Test
    void testCreationAvecNom() {
        CreateMonsterRequest req = new CreateMonsterRequest();
        req.setOwner(OWNER); req.setTemplateId(2); req.setName("Custom");
        req.setElementType("wind"); req.setHp(1500); req.setAtk(200); req.setDef(450); req.setVit(80);
        req.setSkills(new ArrayList<>());
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("Custom", monsterService.createMonster(req).getName());
    }

    @Test
    void testXpSansLevelUp() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(50));
        assertEquals(50, m.getExperience());
        assertEquals(1, m.getLevel());
    }

    @Test
    void testXpAvecLevelUp() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(100));
        assertEquals(2, m.getLevel());
        assertEquals(0, m.getExperience());
        assertEquals(1, m.getSkillPoints());
        assertEquals(1250, m.getHp());
        assertEquals(460, m.getAtk());
        assertEquals(310, m.getDef());
        assertEquals(90, m.getVit());
    }

    @Test
    void testXpMultiLevel() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(350));
        assertEquals(4, m.getLevel());
        assertEquals(50, m.getExperience());
        assertEquals(3, m.getSkillPoints());
    }

    @Test
    void testXpMontantInvalide() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        var ex1 = assertThrows(ResponseStatusException.class,
                () -> monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(-1)));
        assertTrue(ex1.getMessage().contains("positif"));
        // zero aussi
        var ex2 = assertThrows(ResponseStatusException.class,
                () -> monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(0)));
        assertTrue(ex2.getMessage().contains("positif"));
    }

    @Test
    void testUpgradeSkill() {
        sample.setSkillPoints(2);
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(1));
        assertEquals(2, m.getSkills().stream().filter(s -> s.getNum() == 1).findFirst().get().getLevel());
        assertEquals(1, m.getSkillPoints());
    }

    @Test
    void testUpgradeSkillSansPoints() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        var ex = assertThrows(ResponseStatusException.class,
                () -> monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(1)));
        assertTrue(ex.getMessage().contains("points de compétence"));
    }

    @Test
    void testUpgradeSkillMax() {
        sample.setSkillPoints(5);
        sample.getSkills().get(0).setLevel(5);
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        var ex = assertThrows(ResponseStatusException.class,
                () -> monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(1)));
        assertTrue(ex.getMessage().contains("niveau max"));
    }

    @Test
    void testUpgradeSkillInexistant() {
        sample.setSkillPoints(1);
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        var ex = assertThrows(ResponseStatusException.class,
                () -> monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(99)));
        assertTrue(ex.getMessage().contains("non trouvée"));
    }

    @Test
    void testSuppression() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        assertDoesNotThrow(() -> monsterService.deleteMonster(ID, OWNER));
        verify(monsterRepository).delete(sample);
    }

    @Test
    void testSuppressionInexistant() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> monsterService.deleteMonster(ID, OWNER));
    }

    @Disabled("TODO: fix ce test il passe pas")
    @Test
    void testNormalizeSkillsLegacy() {
        // censé tester la normalisation des vieux monstres
        // mais le mock retourne pas les bons skills
    }

    @Test
    void testGetAllMonsters() {
        // smoke test
        when(monsterRepository.findAll()).thenReturn(List.of(sample));
        monsterService.getAllMonsters();
    }
}
