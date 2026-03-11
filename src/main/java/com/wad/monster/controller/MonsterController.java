package com.wad.monster.controller;

import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.dto.GainExperienceRequest;
import com.wad.monster.dto.MonsterResponse;
import com.wad.monster.dto.UpgradeSkillRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.service.AuthService;
import com.wad.monster.service.MonsterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monster")
@RequiredArgsConstructor
@Tag(name = "Monster", description = "API de gestion des monstres")
public class MonsterController {

    private final MonsterService monsterService;
    private final AuthService authService;

    @Operation(summary = "Liste les monstres du joueur authentifié")
    @GetMapping
    public ResponseEntity<List<Monster>> getMyMonsters(@RequestHeader("Authorization") String token) {
        String owner = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.getMonstersByOwner(owner));
    }

    @Operation(summary = "Liste tous les monstres (public)")
    @GetMapping("/list")
    public ResponseEntity<List<MonsterResponse>> getAllMonsters() {
        List<MonsterResponse> monsters = monsterService.getAllMonsters()
                .stream()
                .map(MonsterResponse::from)
                .toList();
        return ResponseEntity.ok(monsters);
    }

    @Operation(summary = "Récupérer les monstres d'un joueur")
    @GetMapping("/player/{owner}")
    public ResponseEntity<List<Monster>> getMonstersByPlayer(
            @PathVariable String owner,
            @RequestHeader("Authorization") String token) {
        String authenticatedUser = authService.validateToken(token);
        if (!authenticatedUser.equals(owner)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès interdit");
        }
        return ResponseEntity.ok(monsterService.getMonstersByOwner(owner));
    }

    @Operation(summary = "Récupérer un monstre par ID (authentifié)")
    @GetMapping("/{id}")
    public ResponseEntity<Monster> getMonsterById(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        String owner = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.getMonsterByIdAndOwner(id, owner));
    }

    @Operation(summary = "Récupérer un monstre par ID (interne, sans auth)")
    @GetMapping("/internal/{id}")
    public ResponseEntity<Monster> getMonsterByIdInternal(@PathVariable String id) {
        return monsterService.getMonsterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Créer un monstre")
    @PostMapping
    public ResponseEntity<Monster> createMonster(
            @RequestBody CreateMonsterRequest request,
            @RequestHeader("Authorization") String token) {
        String owner = authService.validateToken(token);
        request.setOwner(owner);
        Monster created = monsterService.createMonster(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Ajouter de l'expérience à un monstre")
    @PostMapping("/{id}/experience")
    public ResponseEntity<Monster> gainExperience(
            @PathVariable String id,
            @RequestBody GainExperienceRequest request,
            @RequestHeader("Authorization") String token) {
        String owner = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.gainExperience(id, owner, request));
    }

    @Operation(summary = "Améliorer une compétence")
    @PostMapping("/{id}/upgrade-skill")
    public ResponseEntity<Monster> upgradeSkill(
            @PathVariable String id,
            @RequestBody UpgradeSkillRequest request,
            @RequestHeader("Authorization") String token) {
        String owner = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.upgradeSkill(id, owner, request));
    }

    @Operation(summary = "Supprimer un monstre")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMonster(
            @PathVariable String id,
            @RequestHeader("Authorization") String token) {
        String owner = authService.validateToken(token);
        monsterService.deleteMonster(id, owner);
        return ResponseEntity.ok(Map.of("message", "Monstre supprimé"));
    }
}
