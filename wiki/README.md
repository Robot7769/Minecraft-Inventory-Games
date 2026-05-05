# InvGame

InvGame je všestranný Minecraft plugin, který poskytuje minihry založené na GUI přímo uvnitř inventářů.
Nabízí ukládání dat pomocí SQL i lokálních YAML souborů, plně dynamické překlady, a přizpůsobitelné herní systémy.

## Obecná konfigurace

### `config.yml`
Tento soubor určuje globální konfiguraci pluginu, zejména způsob ukládání stavu her a offline postupu hráče (zda se využije SQL databáze, nebo interní přes YAML).

```yaml
# Nastavení SQL
sql:
  enabled: true              # Pokud je `false`, plugin přejde na lokální ukládání do `data.yml`. Zapněte pro BungeeCord sítě apod.
  type: "mariadb"            # Typ databáze (mariadb, mysql, postgresql)
  host: "localhost"          
  port: 3306                 
  database: "invgame_db"     
  username: "root"           
  password: "password"       
```

### `messages/messages.yml`
Zpracovává lokalizaci (překlady) všech prvků GUI, odpovědí na příkazy, statistik a globálních zpráv. Tento soubor kompletně nahrazuje a překládá jakýkoliv fixní text v pluginu.

Proměnné jako `%cookies%`, `%cps%`, `%earned%` nebo `%cost%` mohou být volně vloženy kamkoliv do formátu zpráv. Znak `&` nativně podporuje Minecraftové barvy.
