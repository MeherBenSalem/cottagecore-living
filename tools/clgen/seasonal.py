"""Seasonal decorations: cherry blossom, spring flowers, summer picnic, autumn leaves,
winter snow, and the 'through the seasons' pieces that change with the season."""
from .core import B, box, rbox
from .fam import Variant, family, set_variants
from .interior import S, flame, shapeless
from .shapes import (FLAT, basket, bow, bumps, crate, cross, cross4, disc_xy, disk, garland, lantern, plane_y,
                     plane_z, pot, ring_xz, wreath)


def one(fid, name):
    return [Variant(fid, name)]


# =========================================================================== cherry blossom

def blossom_puffs(points, tex, s=1.25):
    out = []
    for x, y, z in points:
        out += B(x - s, y - s, z - s, x + s, y + s, z + s, tex)
    return out


@family("blossom_garland", "Cherry Blossom Garland", "seasonal", "cherry_blossom", one("blossom_garland", "Cherry Blossom Garland"),
        material="PLANT", mounts=("wall",), walk=True, ambient="PETALS", mineable=None,
        recipe=lambda v: shapeless("minecraft:cherry_leaves", "minecraft:cherry_leaves", "minecraft:string"),
        desc="A swag of cherry blossom branches. Petals drift down now and then.")
def blossom_garland(c, st):
    out = garland(14.5, 4, 14.75, 15.5, c.branch, thick=0.75)
    pts = []
    for i in range(8):
        t = (i + 0.5) / 8
        y = 14.5 - 4 * (1 - (2 * t - 1) ** 2)
        pts.append((i * 2 + 1, y, 14.75))
    out += blossom_puffs(pts, c.blossom, 1.3)
    return out


@family("blossom_branch_vase", "Cherry Blossom Vase", "seasonal", "cherry_blossom",
        one("blossom_branch_vase", "Cherry Blossom Vase"), material="CERAMIC", ambient="PETALS", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:flower_pot", "minecraft:cherry_leaves", "minecraft:stick"),
        desc="Branches of cherry blossom in a tall white vase.")
def blossom_branch_vase(c, st):
    out = disk(8, 8, 2.5, 0, 6.5, c.white_ceramic)
    out += disk(8, 8, 2.6, 2.5, 3.5, c("sets/pink/ceramic"))
    out += disk(8, 8, 1.5, 6.5, 8.5, c.white_ceramic)
    out += ring_xz(8, 8, 1.9, 0.6, 8.5, 9.25, c.white_ceramic)
    out += box((7.5, 8, 7.5), (8.5, 15, 8.5), c.branch, rot=("z", 22.5, (8, 8, 8)))
    out += box((7.5, 8, 7.5), (8.5, 14, 8.5), c.branch, rot=("z", -22.5, (8, 8, 8)))
    out += box((7.5, 8, 7.5), (8.5, 15.5, 8.5), c.branch, rot=("x", 22.5, (8, 8, 8)))
    out += box((7.5, 8, 7.5), (8.5, 13.5, 8.5), c.branch, rot=("x", -22.5, (8, 8, 8)))
    out += blossom_puffs([(5.5, 14.5, 8), (10.75, 13.5, 8), (8, 15, 11), (8, 13.25, 5.25), (8, 12, 8),
                          (6.5, 11.5, 9.5), (9.75, 11, 6.5)], c.blossom, 1.4)
    return out


@family("blossom_lantern", "Cherry Blossom Lantern", "seasonal", "cherry_blossom",
        one("blossom_lantern", "Cherry Blossom Lantern"), material="PAPER", mounts=("ceiling", "floor"),
        light=("SWITCH", 12), mineable=None,
        recipe=lambda v: shapeless("minecraft:paper", "minecraft:paper", "minecraft:torch", "minecraft:pink_petals"),
        desc="A round paper lantern printed with blossoms. Right-click to light it.")
def blossom_lantern(c, st):
    y0 = 5 if st["mount"] == "ceiling" else 0.75
    paper = c.paper_lantern_lit if st["lit"] else c.paper_lantern
    out = disk(8, 8, 3.75, y0 + 1.5, y0 + 6.5, paper)
    out += disk(8, 8, 3, y0 + 0.75, y0 + 1.5, paper)
    out += disk(8, 8, 3, y0 + 6.5, y0 + 7.25, paper)
    out += disk(8, 8, 2, y0, y0 + 0.75, "minecraft:block/dark_oak_planks")
    out += disk(8, 8, 2, y0 + 7.25, y0 + 8, "minecraft:block/dark_oak_planks")
    if st["mount"] == "ceiling":
        out += cross(8, 8, 1, y0 + 8, 16, c.string, uv=(0, 0, 3, 16), shade=True)
        out += B(7.6, y0 - 3.5, 7.6, 8.4, y0, 8.4, c.ribbon_pink)
        out += B(7.2, y0 - 4.5, 7.2, 8.8, y0 - 3.5, 8.8, c.ribbon_pink)
    else:
        out += disk(8, 8, 2.5, 0, 0.75, "minecraft:block/dark_oak_planks")
    return out


@family("blossom_wreath", "Cherry Blossom Wreath", "seasonal", "cherry_blossom",
        one("blossom_wreath", "Cherry Blossom Wreath"), material="PLANT", mounts=("wall",), mineable=None,
        recipe=lambda v: shapeless("minecraft:cherry_leaves", "minecraft:cherry_leaves", "minecraft:cherry_leaves",
                                   "minecraft:stick"),
        desc="A twig wreath tucked with cherry blossoms and a pink bow.")
def blossom_wreath(c, st):
    return wreath(8, 8, 6, 15, 16, c.branch, c.blossom, c.ribbon_pink,
                  deco=blossom_puffs([(3.5, 5, 14.6), (12.5, 11, 14.6), (5, 12.5, 14.6), (11, 3.5, 14.6)],
                                     c.blossom, 1.4))


@family("blossom_petals", "Scattered Blossom Petals", "seasonal", "cherry_blossom",
        one("blossom_petals", "Scattered Blossom Petals"), material="PLANT", walk=True, ambient="PETALS",
        mineable=None, recipe=lambda v: shapeless("minecraft:pink_petals", "minecraft:pink_petals", count=2),
        desc="A sprinkle of fallen petals for tables, paths and windowsills.")
def blossom_petals(c, st):
    return plane_y(0, 0, 16, 16, 0.1, c.petals, shade=True)


# =========================================================================== spring

@family("tulip_crate", "Crate of Tulips", "seasonal", "spring", one("tulip_crate", "Crate of Tulips"),
        material="WOOD",
        recipe=lambda v: shapeless("minecraft:red_tulip", "minecraft:pink_tulip", "#minecraft:wooden_slabs"),
        desc="A wooden crate of tulips fresh from the market.")
def tulip_crate(c, st):
    out = crate(2.5, 4, 13.5, 12, 5, c.crate)
    out += B(3.5, 4, 5, 12.5, 4.5, 11, c.soil, **FLAT)
    out += cross(5.5, 8, 6, 4, 12.5, c.tulips)
    out += cross(10.5, 8, 6, 4, 12, c.tulips)
    out += B(5, 1.5, 3.9, 11, 3.5, 4, c.label, s=None, e=None, w=None, u=None, d=None)
    return out


@family("daffodil_pot", "Potted Daffodils", "seasonal", "spring", one("daffodil_pot", "Potted Daffodils"),
        material="CERAMIC", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:flower_pot", "minecraft:dandelion", "minecraft:dandelion"),
        desc="Sunny daffodils in a terracotta pot.")
def daffodil_pot(c, st):
    out = pot(8, 8, 3.75, 6, c.terracotta, c.soil)
    out += cross4(8, 8, 7, 5.5, 14, c.daffodils)
    return out


@family("flower_crown", "Flower Crown", "seasonal", "spring", set_variants("flower_crown", "Flower Crown"),
        material="PLANT", mineable=None,
        recipe=lambda v: shapeless("#minecraft:small_flowers", "#minecraft:small_flowers", "#minecraft:small_flowers",
                                   "minecraft:string", S),
        desc="A crown of woven flowers with trailing ribbons, left on a table.")
def flower_crown(c, st):
    out = ring_xz(8, 8, 5, 1.5, 0, 1, c.leaves)
    import math
    for i in range(8):
        a = math.radians(i * 45 + 22.5)
        x = 8 + math.cos(a) * 4.25
        z = 8 + math.sin(a) * 4.25
        out += B(x - 1, 0.5, z - 1, x + 1, 2, z + 1, c.flowers)
    out += B(8.5, 0, 12.75, 9.25, 0.25, 16, c.ribbon)
    out += B(9.75, 0, 12.75, 10.5, 0.25, 15.5, c.ribbon)
    return out


@family("egg_basket", "Basket of Painted Eggs", "seasonal", "spring", one("egg_basket", "Basket of Painted Eggs"),
        material="CLOTH", mineable="axe",
        recipe=lambda v: shapeless("cottagecoreliving:wicker_basket", "minecraft:egg", "minecraft:wheat"),
        desc="Pastel painted eggs nestled in straw.")
def egg_basket(c, st):
    out = basket(3.5, 4.5, 12.5, 11.5, 4, c.wicker, handle=True, handle_h=5)
    out += B(4, 2.75, 5, 12, 3.75, 11, c.straw, **FLAT)
    out += bumps(4.25, 5.25, 11.75, 10.75, 3.25, c.egg, n=7, size=2, seed=5, h=2.75)
    out += bow(8, 9, 7.4, 8.6, 0.5, c.ribbon_pink)
    return out


@family("bunny_figurine", "Bunny Figurine", "seasonal", "spring", one("bunny_figurine", "Bunny Figurine"),
        material="CERAMIC", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:clay_ball", "minecraft:clay_ball", "minecraft:rabbit_hide"),
        desc="A little porcelain bunny with a pink bow.")
def bunny_figurine(c, st):
    w = c.white_ceramic
    out = rbox(5, 0, 6, 11, 5, 12, w, r=1)
    out += rbox(6, 4.5, 4.5, 10, 8, 8.5, w, r=0.75)
    out += B(6.5, 8, 6, 7.5, 12, 7, w) + B(8.5, 8, 6, 9.5, 12, 7, w)
    out += B(6.75, 8.5, 5.95, 7.25, 11.5, 6, c("sets/pink/ceramic"), s=None, e=None, w=None, u=None, d=None)
    out += B(8.75, 8.5, 5.95, 9.25, 11.5, 6, c("sets/pink/ceramic"), s=None, e=None, w=None, u=None, d=None)
    out += B(7.25, 1, 12, 8.75, 2.5, 13.25, w)
    out += B(6.75, 6.5, 4.45, 7.25, 7, 4.5, c.dark, s=None, e=None, w=None, u=None, d=None)
    out += B(8.75, 6.5, 4.45, 9.25, 7, 4.5, c.dark, s=None, e=None, w=None, u=None, d=None)
    out += B(7.75, 5.75, 4.4, 8.25, 6.25, 4.5, c("sets/pink/ceramic"))
    out += bow(8, 4, 4.4, 5, 0.5, c.ribbon_pink)
    return out


# =========================================================================== summer picnic

@family("picnic_basket", "Picnic Basket", "seasonal", "summer", one("picnic_basket", "Picnic Basket"),
        kind="STORAGE", material="CLOTH", open_=True, interaction="OPEN_STORAGE", mineable="axe",
        recipe=lambda v: shapeless("cottagecoreliving:wicker_basket", "minecraft:bread", "minecraft:apple"),
        desc="A wicker hamper lined with gingham. Holds nine things for the picnic.")
def picnic_basket(c, st):
    out = B(2, 0, 4, 14, 6, 12, c.wicker)
    out += B(1.9, 4.75, 3.9, 14.1, 6, 12.1, c.gingham_red, u=None, d=None)
    for x in (4, 11):
        out += B(x, 0.5, 3.85, x + 1, 6.25, 4, c.leather, s=None)
    if st["open"]:
        out += B(2.5, 5.5, 4.5, 13.5, 6, 11.5, c.gingham_red, **FLAT)
        out += rbox(3.5, 5.5, 5, 8, 7.5, 7.5, c.bread, r=0.5)
        out += B(9, 5.5, 5.5, 11, 7.5, 7.5, c.apple)
        out += disk(10.5, 9.5, 1, 5.5, 10, c.glass_clear)
        out += B(9.75, 5.5, 8.75, 11.25, 7.5, 10.25, c.lemonade)
        out += B(1.75, 6, 11.5, 14.25, 12.5, 12.5, c.wicker)
    else:
        out += B(1.75, 6, 3.75, 14.25, 7, 8.1, c.wicker)
        out += B(1.75, 6, 7.9, 14.25, 7, 12.25, c.wicker)
        out += B(4, 7, 7.5, 5, 9, 8.5, c.leather)
        out += B(11, 7, 7.5, 12, 9, 8.5, c.leather)
        out += B(4, 9, 7.5, 12, 10, 8.5, c.leather)
    return out


@family("lemonade_pitcher", "Lemonade Pitcher", "seasonal", "summer", one("lemonade_pitcher", "Lemonade Pitcher"),
        material="GLASS", render="translucent", mineable=None,
        recipe=lambda v: shapeless("minecraft:glass", "minecraft:sugar", "minecraft:yellow_dye"),
        desc="Fresh lemonade with lemon slices, and a glass poured ready.")
def lemonade_pitcher(c, st):
    out = disk(6, 8, 2.6, 0.25, 6.5, c.lemonade)
    out += plane_z(4.5, 2, 7.5, 5, 8.5, c.lemon_slice)
    out += disk(6, 8, 3, 0, 8, c.glass_clear, u=None)
    out += B(8.75, 2, 7.5, 10, 6.5, 8.5, c.glass_clear)
    out += B(2.5, 7, 7.5, 3.25, 8, 8.5, c.glass_clear)
    out += disk(12, 11, 1.25, 0.25, 3, c.lemonade)
    out += disk(12, 11, 1.5, 0, 4, c.glass_clear, u=None)
    out += plane_z(11.5, 3, 13.5, 5, 9.6, c.lemon_slice)
    return out


@family("fruit_platter", "Summer Fruit Platter", "seasonal", "summer", one("fruit_platter", "Summer Fruit Platter"),
        material="CERAMIC", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:bowl", "minecraft:melon_slice", "minecraft:sweet_berries", "minecraft:apple"),
        desc="Watermelon slices, strawberries and grapes on a white platter.")
def fruit_platter(c, st):
    out = disk(8, 8, 6, 0, 0.75, c.white_ceramic)
    out += box((3, 0.75, 4.5), (8, 3, 6), c.watermelon, rot=("y", 22.5, (5.5, 1.5, 5.25)))
    out += box((3.5, 0.75, 7), (8.5, 3, 8.5), c.watermelon, rot=("y", -22.5, (6, 1.5, 7.75)))
    out += bumps(9, 4, 13, 8, 0.75, c.strawberry_fruit, n=5, size=1.5, seed=7)
    out += B(8.5, 0.75, 9, 11.5, 2.5, 12, c.grapes)
    out += B(9.5, 2.5, 9.5, 11, 3.25, 11, c.grapes)
    return out


@family("sun_hat", "Sun Hat", "seasonal", "summer", set_variants("sun_hat", "Sun Hat"),
        material="CLOTH", mineable=None,
        recipe=lambda v: shapeless("minecraft:wheat", "minecraft:wheat", "minecraft:wheat", S),
        desc="A wide straw hat with a ribbon band and a flower tucked in.")
def sun_hat(c, st):
    out = disk(8, 8, 7, 0, 0.5, c.straw)
    out += disk(8, 8, 3.5, 0.5, 3.5, c.straw)
    out += disk(8, 8, 2.75, 3.5, 4.25, c.straw)
    out += disk(8, 8, 3.6, 0.75, 1.75, c.ribbon)
    out += B(10.25, 1, 3.75, 12.5, 3, 6, c.flowers)
    out += B(9, 0.5, 11, 9.75, 0.6, 15, c.ribbon, d=None)
    out += B(10.25, 0.5, 11, 11, 0.6, 14.5, c.ribbon, d=None)
    return out


JAMS = [(8, 8, "jam_red")], [(5.5, 7, "jam_red"), (10.5, 9, "jam_blue")], \
       [(5, 6.5, "jam_red"), (11, 7, "jam_blue"), (8, 11.5, "jam_orange")]


@family("jam_jars", "Jam Jars", "seasonal", "summer", one("jam_jars", "Jam Jars"),
        material="GLASS", count=3, mineable=None,
        recipe=lambda v: shapeless("minecraft:glass_bottle", "minecraft:sweet_berries", "minecraft:sugar"),
        desc="Homemade jam with gingham lids. Place more to line up a row.")
def jam_jars(c, st):
    out = []
    for cx, cz, jam in JAMS[st["count"] - 1]:
        out += B(cx - 1.75, 0, cz - 1.75, cx + 1.75, 4, cz + 1.75, c(jam))
        out += B(cx - 2, 4, cz - 2, cx + 2, 4.75, cz + 2, c.gingham_red)
        out += B(cx - 1.9, 3.5, cz - 1.9, cx + 1.9, 4, cz + 1.9, c.gingham_red, u=None, d=None)
        out += B(cx - 1.25, 1, cz - 1.8, cx + 1.25, 2.75, cz - 1.75, c.label, s=None, e=None, w=None, u=None, d=None)
    return out


# =========================================================================== autumn

LEAVES = [("red", "Red"), ("orange", "Orange"), ("golden", "Golden")]
LEAF_DYES = {"red": "minecraft:red_dye", "orange": "minecraft:orange_dye", "golden": "minecraft:yellow_dye"}


@family("leaf_pile", "Leaf Pile", "seasonal", "autumn",
        [Variant(f"{k}_leaf_pile", f"{n} Leaf Pile", tex={"pile": f"leaves_{k}"}, leaf=k) for k, n in LEAVES],
        material="PLANT", count=3, walk=True, ambient="LEAVES", mineable="hoe",
        recipe=lambda v: shapeless("#minecraft:leaves", "#minecraft:leaves", LEAF_DYES[v["leaf"]], count=4),
        desc="A rustling heap of autumn leaves. Place more to pile it higher; walk through it!")
def leaf_pile(c, st):
    h = st["count"] * 2.0
    out = B(1, 0, 1, 15, h * 0.5, 15, c.pile)
    out += B(2.5, 0, 3, 13, h * 0.85, 13.5, c.pile)
    out += B(4.5, 0, 4.5, 11.5, h, 11.5, c.pile)
    out += plane_y(0, 0, 16, 16, 0.05, c.pile, shade=True)
    return out


@family("pumpkin_stack", "Pumpkin Stack", "seasonal", "autumn", one("pumpkin_stack", "Pumpkin Stack"),
        material="PLANT", count=3, mineable="axe",
        recipe=lambda v: shapeless("minecraft:pumpkin", "minecraft:pumpkin_seeds"),
        desc="Little pumpkins and gourds. Place more to stack them up.")
def pumpkin_stack(c, st):
    n = st["count"]
    out = rbox(3, 0, 4, 11, 5, 12, c.pumpkin, r=1)
    out += B(6.5, 5, 7.5, 7.5, 6.5, 8.5, c.branch)
    if n >= 2:
        out += rbox(9.5, 0, 9, 14, 3.5, 13.5, c.pumpkin_white, r=0.75)
        out += B(11.5, 3.5, 11, 12.25, 4.5, 11.75, c.branch)
    if n >= 3:
        out += rbox(5, 5, 6, 9, 8, 10, c.pumpkin_green, r=0.75)
        out += B(6.75, 8, 7.75, 7.5, 9, 8.5, c.branch)
    return out


@family("autumn_wreath", "Autumn Wreath", "seasonal", "autumn", one("autumn_wreath", "Autumn Wreath"),
        material="PLANT", mounts=("wall",), mineable=None,
        recipe=lambda v: shapeless("#minecraft:leaves", "#minecraft:leaves", "#minecraft:leaves",
                                   "minecraft:orange_dye", "minecraft:stick"),
        desc="Red and golden leaves with acorns and an orange ribbon.")
def autumn_wreath(c, st):
    return wreath(8, 8, 6, 15, 16, c.leaves_mixed, c.acorns, c.ribbon_orange)


@family("acorn_bowl", "Bowl of Acorns", "seasonal", "autumn", one("acorn_bowl", "Bowl of Acorns"),
        material="WOOD",
        recipe=lambda v: shapeless("minecraft:bowl", "#minecraft:saplings", "#minecraft:saplings"),
        desc="A wooden bowl of acorns and pine cones gathered on a walk.")
def acorn_bowl(c, st):
    wood = "minecraft:block/spruce_planks"
    out = disk(8, 8, 3.5, 0, 1, wood)
    out += ring_xz(8, 8, 5, 1.25, 1, 3.25, wood)
    out += disk(8, 8, 4, 1, 2.75, c.acorns, n=None, s=None, e=None, w=None, d=None)
    out += bumps(5, 5, 11, 11, 2.25, c.acorns, n=5, size=1.75, seed=9)
    out += box((9, 2.5, 5), (11, 5.5, 7), c.branch, rot=("x", 22.5, (10, 3, 6)))
    out += plane_y(2, 9, 7, 14, 3.4, c.leaves_red, shade=True)
    return out


@family("apple_crate", "Apple Crate", "seasonal", "autumn", one("apple_crate", "Apple Crate"),
        material="WOOD",
        recipe=lambda v: shapeless("#minecraft:wooden_slabs", "minecraft:apple", "minecraft:apple", "minecraft:apple"),
        desc="A crate brimming with freshly picked apples.")
def apple_crate(c, st):
    out = crate(2, 3, 14, 13, 6, c.crate)
    out += B(3, 4, 4, 13, 5, 12, c.leaves, **FLAT)
    out += bumps(3, 4, 13, 12, 4.75, c.apple, n=12, size=2.5, seed=11)
    out += B(5, 2, 2.9, 11, 4, 3, c.label, s=None, e=None, w=None, u=None, d=None)
    return out


# =========================================================================== winter

@family("winter_wreath", "Winter Wreath", "seasonal", "winter", one("winter_wreath", "Winter Wreath"),
        material="PLANT", mounts=("wall",), mineable=None,
        recipe=lambda v: shapeless("minecraft:spruce_leaves", "minecraft:spruce_leaves", "minecraft:spruce_leaves",
                                   "minecraft:red_dye", "minecraft:stick"),
        desc="Pine and holly berries with a red velvet bow, dusted with snow.")
def winter_wreath(c, st):
    return wreath(8, 8, 6, 15, 16, c.pine, c.berries, c.ribbon_red,
                  deco=B(4, 12.5, 14.5, 7, 13.5, 15.25, c.snow) + B(9.5, 12.75, 14.5, 12, 13.75, 15.25, c.snow))


@family("mini_snowman", "Mini Snowman", "seasonal", "winter", one("mini_snowman", "Mini Snowman"),
        material="SNOW", mineable="shovel",
        recipe=lambda v: shapeless("minecraft:snow_block", "minecraft:snowball", "minecraft:carrot"),
        desc="A little snowman in a knitted scarf. He never melts indoors.")
def mini_snowman(c, st):
    out = rbox(4, 0, 4, 12, 6, 12, c.snowball, r=1.5)
    out += rbox(5, 5.5, 5, 11, 10, 11, c.snowball, r=1)
    out += rbox(5.75, 9.75, 5.75, 10.25, 13.5, 10.25, c.snowball, r=0.75)
    out += B(5.5, 13.5, 5.5, 10.5, 14, 10.5, c.coal)
    out += B(6.25, 14, 6.25, 9.75, 16, 9.75, c.coal)
    out += B(6.25, 14.25, 6.2, 9.75, 14.75, 9.8, c.ribbon_red, u=None, d=None)
    out += B(7.5, 11.25, 4.25, 8.5, 12.25, 5.75, c.carrot)
    out += B(6.75, 12.25, 5.6, 7.25, 12.75, 5.75, c.coal) + B(8.75, 12.25, 5.6, 9.25, 12.75, 5.75, c.coal)
    out += disk(8, 8, 3.2, 9.25, 10.25, c.scarf)
    out += B(9, 6.5, 4.6, 10.5, 9.5, 5.1, c.scarf)
    for y in (6.5, 8):
        out += B(7.6, y, 4.9, 8.4, y + 0.75, 5, c.coal, s=None, e=None, w=None, u=None, d=None)
    return out


@family("pine_garland", "Pine Garland", "seasonal", "winter", one("pine_garland", "Pine Garland"),
        material="PLANT", mounts=("wall",), walk=True, light=("ALWAYS", 6), mineable=None,
        recipe=lambda v: shapeless("minecraft:spruce_leaves", "minecraft:spruce_leaves", "minecraft:string",
                                   "minecraft:glowstone_dust", count=2),
        desc="A pine swag strung with tiny twinkling lights.")
def pine_garland(c, st):
    return garland(14, 3, 13.5, 16, c.fairy_lights, accent=c.pine, bead=c.berries, thick=2.5)


MUGS = [[(8, 8, "sets/strawberry/ceramic")], [(6, 7, "sets/strawberry/ceramic"), (10.5, 9.5, "white_ceramic")],
        [(5.5, 6, "sets/strawberry/ceramic"), (10.5, 7, "white_ceramic"), (7.5, 11, "sets/fairy/ceramic")]]


@family("hot_cocoa", "Hot Cocoa", "seasonal", "winter", one("hot_cocoa", "Hot Cocoa"),
        material="CERAMIC", count=3, interaction="STEAM", ambient="STEAM", mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:cocoa_beans", "minecraft:sugar", "minecraft:brick"),
        desc="Mugs of cocoa topped with marshmallows. Place more mugs for friends.")
def hot_cocoa(c, st):
    out = []
    for cx, cz, mug in MUGS[st["count"] - 1]:
        out += B(cx - 1.75, 0, cz - 1.75, cx + 1.75, 4, cz + 1.75, c(mug), u=None)
        out += B(cx - 1.5, 3.5, cz - 1.5, cx + 1.5, 3.75, cz + 1.5, c.cocoa, **FLAT)
        out += B(cx + 1.75, 1, cz - 0.4, cx + 2.75, 3.25, cz + 0.4, c(mug))
    return out


@family("snow_globe", "Snow Globe", "seasonal", "winter", one("snow_globe", "Snow Globe"),
        material="GLASS", interaction="SHAKE", render="translucent", mineable=None,
        recipe=lambda v: shapeless("minecraft:glass", "minecraft:snowball", "#minecraft:wooden_slabs"),
        desc="A tiny snowy cottage under glass. Right-click to shake it.")
def snow_globe(c, st):
    out = B(4.5, 0, 4.5, 11.5, 2, 11.5, "minecraft:block/dark_oak_planks")
    out += B(4.4, 1.25, 4.4, 11.6, 1.75, 11.6, c.brass, u=None, d=None)
    out += disk(8, 8, 2.75, 2, 2.75, c.snow)
    out += B(6, 2.75, 7, 8.5, 4.5, 9.25, c("sets/strawberry/paint_light"))
    out += B(5.75, 4.5, 6.75, 8.75, 5.1, 9.5, c("sets/strawberry/roof"))
    out += B(9.25, 2.75, 7.25, 10.25, 6, 8.25, c.pine)
    out += B(9, 2.75, 7, 10.5, 4, 8.5, c.pine)
    out += cross(8, 8, 5, 2.75, 8, c.snow_globe_scene, uv="fit")
    out += disk(8, 8, 3.25, 2, 8.25, c.glass_clear)
    out += disk(8, 8, 2.25, 8.25, 9, c.glass_clear)
    return out


@family("winter_lantern", "Winter Lantern", "seasonal", "winter", one("winter_lantern", "Winter Lantern"),
        material="LANTERN", mounts=("floor", "ceiling"), light=("SWITCH", 14), mineable="pickaxe",
        recipe=lambda v: shapeless("minecraft:lantern", "minecraft:snowball"),
        desc="A dark iron lantern with snow on its cap and a sprig of pine.")
def winter_lantern(c, st):
    y0 = 3 if st["mount"] == "ceiling" else 0
    glass = c.lantern_glass_lit if st["lit"] else c.lantern_glass
    out = lantern(8, 8, y0, c.coal, glass, c.coal, candle=c("sets/strawberry/wax"),
                  flame=c.flame if st["lit"] else None)
    out += B(5.25, y0 + 8, 5.25, 10.75, y0 + 8.75, 10.75, c.snow)
    out += B(4.5, y0 + 7.25, 5.5, 7, y0 + 8.25, 7.5, c.pine)
    out += B(5.25, y0 + 7.75, 5, 6, y0 + 8.5, 5.75, c.berries)
    if st["mount"] == "ceiling":
        out += cross(8, 8, 1, y0 + 8.75, 16, c.chain, uv=(0, 0, 3, 16), shade=True)
    else:
        out += B(7.5, y0 + 8.75, 7.75, 8.5, y0 + 10, 8.25, c.coal)
    return out


# =========================================================================== through the seasons

SEASON_LOOK = {
    "spring": dict(base="leaves", accent="blossom", bow="ribbon_pink"),
    "summer": dict(base="leaves", accent="lemon", bow="ribbon_blue"),
    "autumn": dict(base="leaves_mixed", accent="acorns", bow="ribbon_orange"),
    "winter": dict(base="pine", accent="berries", bow="ribbon_red"),
}


def look(c, st):
    return {k: c(v) for k, v in SEASON_LOOK[st["season"]].items()}


SEASONAL = dict(kind="SEASONAL", seasons=True, interaction="CYCLE_SEASON")


@family("seasonal_wreath", "Seasonal Wreath", "seasonal", "seasons", one("seasonal_wreath", "Seasonal Wreath"),
        material="PLANT", mounts=("wall",), mineable=None,
        recipe=lambda v: shapeless("minecraft:vine", "minecraft:vine", "minecraft:stick", "#minecraft:small_flowers"),
        desc="Blossoms in spring, lemons in summer, acorns in autumn and holly in winter.", **SEASONAL)
def seasonal_wreath(c, st):
    lk = look(c, st)
    deco = []
    if st["season"] == "winter":
        deco = B(4, 12.5, 14.5, 7, 13.5, 15.25, c.snow) + B(9.5, 12.75, 14.5, 12, 13.75, 15.25, c.snow)
    return wreath(8, 8, 6, 15, 16, lk["base"], lk["accent"], lk["bow"], deco=deco)


@family("seasonal_garland", "Seasonal Garland", "seasonal", "seasons", one("seasonal_garland", "Seasonal Garland"),
        material="PLANT", mounts=("wall",), walk=True, mineable=None,
        recipe=lambda v: shapeless("minecraft:vine", "minecraft:vine", "minecraft:string"),
        desc="A garland that dresses itself for the season.", **SEASONAL)
def seasonal_garland(c, st):
    lk = look(c, st)
    return garland(14.5, 3, 14.5, 15.75, lk["base"], accent=lk["accent"], bead=lk["bow"], thick=2.0)


@family("seasonal_centerpiece", "Seasonal Centerpiece", "seasonal", "seasons",
        one("seasonal_centerpiece", "Seasonal Centerpiece"), material="WOOD",
        recipe=lambda v: shapeless("#minecraft:wooden_slabs", "minecraft:candle", "#minecraft:small_flowers"),
        desc="A table centerpiece: tulips, lemons, little pumpkins or pine and candles, by season.", **SEASONAL)
def seasonal_centerpiece(c, st):
    out = B(2, 0, 5, 14, 1, 11, "minecraft:block/oak_planks")
    out += B(2, 1, 5, 14, 2, 5.75, "minecraft:block/oak_planks") + B(2, 1, 10.25, 14, 2, 11, "minecraft:block/oak_planks")
    s = st["season"]
    if s == "spring":
        out += B(2.75, 1, 5.75, 13.25, 1.75, 10.25, c.soil, **FLAT)
        out += cross(5, 8, 5, 1.5, 9, c.tulips)
        out += cross(11, 8, 5, 1.5, 8.5, c.tulips)
        out += bumps(7, 6.5, 9.5, 9.5, 1, c.egg, n=2, size=1.75, seed=13, h=2.25)
    elif s == "summer":
        out += bumps(3, 6, 13, 10, 1, c.lemon, n=6, size=2, seed=14)
        out += cross(8, 8, 5, 1, 8, c.wildflowers)
    elif s == "autumn":
        out += rbox(3, 1, 6, 7, 4, 10, c.pumpkin, r=0.75)
        out += rbox(10, 1, 6.5, 13, 3, 9.5, c.pumpkin_white, r=0.5)
        out += disk(8.5, 8, 1, 1, 6, c.wax_white)
        out += B(8.35, 6, 7.85, 8.65, 6.75, 8.15, c.wick)
        out += plane_y(2.5, 5.5, 13.5, 10.5, 2.1, c.leaves_mixed)
    else:
        out += B(2.5, 1, 5.5, 13.5, 2.5, 10.5, c.pine)
        out += disk(6, 8, 1, 2.5, 8, c("sets/strawberry/wax"))
        out += disk(10, 8, 1, 2.5, 6.5, c("sets/strawberry/wax"))
        out += B(11.5, 2.5, 6, 13, 4.5, 7.5, c.branch)
        out += bumps(3, 6, 5, 10, 2.5, c.berries, n=2, size=1, seed=15)
    return out


@family("seasonal_tree", "Little Seasonal Tree", "seasonal", "seasons", one("seasonal_tree", "Little Seasonal Tree"),
        material="PLANT", mineable="axe",
        recipe=lambda v: shapeless("minecraft:flower_pot", "#minecraft:saplings"),
        desc="A potted tree that blossoms, fruits, turns gold and wears snow through the year.", **SEASONAL)
def seasonal_tree(c, st):
    out = pot(8, 8, 3, 5, c.terracotta, c.soil)
    out += B(7.25, 4.5, 7.25, 8.75, 10, 8.75, c.branch)
    s = st["season"]
    if s == "winter":
        out += box((7.5, 9, 7.5), (8.5, 14, 8.5), c.branch, rot=("z", 22.5, (8, 9, 8)))
        out += box((7.5, 9, 7.5), (8.5, 13.5, 8.5), c.branch, rot=("z", -22.5, (8, 9, 8)))
        out += box((7.5, 9, 7.5), (8.5, 14.5, 8.5), c.branch, rot=("x", 22.5, (8, 9, 8)))
        out += B(5, 12.5, 7, 7, 13.25, 9, c.snow) + B(9.25, 12, 7, 11.25, 12.75, 9, c.snow)
        out += B(7, 13.75, 9.5, 9, 14.5, 11.5, c.snow)
        out += disk(8, 8, 2.5, 4.5, 5, c.snow, n=None, s=None, e=None, w=None, d=None)
    else:
        canopy = {"spring": c.blossom, "summer": c.leaves, "autumn": c.leaves_orange}[s]
        out += rbox(3.5, 9, 3.5, 12.5, 15.5, 12.5, canopy, r=1.25)
        if s == "summer":
            for x, y, z in ((4, 11, 6), (11.5, 12.5, 9), (7, 14, 3.9), (9.5, 10.5, 12.1)):
                out += B(x - 0.75, y - 0.75, z - 0.75, x + 0.75, y + 0.75, z + 0.75, c.apple)
    return out


@family("seasonal_doormat", "Seasonal Doormat", "seasonal", "seasons", one("seasonal_doormat", "Seasonal Doormat"),
        material="CLOTH", mineable=None,
        recipe=lambda v: shapeless("minecraft:wheat", "minecraft:wheat", "minecraft:wheat", "#minecraft:wool_carpets"),
        desc="A woven doormat with a tulip, a sun, a leaf or a snowflake.", **SEASONAL)
def seasonal_doormat(c, st):
    return B(1, 0, 3, 15, 0.75, 13, c(f"doormat_{st['season']}"), u=(c(f"doormat_{st['season']}"), "fit"))
