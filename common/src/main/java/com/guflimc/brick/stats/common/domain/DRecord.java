package com.guflimc.brick.stats.common.domain;

import com.guflimc.brick.stats.api.actor.Actor;
import com.guflimc.brick.stats.api.container.Record;
import com.guflimc.brick.stats.api.key.StatsKey;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "records")
public class DRecord implements Record {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    @ManyToMany(targetEntity = DActor.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "records_actors",
    joinColumns = @JoinColumn(name = "record_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id"))
    private List<DActor> actors;

    @Column(name = "keyname", nullable = false)
    private String key;

    @DbDefault("0")
    private int value = 0;

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

    public DRecord() {
    }

    public DRecord(@NotNull StatsKey key, @NotNull Actor.ActorSet actors) {
        this.key = key.name();
        this.actors = actors.stream().map(a -> (DActor) a).toList();
    }

    @Override
    public Actor.ActorSet actors() {
        return new Actor.ActorSet(actors.stream().map(a -> (Actor) a).toList());
    }

    @Override
    public StatsKey key() {
        return StatsKey.of(key);
    }

    @Override
    public int value() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
