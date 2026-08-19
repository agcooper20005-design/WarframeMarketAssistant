# WarframeMarketAssistant

> A local-first Warframe.Market assistant for inventory awareness, market analysis, listing management, and faster trading.

![Status](https://img.shields.io/badge/status-active%20development-6ea35c)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot-6DB33F)
![Frontend](https://img.shields.io/badge/frontend-React-61DAFB)
![Database](https://img.shields.io/badge/database-H2%20%7C%20PostgreSQL-4479A1)
![Language](https://img.shields.io/badge/language-Java%2021-orange)
![Platform](https://img.shields.io/badge/platform-Windows-0078D6)

## What is WarframeMarketAssistant?

WarframeMarketAssistant is a local market assistant designed to make Warframe trading faster, easier to manage, and less dependent on constantly checking Warframe.Market by hand.

The goal is not to replace Warframe.Market.

The goal is to give the player a better local interface for:

* Viewing active listings
* Refreshing existing listings
* Analyzing the active market
* Finding a reasonable sell price
* Importing and viewing inventory
* Quickly creating, editing, and deleting listings
* Keeping listings near the active market without blindly undercutting other players

The application runs locally and is intended to face the user and the user only.

## Why I Built It

Warframe trading can become surprisingly repetitive.

A common process looks like this:

1. Check an item on Warframe.Market
2. Look through current sell listings
3. Decide whether the lowest listing is legitimate or just an outlier
4. Check your own price
5. Update your listing
6. Repeat for every item you are selling
7. Repeat again later because your listing has moved down the order book

Doing this manually across dozens of listings is slow.

It also encourages a bad habit:

> See lowest price → undercut lowest price → repeat

That behavior can contribute to unnecessary price drops.

WarframeMarketAssistant tries to solve both problems.

## Core Features

### Market Order Management

You can:

* View active buy and sell orders
* Create listings
* Edit listings
* Delete listings
* Analyze individual listings
* Refresh all eligible sell listings
* See which listings changed during the latest refresh

### Market Analyzer

The analyzer currently shows:

* Lowest in-game sell price
* Highest in-game buy price
* Suggested sell price
* Pricing confidence
* In-game sell listings
* In-game buy listings
* Explanation for why the suggested price was chosen

Only **in-game players** are currently considered by the pricing algorithm.

Online and offline listings are deliberately excluded from the active market calculation.

## Pricing Algorithm

The pricing algorithm is not intended to simply return the lowest sell price.

Instead, it tries to identify the current believable market floor using:

* Supported price levels
* Isolated low listings
* Price clusters
* Distance from the market floor
* Sellers already ahead of the suggested position
* Downward pricing staircases
* Sparse markets
* Market depth
* Confidence in the recommendation

Example:

```text
15
19
20
20
20
20
```

A single 15 platinum listing does not necessarily mean the entire market has moved to 15.

A price of 20 has strong support, so the algorithm may recommend:

```text
20p
```

But:

```text
16
17
18
19
20
20
20
20
```

shows multiple players progressively occupying lower price levels. The algorithm may instead move forward into that trend:

```text
18p
```

The goal is to distinguish an isolated undercut from actual downward market movement.

## Confidence

Confidence represents:

> How strongly does the currently visible in-game market support this recommendation?

It does **not** mean:

> What is the probability this item will sell?

Confidence considers market support, sellers already ahead of the recommendation, distance from the active floor, and overall market depth.

## Market Stability Philosophy

The pricing algorithm is designed to keep the player's listing near the relative top of the active market while avoiding unnecessary undercutting.

The algorithm **cannot intentionally undercut the market**. Instead, it attempts to identify a reasonable active price level and match it.

This helps prevent the tool from contributing to an undercut-driven downward price spiral.

### Important Limitation

The algorithm is intentionally **not designed to promote upward market trends**.

If many players hypothetically used the exact same strategy, prices could become unusually stable around established price levels because the algorithm reacts primarily to existing market positions rather than trying to raise them.

That behavior is accepted.

Users can manually enter their own prices at any time. Manual pricing decisions, changing supply and demand, new listings, and normal player behavior can continue moving the market naturally.

The algorithm is designed to:

* Keep listings competitively visible
* Prefer price matching over undercutting
* Avoid matching isolated undercuts
* Recognize real downward trends
* Avoid treating distant high-price clusters as the true market
* Leave intentional upward repricing and speculation to the player

The algorithm answers:

> **Where should I position myself in the market that currently exists?**

It does not attempt to answer:

> **Where should the market move?**

## Refresh All Orders

WarframeMarketAssistant can analyze all eligible sell listings and refresh them automatically.

```text
Load current orders
        ↓
Analyze each sell listing
        ↓
Determine suggested price
        ↓
PATCH the existing order
        ↓
Record the result
```

Even when the current price already matches the suggested price, the listing can still be refreshed at that same price.

After a full refresh, the application shows:

* Orders checked
* Prices changed
* Listings refreshed at the same price
* Orders skipped
* Per-item results

## Inventory Import

WarframeMarketAssistant can import local Warframe inventory data.

This supports inventory quantities, tradable items, Prime parts, relics, mods, ranked mods, relic refinement, and future inventory-driven listing workflows.

Inventory data is stored locally.

## warframe-api-helper

Inventory extraction is made possible by **warframe-api-helper**, created by **Sainan**.

Project: https://github.com/Sainan/warframe-api-helper

Author: https://github.com/Sainan

Massive thanks to Sainan for publishing this tool.

`warframe-api-helper` is a C++ tool that reads and parses Warframe inventory data into structured, human-readable information that another application can consume.

WarframeMarketAssistant uses that output to populate its local inventory database.

`warframe-api-helper` is a separate project and is not authored by WarframeMarketAssistant. Its license and attribution notices are included in `THIRD_PARTY_LICENSES.txt`.

## Application Icon

The application uses a Warframe icon provided by **Icons8**.

Icon page: https://icons8.com/icon/lv2Y3x71H9lh/warframe

Icons8: https://icons8.com/

The icon is used as application artwork only and is not offered as a standalone downloadable asset.

WarframeMarketAssistant is not affiliated with Digital Extremes, Warframe, Warframe.Market, or Icons8.

## Local First

WarframeMarketAssistant is designed as a local application.

```text
Warframe
    ↓
warframe-api-helper
    ↓
WarframeMarketAssistant
    ↓
Local Database
    ↓
Warframe.Market API
```

There is no central WarframeMarketAssistant account system required for the local version.

Your inventory, local market cache, configuration, and local database remain on your machine.

## Database

### PostgreSQL

Used during development.

### H2

Used for local and distributable builds.

H2 allows the application to store everything in a local file without requiring the user to install PostgreSQL.

```text
data/
└── warframe_extractor.mv.db
```

## Technology Stack

### Backend

```text
Java 21
Spring Boot
Spring Data JPA
Hibernate
Spring RestClient
H2
PostgreSQL
```

### Frontend

```text
React
TypeScript
Vite
Lucide React
```

### External Services and Tools

```text
Warframe.Market API
warframe-api-helper
Icons8
```

## Request Rate Limiting

Warframe.Market API usage is rate limited.

The application deliberately spaces requests instead of aggressively parallelizing market operations.

Bulk market refreshes are performed sequentially.

```text
analyze
wait
patch
wait
analyze
wait
patch
```

API stability is more important than making bulk refreshes instantaneous.

## Current Status

### Working

✅ Warframe.Market authentication

✅ Pull current orders

✅ Create, edit, and delete orders

✅ Refresh existing orders

✅ Bulk refresh sell listings

✅ Dynamic item search

✅ Local market item catalogue

✅ Market analyzer

✅ In-game seller filtering

✅ Lowest sell detection

✅ Highest buy detection

✅ Suggested sell algorithm

✅ Market confidence scoring

✅ Changed listing tracking

✅ Recent refresh history

✅ Inventory extraction and parsing

✅ Mod rank handling

✅ Relic refinement handling

✅ H2 local database

✅ PostgreSQL development database

✅ React market dashboard

✅ Windows packaging workflow

## Still Under Development

**WarframeMarketAssistant is a market assistant, not a complete replacement for Warframe.Market.**

Like all tools, it is not perfect.

The application and pricing algorithm are still under active development. There will be market conditions where manual judgment is better than the automated recommendation.

That is intentional.

The application always allows the player to manually select or enter another price.

## License

WarframeMarketAssistant is currently source-available for viewing and
contribution. No open-source license has been granted for the project at
this time.

Third-party components remain subject to their respective licenses.
See `THIRD_PARTY_LICENSES.txt` for details.

## Important Disclaimer

This project is not affiliated with Digital Extremes or Warframe.Market.

Warframe is a trademark of Digital Extremes.

Warframe.Market is a separate third-party service.

This software is an independent project that interacts with external APIs and third-party tools.

Users are responsible for ensuring their usage complies with the applicable rules, API policies, terms of service, and licenses of the services and tools they use.

## Development Philosophy

> **The frontend displays. The backend decides.**

The backend owns market analysis, pricing decisions, confidence calculation, order construction, validation, refresh behavior, and external API interaction.

The frontend receives those decisions and presents them to the user.

## The Goal

The goal is not automated market domination.

The goal is not flipping.

The goal is not forcing the market in a particular direction.

The goal is much simpler:

> **Spend less time babysitting listings and more time actually playing Warframe.**

**Grind the item.**

**List the item.**

**Stay competitive.**

**Sell the item.**

**Get back into the game.**
