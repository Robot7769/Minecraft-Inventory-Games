# Cookie Clicker Funkce - Souhrn Implementace

## Přidané Funkce

### 1. Podpora PlaceholderAPI/Hologramů
**Umístění:** `PlaceholderManager.java` (nový soubor)

Zástupné znaky dostupné pro použití s PlaceholderAPI (PAPI) a pluginy hologramů:

**Formát:**
- `%games_cookieclicker_cookies%` - Celkové sušenky (neformátované)
- `%games_cookieclicker_cookies_formatted%` - Celkové sušenky (formátované, např. "1,5M")
- `%games_cookieclicker_cps%` - Sušenky za sekundu (neformátované)
- `%games_cookieclicker_cps_formatted%` - Sušenky za sekundu (formátované)
- `%games_cookieclicker_cpc%` - Sušenky za klik (neformátované)
- `%games_cookieclicker_cpc_formatted%` - Sušenky za klik (formátované)
- `%games_cookieclicker_clicks%` - Celkový počet kliknutí (neformátované)
- `%games_cookieclicker_chips%` - Nebeské čipy (neformátované)
- `%games_cookieclicker_chips_formatted%` - Nebeské čipy (formátované)

**Příklad použití (v konfiguraci hologramu):**
```
lines:
  - "§e§lImpérium Sušenek"
  - ""
  - "§f%games_cookieclicker_cookies_formatted%"
  - "§7Sušenky/s: %games_cookieclicker_cps_formatted%"
  - "§7Za Klik: %games_cookieclicker_cpc_formatted%"
```

### 2. Multiplikátory Trvání Zlaté Sušenky
**Umístění:** `cookieclicker.yml` - Přidána nová jednorazová vylepšení

Dvě nová kupitelná vylepšení, která prodlužují trvání efektů zlaté sušenky:

#### Prodloužené Šílenství (Extended Frenzy)
- **Cena:** 1 000 000 sušenek
- **Efekt:** Zdvojnásobuje dobu trvání efektu Frenzy
- **Materiál:** GLOWSTONE
- **Konfigurační klíč:** `golden_cookie_mult: "frenzy_duration"`

#### Prodloužené Zběsilé Klikání (Extended Click Frenzy)
- **Cena:** 2 000 000 sušenek
- **Efekt:** Zdvojnásobuje dobu trvání efektu Click Frenzy
- **Materiál:** AMETHYST_CLUSTER
- **Konfigurační klíč:** `golden_cookie_mult: "click_frenzy_duration"`

### 3. Změny Konfigurace Pluginu
**Umístění:** `plugin.yml`

Přidáno PlaceholderAPI jako měkkou závislost:
```yaml
softdepend:
  - PlaceholderAPI
```

### 4. Konfigurace Buildu
**Umístění:** `build.gradle.kts`

Přidána závislost PlaceholderAPI:
```kotlin
compileOnly("me.clip:placeholderapi:2.11.5")
```

A Maven repozitář pro PlaceholderAPI:
```kotlin
maven("https://repo.extendedclip.com/releases/")
```

### 5. Herní Mechanika
**Umístění:** `CookieClicker.java`

Přidána pole multiplikátorů pro trvání efektů zlaté sušenky:
- `gcFrenzyDurationMult` - Multiplikátor pro trvání efektu šílenství (výchozí: 1.0)
- `gcClickFrenzyDurationMult` - Multiplikátor pro trvání efektu zběsilého klikání (výchozí: 1.0)

Tyto multiplikátory lze zvýšit nákupem nových jednorazových vylepšení.

## Změněné Soubory

1. **Vytvořeno:**
   - `src/main/java/me/robot7769/InvGames/manager/PlaceholderManager.java`

2. **Upraveno:**
   - `src/main/java/me/robot7769/InvGames/InvGamesPlugin.java` - Přidána registrace PlaceholderManager
   - `src/main/java/me/robot7769/InvGames/games/CookieClicker.java` - Přidána pole multiplikátorů trvání
   - `src/main/resources/plugin.yml` - Přidána měkká závislost PlaceholderAPI
   - `src/main/resources/games/cookieclicker.yml` - Přidána nová jednorazová vylepšení
   - `build.gradle.kts` - Přidána závislost PlaceholderAPI a repozitář

## Použití

### Pro Uživatele Hologramů/PAPI:

1. Ujistěte se, že je na serveru nainstalován PlaceholderAPI
2. Použijte zástupné znaky v konfiguraci vašeho pluginu hologramu
3. Plugin automaticky formátuje čísla podle nakonfigurovaných pravidel formátování

### Pro Administrátory Serveru:

1. Hráči si mohou koupit vylepšení "Prodloužené Šílenství" a "Prodloužené Zběsilé Klikání"
2. Každé vylepšení zdvojnásobuje dobu trvání příslušného efektu zlaté sušenky
3. Zástupné znaky fungují automaticky bez potřeby dalších konfigurací

## Poznámky k Integraci

- PlaceholderAPI je **měkká závislost**, což znamená, že plugin funguje správně i bez PlaceholderAPI
- Pokud PlaceholderAPI není k dispozici, je v protokolu zaznamenána zpráva; "PlaceholderAPI expansion registered!" se zobrazí pouze pokud je PAPI přítomno
- Systém zástupných znaků používá reflexi pro přístup k datům CookieClicker, což jej činí robustním a neinvazivním
- Všechny zástupné znaky se aktualizují v reálném čase na základě aktuálních dat hráčů

