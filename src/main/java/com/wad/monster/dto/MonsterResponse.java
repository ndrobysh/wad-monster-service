package com.wad.monster.dto;

import com.wad.monster.model.Monster;

public record MonsterResponse(
        String id,
        String name,
        int hp,
        int atk,
        int def,
        int vit,
        String elementType,
        int level
) {
    public static MonsterResponse from(Monster monster) {
        return new MonsterResponse(
                monster.getId(),
                monster.getName(),
                monster.getHp(),
                monster.getAtk(),
                monster.getDef(),
                monster.getVit(),
                monster.getElementType(),
                monster.getLevel()
        );
    }
}
