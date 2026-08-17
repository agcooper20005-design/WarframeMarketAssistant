package com.aces.warframepersonalextractor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "market_items")
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String marketItemId;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String gameRef;

    @ElementCollection
    @CollectionTable(
            name = "market_item_set_parts",
            joinColumns = @JoinColumn(name = "item_id")
    )
    @Column(name = "set_part")
    private Set<String> setParts = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "market_item_tags",
            joinColumns = @JoinColumn(name = "item_id")
    )
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    private Integer maxRank;

    private Boolean tradable;

    private Boolean bulkTradable;

    private String rarity;
}