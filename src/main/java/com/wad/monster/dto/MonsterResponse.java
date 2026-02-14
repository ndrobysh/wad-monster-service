package com.wad.monster.dto;

import com.wad.monster.model.Monster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonsterResponse {
    private String id;
    private String name;
    private String elementType;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private int level;

    public static MonsterResponse from(Monster m) {
        return new MonsterResponse(
                m.getId(), m.getName(), m.getElementType(),
                m.getHp(), m.getAtk(), m.getDef(), m.getVit(), m.getLevel()
        );
    }
}
