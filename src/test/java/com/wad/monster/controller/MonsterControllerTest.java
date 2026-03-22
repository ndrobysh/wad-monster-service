package com.wad.monster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.dto.GainExperienceRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.model.Ratio;
import com.wad.monster.model.Skill;
import com.wad.monster.service.AuthService;
import com.wad.monster.service.MonsterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonsterController.class)
class MonsterControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private MonsterService monsterService;
    @MockBean private AuthService authService;
    @Autowired private ObjectMapper objectMapper;

    private Monster sample;
    private final String TOKEN = "Bearer valid-token";
    private final String USER = "player1";

    @BeforeEach
    void setUp() {
        sample = new Monster();
        sample.setId("abc123"); sample.setOwner(USER); sample.setTemplateId(1);
        sample.setName("Ifrit"); sample.setElementType("fire");
        sample.setHp(1200); sample.setAtk(450); sample.setDef(300); sample.setVit(85);
        sample.setLevel(1); sample.setExperience(0); sample.setSkillPoints(0);
        sample.setSkills(new ArrayList<>(Arrays.asList(
                new Skill("Flamme", 1, 125, new Ratio("atk", 25), 0, 1, 5))));
    }

    @Test
    void testGetMesMonstres() throws Exception {
        when(authService.validateToken(TOKEN)).thenReturn(USER);
        when(monsterService.getMonstersByOwner(USER)).thenReturn(List.of(sample));
        mockMvc.perform(get("/api/monster").header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].elementType").value("fire"))
                .andExpect(jsonPath("$[0].name").value("Ifrit"));
    }

    @Test
    void testGetMonstres401() throws Exception {
        when(authService.validateToken("Bearer bad"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        mockMvc.perform(get("/api/monster").header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetMonsterParJoueur() throws Exception {
        when(authService.validateToken(TOKEN)).thenReturn(USER);
        when(monsterService.getMonstersByOwner(USER)).thenReturn(List.of(sample));
        mockMvc.perform(get("/api/monster/player/player1").header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ifrit"));
    }

    @Test
    void testListePublique() throws Exception {
        when(monsterService.getAllMonsters()).thenReturn(List.of(sample));
        mockMvc.perform(get("/api/monster/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ifrit"))
                .andExpect(jsonPath("$[0].elementType").value("fire"));
    }

    @Test
    void testCreationMonstre() throws Exception {
        when(authService.validateToken(TOKEN)).thenReturn(USER);
        when(monsterService.createMonster(any())).thenReturn(sample);
        mockMvc.perform(post("/api/monster").header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateMonsterRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc123"));
    }

    @Test
    void testGainXp() throws Exception {
        sample.setExperience(50);
        when(authService.validateToken(TOKEN)).thenReturn(USER);
        when(monsterService.gainExperience(eq("abc123"), eq(USER), any())).thenReturn(sample);
        mockMvc.perform(post("/api/monster/abc123/experience").header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":50}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.experience").value(50));
    }

    @Test
    void testSuppressionMonstre() throws Exception {
        when(authService.validateToken(TOKEN)).thenReturn(USER);
        doNothing().when(monsterService).deleteMonster("abc123", USER);
        mockMvc.perform(delete("/api/monster/abc123").header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Monstre supprimé"));
    }
}
