## Konfigurace Cookie Clicker
Hlavní konfigurace pro Cookie Clicker je umístěna v souboru `games/cookieclicker.yml`.

### Formátování čísel
Všechny numerické hodnoty (ceny, produkce, požadavky) jsou zpracovávány jako `double` a mohou být zadávány v běžném desetném formátu (např. `1000.0`) nebo ve zkráceném formátu s jednotkami (např. `1K`, `2.5M`, `3B`, `4T` atd.).
Plugin interně převádí tyto zkrácené formáty na plné číselné hodnoty pro výpočty.

### Základní rozložení
* `inventory_size`: Určuje velikost hlavního GUI minihry (počet slotů, musí být dělitelný 9).
* `slots`: Slouží pro určení pozic Sušenky a tlačítek pro přepínání menu (Vylepšení, Znovuzrození, Úspěchy, Zpět) v hlavním inventáři pomocí jejich indexů (slotů).

```yaml
inventory_size: 45
slots:
  cookie: 22
  upgrades_button: 26
  rebirth_button: 40
  achievements_button: 18
  one_time_button: 17
  back_button: 36
```

### Škálovatelná Vylepšení (`upgrades:`)
Ve větvi `upgrades:` můžete nadefinovat teoreticky nekonečné množství vylepšení.

Základní škálování pro cenu po každém nákupu využívá standardní inkrementální model:
`Nová_Cena = Math.floor(baseCost * Math.pow(costMultiplier, level))`

Podporuje výrazy začínající na `+` (zýšení o pevnou částku) a `*` (násobení), a umožňuje tak velkou flexibilitu:
* `cpsPerLevel` : Zvyšuje počet generovaných sušenek za sekundu (např. `"+1.0"` přidá celkově 1 CPS, `"*1.05"` znásobí vaši celkovou produkci o 5%).
* `clickPerLevel`: Hodnoty ovlivňující navýšení nebo znásobení pro každé kliknutí.
* `require`: Umožňuje zamčení/skrytí vylepšení do doby, než hráč dosáhne určitých milníků. K odemčení dojde, až když jsou splněny **všechny nastavené požadavky**.
    * Možnosti pro parametr `require`:
        * `cookies`: Minimální nasbíraný počet sušenek za historii samotné hry.
        * `cps`: Minimální produkce sušenek za sekundu, které momentálně hráč dosahuje.
        * `cpc`: Zkratka pro Cookies Per Click. Hráč musí dosáhnout určité úderné síly z 1 zakliknutí.
        * `clicked`: Počet manuálních zakliknutí na sušenku celkem. Výborné pro vylepšení kurzorů nebo myší.

```yaml
upgrades:
  grandma:
    name: "Babička"
    material: "WHEAT"
    baseCost: 100.0
    costMultiplier: 1.15
    cpsPerLevel: "+1.0"
    clickPerLevel: "+0.0"
    require:            # Požadavek na odemčení
      cookies: 10.0     # Hráč musí nejprve napéct 10 cookies
      cps: 0.0          # Omezení automatické produkce
      cpc: 0.0          # Omezení minimální síly kliku
      clicked: 0.0      # Omezení nutných zakliknutí myší celkem
    description: "Peče cookies pasivně."
```

### Jednorázová Vylepšení (`one-time-upgrades:`)
Fungují naprosto stejně jako klasická vylepšení, avšak disponují limitem povolujícím pouze unikátní koupi s jedním použitím v relaci.
Z tohoto důvodu nepoužívají po zakoupení logiku `costMultiplier`.

```yaml
  carpal_tunnel:
    name: "Krém proti karpálnímu tunelu"
    material: "BOWL"
    baseCost: 500.0
    costMultiplier: 1.0 # Negováno skrz definici jednorázového nákupu (one_time)
    cpsPerLevel: "+0.0"
    clickPerLevel: "*2.0" # Efektivní a drastické zdvojnásobení
    require:
      cpc: 0.0          # Lze vyžadovat určitou minimální sílu kliku
      clicked: 100.0    # Odemkne se až pote, co hráč 100x klikne na sušenku
    one_time: true
```

### Úspěchy (`achievements:`)
Odemknou se ve chvíli, kdy vnitřní systém zaznamená dosažení milníku podle atributu `require`. Můžete definovat zvuk přímo ke každému úspěchu na klíči `sound`.
Stránkování GUI je plně dynamické a v případě velkého seznamu úspěchů nativně vytváří další strany.

```yaml
achievements:
  millionaire:
    name: "Sušenkový milionář"
    material: "GOLD_INGOT"
    require: {cookies: 1000000.0, cps: 0.0, cpc: 0.0, clicked: 0.0}
    description: "Upečte celkem 1 000 000 sušenek."
    sound: "ENTITY_UI_TOAST_CHALLENGE_COMPLETE"
```

### Znovuzrození & Prestiž Vylepšení (`rebirth-upgrades:`)
Strom permanentních dovedností, jenž zpřístupní Ascension mechanismus (Znovuzrození / Povznesení).<br>
Vycení se pomocí speciální komodity `Heavenly Chips` ("Nebeské Čipy"), jejíž produkce se kalkuluje dynamicky ze sebraných sušenek: `10^12 * (3K^2 + 3K + 1)` (`K` znamená kolik Heavenly Chips už máš).

Z důvodu vykreslování vylepšení prestiže v inventáři jako jakéhosi "stromu dovedností" konfigurace nuceně vyžaduje index vlastnosti (`slot`), kterým zafixuje mapování sušenek do přesného umístění.

```yaml
rebirth-upgrades:
  heavenly_bakery:
    name: "Nebeská pekárna"
    material: "BRICKS"
    cost: 99.0
    cpsPerLevel: "*2.0"
    clickPerLevel: "+0.0"
    slot: 19 # Fixuje renderování přesně do slotu "19" z inventáře
    description: "Pekárna v oblacích, která zdvojnásobuje vaše CPS."
```
