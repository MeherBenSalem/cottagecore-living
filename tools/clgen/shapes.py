"""Reusable decorative motifs built from the cuboid DSL. Every motif takes concrete
texture references (from a Ctx) so families can mix set materials and shared ones."""
import math

from .core import B, box, move, rot_y90, mirror_x

FLAT = dict(n=None, s=None, e=None, w=None, d=None)


def cross(cx, cz, w, y0, y1, tex, angle=45, uv="fit", shade=False):
    """Two planes crossed at +/- angle around (cx, cz), like vanilla plants."""
    out = []
    for a in (angle, -angle):
        out += box((cx - w / 2, y0, cz), (cx + w / 2, y1, cz), (tex, uv), rot=("y", a, (cx, (y0 + y1) / 2, cz)),
                   shade=shade)
    return out


def cross4(cx, cz, w, y0, y1, tex, uv="fit"):
    """Four planes (crossed at 45 and 0/90) for a fuller bouquet."""
    out = cross(cx, cz, w, y0, y1, tex, 45, uv)
    out += box((cx - w / 2, y0, cz), (cx + w / 2, y1, cz), (tex, uv), shade=False)
    out += box((cx, y0, cz - w / 2), (cx, y1, cz + w / 2), (tex, uv), shade=False)
    return out


def plane_y(x0, z0, x1, z1, y, tex, uv="fit", shade=True):
    """A horizontal sheet seen from above and below."""
    return box((x0, y, z0), (x1, y, z1), (tex, uv), shade=shade)


def plane_z(x0, y0, x1, y1, z, tex, uv="fit", shade=False, rot=None):
    """A vertical sheet in the x/y plane (faces north and south)."""
    return box((x0, y0, z), (x1, y1, z), (tex, uv), rot=rot, shade=shade)


def disk(cx, cz, r, y0, y1, tex, **faces):
    """A rounded (octagonal) column: two crossed boxes."""
    i = r * 0.42
    out = B(cx - r, y0, cz - r + i, cx + r, y1, cz + r - i, tex, **faces)
    out += B(cx - r + i, y0, cz - r, cx + r - i, y1, cz + r, tex, **faces)
    return out


def _rows(r, step):
    """Row bands (offset from the centre) covering a circle of radius r."""
    n = max(2, int(round(2 * r / step)))
    h = 2 * r / n
    return [(-r + i * h, -r + (i + 1) * h) for i in range(n)]


def _half(r, a, b):
    """Half-width of a circle at the row band [a, b] (measured at the band's inner edge)."""
    d = min(abs(a), abs(b)) if a * b > 0 else 0.0
    return math.sqrt(max(0.0, r * r - d * d))


def ring_xz(cx, cz, r, t, y0, y1, tex, step=1.0, **faces):
    """A flat round ring (a rim lying on the ground), built row by row."""
    out = []
    for a, b in _rows(r, step):
        ox = _half(r, a, b)
        ix = _half(r - t, a, b) if r - t > 0 else 0.0
        if min(abs(a), abs(b)) >= r - t or ix <= 0.01:
            out += B(cx - ox, y0, cz + a, cx + ox, y1, cz + b, tex, **faces)
        else:
            out += B(cx - ox, y0, cz + a, cx - ix, y1, cz + b, tex, **faces)
            out += B(cx + ix, y0, cz + a, cx + ox, y1, cz + b, tex, **faces)
    return out


def ring_xy(cx, cy, r, t, z0, z1, tex, step=1.0, **faces):
    """An upright round ring in the x/y plane (hoops, wreaths, clock rims), built row by row."""
    out = []
    for a, b in _rows(r, step):
        ox = _half(r, a, b)
        ix = _half(r - t, a, b) if r - t > 0 else 0.0
        if min(abs(a), abs(b)) >= r - t or ix <= 0.01:
            out += B(cx - ox, cy + a, z0, cx + ox, cy + b, z1, tex, **faces)
        else:
            out += B(cx - ox, cy + a, z0, cx - ix, cy + b, z1, tex, **faces)
            out += B(cx + ix, cy + a, z0, cx + ox, cy + b, z1, tex, **faces)
    return out


def disc_xy(cx, cy, r, z0, z1, tex, face_tex=None, step=1.0):
    """A solid round disc in the x/y plane. face_tex is spread over the whole north face."""
    out = []
    for a, b in _rows(r, step):
        ox = _half(r, a, b)
        x0, x1, y0, y1 = cx - ox, cx + ox, cy + a, cy + b
        if face_tex:
            u0 = (cx + r - x1) / (2 * r) * 16
            u1 = (cx + r - x0) / (2 * r) * 16
            v0 = (cy + r - y1) / (2 * r) * 16
            v1 = (cy + r - y0) / (2 * r) * 16
            n = (face_tex, (u0, v0, u1, v1))
        else:
            n = tex
        out += B(x0, y0, z0, x1, y1, z1, tex, n=n)
    return out


def bow(cx, y, z0, z1, s, tex):
    """A ribbon bow in the x/y plane (6.5s wide, 3s tall), tails down to y."""
    out = []
    out += B(cx - 3 * s, y + 1.25 * s, z0, cx - 0.5 * s, y + 3 * s, z1, tex)
    out += B(cx + 0.5 * s, y + 1.25 * s, z0, cx + 3 * s, y + 3 * s, z1, tex)
    out += B(cx - 1.6 * s, y, z0 + 0.05, cx - 0.5 * s, y + 1.5 * s, z1 - 0.05, tex)
    out += B(cx + 0.5 * s, y, z0 + 0.05, cx + 1.6 * s, y + 1.5 * s, z1 - 0.05, tex)
    out += B(cx - 0.6 * s, y + 1.4 * s, z0 - 0.15, cx + 0.6 * s, y + 2.85 * s, z1 + 0.15, tex)
    return out


def flat_bow(cx, cz, y, s, tex):
    """A bow lying flat (on a hat brim or a box lid)."""
    out = []
    out += B(cx - 3 * s, y, cz - 1 * s, cx - 0.5 * s, y + 0.75, cz + 1 * s, tex)
    out += B(cx + 0.5 * s, y, cz - 1 * s, cx + 3 * s, y + 0.75, cz + 1 * s, tex)
    out += B(cx - 0.6 * s, y, cz - 0.7 * s, cx + 0.6 * s, y + 1, cz + 0.7 * s, tex)
    out += B(cx - 1.4 * s, y, cz + 0.6 * s, cx - 0.6 * s, y + 0.5, cz + 2.4 * s, tex)
    out += B(cx + 0.6 * s, y, cz + 0.6 * s, cx + 1.4 * s, y + 0.5, cz + 2.6 * s, tex)
    return out


def heart_xy(cx, y, z0, z1, s, tex):
    rows = [(0, 1, 4, 6), (1, 2, 3, 7), (2, 3, 2, 8), (3, 5, 1, 9), (5, 7, 0, 10), (7, 8, 0.5, 4.5), (7, 8, 5.5, 9.5),
            (8, 9, 1.5, 3.5), (8, 9, 6.5, 8.5)]
    out = []
    for ya, yb, xa, xb in rows:
        out += B(cx + (xa - 5) * s, y + ya * s, z0, cx + (xb - 5) * s, y + yb * s, z1, tex)
    return out


def basket(x0, z0, x1, z1, h, wicker, rim=None, handle=True, handle_h=5.0):
    """An open wicker basket with a rim and an arched handle across x."""
    rim = rim or wicker
    t = 0.75
    out = []
    out += B(x0 + 0.5, 0, z0 + 0.5, x1 - 0.5, 0.75, z1 - 0.5, wicker)
    out += B(x0, 0.5, z0, x1, h, z0 + t, wicker)
    out += B(x0, 0.5, z1 - t, x1, h, z1, wicker)
    out += B(x0, 0.5, z0 + t, x0 + t, h, z1 - t, wicker)
    out += B(x1 - t, 0.5, z0 + t, x1, h, z1 - t, wicker)
    out += B(x0 - 0.25, h - 0.75, z0 - 0.25, x1 + 0.25, h + 0.25, z0 + 0.5, rim)
    out += B(x0 - 0.25, h - 0.75, z1 - 0.5, x1 + 0.25, h + 0.25, z1 + 0.25, rim)
    out += B(x0 - 0.25, h - 0.75, z0 + 0.5, x0 + 0.5, h + 0.25, z1 - 0.5, rim)
    out += B(x1 - 0.5, h - 0.75, z0 + 0.5, x1 + 0.25, h + 0.25, z1 - 0.5, rim)
    if handle:
        cz = (z0 + z1) / 2
        top = h + handle_h
        out += B(x0 + 0.25, h, cz - 0.5, x0 + 1.25, top - 1, cz + 0.5, rim)
        out += B(x1 - 1.25, h, cz - 0.5, x1 - 0.25, top - 1, cz + 0.5, rim)
        out += B(x0 + 1, top - 1.5, cz - 0.5, x0 + 2.5, top - 0.25, cz + 0.5, rim)
        out += B(x1 - 2.5, top - 1.5, cz - 0.5, x1 - 1, top - 0.25, cz + 0.5, rim)
        out += B(x0 + 2.25, top - 1, cz - 0.5, x1 - 2.25, top, cz + 0.5, rim)
    return out


def pot(cx, cz, r, h, tex, soil, rim=None):
    """A round flower pot (soil top at h - 0.5)."""
    rim = rim or tex
    out = disk(cx, cz, r - 0.75, 0, h - 1, tex, u=None)
    out += ring_xz(cx, cz, r, 1, h - 1.5, h, rim)
    out += disk(cx, cz, r - 0.9, h - 1.5, h - 0.5, soil, n=None, s=None, e=None, w=None, d=None)
    return out


def square_pot(x0, z0, x1, z1, h, tex, soil, rim=None):
    rim = rim or tex
    out = B(x0 + 0.5, 0, z0 + 0.5, x1 - 0.5, h - 1, z1 - 0.5, tex, u=None)
    out += B(x0, h - 1.5, z0, x1, h, z0 + 1, rim)
    out += B(x0, h - 1.5, z1 - 1, x1, h, z1, rim)
    out += B(x0, h - 1.5, z0 + 1, x0 + 1, h, z1 - 1, rim)
    out += B(x1 - 1, h - 1.5, z0 + 1, x1, h, z1 - 1, rim)
    out += B(x0 + 1, h - 1, z0 + 1, x1 - 1, h - 0.5, z1 - 1, soil, **FLAT)
    return out


def wreath(cx, cy, r, z0, z1, base, accent, bow_tex, deco=None):
    """A round wreath on a wall: a thick ring of foliage, some accent clusters and a bow."""
    out = ring_xy(cx, cy, r, 3, z0, z1, base)
    # puffs of foliage poking out of the ring
    for dx, dy in ((-r + 1, 0), (r - 1, 0), (0, -r + 1), (-r * 0.7, r * 0.7), (r * 0.7, -r * 0.7)):
        out += B(cx + dx - 1.25, cy + dy - 1.25, z0 - 0.75, cx + dx + 1.25, cy + dy + 1.25, z0 + 0.5, base)
    for dx, dy in ((-r * 0.75, -r * 0.55), (r * 0.8, r * 0.35), (-r * 0.2, -r + 0.6), (r * 0.35, r - 0.9)):
        out += B(cx + dx - 0.9, cy + dy - 0.9, z0 - 1.25, cx + dx + 0.9, cy + dy + 0.9, z0 - 0.25, accent)
    if deco:
        out += deco
    if bow_tex:
        out += bow(cx, cy + r - 3.2, z0 - 1.5, z0 - 0.5, 0.8, bow_tex)
    return out


def garland(y_end, sag, z0, z1, tex, accent=None, bead=None, thick=2.0):
    """A swag hanging across a wall from x=0 to x=16, lowest in the middle."""
    out = []
    steps = 8
    for i in range(steps):
        xa = i * 16 / steps
        xb = (i + 1) * 16 / steps
        t = (i + 0.5) / steps
        drop = sag * (1 - (2 * t - 1) ** 2)
        yc = y_end - drop
        out += B(xa, yc - thick / 2, z0, xb, yc + thick / 2, z1, tex)
        if accent and i % 2 == 1:
            out += B(xa + 0.2, yc - thick / 2 - 0.4, z0 - 0.6, xb - 0.2, yc + thick / 2 - 0.2, z0 + 0.4, accent)
        if bead and i % 2 == 0:
            out += B(xa + 0.6, yc - thick / 2 - 1, z0 - 0.4, xa + 1.6, yc - thick / 2, z0 + 0.6, bead)
    return out


def lantern(cx, cz, y0, frame, glass, top, candle=None, flame=None, w=6.0, h=8.0):
    """A four-sided lantern standing at y0."""
    hw = w / 2
    out = []
    out += B(cx - hw, y0, cz - hw, cx + hw, y0 + 1, cz + hw, frame)
    # glass sides
    out += B(cx - hw + 0.5, y0 + 1, cz - hw + 0.5, cx + hw - 0.5, y0 + h - 1.5, cz + hw - 0.5, glass, u=None, d=None)
    # corner posts
    for sx in (-1, 1):
        for sz in (-1, 1):
            x = cx + sx * (hw - 0.5)
            z = cz + sz * (hw - 0.5)
            out += B(x - 0.5, y0 + 1, z - 0.5, x + 0.5, y0 + h - 1.5, z + 0.5, frame)
    out += B(cx - hw, y0 + h - 1.5, cz - hw, cx + hw, y0 + h - 0.75, cz + hw, frame)
    out += B(cx - hw + 1, y0 + h - 0.75, cz - hw + 1, cx + hw - 1, y0 + h, cz + hw - 1, top)
    if candle:
        out += B(cx - 1, y0 + 1, cz - 1, cx + 1, y0 + 3.5, cz + 1, candle)
    if flame:
        out += cross(cx, cz, 1.5, y0 + 3.5, y0 + 5, flame, uv=(5, 2, 9, 8))
    return out


def hanger(cx, cz, y0, y1, tex, w=1.0):
    """A chain or string from y0 up to y1."""
    return cross(cx, cz, w, y0, y1, tex, 45, uv=(0, 0, 3, 16), shade=True)


def crate(x0, z0, x1, z1, h, tex):
    out = []
    t = 1
    out += B(x0, 0, z0, x1, 1, z1, tex)
    out += B(x0, 1, z0, x1, h, z0 + t, tex)
    out += B(x0, 1, z1 - t, x1, h, z1, tex)
    out += B(x0, 1, z0 + t, x0 + t, h, z1 - t, tex)
    out += B(x1 - t, 1, z0 + t, x1, h, z1 - t, tex)
    return out


def bumps(x0, z0, x1, z1, y, tex, n=5, size=2.0, seed=0, h=None):
    """A heap of small cubes (fruit, eggs, pebbles) filling a rectangle at height y."""
    import random
    rng = random.Random(f"bumps{seed}{x0}{z0}")
    out = []
    h = h or size
    cols = max(1, int((x1 - x0) // size))
    rows = max(1, int((z1 - z0) // size))
    cells = [(i, j) for i in range(cols) for j in range(rows)]
    rng.shuffle(cells)
    for k, (i, j) in enumerate(cells[:n]):
        x = x0 + i * size + rng.uniform(0, max(0, (x1 - x0) - cols * size))
        z = z0 + j * size + rng.uniform(0, max(0, (z1 - z0) - rows * size))
        lift = rng.choice((0, 0.25, 0.5))
        out += B(x, y + lift, z, x + size * 0.9, y + lift + h * 0.9, z + size * 0.9, tex)
    return out


def flip(els):
    return mirror_x(els)


def turn(els, n=1):
    return rot_y90(els, n)


def shift(els, dx=0, dy=0, dz=0):
    return move(els, dx, dy, dz)
