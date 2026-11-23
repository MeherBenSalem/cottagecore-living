package com.sugarplumstudios.cottagecore.catalog;

import com.sugarplumstudios.cottagecore.block.ShapeUtil;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One kind of decoration (a teapot, a birdhouse) and everything its blocks share:
 * how it is mounted, which block-state properties it has, its hit boxes, light,
 * interaction and ambient effects. Built by the generated {@link DecorCatalog}.
 */
public final class DecorFamily {
    private final String id;
    private final Category category;
    private final String group;
    private final Kind kind;
    private final DecorMaterial material;
    private final boolean facing;
    private final List<Mount> mounts;
    private final int maxCount;
    private final LightMode lightMode;
    private final int lightLevel;
    private final boolean openable;
    private final boolean rod;
    private final boolean seasonal;
    private final int styles;
    private final boolean hours;
    private final boolean walkThrough;
    private final boolean sitsLow;
    private final boolean waterTint;
    private final Interaction interaction;
    private final Ambient ambient;
    private final Map<String, VoxelShape> shapes;
    private final Map<Integer, List<Vec3>> flames;
    private final List<DecorVariant> variants;

    private DecorFamily(Builder b) {
        this.id = b.id;
        this.category = b.category;
        this.group = b.group;
        this.kind = b.kind;
        this.material = b.material;
        this.facing = b.facing;
        this.mounts = List.copyOf(b.mounts);
        this.maxCount = b.maxCount;
        this.lightMode = b.lightMode;
        this.lightLevel = b.lightLevel;
        this.openable = b.openable;
        this.rod = b.rod;
        this.seasonal = b.seasonal;
        this.styles = b.styles;
        this.hours = b.hours;
        this.walkThrough = b.walkThrough;
        this.sitsLow = b.sitsLow;
        this.waterTint = b.waterTint;
        this.interaction = b.interaction;
        this.ambient = b.ambient;
        this.shapes = Map.copyOf(b.shapes);
        this.flames = Map.copyOf(b.flames);
        this.variants = List.copyOf(b.variants);
    }

    public static Builder builder(String id, Category category, String group, Kind kind, DecorMaterial material) {
        return new Builder(id, category, group, kind, material);
    }

    public String id() {
        return id;
    }

    public Category category() {
        return category;
    }

    public String group() {
        return group;
    }

    public String groupTranslationKey() {
        return "group.cottagecoreliving." + group;
    }

    public String descriptionKey() {
        return "decor.cottagecoreliving." + id + ".desc";
    }

    public Kind kind() {
        return kind;
    }

    public DecorMaterial material() {
        return material;
    }

    public boolean hasFacing() {
        return facing;
    }

    public List<Mount> mounts() {
        return mounts;
    }

    public boolean hasMountProperty() {
        return mounts.size() > 1;
    }

    public Mount defaultMount() {
        return mounts.get(0);
    }

    public int maxCount() {
        return maxCount;
    }

    public boolean stacks() {
        return maxCount > 1;
    }

    public LightMode lightMode() {
        return lightMode;
    }

    public int lightLevel() {
        return lightLevel;
    }

    public boolean hasLitProperty() {
        return lightMode == LightMode.CANDLE || lightMode == LightMode.SWITCH;
    }

    public boolean openable() {
        return openable;
    }

    public boolean hasRod() {
        return rod;
    }

    public boolean seasonal() {
        return seasonal;
    }

    public int styles() {
        return styles;
    }

    public boolean hasHours() {
        return hours;
    }

    public boolean walkThrough() {
        return walkThrough;
    }

    /** Floor decorations that drop down to stand on something thin, like a picnic blanket or a carpet. */
    public boolean sitsLow() {
        return sitsLow;
    }

    public boolean waterTint() {
        return waterTint;
    }

    public Interaction interaction() {
        return interaction;
    }

    public Ambient ambient() {
        return ambient;
    }

    public List<DecorVariant> variants() {
        return variants;
    }

    /** The north-facing hit box for a shape key ("base", "floor", "c2", "ceiling_c1", ...). */
    public VoxelShape shape(String key) {
        VoxelShape shape = shapes.get(key);
        if (shape == null) {
            shape = shapes.get("base");
        }
        if (shape == null) {
            shape = shapes.values().iterator().next();
        }
        return shape;
    }

    /** Flame positions (block pixels, north frame) for a number of candles. */
    public List<Vec3> flames(int count) {
        return flames.getOrDefault(count, List.of());
    }

    @Nullable
    public DecorVariant variantFor(DecorSet set) {
        for (DecorVariant variant : variants) {
            if (variant.set() == set) {
                return variant;
            }
        }
        return null;
    }

    public static final class Builder {
        private final String id;
        private final Category category;
        private final String group;
        private final Kind kind;
        private final DecorMaterial material;
        private boolean facing = true;
        private final List<Mount> mounts = new ArrayList<>(List.of(Mount.FLOOR));
        private int maxCount = 1;
        private LightMode lightMode = LightMode.NONE;
        private int lightLevel;
        private boolean openable;
        private boolean rod;
        private boolean seasonal;
        private int styles;
        private boolean hours;
        private boolean walkThrough;
        private boolean sitsLow;
        private boolean waterTint;
        private Interaction interaction = Interaction.NONE;
        private Ambient ambient = Ambient.NONE;
        private final Map<String, VoxelShape> shapes = new HashMap<>();
        private final Map<Integer, List<Vec3>> flames = new HashMap<>();
        private final List<DecorVariant> variants = new ArrayList<>();

        private Builder(String id, Category category, String group, Kind kind, DecorMaterial material) {
            this.id = id;
            this.category = category;
            this.group = group;
            this.kind = kind;
            this.material = material;
        }

        public Builder noFacing() {
            this.facing = false;
            return this;
        }

        public Builder mounts(Mount... mounts) {
            this.mounts.clear();
            this.mounts.addAll(List.of(mounts));
            return this;
        }

        public Builder count(int max) {
            if (max < 1 || max > 3) {
                throw new IllegalArgumentException("count must be 1-3: " + id);
            }
            this.maxCount = max;
            return this;
        }

        public Builder light(LightMode mode, int level) {
            this.lightMode = mode;
            this.lightLevel = level;
            return this;
        }

        public Builder openable() {
            this.openable = true;
            return this;
        }

        public Builder rod() {
            this.rod = true;
            return this;
        }

        public Builder seasonal() {
            this.seasonal = true;
            return this;
        }

        public Builder styles(int styles) {
            if (styles < 2 || styles > 8) {
                throw new IllegalArgumentException("styles must be 2-8: " + id);
            }
            this.styles = styles;
            return this;
        }

        public Builder hours() {
            this.hours = true;
            return this;
        }

        public Builder walkThrough() {
            this.walkThrough = true;
            return this;
        }

        public Builder sitsLow() {
            this.sitsLow = true;
            return this;
        }

        public Builder waterTint() {
            this.waterTint = true;
            return this;
        }

        public Builder interaction(Interaction interaction) {
            this.interaction = interaction;
            return this;
        }

        public Builder ambient(Ambient ambient) {
            this.ambient = ambient;
            return this;
        }

        /** Hit boxes in block pixels {x0, y0, z0, x1, y1, z1, ...} for a model facing north. */
        public Builder shape(String key, double... boxes) {
            shapes.put(key, ShapeUtil.px(boxes));
            return this;
        }

        public Builder flames(int count, double... xyz) {
            List<Vec3> points = new ArrayList<>();
            for (int i = 0; i + 2 < xyz.length; i += 3) {
                points.add(new Vec3(xyz[i], xyz[i + 1], xyz[i + 2]));
            }
            flames.put(count, List.copyOf(points));
            return this;
        }

        public Builder variant(String id) {
            variants.add(new DecorVariant(id, null));
            return this;
        }

        public Builder variant(String id, DecorSet set) {
            variants.add(new DecorVariant(id, set));
            return this;
        }

        public DecorFamily build() {
            if (variants.isEmpty() || shapes.isEmpty()) {
                throw new IllegalStateException("family " + id + " needs variants and a shape");
            }
            return new DecorFamily(this);
        }
    }
}
