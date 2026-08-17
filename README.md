# WarframeMarketAssistant

> A local-first Warframe.Market assistant for inventory awareness,
> market analysis, listing management, and faster trading.

![Status](https://img.shields.io/badge/status-active%20development-6ea35c)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot-6DB33F)
![Frontend](https://img.shields.io/badge/frontend-React-61DAFB)
![Database](https://img.shields.io/badge/database-H2%20%7C%20PostgreSQL-4479A1)
![Language](https://img.shields.io/badge/language-Java%2021-orange)

## What is WarframeMarketAssistant?

WarframeMarketAssistant is a local market assistant designed to make
Warframe trading faster, easier to manage, and less dependent on
constantly checking Warframe.Market by hand.

The goal is not to replace Warframe.Market.

The goal is to give the player a better local interface for:

-   Viewing active listings
-   Refreshing existing listings
-   Analyzing the active market
-   Finding a reasonable sell price
-   Importing and viewing inventory
-   Quickly creating, editing, and deleting listings
-   Keeping listings near the active market without blindly undercutting
    other players

The application runs locally and is intended to face the user and the
user only.

------------------------------------------------------------------------

# Why I Built It

Warframe trading can become surprisingly repetitive.

A common process looks like this:

1.  Check an item on Warframe.Market
2.  Look through current sell listings
3.  Decide whether the lowest listing is legitimate or just an outlier
4.  Check your own price
5.  Update your listing
6.  Repeat for every item you are selling
7.  Repeat again later because your listing has moved down the order
    book

Doing this manually across dozens of listings is slow.

It also encourages a bad habit:

> See lowest price → undercut lowest price → repeat

That behavior can contribute to unnecessary price drops.

WarframeMarketAssistant tries to solve both problems.

------------------------------------------------------------------------

# Core Features

## Market Order Management

The application can pull your Warframe.Market orders and display them in
one local dashboard.

You can:

-   View active buy and sell orders
-   See quantity, price, visibility, rank, and item information
-   Create listings
-   Edit listings
-   Delete listings
-   Analyze individual listings
-   Refresh all eligible sell listings
-   See which listings changed during the latest refresh

## Market Analyzer

Every tradable item can be opened in a dedicated market analyzer.

The analyzer currently shows:

-   Lowest in-game sell price
-   Highest in-game buy price
-   Suggested sell price
-   Pricing confidence
-   In-game sell listings
-   In-game buy listings
-   Explanation for why the suggested price was chosen

Only **in-game players** are currently considered by the pricing
algorithm.

Online and offline listings are deliberately excluded from the active
market calculation.

------------------------------------------------------------------------

# Pricing Algorithm

The pricing algorithm is one of the central parts of the project.

It is not intended to simply return:

> lowest sell price

That would make the tool little more than an automated undercut button.

Instead, the algorithm tries to identify the current believable market
floor.

It looks at the shape of the active sell order book, including:

-   Supported price levels
-   Isolated low listings
-   Price clusters
-   Distance from the market floor
-   Sellers already ahead of the suggested position
-   Downward pricing staircases
-   Sparse markets
-   Market depth
-   Confidence in the recommendation

For example:

``` text
15
19
20
20
20
20
```

A single listing at 15 platinum does not necessarily mean the entire
market has moved to 15.

A price of 20 has strong support, so the algorithm may recommend:

``` text
20p
```

But this:

``` text
16
17
18
19
20
20
20
20
```

shows a very different market shape.

There are multiple players progressively occupying lower price levels.

The algorithm may instead move forward into that trend:

``` text
18p
```

The goal is to distinguish:

``` text
isolated undercut
```

from:

``` text
actual downward market movement
```

------------------------------------------------------------------------

# Confidence

Every pricing recommendation can include a confidence score.

Confidence represents:

> How strongly does the currently visible in-game market support this
> recommendation?

It does **not** mean:

> What is the probability this item will sell?

Confidence currently considers things such as:

-   Number of sellers supporting the suggested price
-   Number of sellers already below the suggested price
-   Distance from the lowest active listing
-   Overall active market depth

A recommendation can still be useful with low confidence. A market with
only a handful of active sellers simply provides less evidence.

------------------------------------------------------------------------

# Market Stability Philosophy

The pricing algorithm is designed to keep the player's listing near the
relative top of the active market while avoiding unnecessary
undercutting.

The algorithm **cannot intentionally undercut the market**. Instead, it
attempts to identify a reasonable active price level and match it.

This helps prevent the tool from contributing to an undercut-driven
downward price spiral.

## Important Limitation

The algorithm is intentionally **not designed to promote upward market
trends**.

Hypothetically, if many players used the exact same strategy, prices
could become unusually stable around established price levels because
the algorithm primarily reacts to existing market positions rather than
attempting to raise them.

That behavior is accepted. It does not necessarily need to be fixed.

Users can manually enter their own prices at any time. Manual pricing
decisions, changing supply and demand, new listings, and normal player
behavior can continue moving the market naturally.

The algorithm is designed to:

-   Keep listings competitively visible
-   Prefer price matching over undercutting
-   Avoid matching isolated undercuts
-   Recognize real downward trends
-   Avoid treating distant high-price clusters as the true market
-   Leave intentional upward repricing and speculation to the player

The algorithm answers:

> **Where should I position myself in the market that currently
> exists?**

It does not attempt to answer:

> **Where should the market move?**

------------------------------------------------------------------------

# Refresh All Orders

The application can analyze all eligible sell listings and refresh them
automatically.

The flow is roughly:

``` text
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

Even when the current price already matches the suggested price, the
listing can still be refreshed at that same price.

This is intentional. The goal is not only repricing. The goal is also
keeping listings current.

After a full refresh, the application shows:

-   Orders checked
-   Prices changed
-   Listings refreshed at the same price
-   Orders skipped
-   Per-item results

Changed items are also labeled in the Orders interface.

------------------------------------------------------------------------

# Recent Changes

The application keeps the latest refresh results locally.

The Recent Changes view shows:

-   Which listings changed
-   Old price
-   New price
-   Which listings were refreshed at the same price
-   Which listings were skipped
-   Why the pricing engine made its decision

The refresh completion window also allows item names to be opened
directly in a side market analyzer.

This makes it easy to inspect the surrounding market immediately after
an automatic price change.

------------------------------------------------------------------------

# Inventory Import

WarframeMarketAssistant can import local Warframe inventory data.

This makes it possible to build features around:

-   Inventory quantities
-   Tradable items
-   Prime parts
-   Relics
-   Mods
-   Ranked mods
-   Relic refinement
-   Market value filtering
-   Future automatic listing workflows

Inventory data is stored locally.

------------------------------------------------------------------------

# warframe-api-helper

Inventory extraction is made possible with another project:

## warframe-api-helper

Created by **Sainan**

Author: https://github.com/Sainan

Project: https://github.com/Sainan/warframe-api-helper

Massive thanks to Sainan for publishing this tool.

`warframe-api-helper` is a software tool written in C++ that reads and
parses Warframe inventory data, turning the game's inventory data from
its encoded form into structured, human-readable information that
another application can consume.

WarframeMarketAssistant uses the resulting inventory data to populate
its local inventory database.

### Important

`warframe-api-helper` is a separate project.

It is not authored by this project.

Please refer to its repository for its source code, license,
documentation, and usage details.

------------------------------------------------------------------------

# Local First

WarframeMarketAssistant is designed as a local application.

The intended architecture is:

``` text
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

There is no central WarframeMarketAssistant account system required for
the local version.

Your inventory, local market cache, configuration, and local database
can remain on your machine.

------------------------------------------------------------------------

# Database

The project currently supports two database setups.

## PostgreSQL

Used during development.

## H2

Used for local and distributable builds.

H2 allows the application to store everything in a local file without
requiring the user to install PostgreSQL.

Example:

``` text
data/
└── warframe_extractor.mv.db
```

This dramatically simplifies distribution.

A user should eventually be able to install the application and run it
without manually configuring a database server.

------------------------------------------------------------------------

# Technology Stack

## Backend

``` text
Java 21
Spring Boot
Spring Data JPA
Hibernate
Spring RestClient
H2
PostgreSQL
```

## Frontend

``` text
React
TypeScript
Vite
Lucide React
```

## External Services and Tools

``` text
Warframe.Market API
warframe-api-helper
```

------------------------------------------------------------------------

# Project Structure

A simplified structure looks like:

``` text
WarframeMarketAssistant/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/aces/warframepersonalextractor/
│       │       ├── controller/
│       │       ├── dto/
│       │       ├── external/
│       │       ├── model/
│       │       ├── repository/
│       │       └── service/
│       │
│       └── resources/
│           ├── application.properties
│           └── static/
│
├── data/
│   └── warframe_extractor.mv.db
│
└── pom.xml
```

Local profile properties, databases, generated inventory files,
credentials, and other machine-specific files should not be committed.

------------------------------------------------------------------------

# Request Rate Limiting

Warframe.Market API usage is rate limited.

The application deliberately spaces requests instead of aggressively
parallelizing market operations.

Bulk market refreshes are performed sequentially.

Conceptually:

``` text
analyze
wait
patch
wait
analyze
wait
patch
```

This is preferred over flooding the API with simultaneous requests.

Market refreshes do not need to be instantaneous. API stability is more
important.

------------------------------------------------------------------------

# Item Catalogue

The application maintains a local catalogue of Warframe.Market tradable
items.

The Warframe.Market item sync is treated as a tradable catalogue.
Anything returned through that synchronization process is stored as
tradable.

The local catalogue is used for:

-   Item search
-   Market item ID resolution
-   Human-readable names
-   Inventory mapping
-   Order creation
-   Market analysis

------------------------------------------------------------------------

# Search

Tradable items can be searched dynamically.

Results repopulate while typing.

Selecting an item opens a focused market workspace where the user can:

-   Analyze the market
-   Inspect current listings
-   See recommendation confidence
-   Choose a price
-   Create an order

------------------------------------------------------------------------

# Navigation

The frontend uses local hash navigation.

Examples:

``` text
#/orders
#/create-listing
#/inventory
#/items
#/changes
#/logs
```

This allows browser Back and Forward navigation while keeping the
application locally hosted through Spring Boot.

------------------------------------------------------------------------

# Current Status

## Working

✅ Warframe.Market authentication

✅ Pull current orders

✅ Create orders

✅ Edit orders

✅ Delete orders

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

✅ Inventory extraction

✅ Inventory parsing

✅ Mod rank handling

✅ Relic refinement handling

✅ H2 local database

✅ PostgreSQL development database

✅ React market dashboard

------------------------------------------------------------------------

# Still Under Development

**WarframeMarketAssistant is a market assistant, not a complete
replacement for Warframe.Market.**

Like all tools, it is not perfect.

The application and its pricing algorithm are still under active
development. There will be market conditions where manual judgment is
better than the automated recommendation.

That is intentional.

The application always allows the player to manually select or enter
another price.

Future development may include:

-   Better confidence modeling
-   Historical market observations
-   Market movement detection
-   Better inventory mapping
-   Inventory value filtering
-   Improved relic support
-   Better mod handling
-   More detailed price reasoning
-   Market history
-   Listing age analysis
-   Improved distribution and installer support
-   Additional safeguards around automated refresh behavior

------------------------------------------------------------------------

# Important Disclaimer

This project is not affiliated with Digital Extremes or Warframe.Market.

Warframe is a trademark of Digital Extremes.

Warframe.Market is a separate third-party service.

This software is an independent project that interacts with external
APIs and tools.

Users are responsible for ensuring their usage complies with the
applicable rules, API policies, terms of service, and licenses of the
services and tools they use.

------------------------------------------------------------------------

# Development Philosophy

This project follows a simple rule:

> **The frontend displays. The backend decides.**

React should not determine market pricing.

The backend owns:

-   Market analysis
-   Pricing decisions
-   Confidence calculation
-   Order construction
-   Validation
-   Refresh behavior
-   External API interaction

The frontend receives those decisions and presents them to the user.

This allows the pricing engine to evolve without moving business logic
into the frontend.

------------------------------------------------------------------------

# The Goal

The goal is not automated market domination.

The goal is not flipping.

The goal is not forcing the market in a particular direction.

The goal is much simpler:

> **Spend less time babysitting listings and more time actually playing
> Warframe.**

**Grind the item.**

**List the item.**

**Stay competitive.**

**Sell the item.**

**Get back into the game.**
