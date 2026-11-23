#!/usr/bin/env python3
"""Generates every Cottagecore Living asset: textures, block and item models,
blockstates, loot tables, recipes, tags, the language file and the generated
Java catalog (src/main/java/.../catalog/DecorCatalog.java).

    python tools/generate_assets.py

The output folders (assets/cottagecoreliving, data/cottagecoreliving and the
data/minecraft tags) are wiped and fully regenerated on every run, then every
blockstate, model and texture reference is checked.
"""
import gzip
import json
import shutil
import struct
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from clgen import data, paint  # noqa: E402
from clgen.core import ASSETS, DATA, MC_DATA, MODID  # noqa: E402


def wipe():
    for root in (ASSETS, DATA, MC_DATA):
        if not root.exists():
            continue
        for child in root.iterdir():
            shutil.rmtree(child) if child.is_dir() else child.unlink()


def empty_structure(size=(8, 6, 8)):
    """A blank structure template (NBT) used by the GameTests."""
    def name(text):
        raw = text.encode("utf-8")
        return struct.pack(">H", len(raw)) + raw

    body = b"\x03" + name("DataVersion") + struct.pack(">i", 3465)
    body += b"\x09" + name("size") + b"\x03" + struct.pack(">i", 3) + b"".join(struct.pack(">i", v) for v in size)
    for key in ("palette", "blocks", "entities"):
        body += b"\x09" + name(key) + b"\x00" + struct.pack(">i", 0)
    nbt = b"\x0a" + name("") + body + b"\x00"
    out = DATA / "structures" / "empty.nbt"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_bytes(gzip.compress(nbt, mtime=0))


def resolve(ref):
    return ref.split(":") if ":" in ref else ("minecraft", ref)


def validate(ids):
    problems = []
    models = {}
    vanilla = set()

    def load_model(ref):
        if ref in models:
            return models[ref]
        ns, path = resolve(ref)
        if ns != MODID:
            models[ref] = None
            return None
        f = ASSETS / "models" / (path + ".json")
        if not f.exists():
            problems.append(f"missing model {ref}")
            models[ref] = None
            return None
        models[ref] = json.loads(f.read_text(encoding="utf-8"))
        return models[ref]

    def check_model(ref):
        chain = []
        cur = ref
        while cur:
            m = load_model(cur)
            if m is None:
                break
            chain.append(m)
            cur = m.get("parent")
        if not chain:
            return
        tex = {}
        for m in reversed(chain):
            tex.update(m.get("textures", {}))
        elements = next((m["elements"] for m in chain if "elements" in m), [])
        for el in elements:
            for face in el["faces"].values():
                var = face["texture"].lstrip("#")
                if var not in tex:
                    problems.append(f"{ref}: unresolved texture #{var}")
        for v in tex.values():
            ns, path = resolve(v)
            if ns == MODID and not (ASSETS / "textures" / (path + ".png")).exists():
                problems.append(f"{ref}: missing texture {v}")
            elif ns == "minecraft":
                vanilla.add(path)

    for bid in ids:
        bs = ASSETS / "blockstates" / f"{bid}.json"
        if not bs.exists():
            problems.append(f"missing blockstate {bid}")
            continue
        state = json.loads(bs.read_text(encoding="utf-8"))
        refs = [v["model"] for v in state.get("variants", {}).values()]
        refs += [p["apply"]["model"] for p in state.get("multipart", [])]
        for ref in set(refs):
            check_model(ref)
        check_model(f"{MODID}:item/{bid}")
        if not (DATA / "loot_tables" / "blocks" / f"{bid}.json").exists():
            problems.append(f"missing loot table {bid}")
        if not (DATA / "recipes" / f"{bid}.json").exists():
            problems.append(f"missing recipe {bid}")
    for path in sorted(vanilla):
        if path not in VANILLA_TEXTURES:
            problems.append(f"unknown vanilla texture minecraft:{path}")
    return problems


# vanilla textures the models may borrow (checked so a typo can't slip through)
VANILLA_TEXTURES = {
    "block/oak_planks", "block/spruce_planks", "block/birch_planks", "block/dark_oak_planks", "block/cherry_planks",
    "block/water_still", "block/stone", "block/smooth_stone", "block/mossy_cobblestone", "block/sandstone_top",
    "block/polished_deepslate", "block/andesite", "block/diorite", "block/calcite", "block/cobblestone",
    "block/stone_bricks", "block/terracotta",
}


def main():
    wipe()
    ids, problems = data.generate()
    count = paint.paint_all()
    empty_structure()
    problems += validate(ids)
    jsons = sum(1 for _ in ASSETS.parent.parent.rglob("*.json"))
    print(f"{len(ids)} blocks, {count} textures, {jsons} json files")
    (Path(__file__).resolve().parent / "generated_ids.txt").write_text("\n".join(ids) + "\n", encoding="utf-8")
    if problems:
        print(f"{len(problems)} problems:")
        for p in problems[:80]:
            print("  " + p)
        sys.exit(1)
    print("all references resolved")


if __name__ == "__main__":
    main()
