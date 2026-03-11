package com.wad.monster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
    private String name;
    private int num;
    private int dmg;
    private Ratio ratio;
    private int cooldown;
    private int level;
    private int lvlMax;
}
