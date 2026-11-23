"""Turns the decoration families into resource files and the generated Java catalog."""
import json
import re
from pathlib import Path

from . import paint
from .core import (ASSETS, DATA, MC_DATA, MODID, Img, bounds, centred_for_item, check_range, display_for, mix,
                   move, retex, serialise, write_json, C, darken, lighten)
from .fam import FAMILIES, Ctx, Family
from .paint import PAL, SETS, SET_JAVA, SET_NAMES

M = MODID + ":"
JAVA_OUT = Path(__file__).resolve().parents[2] / "1.20.1" / "common" / "src" / "main" / "java" / "com" / "sugarplumstudios" / "cottagecore" / "catalog" / "DecorCatalog.java"

FACING_Y = {"north": 0, "east": 90, "south": 180, "west": 270}
WATER = "minecraft:block/water_still"

# --------------------------------------------------------------------------- models


def var_name(ref):
    path = ref.split(":", 1)[1]
    if path.startswith("block/"):
        path = path[6:]
    if ref.startswith("minecraft:"):
        path = "mc_" + path
    return re.sub(r"[^a-z0-9_]", "_", path)


def model_json(name, els, render, display=None):
    check_range(name, els)
    refs = []
    for e in els:
        for f in e.faces.values():
            if f is None:
                continue
            ref = f if isinstance(f, str) else f[0]
            if ref not in refs:
                refs.append(ref)
    mapping = {r: var_name(r) for r in refs}
    body = serialise(retex(els, mapping))
    water_var = mapping.get(WATER)
    if water_var:
        for el in body:
            for face in el["faces"].values():
                if face["texture"] == "#" + water_var:
                    face["tintindex"] = 0
    textures = {v: r for r, v in mapping.items()}
    particle = next((r for r in refs if r != WATER and "flame" not in r and "hands" not in r), refs[0] if refs else None)
    if particle:
        textures["particle"] = particle
    out = {"parent": "minecraft:block/block", "render_type": "minecraft:" + render, "textures": textures,
           "elements": body}
    if display:
        out["display"] = display
    return out


def write_model(path, els, render, display=None):
    write_json(ASSETS / "models" / (path + ".json"), model_json(path, els, render, display), compact=True)
    return M + path


LOW = 15.0  # how far a decoration drops to stand on a blanket or carpet


def item_display(fam, els):
    d = display_for(els)
    if fam.wall_item():
        d["gui"]["rotation"] = [0, 180, 0]
        d["fixed"]["rotation"] = [0, 0, 0]
    return d


# --------------------------------------------------------------------------- shapes


def shape_box(els):
    lo, hi = bounds(els)
    box = []
    for i in range(3):
        a = max(0.0, min(16.0, lo[i]))
        b = max(0.0, min(16.0, hi[i]))
        a = round(a * 2) / 2
        b = round(b * 2) / 2
        if b - a < 1:
            if a + 1 <= 16:
                b = a + 1
            else:
                a = b - 1
        box += [a, b]
    return [box[0], box[2], box[4], box[1], box[3], box[5]]


# --------------------------------------------------------------------------- per family output

class Out:
    def __init__(self):
        self.lang = {}
        self.java = []
        self.ids = []
        self.recipes = {}
        self.mineable = {"axe": [], "pickaxe": [], "hoe": [], "shovel": []}
        self.flames = {}


def standard_family(fam, out):
    states = fam.states()
    ov_models = {}
    shapes = {}
    flames = {}
    # overlays (clock hands, pictures) are shared by every variant of the family
    if fam.overlay:
        kind = "hours" if fam.hours else "style"
        n = 12 if fam.hours else fam.styles
        ctx0 = Ctx(fam.variants[0])
        for i in range(n):
            path = f"block/{fam.id}/{'hands' if fam.hours else 'style'}_{i}"
            ov_models[i] = write_model(path, fam.overlay(ctx0, kind, i, {}), fam.render)
    for v in fam.variants:
        ctx = Ctx(v)
        models = {}
        for st in states:
            key = Family.state_key(st)
            if st.get("low"):
                base = dict(st, low=False)
                if st.get("mount", fam.mounts[0]) != "floor":
                    models[key] = models[Family.state_key(base)]
                    continue
                els = move(fam.build(ctx, base), 0, -LOW, 0)
                models[key] = write_model(f"block/{v.id}/{key}", els, fam.render)
                continue
            els = fam.build(ctx, st)
            models[key] = write_model(f"block/{v.id}/{key}", els, fam.render)
            if v is fam.variants[0]:
                shapes.setdefault(fam.shape_key(st), []).extend(els)
                if fam.light and fam.light[0] == "CANDLE" and st.get("lit"):
                    pts = []
                    for e in els:
                        f = next((f for f in e.faces.values() if f), None)
                        ref = f if isinstance(f, str) else (f[0] if f else "")
                        if ref.endswith("block/flame") and e.rot and e.rot[1] > 0:
                            pts.append(((e.frm[0] + e.to[0]) / 2, e.frm[1] + 0.6, (e.frm[2] + e.to[2]) / 2))
                    flames[st.get("count", 1)] = pts
        # blockstate
        write_blockstate(fam, v, states, models, ov_models)
        # item model: default state plus overlay 0 / ten o'clock
        st = fam.default_state()
        els = fam.build(ctx, st)
        if fam.overlay:
            els = els + fam.overlay(ctx, "hours" if fam.hours else "style", 10 if fam.hours else 0, st)
        els = centred_for_item(els)
        write_model(f"item/{v.id}", els, fam.render, item_display(fam, els))
        out.ids.append(v.id)
        out.lang[f"block.{MODID}.{v.id}"] = v.name
        write_loot(fam, v)
        if fam.recipe:
            out.recipes[v.id] = fam.recipe(v)
        if fam.mineable:
            out.mineable[fam.mineable].append(M + v.id)
    if fam.shape_override:
        shape_data = fam.shape_override
    else:
        shape_data = {k: [shape_box(els)] for k, els in shapes.items()}
    return shape_data, flames


def facing_list(fam):
    return list(FACING_Y.items()) if fam.facing else [(None, 0)]


def state_string(facing, st):
    parts = []
    if facing:
        parts.append(f"facing={facing}")
    for k, v in st.items():
        if isinstance(v, bool):
            v = "true" if v else "false"
        parts.append(f"{k}={v}")
    return ",".join(parts)


def write_blockstate(fam, v, states, models, ov_models):
    path = ASSETS / "blockstates" / f"{v.id}.json"
    if not ov_models:
        variants = {}
        for facing, y in facing_list(fam):
            for st in states:
                entry = {"model": models[Family.state_key(st)]}
                if y:
                    entry["y"] = y
                variants[state_string(facing, st) or ""] = entry
        write_json(path, {"variants": variants})
        return
    parts = []
    prop = "hour" if fam.hours else "style"
    for facing, y in facing_list(fam):
        for st in states:
            when = {"facing": facing} if facing else {}
            for k, val in st.items():
                when[k] = ("true" if val else "false") if isinstance(val, bool) else str(val)
            entry = {"model": models[Family.state_key(st)]}
            if y:
                entry["y"] = y
            parts.append({"when": when, "apply": entry} if when else {"apply": entry})
        for i, ref in ov_models.items():
            when = {"facing": facing} if facing else {}
            when[prop] = str(i)
            entry = {"model": ref}
            if y:
                entry["y"] = y
            parts.append({"when": when, "apply": entry})
    write_json(path, {"multipart": parts})


def write_loot(fam, v, extra_pools=None):
    entry = {"type": "minecraft:item", "name": M + v.id}
    if fam.count > 1:
        entry["functions"] = [{"function": "minecraft:set_count", "count": n, "add": False,
                               "conditions": [{"condition": "minecraft:block_state_property", "block": M + v.id,
                                               "properties": {"count": str(n)}}]}
                              for n in range(2, fam.count + 1)]
        entry["functions"].append({"function": "minecraft:explosion_decay"})
    pools = [{"rolls": 1, "bonus_rolls": 0, "entries": [entry],
              "conditions": [{"condition": "minecraft:survives_explosion"}]}]
    if extra_pools:
        pools += extra_pools
    write_json(DATA / "loot_tables" / "blocks" / f"{v.id}.json",
               {"type": "minecraft:block", "pools": pools, "random_sequence": f"{MODID}:blocks/{v.id}"})


# --------------------------------------------------------------------------- tables (multipart with connections)

def table_family(fam, out):
    from .interior import cloth_drape, cloth_top, table_leg, table_top
    for v in fam.variants:
        ctx = Ctx(v)
        top = write_model(f"block/{v.id}/top", table_top(ctx), "cutout")
        leg = write_model(f"block/{v.id}/leg", table_leg(ctx), "cutout")
        parts = [{"apply": {"model": top}}]
        # leg corners: north-west (0), north-east (90), south-east (180), south-west (270)
        for (a, b), y in ((("north", "west"), 0), (("north", "east"), 90), (("south", "east"), 180),
                          (("south", "west"), 270)):
            entry = {"model": leg}
            if y:
                entry["y"] = y
            parts.append({"when": {a: "false", b: "false"}, "apply": entry})
        for s in SETS:
            parts.append({"when": {"cloth": s}, "apply": {"model": M + f"block/tablecloth/{s}_top"}})
            for side, y in FACING_Y.items():
                entry = {"model": M + f"block/tablecloth/{s}_drape"}
                if y:
                    entry["y"] = y
                parts.append({"when": {"cloth": s, side: "false"}, "apply": entry})
        write_json(ASSETS / "blockstates" / f"{v.id}.json", {"multipart": parts})
        els = centred_for_item(table_top(ctx) + table_leg(ctx) + move(table_leg(ctx), 12, 0, 0)
                               + move(table_leg(ctx), 0, 0, 12) + move(table_leg(ctx), 12, 0, 12))
        write_model(f"item/{v.id}", els, "cutout", item_display(fam, els))
        out.ids.append(v.id)
        out.lang[f"block.{MODID}.{v.id}"] = v.name
        cloth_pools = [{"rolls": 1, "bonus_rolls": 0,
                        "entries": [{"type": "minecraft:item", "name": M + f"{s}_tablecloth"}],
                        "conditions": [{"condition": "minecraft:block_state_property", "block": M + v.id,
                                        "properties": {"cloth": s}}]} for s in SETS]
        write_loot(fam, v, cloth_pools)
        out.recipes[v.id] = fam.recipe(v)
        out.mineable["axe"].append(M + v.id)
    for s in SETS:
        ctx = Ctx(fam_variant_for_set(s))
        write_model(f"block/tablecloth/{s}_top", cloth_top(ctx.print), "cutout")
        write_model(f"block/tablecloth/{s}_drape", cloth_drape(ctx.print, ctx.lace), "cutout")
    return {"base": [[0, 14, 0, 16, 16, 16], [1, 0, 1, 3, 14, 3], [13, 0, 1, 15, 14, 3], [1, 0, 13, 3, 14, 15],
                     [13, 0, 13, 15, 14, 15]]}, {}


def fam_variant_for_set(s):
    from .fam import Variant
    return Variant(f"tablecloth_{s}", "", s)


# --------------------------------------------------------------------------- blankets (carpet with borders)

def blanket_family(fam, out):
    from .garden import blanket_border, blanket_sheet
    for v in fam.variants:
        ctx = Ctx(v)
        sheet = write_model(f"block/{v.id}/sheet", blanket_sheet(ctx), "cutout")
        border = write_model(f"block/{v.id}/border", blanket_border(ctx), "cutout")
        parts = [{"apply": {"model": sheet}}]
        for side, y in FACING_Y.items():
            entry = {"model": border}
            if y:
                entry["y"] = y
            parts.append({"when": {side: "false"}, "apply": entry})
        write_json(ASSETS / "blockstates" / f"{v.id}.json", {"multipart": parts})
        els = []
        for k in range(4):
            from .core import rot_y90
            els += rot_y90(blanket_border(ctx), k)
        els = blanket_sheet(ctx) + els
        write_model(f"item/{v.id}", els, "cutout", item_display(fam, els))
        out.ids.append(v.id)
        out.lang[f"block.{MODID}.{v.id}"] = v.name
        write_loot(fam, v)
        out.recipes[v.id] = fam.recipe(v)
    return {"base": [[0, 0, 0, 16, 1, 16]]}, {}


# --------------------------------------------------------------------------- items

def item_generated(name, texture):
    write_json(ASSETS / "models" / "item" / f"{name}.json",
               {"parent": "minecraft:item/generated", "textures": {"layer0": texture}})


def paint_tablecloth_icon(s):
    p = PAL[s]
    fabric = paint.mat_print(s)
    lace = paint.t_lace()
    img = Img(16, 16)
    # a folded square of fabric, seen at an angle, with a lace edge
    for y in range(3, 14):
        inset = max(0, 6 - y) if y < 6 else 0
        for x in range(1 + inset, 15 - inset):
            c = fabric.get(x, y)
            if y >= 11:
                c = darken(c, 0.12)
            img.set(x, y, c)
    for x in range(1, 15):
        img.set(x, 13, lace.get(x, 0))
        if x % 3 != 1:
            img.set(x, 14, lace.get(x, 1))
    for y in range(6, 14):
        img.set(1, y, darken(fabric.get(1, y), 0.2))
        img.set(14, y, darken(fabric.get(14, y), 0.2))
    for x in range(6, 10):
        img.set(x, 2, C(p["ribbon"]))
    img.set(7, 1, C(p["ribbon"]))
    img.set(8, 1, C(p["ribbon"]))
    return img


def paint_book_icon():
    img = Img(16, 16)
    cover = C("#C7E6C1")
    edge = C("#8DBF85")
    for y in range(1, 15):
        for x in range(2, 14):
            img.set(x, y, cover)
        img.set(2, y, edge)
        img.set(13, y, edge)
    for x in range(2, 14):
        img.set(x, 1, edge)
        img.set(x, 14, edge)
    for y in range(2, 14):
        img.set(3, y, C("#FBF5E6"))
    for x in range(3, 13):
        img.set(x, 14, C("#FBF5E6"))
    for y in range(2, 14):
        img.set(4, y, C("#E5484D"))
    # a little flower on the cover
    pal = {"p": "#F49DBA", "y": "#F8DD78", "g": "#5E9443"}
    paint.stamp(img, 7, 4, [".p.", "pyp", ".p.", ".g.", "gg."], pal)
    for x in range(6, 12):
        img.set(x, 11, C("#E8C46A"))
    return img


paint.ITEM_TEXTURES["collection_book"] = paint_book_icon
for _s in SETS:
    paint.ITEM_TEXTURES[f"{_s}_tablecloth"] = (lambda s: (lambda: paint_tablecloth_icon(s)))(_s)


# --------------------------------------------------------------------------- recipes and tags

SET_TAG_ITEMS = {
    "strawberry": ["minecraft:red_dye", "minecraft:sweet_berries"],
    "lavender": ["minecraft:purple_dye", "minecraft:lilac"],
    "pink": ["minecraft:pink_dye", "minecraft:pink_petals", "minecraft:peony"],
    "fairy": ["minecraft:lime_dye", "minecraft:glow_berries"],
    "vintage": ["minecraft:brown_dye", "minecraft:cyan_dye"],
}


def ingredient(token, set_id):
    if token == "S":
        return {"tag": f"{MODID}:sets/{set_id}"}
    if token.startswith("#"):
        return {"tag": token[1:]}
    return {"item": token}


def write_recipe(rid, r, set_id, group):
    res = {"item": M + rid}
    if r.get("count", 1) > 1:
        res["count"] = r["count"]
    if r["type"] == "shapeless":
        body = {"type": "minecraft:crafting_shapeless", "category": "misc", "group": group,
                "ingredients": [ingredient(t, set_id) for t in r["items"]], "result": res}
    else:
        body = {"type": "minecraft:crafting_shaped", "category": "misc", "group": group, "pattern": r["pattern"],
                "key": {k: ingredient(t, set_id) for k, t in r["key"].items()}, "result": res}
    write_json(DATA / "recipes" / f"{rid}.json", body)


# approximate tag contents, only used to look for recipes that could shadow each other
TAG_GUESS = {
    "#minecraft:small_flowers": {"dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip",
                                 "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy", "cornflower",
                                 "lily_of_the_valley", "wither_rose", "torchflower"},
    "#minecraft:planks": {"oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks",
                          "dark_oak_planks", "mangrove_planks", "cherry_planks", "bamboo_planks", "crimson_planks",
                          "warped_planks"},
    "#minecraft:wooden_slabs": {"oak_slab", "spruce_slab", "birch_slab", "dark_oak_slab", "cherry_slab"},
    "#minecraft:leaves": {"oak_leaves", "spruce_leaves", "birch_leaves", "cherry_leaves", "azalea_leaves"},
    "#minecraft:wool": {"white_wool", "pink_wool"},
    "#minecraft:buttons": {"oak_button", "stone_button"},
    "#minecraft:saplings": {"oak_sapling", "spruce_sapling", "cherry_sapling"},
    "#minecraft:wooden_doors": {"oak_door", "spruce_door"},
    "#minecraft:signs": {"oak_sign", "spruce_sign"},
    "#minecraft:wool_carpets": {"white_carpet", "pink_carpet"},
}


def item_set(token, set_id):
    if token == "S":
        return {i.split(":")[1] for i in SET_TAG_ITEMS[set_id]}
    if token.startswith("#"):
        return TAG_GUESS.get(token, {token})
    return {token.split(":")[1]}


def recipe_conflicts(recipes):
    """Shapeless recipes with matching ingredient lists would shadow each other."""
    sigs = []
    for rid, (r, set_id) in recipes.items():
        if r["type"] != "shapeless":
            continue
        sigs.append((rid, [item_set(t, set_id) for t in r["items"]]))
    problems = []

    def can_match(a, b):
        if len(a) != len(b):
            return False
        used = [False] * len(b)

        def go(i):
            if i == len(a):
                return True
            for j in range(len(b)):
                if not used[j] and a[i] & b[j]:
                    used[j] = True
                    if go(i + 1):
                        return True
                    used[j] = False
            return False
        return go(0)

    for i in range(len(sigs)):
        for j in range(i + 1, len(sigs)):
            if can_match(sigs[i][1], sigs[j][1]):
                problems.append(f"recipes {sigs[i][0]} and {sigs[j][0]} overlap")
    return problems


# --------------------------------------------------------------------------- java


def jnum(v):
    v = round(float(v), 3)
    return str(int(v)) if v == int(v) else str(v)


def java_family(fam, shapes, flames):
    cat = fam.category.upper()
    lines = [f'            builder("{fam.id}", Category.{cat}, "{fam.group}", Kind.{fam.kind}, DecorMaterial.{fam.material})']
    if not fam.facing:
        lines.append("                    .noFacing()")
    if fam.mounts != ("floor",):
        lines.append("                    .mounts(" + ", ".join("Mount." + m.upper() for m in fam.mounts) + ")")
    if fam.count > 1:
        lines.append(f"                    .count({fam.count})")
    if fam.light:
        lines.append(f"                    .light(LightMode.{fam.light[0]}, {fam.light[1]})")
    if fam.open:
        lines.append("                    .openable()")
    if fam.rod:
        lines.append("                    .rod()")
    if fam.seasons:
        lines.append("                    .seasonal()")
    if fam.styles:
        lines.append(f"                    .styles({fam.styles})")
    if fam.hours:
        lines.append("                    .hours()")
    if fam.walk:
        lines.append("                    .walkThrough()")
    if fam.sits_low():
        lines.append("                    .sitsLow()")
    if fam.tint:
        lines.append("                    .waterTint()")
    if fam.interaction != "NONE":
        lines.append(f"                    .interaction(Interaction.{fam.interaction})")
    if fam.ambient != "NONE":
        lines.append(f"                    .ambient(Ambient.{fam.ambient})")
    for key, boxes in sorted(shapes.items()):
        flat = ", ".join(jnum(x) for b in boxes for x in b)
        lines.append(f'                    .shape("{key}", {flat})')
    for count, pts in sorted(flames.items()):
        flat = ", ".join(jnum(x) for p in pts for x in p)
        lines.append(f"                    .flames({count}, {flat})")
    for v in fam.variants:
        if v.set:
            lines.append(f'                    .variant("{v.id}", DecorSet.{SET_JAVA[v.set]})')
        else:
            lines.append(f'                    .variant("{v.id}")')
    lines.append("                    .build()")
    return "\n".join(lines)


def write_java(blocks):
    body = ",\n".join(blocks)
    src = f"""// GENERATED by tools/generate_assets.py from tools/clgen - do not edit by hand.
package com.sugarplumstudios.cottagecore.catalog;

import java.util.List;

import static com.sugarplumstudios.cottagecore.catalog.DecorFamily.builder;

/** Every decoration family in the mod, with its variants, hit boxes and behaviour. */
public final class DecorCatalog {{
    public static final List<DecorFamily> FAMILIES = List.of(
{body}
    );

    private DecorCatalog() {{
    }}
}}
"""
    JAVA_OUT.parent.mkdir(parents=True, exist_ok=True)
    JAVA_OUT.write_text(src, encoding="utf-8")


# --------------------------------------------------------------------------- lang

GROUP_NAMES = {
    "books": "Books", "candles": "Candles", "vases": "Vases & Flowers", "tables": "Tables & Tablecloths",
    "curtains": "Curtains", "wall_decor": "Wall Decor", "clocks": "Clocks", "boxes": "Trinket & Jewelry Boxes",
    "mirrors": "Mirrors", "frames": "Picture Frames", "baskets": "Baskets", "kitchen": "Kitchen & Pantry",
    "birdhouses": "Birdhouses", "chimes": "Wind Chimes", "signs": "Garden Signs", "planters": "Planters & Flower Baskets",
    "tools": "Garden Tools", "picnic": "Picnic Blankets", "stones": "Decorative Stones", "fountains": "Fountains",
    "lanterns": "Garden Lanterns", "fairy": "Fairy Garden", "cherry_blossom": "Cherry Blossom",
    "spring": "Spring Flowers", "summer": "Summer Picnic", "autumn": "Autumn Leaves", "winter": "Winter Snow",
    "seasons": "Through the Seasons",
}


def base_lang(out):
    L = out.lang
    L["itemGroup.cottagecoreliving.interior"] = "Cottagecore Living: Interior"
    L["itemGroup.cottagecoreliving.garden"] = "Cottagecore Living: Garden"
    L["itemGroup.cottagecoreliving.seasonal"] = "Cottagecore Living: Seasonal"
    L["item.cottagecoreliving.collection_book"] = "Decoration Collection Book"
    L["item.cottagecoreliving.collection_book.desc"] = "Browse every decoration and track your collection."
    for s in SETS:
        L[f"item.cottagecoreliving.{s}_tablecloth"] = f"{paint.SET_ADJ[s]} Tablecloth"
        L[f"set.cottagecoreliving.{s}"] = SET_NAMES[s]
    L["item.cottagecoreliving.tablecloth.desc"] = "Right-click a Cottage Table to dress it. Shears take it off again."
    L["category.cottagecoreliving.interior"] = "Interior"
    L["category.cottagecoreliving.garden"] = "Garden"
    L["category.cottagecoreliving.seasonal"] = "Seasonal"
    for g, n in GROUP_NAMES.items():
        L[f"group.cottagecoreliving.{g}"] = n
    for f in FAMILIES:
        L[f"decor.cottagecoreliving.{f.id}.desc"] = f.desc
    L.update(EXTRA_LANG)


EXTRA_LANG = {
    "season.cottagecoreliving.spring": "Spring",
    "season.cottagecoreliving.summer": "Summer",
    "season.cottagecoreliving.autumn": "Autumn",
    "season.cottagecoreliving.winter": "Winter",
    "tooltip.cottagecoreliving.set": "Set: %s",
    "tooltip.cottagecoreliving.collected": "In your collection",
    "tooltip.cottagecoreliving.wall": "Hangs on a wall",
    "tooltip.cottagecoreliving.ceiling": "Hangs from a ceiling",
    "tooltip.cottagecoreliving.floor_or_ceiling": "Stands on a surface or hangs from a ceiling",
    "tooltip.cottagecoreliving.wall_or_floor": "Stands on a surface or hangs on a wall",
    "tooltip.cottagecoreliving.ceiling_or_wall": "Hangs from a ceiling or a wall bracket",
    "tooltip.cottagecoreliving.rotate": "Sneak + right-click with an empty hand to turn it",
    "tooltip.cottagecoreliving.stack": "Place more on top to add to it (up to %s)",
    "tooltip.cottagecoreliving.interaction.toggle": "Right-click to open or close",
    "tooltip.cottagecoreliving.interaction.light_switch": "Right-click to switch on or off",
    "tooltip.cottagecoreliving.interaction.read_book": "Right-click to read a line",
    "tooltip.cottagecoreliving.interaction.compliment": "Right-click to check your reflection",
    "tooltip.cottagecoreliving.interaction.chirp": "Right-click to hear who lives there",
    "tooltip.cottagecoreliving.interaction.chime": "Right-click to ring it; it rings by itself in the breeze",
    "tooltip.cottagecoreliving.interaction.water_plants": "Right-click to water the crops in front of it",
    "tooltip.cottagecoreliving.interaction.fill_bottle": "Right-click with a glass bottle to fill it",
    "tooltip.cottagecoreliving.interaction.shake": "Right-click to shake it",
    "tooltip.cottagecoreliving.interaction.steam": "Right-click for a warm, cosy moment",
    "tooltip.cottagecoreliving.interaction.knock": "Right-click to knock",
    "tooltip.cottagecoreliving.interaction.cycle_style": "Right-click to change the picture",
    "tooltip.cottagecoreliving.interaction.cycle_season": "Right-click to choose a season; follows the world's season by default",
    "tooltip.cottagecoreliving.interaction.tell_time": "Shows the time of day; right-click to read it",
    "tooltip.cottagecoreliving.interaction.open_storage": "Right-click to open (9 slots)",
    "tooltip.cottagecoreliving.light.candle": "Light with flint and steel; empty hand to snuff",
    "tooltip.cottagecoreliving.light.switch": "Right-click to light or put out",
    "tooltip.cottagecoreliving.light.always": "Glows softly",
    "tooltip.cottagecoreliving.cloth": "Dress with a tablecloth; shears remove it",
    "message.cottagecoreliving.compliment.0": "You look lovely today!",
    "message.cottagecoreliving.compliment.1": "What a sweet smile.",
    "message.cottagecoreliving.compliment.2": "That outfit is perfect on you.",
    "message.cottagecoreliving.compliment.3": "You're the coziest person in the cottage.",
    "message.cottagecoreliving.compliment.4": "Radiant as a summer morning!",
    "message.cottagecoreliving.compliment.5": "Your garden must be so proud of you.",
    "message.cottagecoreliving.quote.0": "\"Home is where the kettle is always on.\"",
    "message.cottagecoreliving.quote.1": "\"She planted roses, and the whole lane smelled of June.\"",
    "message.cottagecoreliving.quote.2": "\"There is always time for one more chapter.\"",
    "message.cottagecoreliving.quote.3": "\"The bees knew the way to every flower in the valley.\"",
    "message.cottagecoreliving.quote.4": "\"Rain on the roof, a quilt, and nowhere to be.\"",
    "message.cottagecoreliving.quote.5": "\"Strawberries taste best when you've grown them yourself.\"",
    "message.cottagecoreliving.quote.6": "\"In the hedgerow, the little folk were having a party.\"",
    "message.cottagecoreliving.quote.7": "\"Every jar of jam holds a little bit of summer.\"",
    "message.cottagecoreliving.time": "It's %s:%s %s",
    "message.cottagecoreliving.am": "in the morning",
    "message.cottagecoreliving.pm": "in the afternoon",
    "message.cottagecoreliving.evening": "in the evening",
    "message.cottagecoreliving.night": "at night",
    "message.cottagecoreliving.chirp": "A little bird peeks out and chirps hello.",
    "message.cottagecoreliving.watered": "You give the plants a drink.",
    "message.cottagecoreliving.steam": "Mmm, warm and cosy.",
    "message.cottagecoreliving.knock": "Knock knock... a fairy peeks out!",
    "message.cottagecoreliving.season_follow": "Following the world's season (%s)",
    "message.cottagecoreliving.season_pinned": "Showing %s",
    "message.cottagecoreliving.collected": "New decoration collected: %s (%s/%s)",
    "message.cottagecoreliving.set_complete": "You've collected the whole %s set!",
    "message.cottagecoreliving.delivered": "%s delivered",
    "message.cottagecoreliving.creative_only": "Delivery from the book only works in creative mode",
    "gui.cottagecoreliving.book.title": "Decoration Collection Book",
    "gui.cottagecoreliving.book.search": "Search...",
    "gui.cottagecoreliving.book.all": "All",
    "gui.cottagecoreliving.book.sets": "Sets",
    "gui.cottagecoreliving.book.collected": "Collected %s / %s",
    "gui.cottagecoreliving.book.not_collected": "Not collected yet",
    "gui.cottagecoreliving.book.collected_one": "Collected",
    "gui.cottagecoreliving.book.recipe": "Recipe",
    "gui.cottagecoreliving.book.no_recipe": "No crafting recipe",
    "gui.cottagecoreliving.book.take": "Take",
    "gui.cottagecoreliving.book.take_stack": "Stack",
    "gui.cottagecoreliving.book.take_set": "Take whole set",
    "gui.cottagecoreliving.book.season": "Season: %s",
    "gui.cottagecoreliving.book.season_mode.calendar": "follows the calendar",
    "gui.cottagecoreliving.book.season_mode.cycle": "turns every few days",
    "gui.cottagecoreliving.book.season_mode.fixed": "fixed by the server",
    "gui.cottagecoreliving.book.variants": "%s variants",
    "gui.cottagecoreliving.book.set_progress": "%s / %s collected",
    "gui.cottagecoreliving.book.empty": "Nothing matches your search.",
}


# --------------------------------------------------------------------------- entry point


def generate():
    from . import interior, garden, seasonal  # noqa: F401  (registers the families)
    out = Out()
    java = []
    recipe_sets = {}
    for fam in FAMILIES:
        if fam.kind == "TABLE":
            shapes, flames = table_family(fam, out)
        elif fam.kind == "BLANKET":
            shapes, flames = blanket_family(fam, out)
        else:
            shapes, flames = standard_family(fam, out)
        java.append(java_family(fam, shapes, flames))
        for v in fam.variants:
            if v.id in out.recipes:
                recipe_sets[v.id] = (out.recipes[v.id], v.set)
                write_recipe(v.id, out.recipes[v.id], v.set, fam.id)
    # tablecloths
    for s in SETS:
        iid = f"{s}_tablecloth"
        item_generated(iid, paint.item_tex(iid))
        r = {"type": "shapeless", "items": ["minecraft:white_carpet", "minecraft:white_carpet", "S"], "count": 1}
        recipe_sets[iid] = (r, s)
        write_recipe(iid, r, s, "tablecloth")
    item_generated("collection_book", paint.item_tex("collection_book"))
    r = {"type": "shapeless", "items": ["minecraft:book", "#minecraft:small_flowers"], "count": 1}
    recipe_sets["collection_book"] = (r, None)
    write_recipe("collection_book", r, None, "collection_book")
    # tags
    for s in SETS:
        write_json(DATA / "tags" / "items" / "sets" / f"{s}.json", {"replace": False, "values": SET_TAG_ITEMS[s]})
    write_json(DATA / "tags" / "items" / "decorations.json",
               {"replace": False, "values": [M + i for i in out.ids]})
    write_json(DATA / "tags" / "items" / "tablecloths.json",
               {"replace": False, "values": [M + f"{s}_tablecloth" for s in SETS]})
    for tool, blocks in out.mineable.items():
        write_json(MC_DATA / "tags" / "blocks" / "mineable" / f"{tool}.json", {"replace": False, "values": blocks})
    base_lang(out)
    write_json(ASSETS / "lang" / "en_us.json", dict(sorted(out.lang.items())))
    write_java(java)
    problems = recipe_conflicts(recipe_sets)
    return out.ids, problems
