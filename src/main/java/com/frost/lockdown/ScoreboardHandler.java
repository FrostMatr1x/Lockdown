package com.frost.lockdown;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

public class ScoreboardHandler {
    private static final Map<UUID, CachedScore> CACHE = new ConcurrentHashMap<>();

    private static final class CachedScore {
        final String scoreName;
        final long gameTick;
        final OptionalInt value;

        CachedScore(String scoreName, long gameTick, OptionalInt value) {
            this.scoreName = scoreName;
            this.gameTick = gameTick;
            this.value = value;
        }
    }

    public static OptionalInt tryGetScoreboardValue(String scoreName, Player player)  {
        UUID id = player.getUUID();
        long gameTick = player.level().getGameTime();

        CachedScore cached = CACHE.get(id);
        if (cached != null && Objects.equals(cached.scoreName, scoreName) && cached.gameTick == gameTick) {
            return cached.value;
        }

        OptionalInt value = computeScore(scoreName, player);
        CACHE.put(id, new CachedScore(scoreName, gameTick, value));
        return value;
    }

    private static OptionalInt computeScore(String scoreName, Player player) {
        Scoreboard scoreboard = player.level().getScoreboard();
        Objective objective = scoreboard.getObjective(scoreName);

        if (objective != null) {
            ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(player, objective);
            int score = (scoreInfo != null) ? scoreInfo.value() : 0;
            return OptionalInt.of(score);
        }
        else
        {
            return OptionalInt.empty();
        }
    }
}
