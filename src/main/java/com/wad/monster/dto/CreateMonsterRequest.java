package com.wad.monster.dto;

import com.wad.monster.model.Skill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMonsterRequest {
    private int templateId;
    private String name;
    private String elementType;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    private List<Skill> skills;
    private String owner;
}
