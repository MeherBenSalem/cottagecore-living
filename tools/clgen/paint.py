"""Texture painters for Cottagecore Living.

Every texture is 16x16 so decorations keep vanilla's texel density. Textures are
requested by name while models are built (see `tex`) and painted once at the end,
so only textures that some model actually uses end up in the jar.

Names:
    sets/<set>/<material>   a material painted in one decorative set's palette
    <name>                  a shared texture (wicker, brass, pages, ...)
    minecraft:block/<name>  a vanilla texture (wood planks, stone, snow, ...)
"""
import math
import random

from .core import ASSETS, MODID, C, Img, darken, lighten, mix, scale, with_alpha

CLEAR = (0, 0, 0, 0)
WHITE = C("#FFFFFF")

# --------------------------------------------------------------------------- the five decorative sets

SETS = ["strawberry", "lavender", "pink", "fairy", "vintage"]

SET_JAVA = {
    "strawberry": "STRAWBERRY_COTTAGE",
    "lavender": "LAVENDER_HOME",
    "pink": "PINK_GARDEN",
    "fairy": "FAIRYCORE",
    "vintage": "VINTAGE_COTTAGE",
}

SET_NAMES = {
    "strawberry": "Strawberry Cottage",
    "lavender": "Lavender Home",
    "pink": "Pink Garden",
    "fairy": "Fairycore",
    "vintage": "Vintage Cottage",
}

# the word used in front of a decoration's name ("Strawberry Teapot")
SET_ADJ = {
    "strawberry": "Strawberry",
    "lavender": "Lavender",
    "pink": "Pink Garden",
    "fairy": "Fairy",
    "vintage": "Vintage",
}

PAL = {
    "strawberry": dict(main="#E5484D", deep="#B3343C", light="#FFD3D3", pale="#FFF1EE", cream="#FFF8F0",
                       accent="#FFFFFF", leaf="#5FA046", leafdark="#3E7B34", seed="#F8DD78", metal="#E14C4C",
                       wax="#F7C6CA", ribbon="#D93F4A", flower="#E5484D", flower2="#FFFFFF", stone="#D9A49A"),
    "lavender": dict(main="#B79DE3", deep="#8364C2", light="#E9E0FA", pale="#F6F2FD", cream="#FBF8EF",
                     accent="#F4EBD0", leaf="#92AD7F", leafdark="#6C8A5B", seed="#FFF4B8", metal="#A88BD6",
                     wax="#E6DAF8", ribbon="#8E6CCB", flower="#9D7BD8", flower2="#C9B3EE", stone="#C2B7D3"),
    "pink": dict(main="#F49DBA", deep="#D9668E", light="#FFDDE8", pale="#FFF2F6", cream="#FFFAF7",
                 accent="#FFFFFF", leaf="#6DB55E", leafdark="#4A8C40", seed="#FFE08A", metal="#F28DB2",
                 wax="#FFD3E1", ribbon="#E5739B", flower="#F27BA5", flower2="#FFC1D6", stone="#E8C2C9"),
    "fairy": dict(main="#9FDCC2", deep="#5FAE8C", light="#DDF5EA", pale="#F1FBF6", cream="#FFF8E6",
                  accent="#C9B3F0", leaf="#6A9C55", leafdark="#4A7A3B", seed="#FFF1A0", metal="#8FD1B8",
                  wax="#E9F7EF", ribbon="#A98BE0", flower="#B79BEA", flower2="#8FC9F0", stone="#9DB79A",
                  mushroom="#DE5448"),
    "vintage": dict(main="#8DB3A5", deep="#5D8576", light="#E2EDE6", pale="#F4F1E7", cream="#F1E3C6",
                    accent="#D69C96", leaf="#7D9862", leafdark="#5B7346", seed="#E9D29B", metal="#B79256",
                    wax="#F3E8D2", ribbon="#C98880", flower="#D69C96", flower2="#F4EEDF", stone="#B8C7BE"),
}


def rng_for(*parts):
    return random.Random("|".join(str(p) for p in parts))


def noise_fill(img, base, rng, amount=0.03, grain=0.0, rib=0.0, sheen=0.0):
    for y in range(img.h):
        for x in range(img.w):
            f = 1.0 + (rng.random() - 0.5) * amount * 2
            if grain:
                f += grain * math.sin(x * 0.9 + math.sin(y * 0.35 + x) * 1.4)
            if rib:
                f += rib if x % 2 == 0 else -rib * 0.6
            if sheen:
                f += sheen * math.sin((x + y) * 0.45)
            img.set(x, y, scale(base, f))


def solid(base, seed, **kw):
    img = Img(16, 16)
    noise_fill(img, C(base), rng_for("solid", seed), kw.pop("amount", 0.03), **kw)
    return img


# --------------------------------------------------------------------------- sprites

STRAWBERRY = ["..gg..", ".gGGg.", "rrggrr", "ryrrry", "rrryrr", ".ryrr.", "..rr.."]
HEART = [".X.X.", "XXXXX", "XXXXX", ".XXX.", "..X.."]
SMALL_HEART = ["X.X", "XXX", ".X."]
TOADSTOOL = ["..rrrr..", ".rwrrwr.", "rrrrrwrr", "rwrrrrrr", "..cccc..", "...cc...", "...cc...", "..cccc.."]
ROSE = [".pPp.", "pPdPp", "PdDdP", "pPdPp", ".pPp."]
DAISY = [".w.", "wyw", ".w."]
LAV_SPRIG = ["..p", ".pP", "pPg", ".gp", "Pg.", "g..", "g..", "g.."]
BEE = [".ww.", "ybyb", "ybyb", ".bb."]
CARROT = ["..gg", ".gg.", "oo..", "oo..", "o..."]
TULIP = ["r.r.r", "rrrrr", "rrrrr", ".rrr.", "..g..", "g.g.g", ".ggg.", "..g.."]
SUN = ["y.y.y", ".yyy.", "yyyyy", ".yyy.", "y.y.y"]
LEAF = ["..o", ".oo", "ooo", "oo.", "b.."]
SNOWFLAKE = ["w.w.w", ".www.", "wwwww", ".www.", "w.w.w"]
ACORN = [".bb.", "bBBb", "oooo", "oooo", ".oo."]
APPLE = [".g..", "rrr.", "rRrr", "rrrr", ".rr."]


def stamp(img, x, y, rows, pal):
    img.stamp(x, y, rows, {k: (C(v) if v is not None else None) for k, v in pal.items()})


# --------------------------------------------------------------------------- set materials

def mat_ceramic(s):
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("ceramic", s)
    base = C(p["main"]) if s != "vintage" else C(p["cream"])
    for y in range(16):
        for x in range(16):
            c = scale(base, 1.05 - y * 0.006 + (rng.random() - 0.5) * 0.02)
            if x in (3, 4):
                c = lighten(c, 0.3 if x == 3 else 0.15)
            img.set(x, y, c)
    return img


def mat_glaze(s):
    """A pale glaze with the set's little motif dotted over it."""
    p = PAL[s]
    img = Img(16, 16)
    base = C(p["pale"]) if s != "vintage" else C("#F7F0E1")
    noise_fill(img, base, rng_for("glaze", s), 0.015)
    for x in range(16):
        img.set(x, 0, C(p["main"]))
        img.set(x, 15, C(p["main"]))
    small_motif(img, s, 2, 3)
    small_motif(img, s, 10, 9)
    for x, y in ((11, 3), (4, 11), (14, 13), (7, 6)):
        img.set(x, y, mix(p["main"], base, 0.4))
    for y in range(16):
        img.set(3, y, lighten(img.get(3, y), 0.25))
    return img


def small_motif(img, s, x, y):
    p = PAL[s]
    if s == "strawberry":
        stamp(img, x, y, ["..g.", ".rr.", "rryr", ".rr."], dict(r=p["main"], y=p["seed"], g=p["leaf"]))
    elif s == "lavender":
        stamp(img, x, y, [".p..", "pPp.", ".g.p", ".gPp", ".g.."], dict(p=p["main"], P=p["deep"], g=p["leaf"]))
    elif s == "pink":
        stamp(img, x, y, [".p.", "pyp", ".p.", ".g."], dict(p=p["flower"], y=p["seed"], g=p["leaf"]))
    elif s == "fairy":
        stamp(img, x, y, [".rr.", "rwrr", ".cc.", ".cc."], dict(r=p["mushroom"], w="#FFFFFF", c="#F6EBD5"))
    else:
        stamp(img, x, y, [".rr.", "rRrr", ".rr.", "g..g"], dict(r=p["accent"], R=darken(p["accent"], 0.15), g=p["leaf"]))


def mat_fabric(s):
    return solid(PAL[s]["main"], "fabric" + s, amount=0.035, rib=0.012, sheen=0.02)


def mat_fabric_light(s):
    return solid(PAL[s]["light"], "fabricl" + s, amount=0.03, rib=0.01)


def mat_print(s):
    """The set's signature fabric: gingham, floral or toadstool print."""
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("print", s)
    if s in ("strawberry", "lavender"):
        main = C(p["main"])
        for y in range(16):
            for x in range(16):
                a, b = (x // 4) % 2, (y // 4) % 2
                c = darken(main, 0.05) if a and b else (mix(main, WHITE, 0.5) if a or b else C("#FFFDFB"))
                img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.03))
        return img
    if s == "pink":
        noise_fill(img, C(p["light"]), rng, 0.02, rib=0.008)
        for cx, cy in ((3, 3), (11, 11), (11, 2), (3, 12)):
            stamp(img, cx - 2, cy - 2, ROSE, dict(p=p["flower2"], P=p["flower"], d=p["deep"], D=darken(p["deep"], 0.1)))
        for x, y in ((7, 7), (15, 7), (7, 15), (0, 7)):
            img.set(x, y, C(p["leaf"]))
        return img
    if s == "fairy":
        noise_fill(img, C(p["main"]), rng, 0.02, rib=0.008)
        for cx, cy in ((2, 2), (10, 10)):
            stamp(img, cx, cy, ["rrr", "rwr", ".c."], dict(r=p["mushroom"], w="#FFFFFF", c="#FFF6E2"))
        for x, y in ((11, 3), (4, 12), (14, 7), (7, 7)):
            img.set(x, y, C(p["accent"]))
        return img
    # vintage: cream with faded rose sprigs and a sage check
    noise_fill(img, C(p["cream"]), rng, 0.02, rib=0.008)
    for i in range(16):
        img.set(i, 7, mix(p["main"], p["cream"], 0.45))
        img.set(7, i, mix(p["main"], p["cream"], 0.45))
    for cx, cy in ((2, 2), (10, 10)):
        stamp(img, cx, cy, [".r.", "rRr", "gr."], dict(r=p["accent"], R=darken(p["accent"], 0.2), g=p["leaf"]))
    return img


def mat_paint(s):
    return solid(PAL[s]["main"], "paint" + s, amount=0.02, grain=0.018)


def mat_paint_light(s):
    return solid(PAL[s]["cream"], "paintl" + s, amount=0.02, grain=0.015)


def mat_paint_deep(s):
    return solid(PAL[s]["deep"], "paintd" + s, amount=0.02, grain=0.018)


def mat_enamel(s):
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("enamel", s)
    base = C(p["metal"])
    for y in range(16):
        for x in range(16):
            c = scale(base, 1.04 - y * 0.008 + (rng.random() - 0.5) * 0.02)
            if x == 4 or (x == 5 and y % 3):
                c = lighten(c, 0.3)
            img.set(x, y, c)
    if s == "vintage":  # galvanised zinc with a few spots of wear
        for x, y in ((9, 4), (12, 11), (2, 13), (10, 12)):
            img.set(x, y, mix(base, "#8C8C84", 0.5))
    return img


def mat_wax(s):
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("wax", s)
    for y in range(16):
        for x in range(16):
            c = scale(p["wax"], 1.03 + (rng.random() - 0.5) * 0.025 - (0.04 if y < 1 else 0))
            if x in (2, 3):
                c = lighten(c, 0.18)
            img.set(x, y, c)
    for x in (5, 11):  # drips
        img.set(x, 0, lighten(p["wax"], 0.25))
        img.set(x, 1, lighten(p["wax"], 0.18))
    return img


def cover(base, seed, band):
    img = Img(16, 16)
    rng = rng_for("cover", seed)
    noise_fill(img, C(base), rng, 0.03, rib=0.008)
    for x in range(16):
        img.set(x, 3, C(band))
        img.set(x, 12, C(band))
    for y in range(16):
        img.set(0, y, darken(base, 0.12))
        img.set(15, y, darken(base, 0.12))
    return img


def mat_cover_a(s):
    return cover(PAL[s]["main"], "a" + s, "#E8C46A")


def mat_cover_b(s):
    p = PAL[s]
    return cover(p["leaf"] if s != "lavender" else p["accent"], "b" + s, "#E8C46A")


def mat_cover_c(s):
    p = PAL[s]
    return cover(p["deep"], "c" + s, p["cream"])


def mat_glass(s):
    p = PAL[s]
    img = Img(16, 16)
    tint = C(p["light"], 120)
    for y in range(16):
        for x in range(16):
            img.set(x, y, tint)
    edge = C(p["light"], 190)
    for i in range(16):
        img.set(i, 0, edge)
        img.set(i, 15, edge)
        img.set(0, i, edge)
        img.set(15, i, edge)
    for i, (x, y) in enumerate(((3, 2), (3, 3), (3, 4), (4, 5), (12, 9), (12, 10))):
        img.set(x, y, C("#FFFFFF", 220))
    return img


def mat_ribbon(s):
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("ribbon", s)
    for y in range(16):
        for x in range(16):
            f = 1.0 + 0.07 * math.sin(y * 0.8 + x * 0.15) + (rng.random() - 0.5) * 0.02
            img.set(x, y, scale(p["ribbon"], f))
    return img


def mat_roof(s):
    """Scalloped shingles."""
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("roof", s)
    base = C(p["main"]) if s != "fairy" else C(p["mushroom"])
    for y in range(16):
        for x in range(16):
            row = y // 4
            off = 2 if row % 2 else 0
            lx = (x + off) % 4
            ly = y % 4
            c = base
            if ly == 3 or (ly == 2 and lx in (0, 3)):
                c = darken(base, 0.2)
            elif ly == 0:
                c = lighten(base, 0.12)
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.04))
    if s == "fairy":
        for x, y in ((2, 1), (9, 5), (13, 10), (5, 13)):
            img.set(x, y, WHITE)
    return img


def mat_stone(s):
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("stone", s)
    base = C(p["stone"])
    for y in range(16):
        for x in range(16):
            n = math.sin(x * 1.3 + y * 0.7) * 0.03 + math.sin(x * 0.4 - y * 1.1) * 0.03
            img.set(x, y, scale(base, 1 + n + (rng.random() - 0.5) * 0.06))
    if s == "fairy":  # moss
        for _ in range(22):
            x, y = rng.randrange(16), rng.randrange(16)
            img.set(x, y, mix(img.get(x, y), "#6B9A4C", 0.7))
    if s == "vintage":  # verdigris streaks
        for x in range(0, 16, 5):
            for y in range(rng.randrange(4, 10)):
                img.set(x, y, mix(img.get(x, y), "#77B9A4", 0.6))
    return img


def mat_flowers(s):
    """Top view of a cluster of the set's flowers (cut-out)."""
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("flowers", s)
    for _ in range(26):
        x, y = rng.randrange(1, 15), rng.randrange(1, 15)
        img.set(x, y, C(p["leaf"]) if rng.random() < 0.6 else C(p["leafdark"]))
    if s == "strawberry":
        for cx, cy in ((4, 4), (11, 5), (7, 11)):
            stamp(img, cx - 2, cy - 2, ROSE, dict(p=p["flower"], P=p["deep"], d=darken(p["deep"], 0.1), D=darken(p["deep"], 0.25)))
        for cx, cy in ((12, 12), (2, 11), (8, 2)):
            stamp(img, cx - 1, cy - 1, DAISY, dict(w="#FFFFFF", y=p["seed"]))
    elif s == "lavender":
        for _ in range(30):
            x, y = rng.randrange(1, 15), rng.randrange(1, 15)
            img.set(x, y, C(p["flower"]) if rng.random() < 0.5 else C(p["deep"]))
    elif s == "pink":
        for cx, cy in ((4, 4), (11, 4), (5, 11), (12, 11)):
            stamp(img, cx - 2, cy - 2, ROSE, dict(p=p["flower2"], P=p["flower"], d=p["deep"], D=darken(p["deep"], 0.1)))
    elif s == "fairy":
        for _ in range(9):
            x, y = rng.randrange(2, 14), rng.randrange(2, 14)
            col = p["flower"] if rng.random() < 0.6 else p["flower2"]
            stamp(img, x - 1, y - 1, DAISY, dict(w=col, y=p["seed"]))
    else:
        for cx, cy in ((5, 5), (11, 10)):
            stamp(img, cx - 2, cy - 2, ROSE, dict(p=lighten(p["accent"], 0.3), P=p["accent"], d=darken(p["accent"], 0.1), D=darken(p["accent"], 0.25)))
        for _ in range(14):
            x, y = rng.randrange(1, 15), rng.randrange(1, 15)
            img.set(x, y, C("#FFFDF5"))
    return img


def mat_bouquet(s):
    """Side view of the set's flowers on stems (cut-out, for crossed planes)."""
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("bouquet", s)
    stems = [2, 5, 8, 11, 13]
    for i, x in enumerate(stems):
        top = 3 + (i * 3) % 4
        for y in range(top + 2, 16):
            img.set(x + (1 if y < 8 and i % 2 else 0), y, C(p["leaf"]) if y % 3 else C(p["leafdark"]))
        if i % 2 == 0:
            img.set(x + 1, top + 6, C(p["leaf"]))
            img.set(x + 2, top + 5, C(p["leaf"]))
    if s == "strawberry":
        for i, x in enumerate(stems):
            top = 3 + (i * 3) % 4
            if i % 2 == 0:
                stamp(img, x - 1, top - 1, [".rr.", "rRrr", "rrRr", ".rr."], dict(r=p["flower"], R=p["deep"]))
            else:
                stamp(img, x - 1, top, DAISY, dict(w="#FFFFFF", y=p["seed"]))
    elif s == "lavender":
        for i, x in enumerate(stems):
            top = 1 + (i * 3) % 4
            for y in range(top, top + 7):
                img.set(x, y, C(p["flower"]) if y % 2 else C(p["deep"]))
                if y % 2 == 0:
                    img.set(x - 1, y, C(p["flower2"]))
                    img.set(x + 1, y, C(p["flower"]))
    elif s == "pink":
        for i, x in enumerate(stems):
            top = 3 + (i * 3) % 4
            stamp(img, x - 2, top - 2, [".pPp.", "pPPPp", "pPdPp", ".ppp."], dict(p=p["flower2"], P=p["flower"], d=p["deep"]))
    elif s == "fairy":
        for i, x in enumerate(stems):
            top = 3 + (i * 3) % 4
            col = p["flower"] if i % 2 else p["flower2"]
            # drooping bells
            img.set(x, top, C(p["leaf"]))
            img.set(x + 1, top, C(p["leaf"]))
            stamp(img, x + 1, top + 1, ["b", "B", "b"], dict(b=col, B=darken(col, 0.15)))
            stamp(img, x - 1, top + 2, ["b", "B"], dict(b=col, B=darken(col, 0.15)))
    else:
        for i, x in enumerate(stems):
            top = 3 + (i * 3) % 4
            if i % 2 == 0:
                stamp(img, x - 1, top - 1, [".rr.", "rRrr", ".rr."], dict(r=p["accent"], R=darken(p["accent"], 0.2)))
            else:
                for dx, dy in ((0, 0), (-1, 1), (1, 1), (0, 2), (1, -1)):
                    img.set(x + dx, top + dy, C("#FFFDF5"))
    return img


def mat_motif(s):
    """A 16x16 picture of the set's motif on linen (embroidery hoops, plates, rocks)."""
    p = PAL[s]
    img = Img(16, 16)
    noise_fill(img, C("#FBF6EC"), rng_for("linen", s), 0.025, rib=0.01)
    if s == "strawberry":
        rows = ["....gGg.....", "...gGgGg....", "..rrrgrrr...", ".rrryrrrrr..", "rrrrrrryrr..", "ryrrrrrrrr..",
                "rrrrryrrr...", ".rryrrrrr...", "..rrrryr....", "...rrrr.....", "....rr......"]
        stamp(img, 3, 2, rows, dict(r=p["main"], y=p["seed"], g=p["leaf"], G=p["leafdark"]))
    elif s == "lavender":
        for i, x in enumerate((5, 8, 11)):
            for y in range(3 + i % 2, 9 + i % 2):
                img.set(x, y, C(p["main"]) if y % 2 else C(p["deep"]))
                img.set(x - 1 + (y % 2) * 2, y, C(p["flower2"]))
            for y in range(9 + i % 2, 14):
                img.set(x + (8 - x) * (y - 9) // 6, y, C(p["leaf"]))
        stamp(img, 6, 12, ["rrrr", ".rr."], dict(r=p["ribbon"]))
    elif s == "pink":
        rows = ["...pppp...", "..pPPPPp..", ".pPdddPPp.", ".pPdDDdPp.", ".pPdDdPPp.", "..pPPPPp..", "...pppp...",
                "....g.....", "..ggg..gg.", "...g.gg...", "....g....."]
        stamp(img, 3, 2, rows, dict(p=p["flower2"], P=p["flower"], d=p["deep"], D=darken(p["deep"], 0.15), g=p["leaf"]))
    elif s == "fairy":
        rows = ["...rrrrrr...", "..rrwrrrwr..", ".rrrrrrrrrr.", ".rwrrrwrrrr.", "rrrrrrrrrwrr", "....cccc....",
                "....cCcc....", "....cccc....", "...ccccCc...", "..gg.gg.gg.."]
        stamp(img, 2, 3, rows, dict(r=p["mushroom"], w="#FFFFFF", c="#F8EFD9", C="#E6D8BC", g=p["leaf"]))
        for x, y in ((2, 2), (13, 1), (14, 5)):
            img.set(x, y, C(p["seed"]))
    else:
        rows = ["..rr..rr..", ".rRrr.rRr.", ".rrRr.rrr.", "..rr..rr..", ".g..rr..g.", "gg.rRrr.gg", "g..rrRr..g",
                "....rr....", "...gggg...", "....bb....", "...b..b..."]
        stamp(img, 3, 2, rows, dict(r=p["accent"], R=darken(p["accent"], 0.2), g=p["leaf"], b=p["ribbon"]))
    return img


def mat_rock(s):
    """A painted pebble: set colour with its motif painted on top."""
    p = PAL[s]
    img = Img(16, 16)
    rng = rng_for("rock", s)
    base = {"strawberry": p["main"], "lavender": p["light"], "pink": p["light"], "fairy": p.get("mushroom", "#DE5448"),
            "vintage": p["cream"]}[s]
    noise_fill(img, C(base), rng, 0.04)
    if s == "strawberry":
        for x, y in ((2, 3), (6, 2), (10, 4), (13, 2), (4, 7), (9, 8), (13, 7), (2, 11), (7, 12), (11, 12)):
            img.set(x, y, C(p["seed"]))
    elif s == "fairy":
        for cx, cy in ((3, 3), (11, 4), (6, 10), (13, 11)):
            img.ellipse(cx, cy, 1.4, 1.2, WHITE)
    else:
        small_motif(img, s, 2, 2)
        small_motif(img, s, 9, 8)
    return img


def mat_metal(s):
    """Lantern frames and chime tubes."""
    p = PAL[s]
    base = {"strawberry": "#F4F1EE", "lavender": "#EDE8F7", "pink": "#F7E9EE", "fairy": "#E5D7A6",
            "vintage": "#B79256"}[s]
    img = Img(16, 16)
    rng = rng_for("metal", s)
    for y in range(16):
        for x in range(16):
            c = scale(base, 1.04 - y * 0.01 + (rng.random() - 0.5) * 0.03)
            if x % 8 == 2:
                c = lighten(c, 0.3)
            img.set(x, y, c)
    return img


SET_MATERIALS = {
    "ceramic": mat_ceramic, "glaze": mat_glaze, "fabric": mat_fabric, "fabric_light": mat_fabric_light,
    "print": mat_print, "paint": mat_paint, "paint_light": mat_paint_light, "paint_deep": mat_paint_deep,
    "enamel": mat_enamel, "wax": mat_wax, "cover_a": mat_cover_a, "cover_b": mat_cover_b, "cover_c": mat_cover_c,
    "glass": mat_glass, "ribbon": mat_ribbon, "roof": mat_roof, "stone": mat_stone, "flowers": mat_flowers,
    "bouquet": mat_bouquet, "motif": mat_motif, "rock": mat_rock, "metal": mat_metal,
}

# --------------------------------------------------------------------------- shared materials


def gradient(top, bottom, seed, sparkle=None, noise=0.02):
    img = Img(16, 16)
    rng = rng_for("grad", seed)
    for y in range(16):
        for x in range(16):
            c = scale(mix(top, bottom, y / 15), 1 + (rng.random() - 0.5) * noise)
            if sparkle and (x * 7 + y * 3) % 23 == 0:
                c = lighten(c, sparkle)
            img.set(x, y, c)
    return img


def t_brass():
    img = gradient("#F6DE98", "#C99A45", "brass", sparkle=0.45)
    for i in range(16):
        x = (i + 5) % 16
        img.set(x, i, lighten(img.get(x, i), 0.22))
    return img


def t_silver():
    img = gradient("#F6F8FB", "#AEB8C6", "silver", sparkle=0.5)
    for y in range(16):
        img.set(4, y, lighten(img.get(4, y), 0.35))
    return img


def t_iron():
    return gradient("#C9CDD2", "#8E959D", "iron", sparkle=0.3)


def t_galvanized():
    img = gradient("#D5D9DA", "#A7AEB0", "galv", noise=0.05)
    rng = rng_for("galvspots")
    for _ in range(14):
        x, y = rng.randrange(16), rng.randrange(16)
        img.set(x, y, lighten(img.get(x, y), 0.25))
    return img


def t_pages():
    img = Img(16, 16)
    rng = rng_for("pages")
    for y in range(16):
        for x in range(16):
            c = C("#FBF5E6") if y % 2 else C("#EFE6D0")
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.03))
    return img


def t_page_text():
    """An open page with lines of 'text'."""
    img = Img(16, 16)
    noise_fill(img, C("#FCF7EA"), rng_for("pagetext"), 0.02)
    rng = rng_for("words")
    for y in range(2, 15, 2):
        x = 2
        while x < 14:
            ln = rng.randrange(1, 4)
            for i in range(ln):
                if x + i < 14:
                    img.set(x + i, y, C("#9A8C7A"))
            x += ln + 1
    return img


def t_wicker():
    img = Img(16, 16)
    rng = rng_for("wicker")
    light, dark = C("#E0B97A"), C("#B5874C")
    for y in range(16):
        for x in range(16):
            band = (y // 2) % 2
            weave = ((x + (2 if band else 0)) // 2) % 2
            c = light if weave else mix(light, dark, 0.55)
            if y % 2 == 1:
                c = darken(c, 0.08)
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.06))
    return img


def t_straw():
    img = Img(16, 16)
    rng = rng_for("straw")
    for y in range(16):
        for x in range(16):
            c = C("#EDD08A") if (x + y // 3) % 3 else C("#D4B064")
            if y % 4 == 3:
                c = darken(c, 0.12)
            img.set(x, y, scale(c, 1 + (rng.random() - 0.5) * 0.08))
    return img


def t_linen():
    return solid("#F5EEDF", "linen", amount=0.025, rib=0.012)


def t_lace():
    img = Img(16, 16)
    w, s = C("#FFFDFB"), C("#F1E9E4")
    for y in range(16):
        for x in range(16):
            hole = (x % 4 == 1 and y % 4 == 1)
            ring = (abs(x % 4 - 1.5) + abs(y % 4 - 1.5)) < 1.6
            if not hole:
                img.set(x, y, w if ring else s)
    for x in range(16):
        if x % 4 in (0, 3):
            img.set(x, 15, CLEAR)
    return img


def t_soil():
    img = Img(16, 16)
    noise_fill(img, C("#6B4A33"), rng_for("soil"), 0.12)
    return img


def t_flame():
    img = Img(16, 16)
    rows = ["......yy........", ".....yyy........", ".....yWyy.......", "....yyWWy.......", "....yWWWy.......",
            "....oyWWyo......", "....oyWyyo......", ".....oyyo.......", "......oo........"]
    stamp(img, 2, 3, rows, dict(y="#FFD966", W="#FFFBE6", o="#FF9A3C"))
    return img


def t_wick():
    return solid("#3A2C25", "wick", amount=0.05)


def t_string():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#EDE3CF") if (x + y) % 3 else C("#D3C4A6"))
    return img


def t_rope():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#C9A770") if (x + y) % 4 < 2 else C("#A8864F"))
    return img


def t_chain():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            ring = (y % 4 in (0, 3)) if (x % 4 in (1, 2)) else True
            img.set(x, y, C("#7C8188") if ring else C("#4E5258"))
    return img


def t_leaves():
    img = Img(16, 16)
    rng = rng_for("leaves")
    for y in range(16):
        for x in range(16):
            r = rng.random()
            if r < 0.85:
                img.set(x, y, scale("#6FA552" if r < 0.5 else "#4F8640", 1 + (rng.random() - 0.5) * 0.1))
    return img


def t_foliage():
    """Loose leaves on stems (cut-out side view)."""
    img = Img(16, 16)
    rng = rng_for("foliage")
    for i, x in enumerate((2, 5, 8, 11, 14)):
        for y in range(4 + (i * 2) % 5, 16):
            img.set(x, y, C("#4F8640"))
        for k in range(3):
            y = 5 + k * 3 + (i % 2)
            if y < 15:
                img.set(x - 1, y, C("#6FA552"))
                img.set(x + 1, y - 1, C("#7DB55E"))
    return img


def t_trailing(base="#5E9443", flower=None, seed="trail"):
    """Vines hanging down (cut-out)."""
    img = Img(16, 16)
    rng = rng_for(seed)
    for i, x in enumerate((1, 4, 7, 10, 13)):
        length = 9 + (i * 5) % 7
        for y in range(0, length):
            xx = x + (1 if (y // 3) % 2 else 0)
            img.set(xx, y, C(base))
            if y % 2 == 0:
                img.set(xx + 1, y, lighten(base, 0.15))
            if flower and y % 4 == 3:
                img.set(xx - 1, y, C(flower))
    return img


def t_clock_face():
    img = Img(16, 16)
    img.ellipse(8, 8, 8, 8, C("#FFFAF0"))
    for y in range(16):
        for x in range(16):
            if img.get(x, y)[3] and (x - 7.5) ** 2 + (y - 7.5) ** 2 > 7.2 ** 2:
                img.set(x, y, C("#E8DCC3"))
    for h in range(12):
        a = math.radians(h * 30)
        x = 7.5 + math.sin(a) * 6
        y = 7.5 - math.cos(a) * 6
        img.set(int(round(x)), int(round(y)), C("#5B4636") if h % 3 == 0 else C("#B3A28C"))
    return img


def t_clock_hands(hour):
    img = Img(16, 16)
    # minute hand points at twelve
    for y in range(2, 8):
        img.set(7, y, C("#3B2E25"))
    a = math.radians(hour * 30)
    for i in range(1, 9):
        r = i * 0.5
        x = 7.5 + math.sin(a) * r
        y = 7.5 - math.cos(a) * r
        img.set(int(math.floor(x)), int(math.floor(y)), C("#3B2E25"))
    img.set(7, 7, C("#C99A45"))
    img.set(8, 7, C("#C99A45"))
    img.set(7, 8, C("#C99A45"))
    img.set(8, 8, C("#C99A45"))
    return img


def t_mirror():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            c = mix("#F2F9FF", "#B5D0E8", (x + y) / 30)
            d = (x + y) % 16
            if d in (5, 6) or d == 10:
                c = lighten(c, 0.55 if d != 10 else 0.35)
            img.set(x, y, c)
    return img


def t_dark():
    return solid("#2E2421", "dark", amount=0.06)


def t_glass_clear():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#EAF6FF", 70))
    edge = C("#F4FBFF", 170)
    for i in range(16):
        img.set(i, 0, edge)
        img.set(i, 15, edge)
        img.set(0, i, edge)
        img.set(15, i, edge)
    for x, y in ((3, 2), (3, 3), (3, 4), (4, 5), (12, 10), (12, 11)):
        img.set(x, y, C("#FFFFFF", 230))
    return img


def t_lantern_glass(lit):
    img = Img(16, 16)
    inner = C("#FFE8A8", 200) if lit else C("#E9EEF2", 90)
    for y in range(16):
        for x in range(16):
            img.set(x, y, inner if not lit else mix(inner, "#FFF6D8", max(0, 1 - abs(y - 9) / 8)))
    for i in range(16):
        img.set(i, 0, C("#3D3A38"))
        img.set(i, 15, C("#3D3A38"))
        img.set(0, i, C("#3D3A38"))
        img.set(15, i, C("#3D3A38"))
    for x, y in ((3, 3), (3, 4), (4, 5)):
        img.set(x, y, C("#FFFFFF", 230))
    return img


def t_glow():
    return gradient("#FFF6C8", "#FFD36B", "glow", sparkle=0.6)


def t_cream():
    return solid("#FFF6E4", "cream", amount=0.02)


def t_white_ceramic():
    img = Img(16, 16)
    rng = rng_for("whiteceramic")
    for y in range(16):
        for x in range(16):
            c = scale("#FBF8F4", 1.02 - y * 0.004 + (rng.random() - 0.5) * 0.015)
            if x in (3, 4):
                c = lighten(c, 0.5)
            img.set(x, y, c)
    return img


def t_terracotta():
    return solid("#C7714F", "terracotta", amount=0.05)


# ---- produce and little things

def t_apple():
    img = Img(16, 16)
    noise_fill(img, C("#D93A35"), rng_for("apple"), 0.05)
    for x, y in ((3, 3), (4, 3), (3, 4), (10, 9), (11, 9)):
        img.set(x, y, C("#F57C6B"))
    for x, y in ((8, 1), (2, 12), (13, 5)):
        img.set(x, y, C("#F2C14B"))
    return img


def t_bread():
    img = Img(16, 16)
    noise_fill(img, C("#D9A05B"), rng_for("bread"), 0.06)
    for x in range(16):
        if x % 5 == 2:
            for y in range(16):
                img.set(x, y, C("#F2D49B"))
    return img


def t_yarn(col):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, scale(col, 1.08 if (x + 2 * y) % 4 < 2 else 0.9))
    return img


def t_egg():
    img = Img(16, 16)
    cols = ["#F7EEDB", "#DDEFF4", "#F6D9E2", "#E7DDF6", "#E9F2D5"]
    for y in range(16):
        for x in range(16):
            img.set(x, y, C(cols[((x // 4) + (y // 4) * 2) % 5]))
    rng = rng_for("eggspeck")
    for _ in range(14):
        x, y = rng.randrange(16), rng.randrange(16)
        img.set(x, y, darken(img.get(x, y), 0.15))
    return img


def t_mushroom_cap(col, spots=True):
    img = Img(16, 16)
    noise_fill(img, C(col), rng_for("cap", col), 0.04)
    if spots:
        for cx, cy in ((3, 3), (11, 4), (6, 10), (13, 12), (1, 13)):
            img.set(cx, cy, WHITE)
            img.set(cx + 1, cy, WHITE)
    return img


def t_stem():
    return solid("#F6EEDC", "stem", amount=0.03)


def t_strawberry_fruit():
    img = Img(16, 16)
    noise_fill(img, C("#E0373E"), rng_for("berry"), 0.05)
    for y in range(0, 16, 3):
        for x in range((y // 3) % 2 * 2, 16, 4):
            img.set(x, y, C("#F8DD78"))
    return img


def t_lavender_bunch():
    img = Img(16, 16)
    rng = rng_for("lavbunch")
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#9D7BD8") if rng.random() < 0.6 else C("#7D5DBB"))
    return img


def t_linens():
    img = Img(16, 16)
    cols = ["#FFFFFF", "#F6D9E2", "#DDEFF4", "#F7EEDB"]
    for y in range(16):
        c = cols[(y // 4) % 4]
        for x in range(16):
            img.set(x, y, scale(c, 0.95 if y % 4 == 3 else 1.0))
    return img


def t_honey():
    img = gradient("#FFD36B", "#E59B2C", "honey", sparkle=0.4)
    return img


def t_buttons():
    img = Img(16, 16)
    cols = ["#F49DBA", "#9FDCC2", "#B79DE3", "#F8DD78", "#E5484D", "#8FC9F0"]
    rng = rng_for("buttons")
    for y in range(16):
        for x in range(16):
            img.set(x, y, C(cols[rng.randrange(len(cols))]))
    for y in range(1, 16, 3):
        for x in range(1, 16, 3):
            img.set(x, y, darken(img.get(x, y), 0.35))
    return img


def t_spools():
    img = Img(16, 16)
    cols = ["#E5484D", "#F49DBA", "#B79DE3", "#9FDCC2", "#F8DD78"]
    for y in range(16):
        for x in range(16):
            img.set(x, y, scale(cols[(x // 3) % 5], 1.05 if y % 2 else 0.92))
    return img


def t_wildflowers():
    img = Img(16, 16)
    rng = rng_for("wild")
    for i, x in enumerate(range(1, 16, 2)):
        top = 2 + rng.randrange(5)
        for y in range(top + 1, 16):
            img.set(x, y, C("#5E9443"))
        col = ["#F7D44C", "#FFFFFF", "#8FC9F0", "#F49DBA", "#B79DE3"][i % 5]
        stamp(img, x - 1, top - 1, DAISY, dict(w=col, y="#F7B53C"))
    return img


def t_cocoa():
    img = Img(16, 16)
    noise_fill(img, C("#7B4A2E"), rng_for("cocoa"), 0.04)
    for cx, cy in ((4, 4), (10, 6), (6, 11)):
        img.rect(cx, cy, cx + 3, cy + 3, C("#FFF9F2"))
    return img


def t_lemonade():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#FFE98A", 200))
    for cx, cy in ((4, 5), (11, 10)):
        img.ellipse(cx, cy, 2.5, 2.5, C("#FFF6C2", 230))
    return img


def t_lemon_slice():
    img = Img(16, 16)
    img.ellipse(8, 8, 8, 8, C("#FFF3A1"))
    img.ellipse(8, 8, 6.5, 6.5, C("#FFE15C"))
    for a in range(0, 360, 60):
        r = math.radians(a)
        for k in range(1, 6):
            img.set(int(8 + math.cos(r) * k), int(8 + math.sin(r) * k), C("#FFF6C8"))
    return img


def t_watermelon():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            c = C("#F25C6B")
            if y >= 13:
                c = C("#3F8A3A") if y >= 14 else C("#E8F5D0")
            img.set(x, y, c)
    for x, y in ((3, 4), (8, 3), (12, 6), (5, 8), (10, 10)):
        img.set(x, y, C("#2B2120"))
    return img


def t_grapes():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#8E5BB5") if (x + y) % 3 else C("#B88BD8"))
    return img


def t_jam(col):
    img = Img(16, 16)
    noise_fill(img, C(col), rng_for("jam", col), 0.05)
    for y in range(16):
        img.set(3, y, lighten(col, 0.35))
    return img


def t_label():
    img = Img(16, 16)
    noise_fill(img, C("#FFF8E8"), rng_for("label"), 0.02)
    for x in range(16):
        img.set(x, 0, C("#E5484D"))
        img.set(x, 15, C("#E5484D"))
    for x in range(4, 12):
        img.set(x, 7, C("#9A8C7A"))
    return img


def t_pumpkin(col):
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, scale(col, 1.06 if x % 4 in (1, 2) else 0.86))
    return img


def t_autumn_leaves(seed):
    cols = {"red": ["#C9412E", "#E0603D", "#A9322A"], "orange": ["#E8812F", "#F2A341", "#C9622A"],
            "golden": ["#E9BC3E", "#F4D467", "#C99A2C"], "mixed": ["#C9412E", "#E8812F", "#E9BC3E"]}[seed]
    img = Img(16, 16)
    rng = rng_for("autumn", seed)
    for y in range(16):
        for x in range(16):
            if rng.random() < 0.9:
                img.set(x, y, scale(cols[rng.randrange(3)], 1 + (rng.random() - 0.5) * 0.12))
    for _ in range(5):
        x, y = rng.randrange(16), rng.randrange(16)
        img.set(x, y, C("#7A4B2A"))
    return img


def t_acorns():
    img = Img(16, 16)
    rng = rng_for("acorns")
    noise_fill(img, C("#A87444"), rng, 0.05)
    for cx, cy in ((1, 1), (8, 2), (3, 8), (10, 9), (6, 13)):
        stamp(img, cx, cy, ACORN, dict(b="#6E4A2C", B="#8A5E36", o="#C58A4E"))
    return img


def t_pine():
    img = Img(16, 16)
    rng = rng_for("pine")
    for y in range(16):
        for x in range(16):
            if rng.random() < 0.88:
                img.set(x, y, scale("#2F6B45" if (x + y) % 3 else "#3F8556", 1 + (rng.random() - 0.5) * 0.15))
    return img


def t_berries():
    img = Img(16, 16)
    rng = rng_for("berries")
    for _ in range(20):
        x, y = rng.randrange(15), rng.randrange(15)
        img.set(x, y, C("#C9252F"))
        img.set(x + 1, y, C("#E5484D"))
    return img


def t_fairy_lights():
    img = t_pine()
    cols = ["#FFE07A", "#FFB3C7", "#B8E6FF", "#FFE07A"]
    for i, (x, y) in enumerate(((2, 3), (7, 6), (12, 3), (4, 11), (10, 12), (14, 9))):
        img.set(x, y, C(cols[i % 4]))
    return img


def t_snowball():
    return solid("#F8FCFF", "snowman", amount=0.025)


def t_carrot():
    return solid("#F08A2E", "carrot", amount=0.05)


def t_coal():
    return solid("#2B2B2E", "coal", amount=0.08)


def t_scarf():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#D93F4A") if (y // 2) % 2 else C("#FFF4F0"))
    return img


def t_blossom():
    """Cherry blossom clusters (cut-out)."""
    img = Img(16, 16)
    rng = rng_for("blossom")
    for _ in range(40):
        x, y = rng.randrange(16), rng.randrange(16)
        img.set(x, y, C(["#FFC7DA", "#FFB0CB", "#FFE3EC", "#F48FB1"][rng.randrange(4)]))
    for _ in range(6):
        x, y = rng.randrange(16), rng.randrange(16)
        img.set(x, y, C("#FFF2A6"))
    return img


def t_petals():
    """Petals scattered on a surface (cut-out)."""
    img = Img(16, 16)
    rng = rng_for("petals")
    for _ in range(16):
        x, y = rng.randrange(15), rng.randrange(15)
        col = ["#FFC7DA", "#FFB0CB", "#F8A1BF"][rng.randrange(3)]
        img.set(x, y, C(col))
        if rng.random() < 0.6:
            img.set(x + 1, y, lighten(col, 0.2))
    return img


def t_branch():
    return solid("#5A3D2E", "branch", amount=0.06, grain=0.03)


def t_paper_lantern():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            c = mix("#FFE3EC", "#FFC7DA", abs(x - 7.5) / 8)
            if y % 4 == 0:
                c = darken(c, 0.08)
            img.set(x, y, c)
    for cx, cy in ((4, 6), (11, 10)):
        stamp(img, cx - 1, cy - 1, DAISY, dict(w="#F48FB1", y="#FFF2A6"))
    return img


def t_tulips():
    img = Img(16, 16)
    cols = ["#E5484D", "#F7D44C", "#F49DBA", "#B79DE3", "#FF8C42"]
    for i, x in enumerate((1, 4, 7, 10, 13)):
        top = 2 + (i * 3) % 4
        for y in range(top + 3, 16):
            img.set(x + 1, y, C("#5E9443"))
        stamp(img, x, top, ["c.c", "ccc", "ccc"], dict(c=cols[i]))
        img.set(x, top + 6, C("#6FA552"))
    return img


def t_daffodils():
    img = Img(16, 16)
    for i, x in enumerate((2, 6, 10, 13)):
        top = 2 + (i * 3) % 5
        for y in range(top + 3, 16):
            img.set(x + 1, y, C("#5E9443"))
        stamp(img, x - 1, top, [".y.y.", "yyoyy", ".yoy.", "..y.."], dict(y="#FFE15C", o="#F7A23C"))
    return img


def t_bunny():
    return t_white_ceramic()


def t_gingham_red():
    return mat_print("strawberry")


def t_hay():
    return t_straw()


def t_crate():
    img = Img(16, 16)
    rng = rng_for("crate")
    for y in range(16):
        for x in range(16):
            c = C("#C79A62") if (y // 5) % 2 == 0 else C("#B5874F")
            if y % 5 == 4:
                c = C("#8C6239")
            img.set(x, y, scale(c, 1 + 0.03 * math.sin(x * 0.9 + y) + (rng.random() - 0.5) * 0.04))
    return img


def t_seed_packets():
    img = Img(16, 16)
    cols = ["#F49DBA", "#9FDCC2", "#F8DD78", "#B79DE3", "#E5484D"]
    for x in range(16):
        c = cols[(x // 3) % 5]
        for y in range(16):
            img.set(x, y, C("#FFF8E8") if 4 <= y <= 9 and x % 3 != 2 else C(c))
    for x in range(1, 16, 3):
        img.set(x, 6, C("#5E9443"))
        img.set(x, 5, C("#E5484D"))
    return img


def t_gnome_face():
    img = Img(16, 16)
    noise_fill(img, C("#F6C9A8"), rng_for("gnome"), 0.02)
    for x, y in ((5, 6), (10, 6)):
        img.set(x, y, C("#3B2E25"))
    img.rect(7, 8, 9, 10, C("#F2A78E"))
    return img


def t_beard():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#FFFFFF") if (x + y // 2) % 3 else C("#E9ECEF"))
    return img


def t_snow():
    return solid("#F4FAFF", "snow", amount=0.025)


def t_doormat(season):
    img = Img(16, 16)
    rng = rng_for("mat", season)
    base = {"spring": "#E8C98A", "summer": "#EED48D", "autumn": "#D9B077", "winter": "#D8C6A0"}[season]
    edge = {"spring": "#F49DBA", "summer": "#4FA3D9", "autumn": "#C9622A", "winter": "#C9252F"}[season]
    for y in range(16):
        for x in range(16):
            c = scale(base, 1 + (0.06 if (x + y) % 2 else -0.04) + (rng.random() - 0.5) * 0.05)
            if x in (0, 15) or y in (0, 15):
                c = C(edge)
            img.set(x, y, c)
    if season == "spring":
        stamp(img, 5, 5, TULIP, dict(r="#F27BA5", g="#5E9443"))
    elif season == "summer":
        stamp(img, 5, 5, SUN, dict(y="#F7B53C"))
    elif season == "autumn":
        stamp(img, 6, 5, LEAF, dict(o="#C9412E", b="#7A4B2A"))
    else:
        stamp(img, 5, 5, SNOWFLAKE, dict(w="#FFFFFF"))
    return img


def t_sign_design(i):
    """Pictograms painted on garden signs: drawn into the 12 x 6 window at rows 5-10."""
    img = Img(16, 16)
    designs = [
        (HEART, dict(X="#E5484D")),
        (LAV_SPRIG, dict(p="#9D7BD8", P="#7D5DBB", g="#5E9443")),
        (STRAWBERRY, dict(r="#E5484D", y="#F8DD78", g="#5E9443", G="#3E7B34")),
        (TULIP, dict(r="#F27BA5", g="#5E9443")),
        (BEE, dict(w="#E8F6FF", y="#F7C93C", b="#3B2E25")),
        (CARROT, dict(g="#5E9443", o="#F08A2E")),
        (TOADSTOOL, dict(r="#DE5448", w="#FFFFFF", c="#F6EEDC")),
        (SUN, dict(y="#F7B53C")),
    ]
    rows, pal = designs[i]
    h = len(rows)
    w = len(rows[0])
    sy = 5 + (6 - min(h, 6)) // 2
    # big pictograms are squashed: sample rows to fit the 6 px window
    if h > 6:
        rows = [rows[int(k * h / 6)] for k in range(6)]
        sy = 5
    stamp(img, 3, sy, rows, pal)
    # a little line of 'lettering'
    for x in range(4 + w, 13):
        img.set(x, 7, C("#4A3A2E"))
        if x % 3 != 0:
            img.set(x, 9, C("#4A3A2E"))
    return img


def t_picture(i):
    """Eight little paintings for picture frames."""
    img = Img(16, 16)
    rng = rng_for("picture", i)
    if i == 0:  # cottage on a hill
        for y in range(16):
            for x in range(16):
                img.set(x, y, mix("#BFE3F7", "#E8F6FF", y / 9) if y < 10 else mix("#7DB55E", "#5E9443", (y - 10) / 6))
        img.ellipse(8, 14, 10, 5, C("#86BF63"))
        img.rect(5, 7, 11, 11, C("#FFF4E0"))
        for k in range(4):
            img.rect(4 + k, 6 - k, 12 - k, 7 - k, C("#D9564C"))
        img.rect(7, 9, 9, 11, C("#8C5A3C"))
        img.set(10, 8, C("#9AD0F0"))
        img.ellipse(13, 3, 1.6, 1.6, C("#FFE58A"))
    elif i == 1:  # wildflower meadow
        for y in range(16):
            for x in range(16):
                img.set(x, y, mix("#CDEBFA", "#F3FAFF", y / 7) if y < 7 else mix("#9CCB6E", "#6FA552", (y - 7) / 9))
        for _ in range(26):
            x, y = rng.randrange(16), rng.randrange(8, 16)
            img.set(x, y, C(["#FFFFFF", "#F7D44C", "#F49DBA", "#B79DE3", "#E5484D"][rng.randrange(5)]))
    elif i == 2:  # strawberries in a bowl
        noise_fill(img, C("#F6E6D3"), rng, 0.03)
        img.rect(0, 11, 16, 16, C("#C8925A"))
        img.ellipse(8, 10, 6, 3, C("#FFFFFF"))
        for cx in (5, 8, 11):
            stamp(img, cx - 2, 5, STRAWBERRY[1:], dict(r="#E5484D", y="#F8DD78", g="#5E9443", G="#3E7B34"))
    elif i == 3:  # a sleepy orange cat
        noise_fill(img, C("#E6DCF2"), rng, 0.02)
        img.ellipse(8, 11, 6, 4, C("#F2A65A"))
        img.ellipse(8, 7, 4, 3.5, C("#F2A65A"))
        for x, y in ((5, 3), (6, 4), (11, 3), (10, 4)):
            img.set(x, y, C("#F2A65A"))
        for x in (6, 7, 9, 10):
            img.set(x, 7, C("#5B4636"))
        img.set(8, 8, C("#F49DBA"))
        for x in range(3, 13, 3):
            img.set(x, 12, C("#D98840"))
    elif i == 4:  # mushroom forest
        for y in range(16):
            for x in range(16):
                img.set(x, y, mix("#2F5D3A", "#5E9443", y / 15))
        for x in (2, 13):
            img.rect(x, 0, x + 2, 16, C("#5A3D2E"))
        stamp(img, 4, 8, TOADSTOOL, dict(r="#DE5448", w="#FFFFFF", c="#F6EEDC"))
        for x, y in ((3, 2), (9, 4), (12, 7)):
            img.set(x, y, C("#FFF1A0"))
    elif i == 5:  # lake and mountains
        for y in range(16):
            for x in range(16):
                img.set(x, y, mix("#A9D6F5", "#EAF6FF", y / 8) if y < 9 else mix("#5FA3D0", "#3F7FB0", (y - 9) / 7))
        for k in range(6):
            img.rect(3 + k, 3 + k, 13 - k, 4 + k, C("#8E9AAF"))
        for k in range(2):
            img.rect(6 + k, 3 + k, 10 - k, 4 + k, C("#FFFFFF"))
        img.rect(0, 9, 16, 10, C("#6FA552"))
    elif i == 6:  # sunset by the sea
        for y in range(16):
            for x in range(16):
                img.set(x, y, mix("#F7A8B8", "#FFD58A", y / 9) if y < 10 else mix("#6C8FCB", "#4C6EAD", (y - 10) / 6))
        img.ellipse(8, 10, 3.5, 3.5, C("#FFF0A8"))
        img.rect(0, 10, 16, 11, C("#F7C08A"))
        img.rect(4, 12, 12, 13, C("#9DB7E3"))
    else:  # pressed flower botanical
        noise_fill(img, C("#F8F1E2"), rng, 0.02)
        for y in range(5, 14):
            img.set(8, y, C("#5E9443"))
        for y in (8, 11):
            img.set(7, y, C("#6FA552"))
            img.set(9, y - 1, C("#6FA552"))
        stamp(img, 5, 1, ROSE, dict(p="#FFC1D6", P="#F27BA5", d="#D9668E", D="#B84A72"))
        for x in range(16):
            img.set(x, 15, C("#C8B79A"))
    return img


def t_fairy_glow():
    img = gradient("#FFF7C2", "#FFCF6B", "fairyglow", sparkle=0.7)
    return img


def t_fairy_window():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#FFE59A"))
    for i in range(16):
        img.set(7, i, C("#5A3D2E"))
        img.set(8, i, C("#5A3D2E"))
        img.set(i, 7, C("#5A3D2E"))
        img.set(i, 8, C("#5A3D2E"))
    return img


def t_snow_globe_scene():
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, C("#DCEFFF", 90))
    for x, y in ((2, 3), (6, 1), (12, 4), (9, 7), (3, 9), (13, 10), (7, 12)):
        img.set(x, y, C("#FFFFFF", 240))
    return img


def t_firefly_jar():
    """Pale green glass with glowing fireflies, each with a soft halo."""
    img = Img(16, 16)
    for y in range(16):
        for x in range(16):
            img.set(x, y, mix("#E4F6E8", "#C8EBD2", y / 15)[:3] + (110,))
    for i in range(16):
        img.set(i, 0, C("#F4FBF6", 180))
        img.set(0, i, C("#F4FBF6", 180))
        img.set(15, i, C("#F4FBF6", 180))
    for x, y in ((3, 4), (9, 2), (12, 7), (5, 10), (10, 12), (7, 6), (2, 13), (13, 13)):
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            img.set(x + dx, y + dy, C("#FFF6C2", 170))
        img.set(x, y, C("#FFE45C"))
    for x, y in ((3, 2), (3, 3), (4, 5)):
        img.set(x, y, C("#FFFFFF", 220))
    return img


def t_wax_white():
    return solid("#FFF8EE", "waxwhite", amount=0.02)


def t_herb(kind):
    cols = {"basil": ("#4F9A3A", "#78C25A"), "rosemary": ("#4E7A5A", "#6E9C78"), "mint": ("#5DBB72", "#8EDB9C"),
            "thyme": ("#6E8A4A", "#93AE68"), "sage": ("#8FA98A", "#B4C9AE")}[kind]
    img = Img(16, 16)
    rng = rng_for("herb", kind)
    for i, x in enumerate(range(1, 16, 3)):
        top = 2 + rng.randrange(5)
        for y in range(top, 16):
            img.set(x, y, C(cols[0]))
            if (y + i) % 2 == 0:
                img.set(x + (1 if kind != "rosemary" else 0) + (y % 4 == 0), y, C(cols[1]))
                if kind in ("basil", "mint", "sage"):
                    img.set(x - 1, y, C(cols[1]))
    return img


def t_macrame():
    """Cords converging from a pot rim up to a hook (cut-out)."""
    img = Img(16, 16)
    for y in range(16):
        t = y / 15
        for x0 in (0, 15):
            x = round(x0 + (7.5 - x0) * (1 - t))
            img.set(x, y, C("#EDE3CF"))
            img.set(min(15, x + 1), y, C("#D3C4A6"))
        if y in (11, 12):
            for x in range(16):
                if img.get(x, y)[3]:
                    img.set(x, y, C("#C9A770"))
    return img


def t_ribbon(col, seed):
    img = Img(16, 16)
    rng = rng_for("ribbon", seed)
    for y in range(16):
        for x in range(16):
            f = 1.0 + 0.07 * math.sin(y * 0.8 + x * 0.15) + (rng.random() - 0.5) * 0.02
            img.set(x, y, scale(col, f))
    return img


def t_paper_lantern_lit():
    img = t_paper_lantern()
    img.map(lambda x, y, c: lighten(mix(c, "#FFF1C9", 0.35), 0.15))
    return img


SHARED = {
    "tea": lambda: solid("#B8733A", "tea", amount=0.04), "lemon": lambda: solid("#FFE15C", "lemon", amount=0.05),
    "macrame": t_macrame, "bird_blue": lambda: solid("#6FA8DC", "bird", amount=0.05),
    "paper_lantern_lit": t_paper_lantern_lit, "ribbon_orange": lambda: t_ribbon("#C9622A", "o"),
    "ribbon_red": lambda: t_ribbon("#C9252F", "r"), "ribbon_blue": lambda: t_ribbon("#6FA8DC", "b"),
    "ribbon_pink": lambda: t_ribbon("#F27BA5", "p"), "leather": lambda: solid("#8A5A3C", "leather", amount=0.05),
    "gnome_nose": lambda: solid("#F2A78E", "nose", amount=0.03),

    "brass": t_brass, "silver": t_silver, "iron": t_iron, "galvanized": t_galvanized, "pages": t_pages,
    "page_text": t_page_text, "wicker": t_wicker, "straw": t_straw, "linen": t_linen, "lace": t_lace,
    "soil": t_soil, "flame": t_flame, "wick": t_wick, "string": t_string, "rope": t_rope, "chain": t_chain,
    "leaves": t_leaves, "foliage": t_foliage, "clock_face": t_clock_face, "mirror": t_mirror, "dark": t_dark,
    "glass_clear": t_glass_clear, "lantern_glass": lambda: t_lantern_glass(False),
    "lantern_glass_lit": lambda: t_lantern_glass(True), "glow": t_glow, "cream": t_cream,
    "white_ceramic": t_white_ceramic, "terracotta": t_terracotta, "apple": t_apple, "bread": t_bread,
    "egg": t_egg, "mushroom_red": lambda: t_mushroom_cap("#DE5448"), "mushroom_brown": lambda: t_mushroom_cap("#A97850", False),
    "mushroom_stem": t_stem, "strawberry_fruit": t_strawberry_fruit, "lavender_bunch": t_lavender_bunch,
    "linens": t_linens, "honey": t_honey, "buttons": t_buttons, "spools": t_spools, "wildflowers": t_wildflowers,
    "cocoa": t_cocoa, "lemonade": t_lemonade, "lemon_slice": t_lemon_slice, "watermelon": t_watermelon,
    "grapes": t_grapes, "jam_red": lambda: t_jam("#B8202E"), "jam_blue": lambda: t_jam("#4C3F8F"),
    "jam_orange": lambda: t_jam("#E8812F"), "label": t_label, "pumpkin": lambda: t_pumpkin("#E8812F"),
    "pumpkin_white": lambda: t_pumpkin("#F2EAD8"), "pumpkin_green": lambda: t_pumpkin("#7FA35A"),
    "leaves_red": lambda: t_autumn_leaves("red"), "leaves_orange": lambda: t_autumn_leaves("orange"),
    "leaves_golden": lambda: t_autumn_leaves("golden"), "leaves_mixed": lambda: t_autumn_leaves("mixed"),
    "acorns": t_acorns, "pine": t_pine, "berries": t_berries, "fairy_lights": t_fairy_lights, "snowball": t_snowball,
    "carrot": t_carrot, "coal": t_coal, "scarf": t_scarf, "blossom": t_blossom, "petals": t_petals,
    "branch": t_branch, "paper_lantern": t_paper_lantern, "tulips": t_tulips, "daffodils": t_daffodils,
    "gingham_red": t_gingham_red, "crate": t_crate, "seed_packets": t_seed_packets, "gnome_face": t_gnome_face,
    "beard": t_beard, "snow": t_snow, "fairy_glow": t_fairy_glow, "fairy_window": t_fairy_window,
    "snow_globe_scene": t_snow_globe_scene, "firefly_jar": t_firefly_jar, "wax_white": t_wax_white,
    "yarn_pink": lambda: t_yarn("#F49DBA"), "yarn_mint": lambda: t_yarn("#9FDCC2"),
    "yarn_lilac": lambda: t_yarn("#B79DE3"), "yarn_butter": lambda: t_yarn("#F8DD78"),
    "trailing_ivy": lambda: t_trailing("#5E9443", None, "ivy"),
}
for _h in range(12):
    SHARED[f"clock_hands_{_h}"] = (lambda h: (lambda: t_clock_hands(h)))(_h)
for _i in range(8):
    SHARED[f"sign_design_{_i}"] = (lambda i: (lambda: t_sign_design(i)))(_i)
    SHARED[f"picture_{_i}"] = (lambda i: (lambda: t_picture(i)))(_i)
for _s in ("spring", "summer", "autumn", "winter"):
    SHARED[f"doormat_{_s}"] = (lambda s: (lambda: t_doormat(s)))(_s)
for _k in ("basil", "rosemary", "mint", "thyme", "sage"):
    SHARED[f"herb_{_k}"] = (lambda k: (lambda: t_herb(k)))(_k)
for _s in SETS:
    SHARED[f"trailing_{_s}"] = (lambda s: (lambda: t_trailing(PAL[s]["leaf"], PAL[s]["flower"], "trail" + s)))(_s)

# --------------------------------------------------------------------------- registry

_used = set()


def tex(name):
    """Returns the resource location for a texture name and remembers it for painting."""
    if ":" in name:
        return name
    _used.add(name)
    return f"{MODID}:block/{name}"


def item_tex(name):
    _used.add("item/" + name)
    return f"{MODID}:item/{name}"


ITEM_TEXTURES = {}


def paint_all():
    """Paints every texture that a model asked for. Returns the number painted."""
    count = 0
    for name in sorted(_used):
        if name.startswith("item/"):
            img = ITEM_TEXTURES[name[5:]]()
            img.save(ASSETS / "textures" / "item" / (name[5:] + ".png"))
        elif name.startswith("sets/"):
            _, s, mat = name.split("/")
            SET_MATERIALS[mat](s).save(ASSETS / "textures" / "block" / "sets" / s / (mat + ".png"))
        else:
            if name not in SHARED:
                raise KeyError(f"no painter for texture '{name}'")
            SHARED[name]().save(ASSETS / "textures" / "block" / (name + ".png"))
        count += 1
    return count
