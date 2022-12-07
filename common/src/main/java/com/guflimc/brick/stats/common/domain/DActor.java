package com.guflimc.brick.stats.common.domain;

import com.guflimc.brick.stats.api.actor.Actor;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "actors")
public class DActor implements Actor {

    @Embeddable
    public static class ActorPK {

        private UUID id;

        private String type;

        @Override
        public int hashCode() {
            return id.hashCode() + type.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if ( !(obj instanceof ActorPK pk) ) {
                return false;
            }
            return id.equals(pk.id) && type.equals(pk.type);
        }
    }

    @EmbeddedId
    private ActorPK pk;

    public DActor() {}

    public DActor(@NotNull UUID id, @NotNull String type) {
        this.pk = new ActorPK();
        this.pk.id = id;
        this.pk.type = type;
    }

    @Override
    public UUID id() {
        return pk.id;
    }

    @Override
    public String type() {
        return pk.type;
    }

}
