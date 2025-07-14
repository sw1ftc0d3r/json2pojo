# JSON to POJO IntelliJ Plugin

A plugin per IntelliJ IDEA che genera classi Java POJO da input JSON con supporto per annotazioni Jackson e Lombok.

## Funzionalità

- Genera classi Java da JSON
- Supporto per annotazioni Jackson (@JsonProperty)
- Supporto per annotazioni Lombok configurabili:
  - @Getter
  - @Setter
  - @Data
  - @Builder
  - @NoArgsConstructor
  - @AllArgsConstructor
- Gestione di oggetti annidati
- Gestione di array
- Interfaccia utente intuitiva per la configurazione

## Utilizzo

### Modalità 1: Dal menu Generate
1. Installa il plugin in IntelliJ IDEA
2. Apri un progetto Java
3. Usa il menu "Code" → "Generate" → "Generate POJO from JSON" o la scorciatoia `Ctrl+Alt+J`
4. Incolla il tuo JSON nella finestra di dialogo
5. Configura le opzioni:
   - Nome del package
   - Nome della classe principale
   - Abilitazione Jackson
   - Abilitazione Lombok e selezione delle annotazioni
6. Clicca "OK" per generare le classi

### Modalità 2: Dal menu contestuale New (Consigliata)
1. Installa il plugin in IntelliJ IDEA
2. Apri un progetto Java
3. Fai clic destro su una cartella/package nel Project Explorer
4. Seleziona "New" → "POJO from JSON"
5. Il nome del package viene automaticamente rilevato dalla cartella selezionata
6. Incolla il tuo JSON nella finestra di dialogo
7. Configura le opzioni (il package name è già precompilato)
8. Clicca "OK" per generare le classi nella cartella selezionata

## Esempio

Input JSON:
```json
{
  "name": "John Doe",
  "age": 30,
  "active": true,
  "address": {
    "street": "123 Main St",
    "city": "New York"
  },
  "hobbies": ["reading", "swimming"]
}
```

Output (con Jackson e Lombok @Getter/@Setter):

```java
package com.swiftcoder.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Person {
    @JsonProperty("name")
    private String name;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("address")
    private Address address;

    @JsonProperty("hobbies")
    private List<String> hobbies;
}
```

## Sviluppo

### Prerequisiti
- Java 11 o superiore
- IntelliJ IDEA 2023.1 o superiore

### Build
```bash
./gradlew build
```

### Test
```bash
./gradlew test
```

## Struttura del Progetto

- `src/main/java/com/example/json2pojo/`
  - `actions/` - Azioni dell'IDE
  - `models/` - Modelli di dati
  - `generators/` - Generatori di codice Java
  - `ui/` - Interfaccia utente
  - `JsonParser.java` - Parser JSON principale

## Licenza

MIT License