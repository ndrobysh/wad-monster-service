package com.wad.monster.model;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "monsters")
public class Monster {
    @Id
    private String id;
    @NotBlank
    private String name;
    private int hp;
    private int atk;
    private int def;
    private int vit;
    @Field(targetType = FieldType.STRING)
    private MonsterType type;
}
