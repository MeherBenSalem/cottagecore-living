#!/usr/bin/env python3
"""Builds the mod-list logo from real decoration renders on a cream, scallop-trimmed card,
and contact sheets of every decoration for the docs folder.

    python tools/make_logo.py            (run after generate_assets.py)
    python tools/make_logo.py --sheets   (also writes docs/catalog_<collection>.png)

Set CL_VANILLA to an extracted client jar's assets/minecraft folder so models that
borrow vanilla textures (planks, stone) render too.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from clgen.core import MODID, ROOT, Img, mix  # noqa: E402
from preview_models import render, sheet  # noqa: E402

W, H = 400, 200
M = MODID + ":"
PIECES = [
    (M + "item/strawberry_flower_vase", 5.0, (16, 18)),
    (M + "item/pink_birdhouse", 5.0, (96, 14)),
    (M + "item/lavender_teapot", 6.5, (176, 92)),
    (M + "item/vintage_wall_clock", 5.0, (250, 16)),
    (M + "block/fairy_garden_lantern/floor_lit", 5.5, (330, 40)),
    (M + "block/seasonal_tree/spring", 4.6, (110, 96)),
    (M + "block/picnic_basket/open", 4.4, (262, 104)),
    (M + "item/fairy_garden_gnome", 4.4, (30, 112)),
]


def logo():
    img = Img(W, H)
    for y in range(H):
        for x in range(W):
            img.set(x, y, mix("#FFFBF0", "#E3EDD3", y / H))
    for x in range(0, W, 10):
        col = (229, 72, 77, 255) if (x // 10) % 3 == 0 else (185, 207, 160, 255)
        for y in (0, H - 4):
            img.rect(x + 1, y, x + 9, y + 4, col)
    for y in range(0, H, 10):
        for x in (0, W - 4):
            img.rect(x, y + 1, x + 4, y + 9, (185, 207, 160, 255))
    for ref, scale, (ox, oy) in PIECES:
        tile = render(ref, scale)
        for y in range(tile.h):
            for x in range(tile.w):
                c = tile.get(x, y)
                if c[3]:
                    img.set(ox + x, oy + y, c)
    out = ROOT / f"{MODID}_logo.png"
    img.save(out)
    print("wrote", out)


def sheets():
    ids = (Path(__file__).resolve().parent / "generated_ids.txt").read_text(encoding="utf-8").split()
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    from clgen import data  # noqa: F401  (registers families)
    from clgen import interior, garden, seasonal  # noqa: F401
    from clgen.fam import FAMILIES
    docs = Path(__file__).resolve().parents[1] / "docs"
    docs.mkdir(exist_ok=True)
    for category in ("interior", "garden", "seasonal"):
        refs = [M + "item/" + v.id for f in FAMILIES if f.category == category for v in f.variants if v.id in ids]
        sheet(docs / f"catalog_{category}.png", refs, 3.2, 15)
        print("wrote", docs / f"catalog_{category}.png", len(refs))


if __name__ == "__main__":
    logo()
    if "--sheets" in sys.argv:
        sheets()
