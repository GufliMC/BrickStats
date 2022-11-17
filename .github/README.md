# BrickStats

A simple Minecraft plugin for tracking stats of various entities (not just players).


## Install

Get the [release](https://github.com/GufliMC/BrickStats/releases) and place it in your server.


## API

#### Gradle

```
repositories {
    maven { url "https://repo.jorisg.com/snapshots" }
}

dependencies {
    compileOnly 'com.guflimc.brick.stats:api:+'
}
```

#### Javadocs

Check the javadocs for all platforms [here](https://guflimc.github.io/BrickChat/).

#### Examples

```java
// read total stats
int total_blocks_placed = StatsAPI.get().read(player.getUniqueId(), Keys.BLOCKS_PLACED);

// get a read-only stats container for any uuid, the values in this container may change
StatsContainer container = StatsAPI.get().readAll(player.getUniqueId());

// get a sub statistic
int grass_blocks_placed = container.read(Keys.BLOCKS_PLACED.of(Material.GRASS_BLOCK));

// write stats, using an updater function is recommended.
StatsKey birds_spotted = new StatsKey("birds_spotted");
StatsAPI.get().update(player.getUniqueId(), birds_spotted, x -> x + 2);

// track stats across teams, any stat write for a player will also be triggered for their team.
StatsAPI.get().registerRelationProvider((playerId) -> TeamsAPI.get().findTeam(playerId).map(Team::id));

// get the amount of blocks everyone in a specific team has placed
int total_team_blocks_placed = StatsAPI.get().read(teamId, Keys.BLOCKS_PLACED);

// get the amount of blocks a specific player has placed while in a specific team
int blocks_placed_while_in_team = StatsAPI.get().read(teamId, Keys.BLOCKS_PLACED, player.getUniqueId());

// relations don't just have to be teams, you can also use them for limited time events, 
// or just anything, each unique id can be a relation.

// stats can be tracked for any id, so they must be unique across players and custom providers.
```

