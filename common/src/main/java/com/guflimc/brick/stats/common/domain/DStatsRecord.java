package com.guflimc.brick.stats.common.domain;

import com.guflimc.brick.stats.api.container.StatsRecord;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.Index;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stats")
@Index(columnNames = {"entity_id", "relation", "keyname"}, unique = true)
public class DStatsRecord implements StatsRecord {

    private final static UUID EMPTY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID entityId;

    @Column(nullable = false)
    @DbDefault("00000000-0000-0000-0000-000000000000")
    private UUID relation = EMPTY_UUID;

    @Column(name = "keyname", nullable = false)
    private String key;

    @DbDefault("0")
    private int value;

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

    public DStatsRecord() {
    }

    public DStatsRecord(@NotNull UUID entityId, UUID relation, @NotNull String key, int value) {
        this.entityId = entityId;
        this.key = key;
        this.value = value;

        if ( relation != null ) {
            this.relation = relation;
        }
    }

    public DStatsRecord(@NotNull UUID entityId, @NotNull String key, int value) {
        this(entityId, null, key, value);
    }

    @Override
    public UUID id() {
        return entityId;
    }

    @Override
    public UUID relation() {
        return relation.equals(EMPTY_UUID) ? null : relation;
    }

    public String key() {
        return key;
    }

    @Override
    public int value() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
