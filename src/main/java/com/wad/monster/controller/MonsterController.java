package com.wad.monster.controller;

import com.wad.monster.dto.CreateMonsterRequest;
import com.wad.monster.dto.GainExperienceRequest;
import com.wad.monster.dto.MonsterResponse;
import com.wad.monster.dto.UpgradeSkillRequest;
import com.wad.monster.model.Monster;
import com.wad.monster.service.MonsterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monster")
@RequiredArgsConstructor
@Tag(name = "Monster", description = "API de gestion des monstres")
public class MonsterController {

    private final MonsterService monsterService;

    @Operation(summary = "Liste tous les monstres", description = "Récupère la liste complète des monstres")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des monstres récupérée avec succès")
    })
    @GetMapping("/list")
    public ResponseEntity<List<MonsterResponse>> getAllMonsters() {
        List<MonsterResponse> monsters = monsterService.getAllMonsters()
                .stream()
                .map(MonsterResponse::from)
                .toList();
        return ResponseEntity.ok(monsters);
    }

    @Operation(summary = "Créer un monstre", description = "Crée un nouveau monstre à partir d'un template")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Monstre créé avec succès")
    })
    @PostMapping("/create")
    public ResponseEntity<Monster> createMonster(@RequestBody CreateMonsterRequest request) {
        Monster created = monsterService.createMonster(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Récupérer un monstre par ID", description = "Retourne un monstre selon son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monstre trouvé"),
            @ApiResponse(responseCode = "404", description = "Monstre introuvable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Monster> getMonsterById(@PathVariable String id) {
        return monsterService.getMonsterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Récupérer les monstres d'un joueur", description = "Retourne tous les monstres appartenant à un joueur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des monstres du joueur récupérée avec succès")
    })
    @GetMapping("/player/{owner}")
    public ResponseEntity<List<Monster>> getMonstersByOwner(@PathVariable String owner) {
        List<Monster> monsters = monsterService.getMonstersByOwner(owner);
        return ResponseEntity.ok(monsters);
    }

    @Operation(summary = "Ajouter de l'expérience à un monstre", description = "Ajoute des points d'expérience et gère la montée de niveau")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expérience ajoutée avec succès"),
            @ApiResponse(responseCode = "404", description = "Monstre introuvable")
    })
    @PostMapping("/{id}/experience")
    public ResponseEntity<Monster> gainExperience(
            @PathVariable String id,
            @RequestBody GainExperienceRequest request) {
        return monsterService.gainExperience(id, request.getAmount())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Améliorer une compétence", description = "Dépense un point de compétence pour améliorer une compétence du monstre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compétence améliorée avec succès"),
            @ApiResponse(responseCode = "400", description = "Aucun point de compétence disponible ou compétence déjà au niveau maximum"),
            @ApiResponse(responseCode = "404", description = "Monstre ou compétence introuvable")
    })
    @PostMapping("/{id}/upgrade-skill")
    public ResponseEntity<Monster> upgradeSkill(
            @PathVariable String id,
            @RequestBody UpgradeSkillRequest request) {
        try {
            return monsterService.upgradeSkill(id, request.getSkillNum())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Supprimer un monstre", description = "Supprime un monstre par son identifiant")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Monstre supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Monstre introuvable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonster(@PathVariable String id) {
        if (monsterService.deleteMonster(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
