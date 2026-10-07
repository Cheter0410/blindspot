# Blindspot

[![Modrinth](https://img.shields.io/modrinth/dt/blindspot?color=00AF5C&label=Modrinth&logo=modrinth)](https://modrinth.com/mod/blindspot)
[![GitHub](https://img.shields.io/badge/GitHub-repo-181717?logo=github)](https://github.com/cheter0410/blindspot)
[![GitHub Release](https://img.shields.io/github/v/release/cheter0410/blindspot)](https://github.com/cheter0410/blindspot/releases)
[![GitHub Issues](https://img.shields.io/github/issues/cheter0410/blindspot)](https://github.com/cheter0410/blindspot/issues)
[![License](https://img.shields.io/github/license/cheter0410/blindspot)](https://github.com/cheter0410/blindspot/blob/main/LICENSE)

A Minecraft Fabric mod aiming to fix small, overlooked client-side performance issues that larger optimization mods don't cover.

## Requirements

- Minecraft 26.2
- Fabric Loader >= 0.19.5
- Java 25+

Fabric API is not required.

## Features

- **Friends List Optimization:** Caches the friends list instead of rebuilding it on every call.
- **Scoreboard Sidebar Optimization:** Caches the sorted sidebar entries instead of scanning and sorting every score on every frame.
- **Tab List Optimization:** Caches the tab list player order instead of re-sorting all players on every frame while the list is open.

<details>
<summary><b>Technical Details & Deep Dives</b></summary>

### Friends List presence check overhead

Vanilla's `PlayerSocialManager.getFriends()` converts the whole friends list into a new list through a `Stream` on every call, even though the underlying data only changes when a new friend list arrives. While the friends list is enabled, `PresenceHandler.tick()` calls it once per rendered frame (it is called from `Minecraft.runTick`, not from the 20-per-second game tick). The cost per call is small, but it is repeated work that produces the same result each time.

Blindspot caches the result and only recomputes it when vanilla stores new friend data.

### Scoreboard sidebar rebuilt every frame

To draw the sidebar, vanilla calls `Scoreboard.listPlayerScores()` on every frame. This walks through every score holder the scoreboard knows about, not just the ones on the displayed objective. It then filters out hidden entries, sorts all of them and keeps the top 15. With thousands of entries this scan and sort is paid on every single frame, even when no score has changed. In a test with about 5,000 score entries on the client, the sidebar took about 0.9–1.3 ms per frame in vanilla and about 0.06–0.08 ms with Blindspot.

Blindspot keeps the final filtered, sorted and limited list and hands it to vanilla's pipeline in place of the full score list. To know when that list is outdated, Blindspot adds version counters to the scoreboard that change whenever a score is created, changed or removed, or team membership changes. The list is only rebuilt by vanilla's own code when one of these counters changed or a different objective is displayed. Everything after that step still runs every frame exactly as in vanilla: team colors and prefixes, number formats, text widths. Font, language and resource pack changes therefore behave the same as without the mod.

### Tab list re-sorted every frame

While the tab list is open, vanilla sorts all listed players on every frame. Each comparison looks up both players' teams on the scoreboard. The order only depends on the server-provided list order, spectator mode, team and player name, which rarely change.

Blindspot caches the sorted order and lets vanilla rebuild it only when a player is added to or removed from the list, a player's game mode or list order changes, or team membership changes. Ping updates, display names and skins don't affect the order and don't cause a rebuild. The rows themselves (names, ping, scores, hearts) are still drawn by vanilla every frame.

### Verifying the caches

Start the game with the JVM argument `-Dblindspot.verifyCaches=true` to make Blindspot compute the vanilla result alongside every sidebar and tab list cache hit and compare them. The log then shows any mismatch and, every 1,000 checks, the hit rate. Every check runs the full vanilla computation in addition to the cache, so the game is slower than without Blindspot while it's enabled. It's only meant for testing and bug reports.

</details>

## Installation

1. Download the jar from [Modrinth](https://modrinth.com/mod/blindspot) or the [GitHub Releases page](https://github.com/cheter0410/blindspot/releases)
2. Place it in your `mods` folder.
3. Launch the game normally.

## Compatibility

Blindspot doesn't change what is drawn, what is sent to or received from a server, or how the world is simulated. Its hooks into the sidebar, tab list, scoreboard and packet handling code only cache results vanilla would compute anyway, or note when those results change.

Tested together with Sodium 0.9.2 and Lithium 0.26.2 on Minecraft 26.3, in singleplayer and multiplayer. It hasn't been tested with every mod.

Mods that completely replace the vanilla sidebar or tab list code may conflict with it. Blindspot is set up so that such a conflict usually shows up as a crash at startup instead of silently wrong results in game. If you run into one, please open an issue with your log.

## Building

Requires JDK 25.

```
./gradlew build
```

The jar is written to `build/libs/`.

## License

Licensed under LGPL-3.0-only. See [LICENSE](https://github.com/cheter0410/blindspot/blob/main/LICENSE) for details.

## Contributing

Found another small, overlooked performance issue like these? Feel free to open an issue or pull request.
