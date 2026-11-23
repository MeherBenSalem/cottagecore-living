"""Garden decorations: birdhouses, wind chimes, signs, flower baskets, watering cans,
garden tools, picnic blankets, decorative stones, fountains, planters, lanterns and
a little fairy corner."""
from .core import B, box, rbox, move, rot_y90
from .fam import Family, Variant, family, set_variants
from .interior import S, frame_tex, shaped, shapeless, flame
from .shapes import (FLAT, basket, bow, bumps, cross, cross4, crate, disc_xy, disk, heart_xy, lantern, plane_y,
                     plane_z, pot, ring_xz, square_pot)


# =========================================================================== birdhouses

def roof(x0, x1, ridge_y, eave_y, z0, z1, tex):
    """A gabled roof from two slabs at 45 degrees meeting over the middle."""
    half = (x1 - x0) / 2
    mid = (x0 + x1) / 2
    cy = (ridge_y + eave_y) / 2
    length = half * 1.414 + 0.6
    out = box((mid - half / 2 - length / 2, cy - 0.5, z0), (mid - half / 2 + length / 2, cy + 0.5, z1), tex,
              rot=("z", 45, (mid - half / 2, cy, (z0 + z1) / 2)))
    out += box((mid + half / 2 - length / 2, cy - 0.5, z0), (mid + half / 2 + length / 2, cy + 0.5, z1), tex,
               rot=("z", -45, (mid + half / 2, cy, (z0 + z1) / 2)))
    return out


@family("birdhouse", "Birdhouse", "garden", "birdhouses", set_variants("birdhouse", "Birdhouse"),
        material="WOOD", mounts=("floor", "ceiling"), interaction="CHIRP", ambient="BIRDS",
        recipe=lambda v: shapeless("#minecraft:planks", "#minecraft:planks", "#minecraft:planks", "minecraft:stick", S),
        desc="Pop it on a fence post or hang it from a branch. Birds come and go.")
def birdhouse(c, st):
    y0 = 4 if st["mount"] == "ceiling" else 0
    out = B(4.5, y0, 4.5, 11.5, y0 + 0.75, 11.5, c.paint)
    out += B(5, y0 + 0.75, 5, 11, y0 + 7, 11, c.paint_light)
    out += B(6, y0 + 7, 5, 10, y0 + 8, 11, c.paint_light)
    out += B(7, y0 + 8, 5, 9, y0 + 8.75, 11, c.paint_light)
    out += B(7.1, y0 + 3.75, 4.95, 8.9, y0 + 5.5, 5, c.dark, s=None, e=None, w=None, u=None, d=None)
    out += B(7.75, y0 + 2.5, 3, 8.25, y0 + 3, 5, c.branch)
    out += roof(3.5, 12.5, y0 + 10.75, y0 + 6.25, 4, 12, c.roof)
    out += B(7.4, y0 + 10.25, 3.75, 8.6, y0 + 11.25, 12.25, c.paint)
    if st["mount"] == "ceiling":
        out += cross(8, 8, 1, y0 + 11, 16, c.rope, uv=(0, 0, 3, 16), shade=True)
    else:
        out += B(5.5, y0 + 1.5, 4.75, 6.5, y0 + 2.5, 5, c.ribbon, s=None)
    return out


# =========================================================================== wind chimes

TUBES = [(-2.2, 0, 6), (-0.7, -2.1, 7.5), (1.8, -1.3, 5), (1.8, 1.3, 8), (-0.7, 2.1, 6.5)]


@family("wind_chime", "Wind Chime", "garden", "chimes", set_variants("wind_chime", "Wind Chime"),
        material="METAL", mounts=("ceiling", "wall"), interaction="CHIME", ambient="CHIMES", walk=True,
        mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:iron_nugget", "minecraft:iron_nugget", "minecraft:iron_nugget",
                                   "minecraft:string", S),
        desc="Rings softly in the breeze, more often when it rains. Right-click to ring it.")
def wind_chime(c, st):
    out = []
    cz = 8
    top = 13
    if st["mount"] == "wall":
        out += B(7.5, 14.5, 7, 8.5, 15.5, 16, c.iron)
        out += B(7.5, 13.5, 7, 8.5, 14.5, 8, c.iron)
        out += B(6.5, 15, 15, 9.5, 16, 16, c.iron)
        top = 12.75
    else:
        out += cross(8, cz, 1, top + 0.5, 16, c.string, uv=(0, 0, 3, 16), shade=True)
    out += disk(8, cz, 3, top - 1, top, c.paint)
    out += B(7.5, top, 7.5, 8.5, top + 0.75, 8.5, c.paint)
    for dx, dz, ln in TUBES:
        x, z = 8 + dx, cz + dz
        out += B(x - 0.4, top - 1 - ln, z - 0.4, x + 0.4, top - 1.25, z + 0.4, c.metal)
    out += cross(8, cz, 0.6, 4.5, top - 1, c.string, uv=(0, 0, 3, 16), shade=True)
    out += B(7.25, 5.5, 7.25, 8.75, 6.25, 8.75, c.paint)
    out += heart_xy(8, 1, cz - 0.1, cz + 0.1, 0.35, c.ribbon)
    return out


# =========================================================================== garden signs

def sign_overlay(c, kind, i, st):
    return plane_z(2, 8, 14, 14, 6.9, c(f"sign_design_{i}"), uv=(2, 5, 14, 11), shade=True)


@family("garden_sign", "Garden Sign", "garden", "signs", set_variants("garden_sign", "Garden Sign"),
        material="WOOD", styles=8, interaction="CYCLE_STYLE", overlay=sign_overlay,
        recipe=lambda v: shapeless("#minecraft:signs", S),
        desc="A painted sign on a stake. Right-click to pick one of eight pictures.")
def garden_sign(c, st):
    out = B(7.25, 0, 7.5, 8.75, 8, 9, "minecraft:block/spruce_planks")
    out += B(2, 8, 7, 14, 14, 8, c.paint)
    out += B(1.75, 13.75, 6.75, 14.25, 14.5, 8.25, c.paint_light)
    out += B(1.75, 7.5, 6.75, 14.25, 8.25, 8.25, c.paint_light)
    out += cross(5.5, 9, 4, 0, 3.5, c.foliage)
    out += cross(10.5, 7, 3, 0, 2.5, c.bouquet, uv=(4, 4, 12, 16))
    return out


# =========================================================================== planters & flower baskets

@family("flower_basket", "Flower Basket", "garden", "planters", set_variants("flower_basket", "Flower Basket"),
        material="PLANT", mineable="axe",
        recipe=lambda v: shapeless("cottagecoreliving:wicker_basket", "#minecraft:small_flowers", S),
        desc="A wicker basket overflowing with the set's flowers.")
def flower_basket(c, st):
    out = basket(3, 4, 13, 12, 4.5, c.wicker, handle=True, handle_h=6.5)
    out += B(3.5, 3.5, 4.5, 12.5, 4.75, 11.5, c.leaves, **FLAT)
    out += plane_y(2.75, 3.75, 13.25, 12.25, 6, c.flowers, shade=False)
    out += cross4(8, 8, 9.5, 3.5, 10, c.bouquet)
    out += bow(8, 10.25, 7.4, 8.6, 0.55, c.ribbon)
    return out


@family("hanging_planter", "Hanging Planter", "garden", "planters", set_variants("hanging_planter", "Hanging Planter"),
        material="CERAMIC", mounts=("ceiling",), mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:flower_pot", "minecraft:string", "minecraft:string", S),
        desc="A pot on knotted cords with flowers trailing over the edge. Hang it from a ceiling or a beam.")
def hanging_planter(c, st):
    out = disk(8, 8, 3, 2.5, 3, c.ceramic)
    out += disk(8, 8, 3.5, 3, 7.5, c.glaze, u=None)
    out += ring_xz(8, 8, 3.9, 0.9, 7, 8, c.ceramic)
    out += disk(8, 8, 3, 7, 7.5, c.soil, n=None, s=None, e=None, w=None, d=None)
    out += cross(8, 8, 8, 7.5, 16, c.macrame)
    out += cross4(8, 8, 7, 7, 12.5, c.bouquet)
    trail = c(f"trailing_{c.v.set}")
    out += plane_z(3.5, 0, 12.5, 8, 4.1, trail)
    out += plane_z(3.5, 0.5, 12.5, 8, 11.9, trail)
    out += box((4.1, 0.5, 3.5), (4.1, 8, 12.5), (trail, "fit"), shade=False)
    out += box((11.9, 1, 3.5), (11.9, 8, 12.5), (trail, "fit"), shade=False)
    return out


@family("window_box", "Window Box", "garden", "planters", set_variants("window_box", "Window Box"),
        material="WOOD", mounts=("wall", "floor"),
        recipe=lambda v: shapeless("#minecraft:wooden_slabs", "minecraft:flower_pot", S),
        desc="A painted planter for under a window, with ivy spilling over the front.")
def window_box(c, st):
    out = B(1, 0, 10, 15, 5, 16, c.paint, u=None)
    out += B(0.75, 4.25, 9.75, 15.25, 5.25, 16, c.paint_light)
    out += B(1.5, 4, 10.5, 14.5, 4.6, 15.5, c.soil, **FLAT)
    out += B(5.5, 1.25, 9.9, 10.5, 3.75, 10, c.glaze, s=None, e=None, w=None, u=None, d=None)
    for x in (4, 8, 12):
        out += cross(x, 13, 5, 4.5, 11.5, c.bouquet)
    out += plane_y(1.5, 10.5, 14.5, 15.5, 9, c.flowers, shade=False)
    out += plane_z(1, -1, 7, 4.5, 9.7, c.trailing_ivy)
    out += plane_z(10, 0, 15, 4.5, 9.7, c.trailing_ivy)
    return out


@family("flower_wheelbarrow", "Flower Wheelbarrow", "garden", "planters",
        set_variants("flower_wheelbarrow", "Flower Wheelbarrow"), material="WOOD",
        recipe=lambda v: shapeless("minecraft:iron_ingot", "#minecraft:planks", "#minecraft:planks",
                                   "#minecraft:small_flowers", S),
        desc="A little painted wheelbarrow turned into a flower planter.")
def flower_wheelbarrow(c, st):
    out = []
    # wheel at the front
    out += B(7.25, 0.5, 0.75, 8.75, 4.5, 3.75, c.dark)
    out += B(7.25, 1, 0.25, 8.75, 4, 4.25, c.dark)
    out += B(7, 2, 2, 9, 3, 3, c.iron)
    # tray
    out += B(3, 3.5, 3, 13, 4.5, 13, c.paint)
    out += B(3, 4.5, 3, 13, 8, 4, c.paint)
    out += B(3, 4.5, 12, 13, 8, 13, c.paint)
    out += B(3, 4.5, 4, 4, 8, 12, c.paint)
    out += B(12, 4.5, 4, 13, 8, 12, c.paint)
    out += B(4, 7, 4, 12, 7.5, 12, c.soil, **FLAT)
    # handles and legs
    out += B(4, 3, 11, 5, 4, 16, "minecraft:block/spruce_planks")
    out += B(11, 3, 11, 12, 4, 16, "minecraft:block/spruce_planks")
    out += B(4, 0, 10.5, 5, 3, 11.5, "minecraft:block/spruce_planks")
    out += B(11, 0, 10.5, 12, 3, 11.5, "minecraft:block/spruce_planks")
    out += plane_y(3.5, 3.5, 12.5, 12.5, 10, c.flowers, shade=False)
    out += cross4(8, 8, 9, 7, 13.5, c.bouquet)
    return out


# =========================================================================== watering cans & garden tools

@family("watering_can", "Watering Can", "garden", "tools", set_variants("watering_can", "Watering Can"),
        material="METAL", interaction="WATER_PLANTS", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:bucket", "minecraft:iron_nugget", S),
        desc="An enamel watering can. Right-click to water the crops in front of it.")
def watering_can(c, st):
    out = B(4.5, 0, 5, 11.5, 6.5, 11, c.enamel)
    out += B(5, 6.5, 5.5, 11, 7, 10.5, c.enamel)
    out += B(6.5, 7, 6.75, 9.5, 7.5, 9.25, c.enamel)
    out += box((5.25, 1.5, 0.25), (6.25, 2.5, 5.25), c.enamel, rot=("x", 45, (5.75, 2, 5)))
    out += B(4.75, 4.75, 0.5, 6.75, 6.75, 2, c.enamel)
    out += B(5, 5, 0.4, 6.5, 6.5, 0.5, c.iron, s=None, e=None, w=None, u=None, d=None)
    out += B(7.5, 7, 5.5, 8.5, 9.5, 6.5, c.enamel)
    out += B(7.5, 9.5, 5.5, 8.5, 10.5, 11, c.enamel)
    out += B(7.5, 6.5, 10.5, 8.5, 9.5, 11.5, c.enamel)
    out += B(4.4, 2, 4.9, 11.6, 3, 11.1, c.paint_light, u=None, d=None)
    return out


@family("garden_tool_rack", "Garden Tool Rack", "garden", "tools", [Variant("garden_tool_rack", "Garden Tool Rack")],
        material="WOOD", mounts=("wall",),
        recipe=lambda v: shapeless("minecraft:stick", "minecraft:stick", "minecraft:stick", "minecraft:iron_ingot",
                                   "#minecraft:planks"),
        desc="A trowel, a hand fork and pruners hung up on pegs.")
def garden_tool_rack(c, st):
    oak = "minecraft:block/oak_planks"
    out = B(1, 10, 15, 15, 13, 16, oak)
    for x in (3.25, 7.5, 11.75):
        out += B(x, 11, 13.5, x + 1, 12, 15, oak)
    out += B(3.25, 7, 14, 4.25, 10.75, 15, "minecraft:block/spruce_planks")
    out += B(2.5, 3, 14.25, 5, 7, 14.75, c.iron)
    out += B(3, 2, 14.25, 4.5, 3, 14.75, c.iron)
    out += B(7.5, 7, 14, 8.5, 10.75, 15, "minecraft:block/spruce_planks")
    out += B(7, 6, 14.25, 9, 7, 14.75, c.iron)
    for x in (7, 7.75, 8.5):
        out += B(x, 2.5, 14.25, x + 0.5, 6, 14.75, c.iron)
    out += box((11.75, 4, 14.25), (12.25, 9, 14.75), c.iron, rot=("z", 22.5, (12, 9, 14.5)))
    out += box((11.75, 4, 14.25), (12.25, 9, 14.75), c.iron, rot=("z", -22.5, (12, 9, 14.5)))
    out += B(10.75, 9, 14, 11.75, 11, 15, c("sets/strawberry/enamel"))
    out += B(12.25, 9, 14, 13.25, 11, 15, c("sets/strawberry/enamel"))
    return out


@family("tool_bucket", "Bucket of Garden Tools", "garden", "tools", [Variant("tool_bucket", "Bucket of Garden Tools")],
        material="METAL", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:bucket", "minecraft:stick", "minecraft:stick", "minecraft:iron_nugget"),
        desc="A galvanised bucket stuffed with trowels and forks, gloves over the rim.")
def tool_bucket(c, st):
    out = disk(8, 8, 3.75, 0, 6, c.galvanized, u=None)
    out += ring_xz(8, 8, 4.1, 0.75, 5.5, 6.25, c.galvanized)
    out += disk(8, 8, 3.25, 4.5, 5, c.soil, n=None, s=None, e=None, w=None, d=None)
    out += box((6, 4, 6), (7, 12, 7), "minecraft:block/spruce_planks", rot=("x", 22.5, (6.5, 5, 6.5)))
    out += box((8.5, 4, 8.5), (9.5, 12.5, 9.5), "minecraft:block/spruce_planks", rot=("z", -22.5, (9, 5, 9)))
    out += box((7, 4, 9), (8, 11, 10), "minecraft:block/oak_planks", rot=("x", -22.5, (7.5, 5, 9.5)))
    out += B(10.5, 3.5, 3.5, 12.75, 6.75, 5.5, c("sets/pink/print"))
    out += B(10.5, 5.5, 5.5, 12.75, 6.75, 7, c("sets/pink/print"))
    return out


@family("garden_gloves", "Garden Gloves", "garden", "tools", set_variants("garden_gloves", "Garden Gloves"),
        material="CLOTH", mineable=None,
        recipe=lambda v: shapeless("minecraft:leather", "minecraft:string", S),
        desc="A pair of patterned gardening gloves, left out after weeding.")
def garden_gloves(c, st):
    def glove(x0, z0):
        g = B(x0, 0, z0, x0 + 3.5, 1, z0 + 4, c.print)
        g += B(x0 - 0.25, 0, z0 + 4, x0 + 3.75, 1.25, z0 + 6, c.fabric)
        for i in range(4):
            g += B(x0 + i * 0.9, 0, z0 - 2.5 + (0.5 if i in (0, 3) else 0), x0 + i * 0.9 + 0.75, 0.75, z0, c.print)
        g += B(x0 + 3.5, 0, z0 + 1, x0 + 4.75, 0.75, z0 + 2.5, c.print)
        return g
    out = glove(3, 5)
    out += glove(8.5, 6.5)
    return out


@family("seed_packets", "Seed Packet Crate", "garden", "tools", [Variant("seed_packets", "Seed Packet Crate")],
        material="WOOD",
        recipe=lambda v: shapeless("minecraft:paper", "minecraft:wheat_seeds", "minecraft:beetroot_seeds",
                                   "minecraft:pumpkin_seeds"),
        desc="A little crate of colourful seed packets, sorted for planting day.")
def seed_packets(c, st):
    out = crate(4, 5, 12, 11, 4, c.crate)
    for i, x in enumerate((4.75, 6.25, 7.75, 9.25)):
        out += B(x, 1, 6.25 + (i % 2) * 0.5, x + 1.4, 6.5 - (i % 2) * 0.5, 9.5, c.seed_packets)
    out += B(10.75, 1, 6, 11.25, 7, 6.5, c("minecraft:block/birch_planks"))
    return out


# =========================================================================== picnic blankets

def blanket_sheet(c):
    return B(0, 0, 0, 16, 0.5, 16, c.print, n=None, s=None, e=None, w=None)


def blanket_border(c):
    """The north edge of a blanket; the blockstate turns it to face the other sides."""
    out = B(0, 0, 0, 16, 0.6, 1, c.paint_deep, d=None)
    for x in range(0, 16, 2):
        out += B(x + 0.5, 0, -1, x + 1, 0.25, 0, c.cream, d=None)
    return out


BLANKET = Family("picnic_blanket", "Picnic Blanket", "garden", "picnic", set_variants("picnic_blanket", "Picnic Blanket"),
                 None, kind="BLANKET", material="CLOTH", facing=False, walk=False, mineable=None,
                 recipe=lambda v: shapeless("minecraft:white_carpet", "minecraft:white_carpet", "minecraft:white_carpet",
                                            S, count=3),
                 desc="Lay several side by side: they join into one big blanket with a fringed border.")


# =========================================================================== decorative stones

STONES = [("stone", "Stepping Stones", "minecraft:block/smooth_stone", "minecraft:smooth_stone_slab"),
          ("mossy", "Mossy Stepping Stones", "minecraft:block/mossy_cobblestone", "minecraft:mossy_cobblestone_slab"),
          ("sandstone", "Sandstone Stepping Stones", "minecraft:block/sandstone_top", "minecraft:sandstone_slab"),
          ("slate", "Slate Stepping Stones", "minecraft:block/polished_deepslate", "minecraft:polished_deepslate_slab")]


@family("stepping_stones", "Stepping Stones", "garden", "stones",
        [Variant(f"{k}_stepping_stones" if k != "stone" else "stepping_stones", n, tex={"stone_tex": t}, slab=sl)
         for k, n, t, sl in STONES], material="STONE", mineable="pickaxe",
        recipe=lambda v: shapeless(v["slab"], v["slab"], v["slab"], count=4),
        desc="Flat stones to set into a lawn or a flower bed.")
def stepping_stones(c, st):
    t = c.stone_tex
    out = B(2, 0, 2, 7, 1, 6.5, t) + B(2.5, 0, 1.5, 6.5, 1, 7, t)
    out += B(8.5, 0, 3.5, 14, 1, 8, t) + B(9, 0, 3, 13.5, 1, 8.5, t)
    out += B(4, 0, 9, 10, 1, 14.5, t) + B(4.5, 0, 8.5, 9.5, 1, 15, t)
    return out


@family("painted_rock", "Painted Rock", "garden", "stones", set_variants("painted_rock", "Painted Rock"),
        material="STONE", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:cobblestone", S),
        desc="A smooth pebble painted with the set's motif.")
def painted_rock(c, st):
    out = rbox(4, 0, 5, 12, 3.5, 11, c.rock, r=1)
    out += rbox(11.5, 0, 9.5, 14, 1.5, 12, "minecraft:block/smooth_stone", r=0.5)
    return out


@family("stone_cairn", "Stone Cairn", "garden", "stones", [Variant("stone_cairn", "Stone Cairn")],
        material="STONE", count=3, mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:cobblestone", "minecraft:cobblestone", "minecraft:cobblestone",
                                   "minecraft:flint"),
        desc="Balanced stones. Place more to build it taller.")
def stone_cairn(c, st):
    layers = [(4, 4, 12, 12, 2, "minecraft:block/stone"), (5, 5.5, 11, 10.5, 1.75, "minecraft:block/andesite"),
              (5.5, 5, 10.5, 10, 1.5, "minecraft:block/diorite"), (6, 6, 10, 10, 1.75, "minecraft:block/stone"),
              (6.5, 6.5, 9.5, 9.5, 1.25, "minecraft:block/calcite"), (7, 6.75, 9, 9, 1.25, "minecraft:block/andesite")]
    out = []
    y = 0
    for x0, z0, x1, z1, h, t in layers[:2 * st["count"]]:
        out += rbox(x0, y, z0, x1, y + h, z1, t, r=0.5)
        y += h
    return out


# =========================================================================== fountains

WATER = "minecraft:block/water_still"


@family("mini_fountain", "Mini Fountain", "garden", "fountains", set_variants("mini_fountain", "Mini Fountain"),
        material="STONE", interaction="FILL_BOTTLE", ambient="FOUNTAIN", tint=True, render="translucent",
        mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:stone_bricks", "minecraft:stone_bricks", "minecraft:water_bucket", S),
        desc="A two-tier fountain that splashes away. Fill glass bottles from it.")
def mini_fountain(c, st):
    out = ring_xz(8, 8, 7, 1.5, 0, 4, c.stone)
    out += disk(8, 8, 6, 0, 1, c.stone)
    out += ring_xz(8, 8, 5.6, 5.6, 2.75, 3.25, WATER, step=1.4, n=None, s=None, e=None, w=None, d=None)
    out += disk(8, 8, 1.25, 1, 9, c.stone)
    out += disk(8, 8, 3.5, 8.5, 10, c.stone)
    out += ring_xz(8, 8, 3.75, 0.75, 10, 11, c.stone)
    out += disk(8, 8, 3, 10.25, 10.5, WATER, n=None, s=None, e=None, w=None, d=None)
    out += disk(8, 8, 0.75, 10.5, 12.5, c.stone)
    out += B(7.25, 12.5, 7.25, 8.75, 13.25, 8.75, c.stone)
    return out


@family("birdbath", "Birdbath", "garden", "fountains", [Variant("birdbath", "Birdbath")],
        material="STONE", interaction="FILL_BOTTLE", ambient="BIRDS", tint=True, render="translucent",
        mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:stone_bricks", "minecraft:stone_slab", "minecraft:water_bucket"),
        desc="A stone birdbath with a little bluebird perched on the rim.")
def birdbath(c, st):
    stone = "minecraft:block/stone"
    out = disk(8, 8, 2.5, 0, 1, stone)
    out += disk(8, 8, 1.25, 1, 8, stone)
    out += disk(8, 8, 4.5, 8, 9.5, stone)
    out += ring_xz(8, 8, 6, 1.25, 9, 11, stone)
    out += ring_xz(8, 8, 4.9, 4.9, 10, 10.25, WATER, step=1.4, n=None, s=None, e=None, w=None, d=None)
    out += B(12.25, 11, 7.5, 14.25, 12.25, 8.5, c.bird_blue)
    out += B(12, 12, 7.6, 13, 13, 8.4, c.bird_blue)
    out += B(11.5, 12.25, 7.85, 12, 12.6, 8.15, c.brass)
    out += B(14, 11.75, 7.75, 15, 12.25, 8.25, c.bird_blue)
    return out


# =========================================================================== lanterns

@family("garden_lantern", "Garden Lantern", "garden", "lanterns", set_variants("garden_lantern", "Garden Lantern"),
        material="LANTERN", mounts=("floor", "ceiling"), light=("SWITCH", 14), mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:lantern", S),
        desc="A candle lantern for paths and porches. Right-click to light it; hang it from a ceiling.")
def garden_lantern(c, st):
    y0 = 3 if st["mount"] == "ceiling" else 0
    glass = c.lantern_glass_lit if st["lit"] else c.lantern_glass
    out = lantern(8, 8, y0, c.metal, glass, c.metal, candle=c.wax_white, flame=c.flame if st["lit"] else None)
    out += B(7.5, y0 + 8, 7.5, 8.5, y0 + 9, 8.5, c.metal)
    if st["mount"] == "ceiling":
        out += cross(8, 8, 1, y0 + 9, 16, c.chain, uv=(0, 0, 3, 16), shade=True)
    else:
        out += B(6.5, y0 + 9, 7.75, 9.5, y0 + 9.5, 8.25, c.metal)
        out += B(6, y0 + 8, 7.75, 6.5, y0 + 9.5, 8.25, c.metal)
        out += B(9.5, y0 + 8, 7.75, 10, y0 + 9.5, 8.25, c.metal)
    out += bow(8, y0 + 5.5, 4.35, 4.85, 0.5, c.ribbon)
    return out


# =========================================================================== fairy corner

@family("fairy_door", "Fairy Door", "garden", "fairy", set_variants("fairy_door", "Fairy Door"),
        material="WOOD", mounts=("wall",), open_=True, interaction="KNOCK", ambient="SPARKLES",
        recipe=lambda v: shapeless("#minecraft:wooden_doors", S),
        desc="A tiny door for the base of a tree or a garden wall. Knock and see who answers.")
def fairy_door(c, st):
    stone = "minecraft:block/stone_bricks"
    out = B(3.5, 0, 15, 4.5, 8, 16, stone)
    out += B(11.5, 0, 15, 12.5, 8, 16, stone)
    out += B(4, 8, 15, 12, 9, 16, stone)
    out += B(5, 9, 15, 11, 10, 16, stone)
    out += B(6.5, 10, 15, 9.5, 10.75, 16, stone)
    out += B(4, 0, 13.5, 12, 0.75, 15, stone)
    door = B(4.5, 0.75, 15.25, 11.5, 8, 15.75, c.paint)
    door += B(5, 8, 15.25, 11, 9, 15.75, c.paint)
    door += B(6.5, 9, 15.25, 9.5, 9.75, 15.75, c.paint)
    door += B(9.75, 3.5, 14.75, 10.5, 4.25, 15.25, c.brass)
    door += B(7, 5.5, 15.2, 9, 7.5, 15.25, c.fairy_window, s=None, e=None, w=None, u=None, d=None)
    if st["open"]:
        out += B(4.5, 0.75, 15.75, 11.5, 9.75, 16, c.fairy_glow, s=None)
        for e in door:
            e.rot = ("y", -45, (4.5, 4, 15.5))
    out += door
    # a toadstool and some flowers on the doorstep
    out += B(1.75, 0, 13.25, 2.75, 1.5, 14.25, c.mushroom_stem)
    out += B(1, 1.5, 12.5, 3.5, 2.5, 15, c.mushroom_red)
    out += cross(13.75, 14, 3, 0, 3.5, c.bouquet, uv=(4, 4, 12, 16))
    return out


CAPS = {"strawberry": "mushroom_red", "lavender": "sets/lavender/roof", "pink": "sets/pink/roof",
        "fairy": "sets/fairy/roof", "vintage": "mushroom_brown"}


@family("toadstools", "Toadstools", "garden", "fairy", set_variants("toadstools", "Toadstools"),
        material="PLANT", mineable=None,
        recipe=lambda v: shapeless("minecraft:red_mushroom", "minecraft:brown_mushroom", S),
        desc="A ring of painted toadstools, just right for a fairy garden.")
def toadstools(c, st):
    cap = c(CAPS[c.v.set])
    out = []
    for x, z, s in ((6, 7, 1.0), (11, 5, 0.65), (10.5, 11, 0.8)):
        out += B(x - 0.75 * s, 0, z - 0.75 * s, x + 0.75 * s, 4 * s, z + 0.75 * s, c.mushroom_stem)
        out += B(x - 2.5 * s, 4 * s, z - 2.5 * s, x + 2.5 * s, 5.5 * s, z + 2.5 * s, cap)
        out += B(x - 1.5 * s, 5.5 * s, z - 1.5 * s, x + 1.5 * s, 6.25 * s, z + 1.5 * s, cap)
    out += cross(4, 12, 3, 0, 2.5, c.foliage)
    return out


@family("garden_gnome", "Garden Gnome", "garden", "fairy", set_variants("garden_gnome", "Garden Gnome"),
        material="CERAMIC", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:terracotta", "minecraft:white_wool", S),
        desc="A cheerful gnome to keep an eye on the vegetable patch.")
def garden_gnome(c, st):
    boots = c.leather
    out = B(5, 0, 6, 7.5, 1.5, 9.5, boots) + B(8.5, 0, 6, 11, 1.5, 9.5, boots)
    out += B(5, 1.5, 6.5, 11, 7, 10.5, c.fabric)
    out += B(4.9, 3.25, 6.4, 11.1, 4, 10.6, c.leather)
    out += B(7.4, 3.1, 6.25, 8.6, 4.15, 6.5, c.brass)
    out += B(3.75, 4, 7.5, 5, 6.75, 9.5, c.fabric) + B(11, 4, 7.5, 12.25, 6.75, 9.5, c.fabric)
    out += B(3.85, 3, 7.75, 4.9, 4, 9.25, c.gnome_face) + B(11.1, 3, 7.75, 12.15, 4, 9.25, c.gnome_face)
    out += B(5.75, 7, 6.75, 10.25, 10, 10, c.gnome_face, n=(c.gnome_face, (0, 0, 16, 16)))
    out += B(5.5, 4.5, 6.25, 10.5, 8, 7, c.beard)
    out += B(6.5, 3.5, 6.25, 9.5, 4.5, 7, c.beard)
    out += B(7.4, 8, 6.1, 8.6, 9, 6.75, c.gnome_nose)
    hat = c.paint_deep if c.v.set != "fairy" else c.roof
    out += B(5.25, 9.75, 6.25, 10.75, 11, 10.5, hat)
    out += B(5.75, 11, 6.75, 10.25, 12.5, 10, hat)
    out += B(6.5, 12.5, 7.25, 9.5, 14, 9.5, hat)
    out += B(7.1, 14, 7.75, 8.9, 15.25, 9, hat)
    out += B(7.5, 15.25, 8, 8.5, 16, 8.75, hat)
    return out
