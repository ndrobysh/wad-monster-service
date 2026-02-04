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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monsters")
@Tag(name = "Monsters", description = "API de gestion des monstres des joueurs")
public class MonsterController {

    private final MonsterService monsterService;
    private final AuthService authService;

    public MonsterController(MonsterService monsterService, AuthService authService) {
        this.monsterService = monsterService;
        this.authService = authService;
    }

    @Operation(summary = "Tous les monstres du joueur", description = "Récupère la liste des monstres du joueur authentifié")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée"),
            @ApiResponse(responseCode = "401", description = "Token invalide")
    })
    @GetMapping
    public ResponseEntity<List<Monster>> getAllMonsters(@RequestHeader("Authorization") String token) {
        String username = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.getMonstersByOwner(username));
    }

    @Operation(summary = "Monstres d'un joueur", description = "Route utilisée par le frontend")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée"),
            @ApiResponse(responseCode = "403", description = "Accès interdit")
    })
    @GetMapping("/player/{owner}")
    public ResponseEntity<List<Monster>> getMonstersByPlayer(
            @RequestHeader("Authorization") String token,
            @PathVariable String owner) {
        String username = authService.validateToken(token);
        if (!username.equals(owner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(monsterService.getMonstersByOwner(owner));
    }

    @Operation(summary = "Liste complète (sans auth)", description = "Liste tous les monstres en base")
    @GetMapping("/list")
    public ResponseEntity<List<MonsterResponse>> listAllMonsters() {
        List<MonsterResponse> monsters = monsterService.getAllMonsters()
                .stream().map(MonsterResponse::from).toList();
        return ResponseEntity.ok(monsters);
    }

    @Operation(summary = "Un monstre par ID")
    @GetMapping("/{id}")
    public ResponseEntity<Monster> getMonsterById(@RequestHeader("Authorization") String token,
                                                   @PathVariable String id) {
        String username = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.getMonsterByIdAndOwner(id, username));
    }

    @Operation(summary = "Créer un monstre", description = "Appelé par invocation-service")
    @PostMapping
    public ResponseEntity<Monster> createMonster(@RequestHeader("Authorization") String token,
                                                  @RequestBody CreateMonsterRequest request) {
        String username = authService.validateToken(token);
        request.setOwner(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(monsterService.createMonster(request));
    }

    @Operation(summary = "Gain d'XP pour un monstre")
    @PostMapping("/{id}/experience")
    public ResponseEntity<Monster> gainExperience(@RequestHeader("Authorization") String token,
                                                   @PathVariable String id,
                                                   @RequestBody GainExperienceRequest request) {
        String username = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.gainExperience(id, username, request));
    }

    @Operation(summary = "Améliorer une compétence")
    @PostMapping("/{id}/upgrade-skill")
    public ResponseEntity<Monster> upgradeSkill(@RequestHeader("Authorization") String token,
                                                 @PathVariable String id,
                                                 @RequestBody UpgradeSkillRequest request) {
        String username = authService.validateToken(token);
        return ResponseEntity.ok(monsterService.upgradeSkill(id, username, request));
    }

    @Operation(summary = "Supprimer un monstre")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMonster(@RequestHeader("Authorization") String token,
                                                              @PathVariable String id) {
        String username = authService.validateToken(token);
        monsterService.deleteMonster(id, username);
        return ResponseEntity.ok(Map.of("message", "Monstre supprimé", "id", id));
    }
}
