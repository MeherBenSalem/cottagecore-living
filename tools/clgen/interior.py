"""Interior decorations: books, candles, vases, tables, curtains, wall decor,
clocks, trinket and jewelry boxes, mirrors, picture frames, baskets and kitchen bits."""
from .core import B, box, rbox, move, mirror_x
from .fam import Family, Variant, family, set_variants
from .paint import SETS
from .shapes import (FLAT, basket, bow, bumps, cross, cross4, disc_xy, disk, flat_bow, plane_y, plane_z, pot,
                     ring_xy, ring_xz, square_pot)

S = "S"  # stands for the set's ingredient tag in recipes


def shapeless(*items, count=1):
    return dict(type="shapeless", items=list(items), count=count)


def shaped(pattern, key, count=1):
    return dict(type="shaped", pattern=pattern, key=key, count=count)


def frame_tex(c):
    return c.brass if c.v.set == "vintage" else c.paint


FLAME_UV = (6, 3, 12, 12)


def flame(cx, cz, y, size=1.0, tex=None):
    return cross(cx, cz, 1.5 * size, y, y + 2.25 * size, tex, uv=FLAME_UV)


# =========================================================================== books

def book_flat(x0, y, z0, x1, h, z1, cover, pages, spine="west"):
    out = B(x0, y, z0, x1, y + 0.3, z1, cover)
    out += B(x0 + 0.25, y + 0.3, z0 + 0.25, x1 - 0.25, y + h - 0.3, z1 - 0.25, pages)
    if spine == "west":
        out += B(x0, y + 0.3, z0, x0 + 0.3, y + h - 0.3, z1, cover)
    else:
        out += B(x1 - 0.3, y + 0.3, z0, x1, y + h - 0.3, z1, cover)
    out += B(x0, y + h - 0.3, z0, x1, y + h, z1, cover)
    return out


@family("stacked_books", "Stacked Books", "interior", "books", set_variants("stacked_books", "Stacked Books"),
        material="PAPER", count=3, item_state={"count": 2},
        recipe=lambda v: shapeless("minecraft:book", "minecraft:book", S),
        desc="A cosy pile of well-loved books. Place more on top to stack them higher.")
def stacked_books(c, st):
    books = [(2.5, 3, 13.5, 12, 2.0, c.cover_a), (3.5, 4, 12.5, 12.5, 1.75, c.cover_b),
             (2.25, 3.5, 12.25, 11.5, 2.25, c.cover_c), (4, 3, 13, 11, 1.5, c.cover_a),
             (3, 4, 12.5, 12, 2.0, c.cover_b), (3.75, 3.5, 11.75, 11.5, 1.75, c.cover_c)]
    out = []
    y = 0
    for i, (x0, z0, x1, z1, h, cov) in enumerate(books[:2 * st["count"]]):
        out += book_flat(x0, y, z0, x1, h, z1, cov, c.pages, "west" if i % 2 else "east")
        y += h
    return out


@family("open_book", "Open Book", "interior", "books", set_variants("open_book", "Open Book"),
        material="PAPER", interaction="READ_BOOK",
        recipe=lambda v: shapeless("minecraft:book", "minecraft:paper", S),
        desc="Left open at a favourite page. Right-click to read a line.")
def open_book(c, st):
    out = B(2, 0, 3.5, 14, 0.5, 12.5, c.cover_a)
    out += B(2.5, 0.5, 4, 7.75, 1.5, 12, c.pages, u=(c.page_text, (0, 0, 16, 16)))
    out += B(8.25, 0.5, 4, 13.5, 1.5, 12, c.pages, u=(c.page_text, (0, 0, 16, 16)))
    out += B(7.6, 0.5, 4, 8.4, 1.25, 12, c.pages)
    # ribbon bookmark spilling out of the bottom edge
    out += B(9.5, 1.5, 6, 10, 1.6, 12.6, c.ribbon, d=None)
    out += B(9.5, 0, 12.6, 10, 1.6, 12.8, c.ribbon)
    # a pressed flower on the left page
    out += B(4, 1.5, 6.5, 5, 1.6, 7.5, c.ribbon, d=None)
    out += B(4.25, 1.5, 7.5, 4.75, 1.55, 9.5, c.cover_b, d=None)
    return out


@family("book_row", "Book Row", "interior", "books", set_variants("book_row", "Book Row"),
        material="PAPER", recipe=lambda v: shapeless("minecraft:book", "minecraft:book", "minecraft:book", S, "minecraft:stick"),
        desc="A neat row of books held up by painted bookends.")
def book_row(c, st):
    out = []
    end = frame_tex(c)
    out += B(1.5, 0, 4.5, 4, 0.75, 11.5, end)
    out += B(1.5, 0.75, 4.5, 2.5, 8, 11.5, end)
    out += B(12, 0, 4.5, 14.5, 0.75, 11.5, end)
    out += B(13.5, 0.75, 4.5, 14.5, 8, 11.5, end)
    covers = [c.cover_a, c.cover_b, c.cover_c, c.cover_a, c.cover_c, c.cover_b]
    widths = [1.5, 1.25, 2.0, 1.25, 1.5, 1.75]
    heights = [7.0, 6.0, 7.5, 5.5, 6.5, 7.0]
    x = 2.6
    for i, (w, h) in enumerate(zip(widths, heights)):
        cov = covers[i]
        out += box((x, 0.75, 5), (x + w, 0.75 + h, 11), cov, up=c.pages)
        out += B(x, 0.75 + h - 1.6, 4.95, x + w, 0.75 + h - 1.1, 5, c.brass, s=None, e=None, w=None, u=None, d=None)
        x += w + 0.08
    return out


# =========================================================================== candles

CANDLES = {1: [(8, 8, 2, 6)], 2: [(6, 7, 2, 7), (11, 9.5, 1.5, 4.5)],
           3: [(5.5, 8.5, 2, 7.5), (10.5, 10, 1.5, 4.5), (9.5, 5, 1.5, 9.5)]}


def pillar_flames(count):
    return [(cx, h + 1.4, cz) for cx, cz, r, h in CANDLES[count]]


@family("pillar_candles", "Pillar Candles", "interior", "candles", set_variants("pillar_candles", "Pillar Candles"),
        material="CANDLE", count=3, light=("CANDLE", 4), mineable=None,
        recipe=lambda v: shapeless("minecraft:candle", "minecraft:honeycomb", S),
        desc="Chunky pillar candles. Light them with flint and steel; place more to make a cluster.")
def pillar_candles(c, st):
    out = []
    for cx, cz, r, h in CANDLES[st["count"]]:
        out += disk(cx, cz, r, 0, h, c.wax)
        out += B(cx - 0.25, h, cz - 0.25, cx + 0.25, h + 1, cz + 0.25, c.wick)
        if st["lit"]:
            out += flame(cx, cz, h + 0.4, 0.9, c.flame)
    return out


@family("candle_jar", "Candle Jar", "interior", "candles", set_variants("candle_jar", "Candle Jar"),
        material="GLASS", light=("CANDLE", 6), render="translucent", mineable=None,
        recipe=lambda v: shapeless("minecraft:candle", "minecraft:glass", S),
        desc="A scented candle in a glass jar. Light it with flint and steel.")
def candle_jar(c, st):
    out = []
    out += B(5, 0.25, 5, 11, 4, 11, c.wax)
    out += B(7.75, 4, 7.75, 8.25, 5, 8.25, c.wick)
    out += B(4.4, 2, 4.4, 11.6, 4.5, 11.6, c.print, u=None, d=None)
    out += B(4.5, 0, 4.5, 11.5, 7, 11.5, c.glass, u=None)
    if st["lit"]:
        out += flame(8, 8, 4.6, 0.9, c.flame)
    else:
        out += B(4.25, 7, 4.25, 11.75, 8, 11.75, "minecraft:block/oak_planks")
        out += B(7.25, 8, 7.25, 8.75, 8.5, 8.75, c.brass)
    return out


@family("candlestick", "Candlestick", "interior", "candles", set_variants("candlestick", "Candlestick"),
        material="METAL", light=("CANDLE", 7), mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:candle", "minecraft:gold_nugget", "minecraft:gold_nugget", S),
        desc="A tall taper in a brass chamberstick.")
def candlestick(c, st):
    out = disk(8, 8, 3.5, 0, 0.75, c.brass)
    out += disk(8, 8, 1, 0.75, 3, c.brass)
    out += disk(8, 8, 1.5, 3, 4.25, c.brass)
    out += B(11.25, 0.25, 7.25, 13.25, 0.75, 8.75, c.brass)
    out += B(12.75, 0.75, 7.25, 13.25, 2.25, 8.75, c.brass)
    out += B(11.75, 1.75, 7.25, 12.75, 2.25, 8.75, c.brass)
    out += B(7.25, 4.25, 7.25, 8.75, 12, 8.75, c.wax)
    out += B(7.85, 12, 7.85, 8.15, 12.75, 8.15, c.wick)
    if st["lit"]:
        out += flame(8, 8, 12.25, 0.9, c.flame)
    return out


@family("candle_sconce", "Candle Sconce", "interior", "candles", set_variants("candle_sconce", "Candle Sconce"),
        material="METAL", light=("CANDLE", 8), mounts=("wall",), mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:candle", "minecraft:iron_nugget", "minecraft:gold_nugget", S),
        desc="A wall sconce with a little drip dish. Hangs on any wall.")
def candle_sconce(c, st):
    plate = frame_tex(c)
    out = B(6, 3, 15, 10, 13, 16, plate)
    out += B(6.5, 13, 15, 9.5, 14, 16, plate)
    out += B(7, 14, 15, 9, 14.5, 16, plate)
    out += B(6.5, 2.25, 15, 9.5, 3, 16, plate)
    out += B(7.5, 5, 10, 8.5, 6, 15, c.brass)
    out += disk(8, 9.5, 2.5, 5.5, 6, c.brass)
    out += disk(8, 9.5, 1.5, 6, 7, c.brass)
    out += B(7.25, 7, 8.75, 8.75, 12, 10.25, c.wax)
    out += B(7.85, 12, 9.35, 8.15, 12.75, 9.65, c.wick)
    if st["lit"]:
        out += flame(8, 9.5, 12.25, 0.9, c.flame)
    return out


# =========================================================================== vases

@family("flower_vase", "Flower Vase", "interior", "vases", set_variants("flower_vase", "Flower Vase"),
        material="CERAMIC", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:flower_pot", "#minecraft:small_flowers", "#minecraft:small_flowers", S),
        desc="A painted vase with a fresh bouquet in the set's flowers.")
def flower_vase(c, st):
    out = disk(8, 8, 2.5, 0, 1, c.ceramic)
    out += disk(8, 8, 3.5, 1, 6, c.glaze)
    out += disk(8, 8, 2.5, 6, 7, c.ceramic)
    out += disk(8, 8, 1.75, 7, 8.5, c.ceramic)
    out += ring_xz(8, 8, 2.5, 0.75, 8.5, 9.5, c.ceramic)
    out += cross4(8, 8, 10, 7.5, 16, c.bouquet)
    out += plane_y(3.5, 3.5, 12.5, 12.5, 13.5, c.flowers, shade=False)
    return out


@family("bud_vase", "Bud Vase", "interior", "vases", set_variants("bud_vase", "Bud Vase"),
        material="CERAMIC", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:glass_bottle", "#minecraft:small_flowers", S),
        desc="A slim vase holding a single bloom.")
def bud_vase(c, st):
    out = disk(8, 8, 1.75, 0, 3.5, c.glaze)
    out += disk(8, 8, 0.75, 3.5, 5.5, c.ceramic)
    out += disk(8, 8, 1.1, 5.5, 6, c.ceramic)
    out += B(7.2, 4.5, 7.2, 8.8, 5, 8.8, c.ribbon)
    out += cross(8, 8, 3, 5, 14.6, c.bouquet, uv=(6, 0, 11, 16))
    return out


# =========================================================================== tables & tablecloths

WOODS = [("oak", "Oak"), ("spruce", "Spruce"), ("birch", "Birch"), ("dark_oak", "Dark Oak"), ("cherry", "Cherry")]


def table_top(c):
    return B(0, 14, 0, 16, 16, 16, c("minecraft:block/" + c.v["wood"] + "_planks"))


def table_leg(c):
    """North-west leg; the blockstate rotates it into the other corners."""
    wood = c("minecraft:block/" + c.v["wood"] + "_planks")
    out = B(1, 0, 1, 3, 2, 3, wood)
    out += B(1.4, 2, 1.4, 2.6, 12, 2.6, wood)
    out += B(1, 12, 1, 3, 14, 3, wood)
    return out


def cloth_top(tex):
    return B(0, 16, 0, 16, 16.1, 16, tex, n=None, s=None, e=None, w=None, d=None)


def cloth_drape(tex, lace):
    """The north drape of a tablecloth; the blockstate rotates it to the other sides."""
    out = B(-0.1, 11, -0.12, 16.1, 16.1, 0, tex, s=None, e=None, w=None, d=None, u=None)
    out += B(-0.1, 10.1, -0.16, 16.1, 11, -0.06, (lace, (0, 12, 16, 16)), s=None, e=None, w=None, d=None, u=None)
    return out


TABLE = Family("cottage_table", "Cottage Table", "interior", "tables",
               [Variant(f"{w}_cottage_table", f"{n} Cottage Table", wood=w) for w, n in WOODS],
               None, kind="TABLE", material="WOOD", facing=False,
               recipe=lambda v: shaped(["sss", "f f", "f f"], {"s": f"minecraft:{v['wood']}_slab",
                                                               "f": f"minecraft:{v['wood']}_fence"}, 2),
               desc="A sturdy table for showing off decorations. Dress it with a tablecloth; tables side by side join up.")


# =========================================================================== curtains

def curtain_panel(x0, x1, ytop, tex, lace, pleats=4):
    out = []
    w = (x1 - x0) / pleats
    for i in range(pleats):
        z0 = 13.25 if i % 2 else 13.75
        out += B(x0 + i * w, 0, z0, x0 + (i + 1) * w, ytop, z0 + 1, tex)
        out += B(x0 + i * w, 0, z0 - 0.06, x0 + (i + 1) * w, 1, z0, (lace, (0, 12, 16, 16)), s=None, e=None, w=None,
                 u=None, d=None)
    return out


def curtain_gather(x0, x1, ytop, tex, ribbon):
    out = B(x0, 7.5, 12.75, x1, ytop, 15, tex)
    out += B(x0 + 0.5, 6.25, 12.6, x1 - 0.5, 7.5, 15.1, ribbon)
    out += B(x0 - 0.25, 0, 12.5, x1 + 0.25, 6.25, 15, tex)
    return out


@family("curtains", "Curtains", "interior", "curtains", set_variants("curtains", "Curtains"),
        kind="CURTAIN", material="CLOTH", mounts=("wall",), open_=True, rod=True, walk=True,
        interaction="TOGGLE", item_state={"rod": True}, mineable=None,
        recipe=lambda v: shapeless("minecraft:white_wool", "minecraft:white_wool", "minecraft:stick", S, count=2),
        desc="Hang them in front of a window. Right-click to tie them back. Stack them for tall windows.")
def curtains(c, st):
    top = 15 if st["rod"] else 16
    out = []
    if st["open"]:
        out += curtain_gather(0, 4, top, c.print, c.ribbon)
        out += curtain_gather(12, 16, top, c.print, c.ribbon)
    else:
        out += curtain_panel(0, 8, top, c.print, c.lace)
        out += curtain_panel(8, 16, top, c.print, c.lace)
    if st["rod"]:
        out += B(0, 15, 14.25, 16, 15.75, 15, c.brass)
        for x in (1, 5, 9, 13) if not st["open"] else (1, 13):
            out += B(x, 14.6, 14, x + 2, 15.9, 15.25, c.brass)
    return out


# =========================================================================== wall decor

@family("embroidery_hoop", "Embroidery Hoop", "interior", "wall_decor", set_variants("embroidery_hoop", "Embroidery Hoop"),
        material="CLOTH", mounts=("wall",), mineable=None,
        recipe=lambda v: shapeless("minecraft:stick", "minecraft:string", "minecraft:white_carpet", S),
        desc="A finished piece of embroidery, stitched with the set's motif.")
def embroidery_hoop(c, st):
    out = disc_xy(8, 8.5, 5.5, 15.25, 15.75, c.linen, face_tex=c.motif)
    out += ring_xy(8, 8.5, 6, 0.9, 14.75, 16, "minecraft:block/birch_planks")
    out += B(7.25, 14.25, 14.5, 8.75, 15.4, 16, c.brass)
    out += B(7.6, 15.4, 15.2, 8.4, 16, 15.8, c.ribbon)
    return out


@family("wall_plates", "Wall Plates", "interior", "wall_decor", set_variants("wall_plates", "Wall Plates"),
        material="CERAMIC", mounts=("wall",), mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:clay_ball", "minecraft:clay_ball", "minecraft:clay_ball", S),
        desc="A trio of hand-painted plates for a cottage kitchen wall.")
def wall_plates(c, st):
    out = ring_xy(8, 9.5, 5, 1, 15, 16, c.ceramic)
    out += disc_xy(8, 9.5, 4.1, 15.4, 16, c.ceramic, face_tex=c.motif)
    for cx in (2.75, 13.25):
        out += ring_xy(cx, 4.5, 2.5, 0.6, 15.1, 16, c.ceramic)
        out += disc_xy(cx, 4.5, 2, 15.5, 16, c.glaze)
    out += B(7.5, 14.4, 15.6, 8.5, 15.4, 16, c.brass)
    return out


@family("dried_flowers", "Dried Flower Bundle", "interior", "wall_decor",
        set_variants("dried_flowers", "Dried Flower Bundle"), material="PLANT", mounts=("wall",), walk=True,
        mineable=None,
        recipe=lambda v: shapeless("#minecraft:small_flowers", "#minecraft:small_flowers", "minecraft:string", S),
        desc="Flowers hung upside down to dry, tied with twine.")
def dried_flowers(c, st):
    out = B(7.5, 14, 15, 8.5, 15, 16, c.iron)
    out += B(7, 12, 13.5, 9, 13.5, 15, c.string)
    out += B(7.25, 13.5, 13.75, 8.75, 14.5, 14.75, c.branch)
    tex = (c.bouquet, "fit", 180)
    out += box((4, 2, 14), (12, 12.5, 14), tex, shade=False)
    out += box((4.5, 2.5, 14.2), (11.5, 12.5, 14.2), tex, rot=("y", 22.5, (8, 8, 14.2)), shade=False)
    out += box((4.5, 2.5, 14.2), (11.5, 12.5, 14.2), tex, rot=("y", -22.5, (8, 8, 14.2)), shade=False)
    return out


@family("bunting", "Bunting", "interior", "wall_decor", set_variants("bunting", "Bunting"),
        material="CLOTH", mounts=("wall",), walk=True, mineable=None,
        recipe=lambda v: shapeless("minecraft:string", "minecraft:string", "minecraft:paper", "minecraft:paper", S, count=2),
        desc="Little fabric flags on a string. Hang several in a row along a wall.")
def bunting(c, st):
    out = []
    ys = []
    for i in range(8):
        xa, xb = i * 2, i * 2 + 2
        t = (i + 0.5) / 8
        y = 14.5 - 1.5 * (1 - (2 * t - 1) ** 2)
        ys.append(y)
        out += B(xa, y - 0.2, 15.4, xb, y + 0.2, 15.6, c.string)
    flags = [c.print, c.fabric, c.fabric_light, c.print]
    for k, cx in enumerate((2, 6, 10, 14)):
        t = cx / 16
        ytop = 14.5 - 1.5 * (1 - (2 * t - 1) ** 2) - 0.1
        for j, half in enumerate((1.75, 1.25, 0.75, 0.3)):
            out += B(cx - half, ytop - (j + 1) * 1.25, 15.35, cx + half, ytop - j * 1.25, 15.65, flags[k])
    return out


@family("wall_shelf", "Little Wall Shelf", "interior", "wall_decor", set_variants("wall_shelf", "Little Wall Shelf"),
        material="WOOD", mounts=("wall",),
        recipe=lambda v: shapeless("#minecraft:wooden_slabs", "#minecraft:wooden_slabs", S),
        desc="A floating shelf with a jar, a tiny plant and two books.")
def wall_shelf(c, st):
    out = B(1, 6, 10, 15, 7, 16, c.paint)
    out += B(2.5, 3, 14.5, 3.5, 6, 16, c.paint)
    out += B(12.5, 3, 14.5, 13.5, 6, 16, c.paint)
    out += B(2.5, 5, 13, 3.5, 6, 14.5, c.paint)
    out += B(12.5, 5, 13, 13.5, 6, 14.5, c.paint)
    out += disk(4, 13, 1.5, 7, 10, c.glaze)
    out += B(2.6, 10, 11.6, 5.4, 10.75, 14.4, c.paint_light)
    out += B(6.75, 7, 11.75, 9.25, 9.5, 14.25, c.ceramic)
    out += B(7, 9, 12, 9, 9.25, 14, c.soil, **FLAT)
    out += cross(8, 13, 4, 9.25, 13.5, c.foliage)
    out += box((10.5, 7, 11.5), (11.75, 11.5, 15), c.cover_a, up=c.pages)
    out += box((11.8, 7, 11.5), (13.3, 10.75, 15), c.cover_c, up=c.pages)
    return out


# =========================================================================== clocks

def hands(c, hour, cx, cy, r, z):
    return plane_z(cx - r, cy - r, cx + r, cy + r, z, c(f"clock_hands_{hour}"), uv="fit", shade=True)


def clock_overlay(spec):
    cx, cy, r, z = spec

    def overlay(c, kind, i, st):
        return hands(c, i, cx, cy, r, z)
    return overlay


@family("wall_clock", "Wall Clock", "interior", "clocks", set_variants("wall_clock", "Wall Clock"),
        kind="CLOCK", material="WOOD", mounts=("wall",), hours=True, interaction="TELL_TIME",
        overlay=clock_overlay((8, 8, 5.25, 14.9)),
        recipe=lambda v: shapeless("minecraft:clock", "#minecraft:planks", S),
        desc="Its hands follow the time of day. Right-click to read the time.")
def wall_clock(c, st):
    out = ring_xy(8, 8, 6.5, 1.5, 14, 16, c.paint)
    out += disc_xy(8, 8, 5.25, 15, 15.75, c.cream, face_tex=c.clock_face)
    out += bow(8, 14, 13.4, 14.4, 0.6, c.ribbon)
    return out


@family("mantel_clock", "Mantel Clock", "interior", "clocks", set_variants("mantel_clock", "Mantel Clock"),
        kind="CLOCK", material="WOOD", hours=True, interaction="TELL_TIME",
        overlay=clock_overlay((8, 6.5, 3.5, 4.4)),
        recipe=lambda v: shapeless("minecraft:clock", "#minecraft:wooden_slabs", "minecraft:gold_nugget", S),
        desc="A little arched clock for a shelf or mantelpiece.")
def mantel_clock(c, st):
    out = B(2.5, 0, 4.5, 13.5, 0.75, 11.5, c.paint_deep)
    out += B(3, 0.75, 5, 13, 9, 11, c.paint)
    out += B(4, 9, 5, 12, 10.5, 11, c.paint)
    out += B(5.5, 10.5, 5, 10.5, 11.5, 11, c.paint)
    out += B(7.5, 11.5, 7.5, 8.5, 12.5, 8.5, c.brass)
    out += disc_xy(8, 6.5, 3.5, 4.5, 5, c.cream, face_tex=c.clock_face)
    out += ring_xy(8, 6.5, 3.9, 0.6, 4.25, 5, c.brass)
    return out


@family("cuckoo_clock", "Cuckoo Clock", "interior", "clocks", set_variants("cuckoo_clock", "Cuckoo Clock"),
        kind="CLOCK", material="WOOD", mounts=("wall",), hours=True, interaction="TELL_TIME",
        overlay=clock_overlay((8, 9, 2.75, 10.4)),
        recipe=lambda v: shapeless("minecraft:clock", "minecraft:feather", "#minecraft:planks", S),
        desc="A carved chalet clock. A little bird calls out every hour.")
def cuckoo_clock(c, st):
    out = B(3, 5, 11, 13, 13, 16, c.paint)
    # gable under the roof, then two sloped slabs meeting at the ridge
    out += B(4, 13, 11.25, 12, 14, 16, c.paint)
    out += B(5.5, 14, 11.25, 10.5, 14.75, 16, c.paint)
    out += B(7, 14.75, 11.25, 9, 15.25, 16, c.paint)
    out += box((1.1, 13.5, 10.25), (8.7, 14.5, 16), c.roof, rot=("z", 22.5, (4.9, 14, 13)))
    out += box((7.3, 13.5, 10.25), (14.9, 14.5, 16), c.roof, rot=("z", -22.5, (11.1, 14, 13)))
    out += disc_xy(8, 9, 2.75, 10.5, 11, c.cream, face_tex=c.clock_face)
    out += B(7, 12, 10.75, 9, 12.9, 11, c.paint_deep, s=None)
    out += B(7.6, 12.8, 10.4, 8.4, 13.1, 10.8, c.brass)
    # carved leaves either side
    out += B(2.25, 9, 12, 3, 12, 15, c.paint_deep)
    out += B(13, 9, 12, 13.75, 12, 15, c.paint_deep)
    # pendulum and pine-cone weights
    out += B(7.75, 2.5, 11.5, 8.25, 5, 12, c.brass)
    out += B(7, 1.5, 11.25, 9, 3, 11.75, c.brass)
    for x in (5.5, 10.5):
        out += B(x - 0.15, 2.5, 13.35, x + 0.15, 5, 13.65, c.chain)
        out += B(x - 0.75, 0, 12.75, x + 0.75, 2.5, 14.25, c.branch)
    return out


# =========================================================================== trinket & jewelry boxes

@family("trinket_box", "Trinket Box", "interior", "boxes", set_variants("trinket_box", "Trinket Box"),
        kind="STORAGE", material="WOOD", open_=True, interaction="OPEN_STORAGE",
        recipe=lambda v: shapeless("#minecraft:wooden_slabs", "#minecraft:wooden_slabs", "minecraft:gold_nugget", S),
        desc="A fabric-lidded box with room for nine little treasures.")
def trinket_box(c, st):
    out = B(4, 0, 5, 12, 3, 11, c.paint)
    if st["open"]:
        out += B(4.5, 0.75, 5.5, 11.5, 2.5, 10.5, c.fabric, n=None, s=None, e=None, w=None)
        out += B(5, 2.5, 6, 6.5, 3.25, 7.5, c.silver)
        out += B(8, 2.5, 6.5, 9.5, 3, 8, c.brass)
        out += B(6.5, 2.5, 8.5, 8, 3.5, 10, c.glass)
        out += B(3.75, 3, 10.75, 12.25, 9.5, 12.25, c.print)
    else:
        out += B(3.75, 3, 4.75, 12.25, 4.5, 11.25, c.print)
        out += B(7.5, 2, 4.5, 8.5, 3.5, 5, c.brass)
        out += flat_bow(8, 8, 4.5, 0.6, c.ribbon)
    return out


@family("jewelry_box", "Jewelry Box", "interior", "boxes", set_variants("jewelry_box", "Jewelry Box"),
        kind="STORAGE", material="WOOD", open_=True, interaction="OPEN_STORAGE",
        recipe=lambda v: shapeless("#minecraft:planks", "#minecraft:planks", "minecraft:gold_nugget", "minecraft:glass_pane", S),
        desc="Opens with a little chime to show a mirror and a string of pearls.")
def jewelry_box(c, st):
    out = []
    for x in (3.5, 11.5):
        for z in (5, 10):
            out += B(x, 0, z, x + 1, 0.5, z + 1, c.brass)
    out += B(3.5, 0.5, 5, 12.5, 6, 11, c.paint)
    out += B(4.5, 1.25, 4.75, 11.5, 3, 5, c.paint_light, s=None)
    out += B(7.6, 1.8, 4.4, 8.4, 2.5, 4.75, c.brass)
    out += B(4.5, 3.5, 4.75, 11.5, 5.25, 5, c.paint_light, s=None)
    if st["open"]:
        out += B(4, 5.5, 5.5, 12, 5.75, 10.5, c.fabric, **FLAT)
        for i in range(6):
            out += B(5 + i, 5.75, 6.5 + (i % 2) * 0.5, 5.75 + i, 6.25, 7 + (i % 2) * 0.5, c.cream)
        out += B(9.5, 5.75, 8, 11, 6.5, 9.5, c.glass)
        out += B(3.25, 6, 11, 12.75, 12.5, 12.5, c.paint)
        out += B(4.25, 7, 10.95, 11.75, 11.5, 11, c.mirror, s=None, e=None, w=None, u=None, d=None)
    else:
        out += B(3.25, 6, 4.75, 12.75, 7.5, 11.25, c.paint)
        out += B(4.5, 7.5, 6, 11.5, 8, 10, c.fabric)
        out += B(7.25, 8, 7.25, 8.75, 8.75, 8.75, c.glass)
    return out


# =========================================================================== mirrors

def oval(cx, cy, z0, z1, tex, grow=0.0):
    rows = [(cx - 4 - grow, cy - 5 - grow * 0.5, cx + 4 + grow, cy + 5 + grow * 0.5),
            (cx - 3 - grow, cy + 5 + grow * 0.5, cx + 3 + grow, cy + 6.25 + grow),
            (cx - 1.5 - grow * 0.5, cy + 6.25 + grow, cx + 1.5 + grow * 0.5, cy + 7 + grow),
            (cx - 3 - grow, cy - 6.25 - grow, cx + 3 + grow, cy - 5 - grow * 0.5),
            (cx - 1.5 - grow * 0.5, cy - 7 - grow, cx + 1.5 + grow * 0.5, cy - 6.25 - grow)]
    out = []
    for x0, y0, x1, y1 in rows:
        out += B(x0, y0, z0, x1, y1, z1, tex)
    return out


@family("wall_mirror", "Oval Wall Mirror", "interior", "mirrors", set_variants("wall_mirror", "Oval Wall Mirror"),
        material="GLASS", mounts=("wall",), interaction="COMPLIMENT", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:glass_pane", "minecraft:glass_pane", "minecraft:iron_ingot", S),
        desc="Right-click to check your reflection. It always has something nice to say.")
def wall_mirror(c, st):
    out = oval(8, 8, 15.4, 16, frame_tex(c), grow=1.0)
    out += oval(8, 8, 15, 15.4, c.mirror)
    out += bow(8, 14.6, 14.4, 15.2, 0.55, c.ribbon)
    return out


@family("vanity_mirror", "Vanity Mirror", "interior", "mirrors", set_variants("vanity_mirror", "Vanity Mirror"),
        material="GLASS", interaction="COMPLIMENT", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:glass_pane", "minecraft:iron_nugget", "minecraft:stick", S),
        desc="A small round mirror on a stand, for a dressing table.")
def vanity_mirror(c, st):
    f = frame_tex(c)
    out = disk(8, 8, 3, 0, 1, f)
    out += B(7.5, 1, 7.5, 8.5, 5, 8.5, c.brass)
    out += disc_xy(8, 8.75, 3.5, 7.6, 8.4, f, face_tex=c.mirror)
    out += ring_xy(8, 8.75, 4, 0.75, 7.4, 8.6, f)
    out += bow(8, 12, 7.0, 7.5, 0.4, c.ribbon)
    return out


# =========================================================================== picture frames

def picture_overlay(spec):
    x0, y0, x1, y1, z, uv, rot = spec

    def overlay(c, kind, i, st):
        return box((x0, y0, z), (x1, y1, z), (c(f"picture_{i}"), uv), rot=rot, shade=True)
    return overlay


@family("wall_frame", "Picture Frame", "interior", "frames", set_variants("wall_frame", "Picture Frame"),
        material="WOOD", mounts=("wall",), styles=8, interaction="CYCLE_STYLE",
        overlay=picture_overlay((3, 4, 13, 12, 15.4, (0, 1.6, 16, 14.4), None)),
        recipe=lambda v: shapeless("minecraft:painting", S),
        desc="Holds one of eight little paintings. Right-click to swap the picture.")
def wall_frame(c, st):
    f = frame_tex(c)
    out = B(2, 12, 15, 14, 13, 16, f)
    out += B(2, 3, 15, 14, 4, 16, f)
    out += B(2, 4, 15, 3, 12, 16, f)
    out += B(13, 4, 15, 14, 12, 16, f)
    out += B(3, 4, 15.5, 13, 12, 16, c.linen)
    out += bow(8, 12.6, 14.6, 15.2, 0.45, c.ribbon)
    return out


STAND_ROT = ("x", 22.5, (8, 0, 8))


@family("standing_frame", "Standing Photo Frame", "interior", "frames",
        set_variants("standing_frame", "Standing Photo Frame"), material="WOOD", styles=8,
        interaction="CYCLE_STYLE", overlay=picture_overlay((6, 1, 10, 7, 7.4, (2.67, 0, 13.33, 16), STAND_ROT)),
        recipe=lambda v: shapeless("minecraft:painting", "minecraft:stick", S),
        desc="A little frame on an easel back. Right-click to swap the picture.")
def standing_frame(c, st):
    f = frame_tex(c)
    els = []
    els += B(5, 7, 7.5, 11, 8, 8.5, f)
    els += B(5, 0, 7.5, 11, 1, 8.5, f)
    els += B(5, 1, 7.5, 6, 7, 8.5, f)
    els += B(10, 1, 7.5, 11, 7, 8.5, f)
    els += B(6, 1, 8, 10, 7, 8.5, c.linen)
    for e in els:
        e.rot = STAND_ROT
    out = els
    out += box((7.5, 0, 12.5), (8.5, 5.5, 13.5), f, rot=("x", -22.5, (8, 0, 13)))
    return out


# =========================================================================== baskets

def basket_contents(c, kind):
    v = kind
    out = []
    if v == "empty":
        out += B(3.75, 3.5, 4.75, 12.25, 4, 11.25, c.gingham_red, **FLAT)
        out += B(3.6, 3, 4.5, 6, 5.25, 4.75, c.gingham_red)
    elif v == "apples":
        out += bumps(3.75, 4.75, 12.25, 11.25, 3.25, c.apple, n=8, size=2.5, seed=1)
    elif v == "bread":
        out += B(3.75, 3.5, 4.75, 12.25, 4, 11.25, c.linen, **FLAT)
        out += rbox(4, 3.5, 5.5, 9, 6.5, 8, c.bread, r=0.5)
        out += rbox(6.5, 3.5, 8, 12, 6, 10.75, c.bread, r=0.5)
    elif v == "yarn":
        out += rbox(4, 3.5, 5, 7.5, 7, 8.5, c.yarn_pink, r=0.75)
        out += rbox(7.75, 3.5, 5.5, 10.75, 6.5, 8.5, c.yarn_mint, r=0.75)
        out += rbox(5.5, 3.5, 8.25, 8.5, 6.5, 11, c.yarn_lilac, r=0.75)
        out += rbox(9, 3.5, 8.5, 12, 6, 11, c.yarn_butter, r=0.75)
        out += box((6, 5, 7.5), (6.5, 11, 8), c.brass, rot=("z", 22.5, (6.25, 8, 7.75)))
        out += box((9.5, 5, 7), (10, 11, 7.5), c.brass, rot=("z", -22.5, (9.75, 8, 7.25)))
    elif v == "eggs":
        out += B(3.75, 3, 4.75, 12.25, 4, 11.25, c.straw, **FLAT)
        out += bumps(4, 5, 12, 11, 3.75, c.egg, n=7, size=2.25, seed=2, h=2.75)
    elif v == "flowers":
        out += plane_y(3.5, 4.5, 12.5, 11.5, 5, c.wildflowers, shade=False)
        out += cross4(8, 8, 9, 3, 11, c.wildflowers)
    elif v == "mushrooms":
        out += B(3.75, 3, 4.75, 12.25, 3.75, 11.25, c.leaves, **FLAT)
        for x, z, s in ((5.5, 6.5, 1.0), (9.5, 7, 0.8), (7, 9.5, 0.9), (10.5, 9.75, 0.7)):
            out += B(x - 0.5 * s, 3.5, z - 0.5 * s, x + 0.5 * s, 5.5 * s + 1, z + 0.5 * s, c.mushroom_stem)
            cap = c.mushroom_red if s > 0.85 else c.mushroom_brown
            out += B(x - 1.5 * s, 5.5 * s + 1, z - 1.5 * s, x + 1.5 * s, 5.5 * s + 2, z + 1.5 * s, cap)
    elif v == "linens":
        out += B(4, 3.5, 5, 12, 7.5, 11, c.linens)
        out += B(3.9, 7.25, 4.9, 12.1, 7.5, 11.1, c.lace, d=None)
    elif v == "strawberries":
        out += B(3.75, 3.5, 4.75, 12.25, 4, 11.25, c.leaves, **FLAT)
        out += bumps(4, 5, 12, 11, 3.75, c.strawberry_fruit, n=10, size=1.75, seed=3)
    elif v == "lavender":
        for k, z in enumerate((5.5, 7.25, 9)):
            out += box((2.5, 4 + k * 0.5, z), (10, 4.75 + k * 0.5, z + 1.5), c.leaves)
            out += box((10, 3.75 + k * 0.5, z - 0.25), (14, 5.25 + k * 0.5, z + 1.75), c.lavender_bunch)
        out += B(9.5, 3.5, 5, 10.25, 6.75, 11, c.string)
    return out


BASKETS = [("empty", "wicker_basket", "Wicker Basket"), ("apples", "basket_of_apples", "Basket of Apples"),
           ("bread", "basket_of_bread", "Basket of Bread"), ("yarn", "basket_of_yarn", "Basket of Yarn"),
           ("eggs", "basket_of_eggs", "Basket of Eggs"), ("flowers", "basket_of_flowers", "Basket of Wildflowers"),
           ("mushrooms", "basket_of_mushrooms", "Basket of Mushrooms"),
           ("linens", "basket_of_linens", "Basket of Linens"),
           ("strawberries", "basket_of_strawberries", "Basket of Strawberries"),
           ("lavender", "basket_of_lavender", "Basket of Lavender")]

BASKET_RECIPES = {
    "empty": shaped(["w w", "www"], {"w": "minecraft:wheat"}),
    "apples": shapeless("cottagecoreliving:wicker_basket", "minecraft:apple", "minecraft:apple"),
    "bread": shapeless("cottagecoreliving:wicker_basket", "minecraft:bread", "minecraft:bread"),
    "yarn": shapeless("cottagecoreliving:wicker_basket", "#minecraft:wool", "minecraft:string"),
    "eggs": shapeless("cottagecoreliving:wicker_basket", "minecraft:egg", "minecraft:egg", "minecraft:egg"),
    "flowers": shapeless("cottagecoreliving:wicker_basket", "#minecraft:small_flowers", "#minecraft:small_flowers",
                         "#minecraft:small_flowers"),
    "mushrooms": shapeless("cottagecoreliving:wicker_basket", "minecraft:red_mushroom", "minecraft:brown_mushroom"),
    "linens": shapeless("cottagecoreliving:wicker_basket", "minecraft:white_carpet", "minecraft:white_carpet"),
    "strawberries": shapeless("cottagecoreliving:wicker_basket", "minecraft:sweet_berries", "minecraft:sweet_berries",
                              "minecraft:sweet_berries"),
    "lavender": shapeless("cottagecoreliving:wicker_basket", "minecraft:lilac"),
}


@family("basket", "Basket", "interior", "baskets",
        [Variant(bid, name, content=k) for k, bid, name in BASKETS], material="CLOTH", mineable="axe",
        recipe=lambda v: BASKET_RECIPES[v["content"]],
        desc="A woven wicker basket. Fill an empty one with apples, bread, yarn and more.")
def basket_family(c, st):
    out = basket(3, 4, 13, 12, 4.5, c.wicker, handle=True, handle_h=5.5)
    out += basket_contents(c, c.v["content"])
    return out


# =========================================================================== kitchen

@family("teapot", "Teapot", "interior", "kitchen", set_variants("teapot", "Teapot"),
        material="CERAMIC", interaction="STEAM", ambient="STEAM", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:brick", "minecraft:brick", "minecraft:brick", S),
        desc="Right-click to brew a fresh pot. Wisps of steam rise from the spout.")
def teapot(c, st):
    out = disk(8, 8, 2.75, 0, 0.5, c.ceramic)
    out += disk(8, 8, 3.5, 0.5, 5.5, c.glaze)
    out += disk(8, 8, 2.75, 5.5, 6.5, c.ceramic)
    out += disk(8, 8, 2, 6.5, 7, c.ceramic)
    out += B(7.5, 7, 7.5, 8.5, 8, 8.5, c.ceramic)
    out += B(2.5, 2, 7.25, 4.75, 3.5, 8.75, c.ceramic)
    out += B(1.5, 3, 7.4, 3, 5, 8.6, c.ceramic)
    out += B(1, 4.5, 7.5, 2, 6, 8.5, c.ceramic)
    out += B(11.25, 4.5, 7.5, 13, 5.5, 8.5, c.ceramic)
    out += B(12.5, 1.5, 7.5, 13.5, 5, 8.5, c.ceramic)
    out += B(11.25, 1.5, 7.5, 13, 2.5, 8.5, c.ceramic)
    return out


@family("teacups", "Teacups", "interior", "kitchen", set_variants("teacups", "Teacups"),
        material="CERAMIC", interaction="STEAM", ambient="STEAM", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:brick", "minecraft:bowl", S),
        desc="Two cups of tea on saucers, with a little silver spoon.")
def teacups(c, st):
    out = []
    for cx, cz in ((5, 6), (11, 10)):
        out += disk(cx, cz, 2.75, 0, 0.5, c.glaze)
        out += disk(cx, cz, 1.75, 0.5, 3, c.ceramic, u=None)
        out += disk(cx, cz, 1.4, 2.5, 2.75, c.tea, n=None, s=None, e=None, w=None, d=None)
        out += B(cx + 1.6, 1.25, cz - 0.25, cx + 2.5, 2.5, cz + 0.25, c.ceramic)
    out += box((8, 0.5, 4), (8.5, 0.75, 7.5), c.silver, rot=("y", 22.5, (8.25, 0.6, 5.75)))
    out += B(5, 0, 11, 8, 2, 14, c.glaze)
    out += B(5.5, 2, 11.5, 7.5, 2.5, 13.5, c.ceramic)
    return out


HERBS = [("basil", "Basil", "minecraft:green_dye"), ("rosemary", "Rosemary", "minecraft:cyan_dye"),
         ("mint", "Mint", "minecraft:lime_dye"), ("thyme", "Thyme", "minecraft:gray_dye"),
         ("sage", "Sage", "minecraft:light_gray_dye")]


@family("potted_herbs", "Potted Herbs", "interior", "kitchen",
        [Variant(f"potted_{k}", f"Potted {n}", herb=k, dye=d) for k, n, d in HERBS], material="CERAMIC",
        mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:flower_pot", "minecraft:wheat_seeds", v["dye"]),
        desc="Kitchen herbs in a terracotta pot, with a little name tag.")
def potted_herbs(c, st):
    out = square_pot(5, 5, 11, 11, 6, c.terracotta, c.soil)
    out += cross(8, 8, 6, 5.5, 13, c("herb_" + c.v["herb"]))
    out += B(9.75, 5.5, 6.25, 10.25, 8.5, 6.75, c("minecraft:block/birch_planks"))
    out += B(9, 8, 6, 11, 9.5, 6.5, c.label)
    return out


JARS = [("wildflowers", "Jar of Wildflowers"), ("buttons", "Jar of Buttons"), ("honey", "Jar of Honey"),
        ("ribbons", "Jar of Ribbons")]
JAR_RECIPES = {
    "wildflowers": shapeless("minecraft:glass", "#minecraft:small_flowers", "#minecraft:small_flowers"),
    "buttons": shapeless("minecraft:glass", "#minecraft:buttons", "#minecraft:buttons"),
    "honey": shapeless("minecraft:glass", "minecraft:honey_bottle"),
    "ribbons": shapeless("minecraft:glass", "minecraft:string", "minecraft:pink_dye"),
}


@family("mason_jar", "Mason Jar", "interior", "kitchen",
        [Variant(f"jar_of_{k}", n, content=k) for k, n in JARS], material="GLASS", render="translucent",
        mineable=None, recipe=lambda v: JAR_RECIPES[v["content"]],
        desc="A glass jar full of something sweet or useful.")
def mason_jar(c, st):
    k = c.v["content"]
    out = []
    if k == "wildflowers":
        out += cross(8, 8, 7, 1, 14, c.wildflowers)
        out += B(4.9, 5.5, 4.9, 11.1, 6.25, 11.1, c.string, u=None, d=None)
    elif k == "buttons":
        out += B(5.25, 0.25, 5.25, 10.75, 5, 10.75, c.buttons)
    elif k == "honey":
        out += B(5.25, 0.25, 5.25, 10.75, 6, 10.75, c.honey)
        out += B(7.5, 5, 7.5, 8.5, 10.5, 8.5, c.branch)
    else:
        out += B(5.25, 0.25, 5.25, 10.75, 5.5, 10.75, c.spools)
    out += B(5, 0, 5, 11, 7, 11, c.glass_clear, u=None)
    if k in ("buttons", "ribbons"):
        out += B(4.5, 7, 4.5, 11.5, 7.75, 11.5, c.gingham_red)
        out += B(4.4, 5.75, 4.4, 11.6, 7, 11.6, c.gingham_red, u=None, d=None)
        out += B(4.3, 6.25, 4.3, 11.7, 6.75, 11.7, c.string, u=None, d=None)
    elif k == "honey":
        out += B(4.75, 7, 4.75, 11.25, 7.5, 11.25, c.brass)
        out += B(5.5, 2, 4.95, 10.5, 4.5, 5, c.label, s=None, e=None, w=None, u=None, d=None)
    return out


@family("firefly_jar", "Firefly Jar", "interior", "kitchen", [Variant("firefly_jar", "Firefly Jar")],
        material="GLASS", render="translucent", mounts=("floor", "ceiling"), light=("ALWAYS", 9),
        ambient="FIREFLIES", mineable=None,
        recipe=lambda v: shapeless("minecraft:glass", "minecraft:glow_berries", "minecraft:glowstone_dust"),
        desc="A jar of gentle glowing fireflies. Stand it on a table or hang it up.")
def firefly_jar(c, st):
    y0 = 4 if st["mount"] == "ceiling" else 0
    out = B(5, y0, 5, 11, y0 + 8, 11, c.firefly_jar)
    out += B(4.75, y0 + 8, 4.75, 11.25, y0 + 8.75, 11.25, c.brass)
    out += B(7.6, y0 + 8.75, 7.6, 8.4, y0 + 9.5, 8.4, c.brass)
    if st["mount"] == "ceiling":
        out += cross(8, 8, 1, y0 + 9.5, 16, c.string, uv=(0, 0, 3, 16), shade=True)
    else:
        out += B(4.5, y0 + 8.5, 7.75, 5, y0 + 10.5, 8.25, c.iron)
        out += B(11, y0 + 8.5, 7.75, 11.5, y0 + 10.5, 8.25, c.iron)
        out += B(5, y0 + 10, 7.75, 11, y0 + 10.5, 8.25, c.iron)
    return out


# the tablecloth items (5 sets) are made here too; see data.py
TABLECLOTHS = [(s, f"{s}_tablecloth") for s in SETS]
