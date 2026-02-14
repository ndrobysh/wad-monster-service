package com.wad.monster.dto;

import com.wad.monster.model.Monster;
import com.wad.monster.model.MonsterType;

public record MonsterResponse(
        String id,
        String name,
        int hp,
        int atk,
        int def,
        int vit,
        MonsterType type
) {
    public static MonsterResponse from(Monster monster) {
        return new MonsterResponse(
                monster.getId(),
                monster.getName(),
                monster.getHp(),
                monster.getAtk(),
                monster.getDef(),
                monster.getVit(),
                monster.getType()
        );
    }
}
