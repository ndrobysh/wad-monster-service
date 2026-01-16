package com.wad.monster.controller;

import com.wad.monster.dto.MonsterResponse;
import com.wad.monster.model.MonsterType;
import com.wad.monster.service.MonsterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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


}
