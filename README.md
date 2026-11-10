# Grupo_3_Mod_Minecraft — HeeHeeMod

Mod de terror para Minecraft (Forge) que agrega al **Ayuwoki**, una criatura que acecha al jugador, se teletransporta, desaparece y reaparece con jumpscares.

## Contenido del mod

### El Ayuwoki
Un monstruo hostil de 3.8 bloques de alto, más alto que un Enderman.

| Atributo | Valor |
|---|---|
| Vida | 250 (125 corazones) |
| Daño por golpe | 19 (9.5 corazones) |
| Velocidad | Un poco más rápido que un jugador corriendo |
| Rango de detección | 48 bloques |
| Resistencia al empuje | 50% |
| Experiencia al morir | 30 |

**Mecánicas:**
- **Golpe maldito:** cada golpe que acierta aplica **Oscuridad** (30 s) y **Wither II** (10 s).
- **Teletransporte:** si el jugador se aleja más de 7 bloques, aparece delante de él cada pocos segundos.
- **Desaparición con jumpscare:** cada 20–40 s se vuelve invisible e invulnerable, se coloca detrás del jugador, suena un *"hee hee"* y reaparece pegado a él con un jumpscare en pantalla.
- **"Te veo":** cada 45–90 s desaparece, susurra *"Te veo..."* (sonido y texto en pantalla) y 4 s después reaparece a lo lejos, delante del jugador, mirándolo.
- **Música de persecución:** suena una música tensa mientras te persigue.
- **Ambiente:** suelta ceniza flotando y humo negro cuando persigue.
- **No desaparece** aunque el jugador se aleje.
- **Botín:** al morir suelta 1 o 2 **Ojos de Ayuwoki**.

### Ítems y bloques
- **Ojo de Ayuwoki:** cada jugador recibe uno la primera vez que entra al mundo. Está en la pestaña *Ingredientes* del modo creativo.
- **Altar:** bloque disponible en la pestaña *Bloques de construcción* del modo creativo. Tiene dos estados: vacío y con un **Ojo de Ayuwoki** colocado, cada uno con su propio modelo.

## Requisitos
- Minecraft y Forge en las versiones indicadas en `gradle.properties`
- Java 21 (o el JDK que pida la versión de Forge del proyecto)

## Instalación (para jugar)
1. Instalá Forge para la versión de Minecraft del mod.
2. Generá el `.jar` con `./gradlew build` (queda en `build/libs/`).
3. Copiá el `.jar` en la carpeta `mods` de tu instalación de Minecraft.
4. Abrí el juego con el perfil de Forge.

## Desarrollo
Clonar el repositorio y abrirlo en IntelliJ IDEA como proyecto Gradle.

```bash
./gradlew runClient   # abre Minecraft con el mod cargado
./gradlew build       # genera el .jar
```

En Windows usar `gradlew.bat` en lugar de `./gradlew`.

### Probar el Ayuwoki
Dentro del juego, con trucos activados:

```
/summon heeheemod:ayuwoki
```

### Estructura del código
```
src/main/java/com/heeheemod/
├── HeeHeeMod.java        # Clase principal: registra todo y agrega ítems al creativo
├── ModEvents.java        # Ojo inicial al entrar y botín del Ayuwoki al morir
├── block/                # Bloques (Altar)
├── client/               # Efectos del cliente (jumpscare, música de persecución)
├── entity/               # El Ayuwoki, su registro y sus atributos
├── item/                 # Ítems (Ojo de Ayuwoki)
└── sound/                # Sonidos ("hee hee", "te veo")
```

### Balancear el mob
Los valores principales (daño, duración de efectos y tiempos entre habilidades) están como constantes al principio de `entity/AyuwokiEntity.java`. El botín se ajusta en `onLivingDrops` dentro de `ModEvents.java`.

## Integrantes — Grupo 3
- Lucas Peraza
- Facundo Gutierrez
- Nahuel Laurrauri
- Lucas Jaques

## Licencia
Ver el archivo [LICENSE](LICENSE).
