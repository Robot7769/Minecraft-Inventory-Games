# Konfigurace Minihry Cookie Clicker

Tento soubor popisuje možnosti konfigurace pro minihru Cookie Clicker v rámci pluginu Minecraft Inventory Games.

## Hlavní nastavení

- `inventory_size`: Velikost GUI inventáře. Musí být násobek 9 (min 9, max 54).
- `slots`: Definice pozic jednotlivých tlačítek v inventáři.
- `formating`: Nastavení pro zobrazení velkých čísel (např. 1.25M místo 1,250,000).

## Vylepšení (`upgrades`)

Základní budovy, které lze kupovat opakovaně. Každá úroveň zvyšuje CPS (Cookies Per Second) nebo CPC (Cookies Per Click).

- `name`: Název vylepšení.
- `material`: Minecraft materiál zástupného předmětu.
- `baseCost`: Cena za 1. úroveň.
- `costMultiplier`: Koeficient navýšení ceny (obvykle 1.15).
- `cpsPerLevel`: Bonus k produkci za sekundu. Podporuje:
  - `+X.X`: Přičte hodnotu k základu.
  - `*X.X`: Vynásobí celkovou produkci.
- `require`: Podmínky pro zobrazení v obchodě.
  - `cookies`: Potřebný počet sušenek v držení.
  - `cps`: Potřebná produkce za sekundu.
  - `cpc`: Potřebný výdělek za klik.
  - `clicked`: Celkový počet kliknutí na sušenku.

## Jednorázová vylepšení (`one-time-upgrades`)

Vylepšení, která lze koupit pouze jednou. Často zdvojnásobují efektivitu konkrétních budov nebo přidávají globální bonusy.

- Parametry jsou stejné jako u běžných vylepšení.
- `one_time: true`: Označuje, že vylepšení zmizí po zakoupení.

## Úspěchy (`achievements`)

Milníky, za které hráč získává oznámení a zvukový efekt.

- `require`: Podmínky pro získání (stejné jako u vylepšení).
- `sound`: Název Minecraft zvuku (např. `ENTITY_PLAYER_LEVELUP`).

## Rebirth Vylepšení (`rebirth-upgrades`)

Trvalá vylepšení kupovaná za Heavenly Chips po provedení Rebirthu.

- `cost`: Cena v Heavenly Chips.
- `require_rebirth`: ID jiného rebirth vylepšení, které musí být vlastněno předem.
- `slot`: Pozice v rebirth menu.

