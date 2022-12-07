package com.guflimc.brick.stats.api.actor;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public interface Actor {

    UUID id();

    String type();

    //

    static Actor player(@NotNull UUID id) {
        return new TempActor(id, "PLAYER");
    }

    record TempActor(@NotNull UUID id, @NotNull String type) implements Actor {
    }

    //

    class ActorSet implements Iterable<Actor> {

        private final Set<Actor> actors;

        public ActorSet(@NotNull Collection<? extends Actor> c) {
            if ( c.isEmpty() ) {
                throw new IllegalArgumentException("ActorSet must contain at least one actor.");
            }
            this.actors = Set.copyOf(c);
        }

        public <T extends Actor> ActorSet(@NotNull T... actors) {
            this(Set.of(actors));
        }

        public int size() {
            return actors.size();
        }

        public Actor first() {
            return iterator().next();
        }

        public Stream<Actor> stream() {
            return actors.stream();
        }

        public Set<Actor> copySet() {
            return new HashSet<>(actors);
        }

        @NotNull
        @Override
        public Iterator<Actor> iterator() {
            return actors.iterator();
        }

        //


        @Override
        public int hashCode() {
            return actors.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ActorSet as && actors.equals(as.actors);
        }
    }
}

