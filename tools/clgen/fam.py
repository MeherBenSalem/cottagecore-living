"""Decoration families: one family = one kind of decoration (a teapot, a birdhouse)
in one or more variants (usually the five decorative sets). A family knows its
geometry, the block-state axes that change its model, its Java behaviour and its
recipe; data.py turns all of that into models, blockstates, loot, recipes, lang
and the generated Java catalog."""
from . import paint
from .paint import SETS, SET_ADJ, SET_MATERIALS, SHARED

FAMILIES = []


class Variant:
    def __init__(self, block_id, name, set_id=None, tex=None, **extra):
        self.id = block_id
        self.name = name
        self.set = set_id
        self.tex = tex or {}
        self.extra = extra

    def __getitem__(self, key):
        return self.extra[key]

    def get(self, key, default=None):
        return self.extra.get(key, default)


class Ctx:
    """Texture resolver for one variant: ctx.ceramic -> the variant's ceramic texture."""

    def __init__(self, variant):
        self.v = variant
        self.pal = paint.PAL.get(variant.set) if variant.set else None

    def __call__(self, name):
        if name in self.v.tex:
            return paint.tex(self.v.tex[name])
        if ":" in name:
            return name
        if name.startswith("sets/"):
            return paint.tex(name)
        if self.v.set and name in SET_MATERIALS:
            return paint.tex(f"sets/{self.v.set}/{name}")
        if name in SHARED:
            return paint.tex(name)
        raise KeyError(f"{self.v.id}: unknown texture '{name}'")

    def __getattr__(self, name):
        if name.startswith("__"):
            raise AttributeError(name)
        return self(name)


def set_variants(family_id, name, **extra):
    return [Variant(f"{s}_{family_id}", f"{SET_ADJ[s]} {name}", s, **extra) for s in SETS]


class Family:
    def __init__(self, fid, name, category, group, variants, build, *, kind="PLAIN", material="WOOD",
                 interaction="NONE", ambient="NONE", mounts=("floor",), facing=True, count=1, light=None,
                 open_=False, rod=False, seasons=False, styles=0, hours=False, walk=False, tint=False,
                 render="cutout", overlay=None, recipe=None, item_state=None, desc="", theme=None,
                 shape_override=None, lit_default=False, mineable="axe"):
        self.id = fid
        self.name = name
        self.category = category
        self.group = group
        self.variants = variants
        self.build = build
        self.kind = kind
        self.material = material
        self.interaction = interaction
        self.ambient = ambient
        self.mounts = tuple(mounts)
        self.facing = facing
        self.count = count
        self.light = light  # (mode, level) with mode CANDLE / SWITCH / ALWAYS
        self.open = open_
        self.rod = rod
        self.seasons = seasons
        self.styles = styles
        self.hours = hours
        self.walk = walk
        self.tint = tint
        self.render = render
        self.overlay = overlay  # fn(ctx, kind, index, st) -> elements drawn over the base (hands, pictures)
        self.recipe = recipe
        self.item_state = item_state or {}
        self.desc = desc
        self.theme = theme
        self.shape_override = shape_override
        self.lit_default = lit_default
        self.mineable = mineable
        FAMILIES.append(self)

    # ---- block-state axes that change the base model

    def axes(self):
        ax = []
        if len(self.mounts) > 1:
            ax.append(("mount", list(self.mounts)))
        if self.count > 1:
            ax.append(("count", list(range(1, self.count + 1))))
        if self.light and self.light[0] in ("CANDLE", "SWITCH"):
            ax.append(("lit", [False, True]))
        if self.open:
            ax.append(("open", [False, True]))
        if self.rod:
            ax.append(("rod", [False, True]))
        if self.seasons:
            ax.append(("season", ["spring", "summer", "autumn", "winter"]))
        if self.sits_low():
            ax.append(("low", [False, True]))
        return ax

    def sits_low(self):
        """Floor decorations drop 15 px to stand on something thin (a blanket, a carpet)."""
        return "floor" in self.mounts and self.kind not in ("TABLE", "BLANKET") and not self.walk

    def states(self):
        out = [{}]
        for key, values in self.axes():
            out = [dict(s, **{key: v}) for s in out for v in values]
        return out

    @staticmethod
    def state_key(st):
        parts = []
        for k, v in st.items():
            if isinstance(v, bool):
                if v:
                    parts.append(k)
            else:
                parts.append(f"{v}" if k != "count" else f"c{v}")
        return "_".join(parts) or "base"

    def default_state(self):
        st = {}
        for key, values in self.axes():
            st[key] = values[0]
        st.update(self.item_state)
        return st

    def shape_key(self, st):
        """Matches DecorBlock#shapeKey on the Java side."""
        parts = []
        if len(self.mounts) > 1:
            parts.append(st["mount"])
        if self.count > 1:
            parts.append(f"c{st['count']}")
        return "_".join(parts) or "base"

    def wall_item(self):
        st = self.default_state()
        mount = st.get("mount", self.mounts[0])
        return mount == "wall"


def family(*args, **kw):
    """Decorator form: @family('teapot', 'Teapot', 'interior', 'kitchen', variants, ...)."""
    def wrap(fn):
        return Family(args[0], args[1], args[2], args[3], args[4], fn, **kw)
    return wrap
