# Blindspot

[![Modrinth](https://img.shields.io/modrinth/dt/blindspot?color=00AF5C&label=Modrinth&logo=modrinth)](https://modrinth.com/mod/blindspot)
[![License](https://img.shields.io/github/license/cheter0410/blindspot)](LICENSE)

A Minecraft Fabric mod aiming to fix small, overlooked client-side performance issues that larger optimization mods don't cover.

## Requirements

- Minecraft 26.2
- Fabric Loader >= 0.19.3
- Fabric API
- Java 25+

## Features

Blindspot targets specific micro-stutters and inefficient background tasks in the vanilla client.

- **Friends List Optimization:** Eliminates continuous memory allocations in the presence handler, recovering up to 5% of frame time.

<details>
<summary><b>Technical Details & Deep Dives</b></summary>

### Friends List presence check overhead

Vanilla's `PlayerSocialManager.getFriends()` rebuilds the entire friends list into a new list on every single call, using a `Stream` allocation each time, even though the underlying data rarely changes. Because this method is called every client tick (via `PresenceHandler.tick()`), this can end up costing a measurable share of frame time.

Blindspot caches the result and only recomputes it when the underlying friend data actually changes.

</details>

## Installation

1. Download the jar from [Modrinth](PLATZHALTER_MODRINTH_URL) or the [GitHub Releases page](PLATZHALTER_GITHUB_RELEASES_URL).
2. Place it in your `mods` folder alongside Fabric API.
3. Launch the game normally.

## Compatibility

Blindspot only uses vanilla client-side code and doesn't modify rendering, world simulation, or networking. It should be compatible with essentially any modpack, including those using Sodium, Lithium, and similar optimization mods.

## License

Licensed under LGPL-3.0-only. See [LICENSE](LICENSE) for details.

## Contributing

Found another small, overlooked performance issue like this one? Feel free to open an issue or pull request.