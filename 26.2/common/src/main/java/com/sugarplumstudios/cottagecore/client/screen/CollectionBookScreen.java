package com.sugarplumstudios.cottagecore.client.screen;

import com.sugarplumstudios.cottagecore.catalog.Category;
import com.sugarplumstudios.cottagecore.catalog.DecorFamily;
import com.sugarplumstudios.cottagecore.catalog.DecorSet;
import com.sugarplumstudios.cottagecore.collection.Collectibles;
import com.sugarplumstudios.cottagecore.item.DecorBlockItem;
import com.sugarplumstudios.cottagecore.platform.Services;
import com.sugarplumstudios.cottagecore.network.OpenBookPacket;
import com.sugarplumstudios.cottagecore.network.TakePacket;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The Decoration Collection Book: browse every decoration by collection or by
 * themed set, search, see what each one does and how to craft it, and track
 * which ones you've collected. In creative mode it can hand decorations out.
 */
public class CollectionBookScreen extends Screen {
    private static final int PANEL = 0xFFFFFBF0;
    private static final int PANEL_SHADE = 0xFFF5EEDC;
    private static final int BORDER = 0xFFB9CFA0;
    private static final int BORDER_DARK = 0xFF7FA266;
    private static final int TAB = 0xFFEFF5E4;
    private static final int TAB_HOVER = 0xFFE3EDD3;
    private static final int TAB_SELECTED = 0xFFCFE3B8;
    private static final int TEXT = 0xFF4E5B3A;
    private static final int TEXT_SOFT = 0xFF8C9A78;
    private static final int SLOT = 0xFFFFFFFF;
    private static final int SLOT_EDGE = 0xFFE2E9D3;
    private static final int SLOT_SELECTED = 0xFFF7D9DC;
    private static final int STRAWBERRY = 0xFFE5484D;
    private static final int MISSING_WASH = 0xB0FFFBF0;
    private static final int CELL = 18;
    private static final int SIDEBAR = 82;
    private static final int DETAIL = 132;
    private static final int HEADER = 32;

    private enum Tab {
        ALL("gui.cottagecoreliving.book.all", null),
        INTERIOR(Category.INTERIOR.translationKey(), Category.INTERIOR),
        GARDEN(Category.GARDEN.translationKey(), Category.GARDEN),
        SEASONAL(Category.SEASONAL.translationKey(), Category.SEASONAL),
        SETS("gui.cottagecoreliving.book.sets", null);

        final String key;
        @Nullable
        final Category category;

        Tab(String key, @Nullable Category category) {
            this.key = key;
            this.category = category;
        }
    }

    private final Set<String> collected;
    private final boolean canTake;
    private final Component seasonLine;

    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int columns;
    private int rows;
    private int gridTop;

    private Tab tab = Tab.ALL;
    @Nullable
    private DecorSet setFilter;
    private String query = "";
    private int scroll;
    private List<Collectibles.Entry> shown = List.of();
    @Nullable
    private Collectibles.Entry selected;
    @Nullable
    private DecorSet selectedSet = DecorSet.STRAWBERRY_COTTAGE;
    @Nullable
    private RecipeDisplay recipe;
    private ItemStack hovered = ItemStack.EMPTY;
    @Nullable
    private Component hoveredLabel;

    private EditBox search;
    private Button takeOne;
    private Button takeStack;
    private Button takeSet;

    public CollectionBookScreen(OpenBookPacket packet) {
        super(Component.translatable("gui.cottagecoreliving.book.title"));
        this.collected = new HashSet<>(packet.collected());
        this.canTake = packet.canTake();
        String mode = switch (packet.mode()) {
            case CALENDAR -> "gui.cottagecoreliving.book.season_mode.calendar";
            case CYCLE -> "gui.cottagecoreliving.book.season_mode.cycle";
            case FIXED -> "gui.cottagecoreliving.book.season_mode.fixed";
        };
        this.seasonLine = Component.translatable("gui.cottagecoreliving.book.season", packet.season().displayName())
                .append(" · ").append(Component.translatable(mode));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(404, width - 8);
        panelHeight = Math.min(246, height - 8);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        gridTop = top + HEADER + 34;
        int gridWidth = panelWidth - SIDEBAR - DETAIL - 16;
        columns = Math.max(3, gridWidth / CELL);
        rows = Math.max(3, (top + panelHeight - 5 - gridTop) / CELL);

        search = new EditBox(font, left + SIDEBAR + 5, top + HEADER + 4, columns * CELL - 2, 12,
                Component.translatable("gui.cottagecoreliving.book.search"));
        search.setHint(Component.translatable("gui.cottagecoreliving.book.search").withStyle(s -> s.withColor(TEXT_SOFT)));
        search.setMaxLength(40);
        search.setValue(query);
        search.setResponder(value -> {
            query = value.toLowerCase(Locale.ROOT);
            scroll = 0;
            refilter();
        });
        addRenderableWidget(search);

        int dx = left + panelWidth - DETAIL + 4;
        int by = top + panelHeight - 22;
        takeOne = addRenderableWidget(Button.builder(Component.translatable("gui.cottagecoreliving.book.take"),
                b -> take(false)).bounds(dx, by, 60, 18).build());
        takeStack = addRenderableWidget(Button.builder(Component.translatable("gui.cottagecoreliving.book.take_stack"),
                b -> take(true)).bounds(dx + 63, by, 61, 18).build());
        takeSet = addRenderableWidget(Button.builder(Component.translatable("gui.cottagecoreliving.book.take_set"),
                b -> takeWholeSet()).bounds(dx, by, DETAIL - 8, 18).build());
        refilter();
        updateButtons();
    }

    private void updateButtons() {
        boolean sets = tab == Tab.SETS;
        takeOne.visible = takeStack.visible = !sets && selected != null && canTake;
        takeSet.visible = sets && selectedSet != null && canTake;
        search.visible = !sets;
    }

    private void refilter() {
        List<Collectibles.Entry> out = new ArrayList<>();
        for (Collectibles.Entry entry : Collectibles.all()) {
            if (tab.category != null && entry.category() != tab.category) {
                continue;
            }
            if (setFilter != null && entry.set() != setFilter) {
                continue;
            }
            if (!query.isEmpty() && !matches(entry)) {
                continue;
            }
            out.add(entry);
        }
        shown = out;
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    private boolean matches(Collectibles.Entry entry) {
        String name = new ItemStack(entry.item()).getHoverName().getString().toLowerCase(Locale.ROOT);
        if (name.contains(query)) {
            return true;
        }
        String group = Component.translatable("group.cottagecoreliving." + entry.group()).getString().toLowerCase(Locale.ROOT);
        return group.contains(query);
    }

    private int maxScroll() {
        int totalRows = (shown.size() + columns - 1) / columns;
        return Math.max(0, totalRows - rows);
    }

    private void select(Collectibles.Entry entry) {
        selected = entry;
        recipe = findRecipe(entry.item());
        updateButtons();
    }

    /**
     * The decoration's crafting recipe as the client knows it. Since 1.21.2 clients only receive the recipes in their
     * recipe book (the mod unlocks all of its recipes on join unless that is turned off in the config).
     */
    @Nullable
    private RecipeDisplay findRecipe(Item item) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            return null;
        }
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        RecipeDisplay best = null;
        for (RecipeCollection collection : minecraft.player.getRecipeBook().getCollections()) {
            for (RecipeDisplayEntry entry : collection.getRecipes()) {
                RecipeDisplay display = entry.display();
                if (!(display instanceof ShapedCraftingRecipeDisplay) && !(display instanceof ShapelessCraftingRecipeDisplay)) {
                    continue;
                }
                if (display.result().resolveForFirstStack(context).is(item)
                        && (best == null || display instanceof ShapedCraftingRecipeDisplay && !(best instanceof ShapedCraftingRecipeDisplay))) {
                    best = display;
                }
            }
        }
        return best;
    }

    private void take(boolean stack) {
        if (selected != null) {
            Identifier id = BuiltInRegistries.ITEM.getKey(selected.item());
            Services.PLATFORM.sendToServer(new TakePacket(List.of(id), stack));
            collected.add(selected.id());
        }
    }

    private void takeWholeSet() {
        if (selectedSet != null) {
            List<Identifier> ids = new ArrayList<>();
            for (Collectibles.Entry entry : Collectibles.inSet(selectedSet)) {
                ids.add(BuiltInRegistries.ITEM.getKey(entry.item()));
                collected.add(entry.id());
            }
            Services.PLATFORM.sendToServer(new TakePacket(ids, false));
        }
    }

    private boolean has(Collectibles.Entry entry) {
        return collected.contains(entry.id());
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        hovered = ItemStack.EMPTY;
        hoveredLabel = null;
        drawFrame(g);
        drawHeader(g);
        drawTabs(g, mouseX, mouseY);
        if (tab == Tab.SETS) {
            drawSetList(g, mouseX, mouseY);
            drawSetDetail(g, mouseX, mouseY);
        } else {
            drawSwatches(g, mouseX, mouseY);
            drawGrid(g, mouseX, mouseY);
            drawDetail(g, mouseX, mouseY);
        }
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        drawTooltip(g, mouseX, mouseY);
    }

    private void drawTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!hovered.isEmpty()) {
            List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), hovered));
            Collectibles.Entry entry = Collectibles.of(hovered.getItem());
            if (entry != null) {
                lines.add(Component.translatable(has(entry) ? "gui.cottagecoreliving.book.collected_one"
                        : "gui.cottagecoreliving.book.not_collected").withStyle(s -> s.withColor(has(entry) ? 0x5E9443 : 0xB3343C)));
            }
            g.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
        } else if (hoveredLabel != null) {
            g.setTooltipForNextFrame(font, hoveredLabel, mouseX, mouseY);
        }
    }

    private void drawFrame(GuiGraphicsExtractor g) {
        int r = left + panelWidth;
        int b = top + panelHeight;
        g.fill(left - 2, top - 2, r + 2, b + 2, BORDER_DARK);
        g.fill(left - 1, top - 1, r + 1, b + 1, BORDER);
        g.fill(left, top, r, b, PANEL);
        // a little scalloped trim of flowers along the top and bottom
        for (int x = left + 4; x < r - 4; x += 10) {
            int color = (x / 10) % 3 == 0 ? STRAWBERRY : BORDER;
            g.fill(x, top - 4, x + 4, top - 2, color);
            g.fill(x + 1, top - 5, x + 3, top - 4, color);
            g.fill(x, b + 2, x + 4, b + 4, color);
            g.fill(x + 1, b + 4, x + 3, b + 5, color);
        }
        g.fill(left, top, r, top + HEADER, PANEL_SHADE);
        g.fill(left, top + HEADER, r, top + HEADER + 1, BORDER);
        g.fill(left + SIDEBAR - 1, top + HEADER + 1, left + SIDEBAR, b, BORDER);
        int dx = left + panelWidth - DETAIL;
        g.fill(dx - 1, top + HEADER + 1, dx, b, BORDER);
        g.fill(dx, top + HEADER + 1, r, b, PANEL_SHADE);
    }

    private void drawHeader(GuiGraphicsExtractor g) {
        Component title = Component.literal("✿ ").append(this.title).append(" ✿");
        g.text(font, title, left + 6, top + 5, TEXT, false);
        int total = Collectibles.total();
        int have = (int) Collectibles.all().stream().filter(this::has).count();
        Component progress = Component.translatable("gui.cottagecoreliving.book.collected", have, total);
        int px = left + panelWidth - 6 - font.width(progress);
        g.text(font, progress, px, top + 5, TEXT, false);
        int barX = left + 6;
        int barW = panelWidth - 12;
        int barY = top + 17;
        g.fill(barX, barY, barX + barW, barY + 4, SLOT_EDGE);
        g.fill(barX, barY, barX + Math.max(1, barW * have / Math.max(1, total)), barY + 4, STRAWBERRY);
        g.text(font, seasonLine, left + 6, top + 23, TEXT_SOFT, false);
    }

    private void drawTabs(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y = top + HEADER + 5;
        for (Tab t : Tab.values()) {
            boolean hover = inside(mouseX, mouseY, left + 4, y, SIDEBAR - 9, 18);
            g.fill(left + 4, y, left + SIDEBAR - 5, y + 18, t == tab ? TAB_SELECTED : hover ? TAB_HOVER : TAB);
            if (t == tab) {
                g.fill(left + 4, y, left + 6, y + 18, BORDER_DARK);
            }
            Component label = Component.translatable(t.key);
            g.text(font, font.plainSubstrByWidth(label.getString(), SIDEBAR - 16), left + 9, y + 5, TEXT, false);
            y += 20;
        }
        if (tab != Tab.SETS) {
            int have = (int) shown.stream().filter(this::has).count();
            String count = have + " / " + shown.size();
            g.text(font, count, left + 9, top + panelHeight - 13, TEXT_SOFT, false);
        }
    }

    private void drawSwatches(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = left + SIDEBAR + 5;
        int y = top + HEADER + 19;
        drawSwatch(g, x, y, 0xFFFFFFFF, setFilter == null, true);
        if (inside(mouseX, mouseY, x, y, 12, 12)) {
            hoveredLabel = Component.translatable("gui.cottagecoreliving.book.all");
        }
        x += 15;
        for (DecorSet set : DecorSet.values()) {
            drawSwatch(g, x, y, 0xFF000000 | set.color(), setFilter == set, false);
            if (inside(mouseX, mouseY, x, y, 12, 12)) {
                hoveredLabel = set.displayName();
            }
            x += 15;
        }
    }

    private void drawSwatch(GuiGraphicsExtractor g, int x, int y, int rgb, boolean active, boolean rainbow) {
        g.fill(x - 1, y - 1, x + 13, y + 13, active ? BORDER_DARK : SLOT_EDGE);
        g.fill(x, y, x + 12, y + 12, rgb);
        if (rainbow) {
            DecorSet[] sets = DecorSet.values();
            for (int i = 0; i < sets.length; i++) {
                g.fill(x + 1 + i * 2, y + 1, x + 3 + i * 2, y + 11, 0xFF000000 | sets[i].color());
            }
        }
    }

    private void drawGrid(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int gx = left + SIDEBAR + 4;
        int gy = gridTop;
        if (shown.isEmpty()) {
            g.text(font, Component.translatable("gui.cottagecoreliving.book.empty"), gx + 2, gy + 4, TEXT_SOFT, false);
        }
        List<int[]> washes = new ArrayList<>();
        for (int i = 0; i < rows * columns; i++) {
            int index = scroll * columns + i;
            int x = gx + (i % columns) * CELL;
            int y = gy + (i / columns) * CELL;
            boolean isSelected = index < shown.size() && shown.get(index) == selected;
            g.fill(x, y, x + CELL - 1, y + CELL - 1, isSelected ? BORDER_DARK : SLOT_EDGE);
            g.fill(x + 1, y + 1, x + CELL - 2, y + CELL - 2, isSelected ? SLOT_SELECTED : SLOT);
            if (index >= shown.size()) {
                continue;
            }
            Collectibles.Entry entry = shown.get(index);
            ItemStack stack = new ItemStack(entry.item());
            g.item(stack, x + 1, y + 1);
            if (!has(entry)) {
                washes.add(new int[]{x + 1, y + 1, x + CELL - 2, y + CELL - 2, MISSING_WASH});
            }
            if (inside(mouseX, mouseY, x, y, CELL - 1, CELL - 1)) {
                washes.add(new int[]{x + 1, y + 1, x + CELL - 2, y + CELL - 2, 0x40FFFFFF});
                hovered = stack;
            }
        }
        drawWashes(g, washes);
        int max = maxScroll();
        int barX = gx + columns * CELL + 2;
        int barHeight = rows * CELL - 1;
        g.fill(barX, gy, barX + 4, gy + barHeight, SLOT_EDGE);
        int knob = Math.max(12, barHeight / (max + 1));
        int knobY = max == 0 ? gy : gy + (barHeight - knob) * scroll / max;
        g.fill(barX, knobY, barX + 4, knobY + knob, BORDER_DARK);
    }

    private void drawDetail(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int dx = left + panelWidth - DETAIL + 4;
        int width = DETAIL - 8;
        int y = top + HEADER + 5;
        if (selected == null) {
            selectFirst();
            if (selected == null) {
                return;
            }
        }
        ItemStack stack = new ItemStack(selected.item());
        g.pose().pushMatrix();
        g.pose().translate(dx + width / 2.0F - 20, y);
        g.pose().scale(2.5F, 2.5F);
        g.item(stack, 0, 0);
        g.pose().popMatrix();
        y += 42;
        for (FormattedCharSequence line : font.split(stack.getHoverName(), width)) {
            g.text(font, line, dx + (width - font.width(line)) / 2, y, TEXT, false);
            y += 10;
        }
        DecorSet set = selected.set();
        Component sub = set != null ? set.displayName() : Component.translatable("group.cottagecoreliving." + selected.group());
        int subColor = set != null ? 0xFF000000 | set.color() : TEXT_SOFT;
        g.text(font, sub, dx + (width - font.width(sub)) / 2, y, subColor, false);
        y += 10;
        Component status = Component.translatable(has(selected) ? "gui.cottagecoreliving.book.collected_one"
                : "gui.cottagecoreliving.book.not_collected");
        g.text(font, status, dx + (width - font.width(status)) / 2, y, has(selected) ? 0xFF5E9443 : 0xFFB3343C, false);
        y += 13;
        y = drawRecipe(g, dx, y, width, mouseX, mouseY);
        int bottom = top + panelHeight - (canTake ? 26 : 4);
        List<Component> lines = new ArrayList<>();
        DecorFamily family = selected.family();
        if (family != null) {
            lines.add(Component.translatable(family.descriptionKey()));
            lines.addAll(DecorBlockItem.hints(family));
        } else {
            lines.add(Component.translatable("item.cottagecoreliving.tablecloth.desc"));
        }
        boolean first = true;
        for (Component line : lines) {
            for (FormattedCharSequence part : font.split(line, width)) {
                if (y + 9 > bottom) {
                    return;
                }
                g.text(font, part, dx, y, first ? TEXT : TEXT_SOFT, false);
                y += 9;
            }
            first = false;
            y += 1;
        }
    }

    private void selectFirst() {
        if (!shown.isEmpty()) {
            select(shown.get(0));
        }
    }

    private int drawRecipe(GuiGraphicsExtractor g, int dx, int y, int width, int mouseX, int mouseY) {
        if (recipe == null) {
            Component none = Component.translatable("gui.cottagecoreliving.book.no_recipe");
            g.text(font, none, dx + (width - font.width(none)) / 2, y, TEXT_SOFT, false);
            return y + 12;
        }
        int gw = 3;
        int gh = 3;
        List<SlotDisplay> ingredients;
        if (recipe instanceof ShapedCraftingRecipeDisplay shaped) {
            gw = shaped.width();
            gh = shaped.height();
            ingredients = shaped.ingredients();
        } else {
            ingredients = ((ShapelessCraftingRecipeDisplay) recipe).ingredients();
        }
        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        int size = 16;
        int gridX = dx + (width - 3 * size) / 2;
        long cycle = Util.getMillis() / 1000L;
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = gridX + col * size;
            int sy = y + row * size;
            g.fill(x, sy, x + size - 1, sy + size - 1, SLOT_EDGE);
            g.fill(x + 1, sy + 1, x + size - 2, sy + size - 2, SLOT);
            int index = recipe instanceof ShapedCraftingRecipeDisplay ? (col < gw && row < gh ? row * gw + col : -1) : i;
            if (index < 0 || index >= ingredients.size()) {
                continue;
            }
            List<ItemStack> options = ingredients.get(index).resolveForStacks(context);
            if (options.isEmpty()) {
                continue;
            }
            ItemStack option = options.get((int) (cycle % options.size()));
            g.pose().pushMatrix();
            g.pose().translate(x, sy);
            g.pose().scale(0.9375F, 0.9375F);
            g.item(option, 0, 0);
            g.pose().popMatrix();
            if (inside(mouseX, mouseY, x, sy, size - 1, size - 1)) {
                hovered = option;
            }
        }
        return y + 3 * size + 5;
    }

    private void drawSetList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = left + SIDEBAR + 4;
        int y = top + HEADER + 5;
        int w = panelWidth - SIDEBAR - DETAIL - 8;
        int h = Math.min(38, (top + panelHeight - y - 4) / DecorSet.values().length - 3);
        for (DecorSet set : DecorSet.values()) {
            boolean active = set == selectedSet;
            boolean hover = inside(mouseX, mouseY, x, y, w, h);
            g.fill(x, y, x + w, y + h, active ? BORDER_DARK : SLOT_EDGE);
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, active ? SLOT_SELECTED : hover ? 0xFFFFF7F0 : SLOT);
            g.fill(x + 1, y + 1, x + 4, y + h - 1, 0xFF000000 | set.color());
            List<Collectibles.Entry> pieces = Collectibles.inSet(set);
            int have = (int) pieces.stream().filter(this::has).count();
            DecorSetIcons.render(g, set, x + 8, y + (h - 16) / 2);
            g.text(font, set.displayName(), x + 30, y + 5, TEXT, false);
            Component progress = Component.translatable("gui.cottagecoreliving.book.set_progress", have, pieces.size());
            g.text(font, progress, x + 30, y + 16, TEXT_SOFT, false);
            int barW = w - 36;
            if (h >= 32) {
                g.fill(x + 30, y + h - 8, x + 30 + barW, y + h - 5, SLOT_EDGE);
                g.fill(x + 30, y + h - 8, x + 30 + barW * have / Math.max(1, pieces.size()), y + h - 5,
                        0xFF000000 | set.color());
            }
            y += h + 3;
        }
    }

    private void drawSetDetail(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int dx = left + panelWidth - DETAIL + 4;
        int width = DETAIL - 8;
        int y = top + HEADER + 5;
        if (selectedSet == null) {
            return;
        }
        Component name = selectedSet.displayName();
        g.text(font, name, dx + (width - font.width(name)) / 2, y, 0xFF000000 | selectedSet.color(), false);
        y += 13;
        int perRow = width / CELL;
        List<Collectibles.Entry> pieces = Collectibles.inSet(selectedSet);
        int bottom = top + panelHeight - (canTake ? 26 : 4);
        List<int[]> washes = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            int x = dx + (i % perRow) * CELL;
            int sy = y + (i / perRow) * CELL;
            if (sy + CELL > bottom) {
                break;
            }
            Collectibles.Entry entry = pieces.get(i);
            g.fill(x, sy, x + CELL - 1, sy + CELL - 1, SLOT_EDGE);
            g.fill(x + 1, sy + 1, x + CELL - 2, sy + CELL - 2, SLOT);
            ItemStack stack = new ItemStack(entry.item());
            g.item(stack, x + 1, sy + 1);
            if (!has(entry)) {
                washes.add(new int[]{x + 1, sy + 1, x + CELL - 2, sy + CELL - 2, MISSING_WASH});
            }
            if (inside(mouseX, mouseY, x, sy, CELL - 1, CELL - 1)) {
                hovered = stack;
            }
        }
        drawWashes(g, washes);
    }

    /** Washes and highlights go over the items, so they are drawn in the next stratum. */
    private static void drawWashes(GuiGraphicsExtractor g, List<int[]> washes) {
        if (washes.isEmpty()) {
            return;
        }
        g.nextStratum();
        for (int[] w : washes) {
            g.fill(w[0], w[1], w[2], w[3], w[4]);
        }
    }

    /** Opens a tab and selects a decoration, as if clicked (used by the dev showcase). */
    public void previewSelect(int tabIndex, Item item) {
        tab = Tab.values()[tabIndex];
        refilter();
        for (Collectibles.Entry entry : shown) {
            if (entry.item() == item) {
                select(entry);
            }
        }
        updateButtons();
    }

    /** Opens the Sets tab with a set selected (used by the dev showcase). */
    public void previewSet(DecorSet set) {
        tab = Tab.SETS;
        selectedSet = set;
        updateButtons();
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        double mouseX = event.x();
        double mouseY = event.y();
        int y = top + HEADER + 5;
        for (Tab t : Tab.values()) {
            if (inside(mouseX, mouseY, left + 4, y, SIDEBAR - 9, 18)) {
                tab = t;
                scroll = 0;
                refilter();
                if (selected != null && !shown.contains(selected)) {
                    selected = null;
                    recipe = null;
                }
                updateButtons();
                return true;
            }
            y += 20;
        }
        if (tab == Tab.SETS) {
            int x = left + SIDEBAR + 4;
            int sy = top + HEADER + 5;
            int w = panelWidth - SIDEBAR - DETAIL - 8;
            int h = Math.min(38, (top + panelHeight - sy - 4) / DecorSet.values().length - 3);
            for (DecorSet set : DecorSet.values()) {
                if (inside(mouseX, mouseY, x, sy, w, h)) {
                    selectedSet = set;
                    updateButtons();
                    return true;
                }
                sy += h + 3;
            }
            return false;
        }
        int sx = left + SIDEBAR + 5;
        int swy = top + HEADER + 19;
        if (inside(mouseX, mouseY, sx, swy, 12, 12)) {
            setFilter = null;
            refilter();
            return true;
        }
        for (DecorSet set : DecorSet.values()) {
            sx += 15;
            if (inside(mouseX, mouseY, sx, swy, 12, 12)) {
                setFilter = setFilter == set ? null : set;
                scroll = 0;
                refilter();
                return true;
            }
        }
        int gx = left + SIDEBAR + 4;
        if (inside(mouseX, mouseY, gx, gridTop, columns * CELL, rows * CELL)) {
            int col = (int) (mouseX - gx) / CELL;
            int row = (int) (mouseY - gridTop) / CELL;
            int index = (scroll + row) * columns + col;
            if (index < shown.size()) {
                select(shown.get(index));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tab != Tab.SETS) {
            scroll = Mth.clamp(scroll - (int) Math.signum(scrollY), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (search.isFocused() && !event.isEscape()) {
            return search.keyPressed(event) || search.canConsumeInput();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** The icon drawn beside each set in the Sets tab: the set's teapot. */
    private static final class DecorSetIcons {
        private DecorSetIcons() {
        }

        static void render(GuiGraphicsExtractor g, DecorSet set, int x, int y) {
            Collectibles.Entry teapot = Collectibles.byId(set.id() + "_teapot");
            if (teapot != null) {
                g.item(new ItemStack(teapot.item()), x, y);
            }
        }
    }
}
