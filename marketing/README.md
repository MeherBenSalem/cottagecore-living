# Marketing assets: 1.0

Listing copy and artwork for CurseForge (NightBeam publishes to CurseForge only). Nothing here ships in the mod JAR.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full CurseForge project description (Markdown) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short CurseForge project summary (224 characters) |
| `images/icon.png` | Project avatar |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/cottagecore-living/main/marketing/images/...`. **They only work for visitors once this folder is on `main` and the repository is public.** While the repository is private, upload the images to CurseForge instead (the description editor takes uploads) and replace each URL.

The description leads with the pitch, then the collection, the five sets, interior, garden, the seasons, right-click interactions, placing, the Collection Book, crafting, configuration, everything else, downloads and compatibility, the FAQ and a gallery.

## Adding a Minecraft version

The supported versions are listed in these places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, for example `{ minecraft: '26.4', loaders: ['Fabric', 'NeoForge'] },`. The banner's loader pills and version line are built from it. Then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`. Add one row per loader.
3. The FAQ answer **Is there a Fabric or NeoForge version?** in `curseforge/description.md`.

No other copy or artwork names a Minecraft version. A new release number goes in `RELEASE` in `render.cjs` and in the banner's alt text on the first line of the description.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600, 855 KB | Description header: the strawberry parlour, the title, "The little details.", the loader pills and version line |
| `images/icon.png` | 512×512, 136 KB | Project avatar: the Strawberry Cottage teapot in a red gingham ring |
| `images/card-collection.png` | 1600×900, 608 KB | Fifteen decorations from all three collections; 285 decorations, 84 kinds, 5 sets, 5 tablecloths |
| `images/card-sets.png` | 1600×900, 564 KB | The five sets, each with its teapot, flower vase, cuckoo clock, birdhouse, garden lantern, gnome and toadstools |
| `images/card-interior.png` | 1600×900, 1142 KB | The strawberry parlour close-up, plus seven interior pieces |
| `images/card-garden.png` | 1600×900, 591 KB | Eight garden pieces with what they do |
| `images/card-seasons.png` | 1600×900, 545 KB | The seasonal tree, wreath and centerpiece in each of the four seasons |
| `images/card-interactions.png` | 1600×900, 578 KB | Eight pieces with their right-click |
| `images/card-placing.png` | 1600×900, 1189 KB | The decorated parlour, plus floor, wall, ceiling, window and joining pieces |
| `images/card-book.png` | 1600×900, 865 KB | The Collection Book's Interior and Sets pages |
| `images/card-config.png` | 1600×900, 497 KB | Excerpts of `cottagecoreliving-common.toml` and `cottagecoreliving-client.toml` |
| `images/header-*.png` | 1400×120, transparent, 32–53 KB | 14 section headings for the description |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/screenshots/*.jpg` | 1280×720, 116–262 KB | 9 captures: gallery images and card art |

### Where the pictures come from

- **Screenshots** are the unedited captures in `docs/screenshots/`, converted to JPEG. They were taken by the showcase harness (`cd 1.20.1 && ./gradlew :forge:runClient -Pshowcase`, `1.20.1/forge/.../dev/ShowcaseHarness.java`): a flat creative world in Peaceful, 1280×720, HUD hidden. The harness builds the parlour, garden, seasons corner, wall and set lineup itself, so every block in them is placed by the mod's own code.
- **Decoration renders** on the cards, banner and icon are isometric renders of the mod's own generated models and textures, made without Minecraft by [`source/renders.py`](source/renders.py) (which uses `tools/preview_models.py`). They are in `source/renders/`, one PNG per item plus each season of the seasonal pieces, the lit garden lanterns, the open picnic basket and the open fairy door.

The screenshots are smaller than the 1920×1080 captures of the sibling kits and show a flat world. New captures in a generated world at 1920×1080 would make the gallery and the interior and placing cards stronger: retake them, drop them into `images/screenshots/` under the same names, and adjust the `region()` crops in `render.cjs`, which are pixel regions of the 1280×720 captures.

## Regenerating the artwork

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots, the decoration renders and the bundled fonts:

```bash
CL_VANILLA=/path/to/extracted-1.20.1-client/assets/minecraft python marketing/source/renders.py
```

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs            # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

- `renders.py` needs `CL_VANILLA` for the pieces that borrow vanilla planks and stone (tables, stepping stones, fountains and a few more); without it they are skipped and listed.
- Set `CHROMIUM=/path/to/chromium` to render with a Chromium other than Playwright's own download.
- `source/backdrop.jpg` is the blurred card background, a copy of `screenshots/parlour-closeup.jpg`.
- `source/logo.png` is the mod-list logo (`src/main/resources/cottagecoreliving_logo.png`).
- `source/fonts/` holds Fredoka and Nunito with their SIL Open Font License 1.1 texts. The script uses only these files.

Set names, colours and motifs on the cards are written at the top of `render.cjs`, copied from `catalog/DecorSet.java`, the lang file and the project README. Update them when those change.

## Wording

Every number in the copy and on the cards is a 1.0.0 default from `config/CottageConfig.java`, a recipe, a tag, or `catalog/DecorCatalog.java` (285 decorations in 84 kinds: 155 interior, 89 garden, 41 seasonal; 44 per set). Update the copy when those change. Call the placeable blocks **decorations**, the five themes **sets**, Interior, Garden and Seasonal **collections**, and the book the **Decoration Collection Book** (the **Collection Book** after first mention).
