package com.wad.monster.service;

import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.dto.GainExperienceRequest;
import com.wad.monster.dto.UpgradeSkillRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.model.Ratio;
import com.wad.monster.model.Skill;
import com.wad.monster.repository.MonsterRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.*;
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
    void getMonstersByOwner_returnsList() {
        when(monsterRepository.findByOwner(OWNER)).thenReturn(List.of(sample));
        List<Monster> result = monsterService.getMonstersByOwner(OWNER);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getElementType()).isEqualTo("fire");
        assertThat(result.get(0).getName()).isEqualTo("Ifrit");
    }

    @Test
    void getMonstersByOwner_returnsEmptyWhenNone() {
        when(monsterRepository.findByOwner(OWNER)).thenReturn(List.of());
        assertThat(monsterService.getMonstersByOwner(OWNER)).isEmpty();
    }

    @Test
    void getMonsterByIdAndOwner_returnsMonster() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        Monster m = monsterService.getMonsterByIdAndOwner(ID, OWNER);
        assertThat(m.getHp()).isEqualTo(1200);
        assertThat(m.getElementType()).isEqualTo("fire");
    }

    @Test
    void getMonsterByIdAndOwner_throwsWhenNotFound() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> monsterService.getMonsterByIdAndOwner(ID, OWNER))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createMonster_setsDefaultsAndName() {
        CreateMonsterRequest req = new CreateMonsterRequest();
        req.setOwner(OWNER);
        req.setTemplateId(1);
        req.setElementType("fire");
        req.setHp(1200); req.setAtk(450); req.setDef(300); req.setVit(85);
        req.setSkills(new ArrayList<>(List.of(new Skill("Flamme", 1, 125, new Ratio("atk", 25), 0, 0, 5))));
        when(monsterRepository.save(any(Monster.class))).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.createMonster(req);
        assertThat(m.getName()).isEqualTo("Ifrit");
        assertThat(m.getLevel()).isEqualTo(1);
        assertThat(m.getExperience()).isEqualTo(0);
        assertThat(m.getSkillPoints()).isEqualTo(0);
        assertThat(m.getSkills()).allSatisfy(s -> assertThat(s.getLevel()).isEqualTo(1));
    }

    @Test
    void createMonster_usesProvidedName() {
        CreateMonsterRequest req = new CreateMonsterRequest();
        req.setOwner(OWNER); req.setTemplateId(2); req.setName("Custom");
        req.setElementType("wind"); req.setHp(1500); req.setAtk(200); req.setDef(450); req.setVit(80);
        req.setSkills(new ArrayList<>());
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(monsterService.createMonster(req).getName()).isEqualTo("Custom");
    }

    @Test
    void gainExperience_addsXpNoLevelUp() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(50));
        assertThat(m.getExperience()).isEqualTo(50);
        assertThat(m.getLevel()).isEqualTo(1);
    }

    @Test
    void gainExperience_levelsUpExactly() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(100));
        assertThat(m.getLevel()).isEqualTo(2);
        assertThat(m.getExperience()).isEqualTo(0);
        assertThat(m.getSkillPoints()).isEqualTo(1);
        assertThat(m.getHp()).isEqualTo(1250);
        assertThat(m.getAtk()).isEqualTo(460);
        assertThat(m.getDef()).isEqualTo(310);
        assertThat(m.getVit()).isEqualTo(90);
    }

    @Test
    void gainExperience_levelsUpMultipleTimes() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(350));
        assertThat(m.getLevel()).isEqualTo(4);
        assertThat(m.getExperience()).isEqualTo(50);
        assertThat(m.getSkillPoints()).isEqualTo(3);
    }

    @Test
    void gainExperience_throwsOnNegative() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        assertThatThrownBy(() -> monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(-1)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("positif");
    }

    @Test
    void gainExperience_throwsOnZero() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        assertThatThrownBy(() -> monsterService.gainExperience(ID, OWNER, new GainExperienceRequest(0)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("positif");
    }

    @Test
    void upgradeSkill_upgradesAndConsumesPoint() {
        sample.setSkillPoints(2);
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        when(monsterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Monster m = monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(1));
        assertThat(m.getSkills().stream().filter(s -> s.getNum() == 1).findFirst().get().getLevel()).isEqualTo(2);
        assertThat(m.getSkillPoints()).isEqualTo(1);
    }

    @Test
    void upgradeSkill_throwsWhenNoPoints() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        assertThatThrownBy(() -> monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(1)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("points de compétence");
    }

    @Test
    void upgradeSkill_throwsWhenAtMax() {
        sample.setSkillPoints(5);
        sample.getSkills().get(0).setLevel(5);
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        assertThatThrownBy(() -> monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(1)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("niveau max");
    }

    @Test
    void upgradeSkill_throwsWhenSkillNotFound() {
        sample.setSkillPoints(1);
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        assertThatThrownBy(() -> monsterService.upgradeSkill(ID, OWNER, new UpgradeSkillRequest(99)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("non trouvée");
    }

    @Test
    void deleteMonster_deletes() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.of(sample));
        monsterService.deleteMonster(ID, OWNER);
        verify(monsterRepository).delete(sample);
    }

    @Test
    void deleteMonster_throwsWhenNotFound() {
        when(monsterRepository.findByIdAndOwner(ID, OWNER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> monsterService.deleteMonster(ID, OWNER))
                .isInstanceOf(ResponseStatusException.class);
    }
}
