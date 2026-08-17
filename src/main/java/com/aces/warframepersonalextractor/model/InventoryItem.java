package com.aces.warframepersonalextractor.model;

import com.aces.warframepersonalextractor.model.enums.RelicRefinement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameRef;

    private String marketItemId;

    private String name;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private RelicRefinement refinement;

    // null for normal items, populated for ranked items such as mods/arcanes
    private Integer rank;
}