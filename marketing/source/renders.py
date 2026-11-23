#!/usr/bin/env python3
"""Renders the decorations the artwork shows from the mod's own generated models and textures,
with transparent backgrounds, into marketing/source/renders/. Uses tools/preview_models.py,
so it needs no Minecraft client. Run from the repository root after tools/generate_assets.py:

    CL_VANILLA=/path/to/assets/minecraft python marketing/source/renders.py

CL_VANILLA is an extracted 1.20.1 client jar's assets/minecraft folder; the tables, stepping
stones, fountains and a few others borrow vanilla planks and stone and are skipped without it.
"""
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
from clgen.core import MODID  # noqa: E402
from preview_models import render  # noqa: E402

OUT = Path(__file__).resolve().parent / "renders"
SCALE = 10
# Block-state models the cards show besides the item models: each season of the seasonal
# pieces, lit lanterns, an open picnic basket, tied-back curtains and an open fairy door.
STATES = [f"{piece}/{season}" for piece in ("seasonal_tree", "seasonal_wreath", "seasonal_centerpiece",
                                            "seasonal_garland", "seasonal_doormat")
          for season in ("spring", "summer", "autumn", "winter")]
STATES += [f"{s}_garden_lantern/floor_lit" for s in ("strawberry", "lavender", "pink", "fairy", "vintage")]
STATES += ["picnic_basket/open", "strawberry_curtains/open_rod", "fairy_fairy_door/open"]


def main():
    OUT.mkdir(exist_ok=True)
    ids = (ROOT / "tools" / "generated_ids.txt").read_text(encoding="utf-8").split()
    jobs = [(f"{MODID}:item/{i}", i) for i in ids]
    jobs += [(f"{MODID}:block/{s}", s.replace("/", "@")) for s in STATES]
    skipped = []
    for ref, name in jobs:
        try:
            render(ref, SCALE).save(OUT / f"{name}.png")
        except FileNotFoundError:
            skipped.append(name)
    print(f"rendered {len(jobs) - len(skipped)} of {len(jobs)} into {OUT}")
    if skipped:
        print("skipped (set CL_VANILLA):", " ".join(skipped))


if __name__ == "__main__":
    main()
