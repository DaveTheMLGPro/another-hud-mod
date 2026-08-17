package net.davethemlgpro.client.module.miningsession;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.module.HudModule;
import net.davethemlgpro.client.translation.TranslationKey;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class MiningSessionHudModule implements HudModule<MiningSessionHudConfig> {
	public static final Identifier ID = AnotherHUDMod.id("mining_session");
	private static final Component DISPLAY_NAME = TranslationKey.MODULE_MINING_SESSION.component();
	private static final int ICON_LEFT = 7;
	private static final int TEXT_LEFT = 27;
	private static final int RIGHT_PADDING = 7;
	private static final int PREVIEW_SECONDS = 5_077;
	private static final long GOAL_SCROLL_NANOS_PER_ROW = 2_000_000_000L;

	private final MiningSessionState session = new MiningSessionState();
	private Map<MiningSessionTrackedItem, Integer> inventoryCounts = Map.of();
	private Map<String, Integer> goalInventoryCounts = Map.of();
	private Map<String, Integer> observedTrackedCounts = Map.of();
	private final Map<String, Integer> sessionTrackedGains = new HashMap<>();
	private boolean inventoryBaselinePending = true;
	private long liveDataRevision;
	private RenderSnapshot liveSnapshot;
	private RenderSnapshot previewSnapshot;

	@Override
	public Identifier id() {
		return ID;
	}

	@Override
	public Component displayName() {
		return DISPLAY_NAME;
	}

	@Override
	public Component description() {
		return TranslationKey.MODULE_MINING_SESSION_DESCRIPTION.component();
	}

	@Override
	public HudSize measure(Minecraft minecraft, MiningSessionHudConfig config) {
		HudSize size = snapshot(minecraft, config, false).size();
		return scaledSize(size, fittedScale(minecraft, config, size));
	}

	@Override
	public HudSize measureEditorPreview(Minecraft minecraft, MiningSessionHudConfig config) {
		HudSize size = snapshot(minecraft, config, true).size();
		return scaledSize(size, fittedScale(minecraft, config, size));
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
					   MiningSessionHudConfig config, HudBounds bounds) {
		RenderSnapshot snapshot = snapshot(minecraft, config, false);
		renderPanel(graphics, minecraft, config, bounds, snapshot,
			session.status(), elapsedSeconds(System.nanoTime()), fittedScale(minecraft, config, snapshot.size()));
	}

	@Override
	public void renderEditorPreview(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
								MiningSessionHudConfig config, HudBounds bounds) {
		RenderSnapshot snapshot = snapshot(minecraft, config, true);
		renderPanel(graphics, minecraft, config, bounds, snapshot,
			MiningSessionStatus.RUNNING, PREVIEW_SECONDS,
			fittedScale(minecraft, config, snapshot.size()));
	}

	public void toggleSession() {
		boolean startingNewSession = session.status() == MiningSessionStatus.IDLE;
		session.toggle(System.nanoTime());
		if (startingNewSession && session.status() == MiningSessionStatus.RUNNING) {
			clearSessionInventoryProgress();
		}
	}

	public void resetSession() {
		session.reset();
		inventoryCounts = Map.of();
		goalInventoryCounts = Map.of();
		observedTrackedCounts = Map.of();
		clearSessionInventoryProgress();
		liveDataRevision++;
	}

	public void pauseSession() {
		session.pause(System.nanoTime());
	}

	public MiningSessionStatus status() {
		return session.status();
	}

	public void recordBlock(BlockState state, MiningSessionHudConfig config) {
		if (state == null || state.isAir()) {
			return;
		}
		if (config.isOresOnly() && !state.is(ConventionalBlockTags.ORES)) {
			return;
		}
		if (session.status() == MiningSessionStatus.IDLE && config.isAutoStartOnBlockBreak()) {
			session.startOrResume(System.nanoTime());
			clearSessionInventoryProgress();
		}
		Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		if (id != null && session.status() == MiningSessionStatus.RUNNING) {
			session.recordBlock(id.toString());
			liveDataRevision++;
		}
	}

	public void updateInventory(LocalPlayer player, MiningSessionHudConfig config) {
		if (player == null) {
			setInventoryCounts(Map.of());
			setGoalInventoryCounts(Map.of());
			return;
		}
		Map<MiningSessionTrackedItem, Integer> counts = new HashMap<>();
		List<MiningSessionTrackedItem> trackedItems = config.getTrackedInventoryEntries();
		for (MiningSessionTrackedItem entry : trackedItems) {
			counts.put(entry, 0);
		}
		Map<String, Integer> goalCounts = new HashMap<>();
		for (MiningSessionItemGoal goal : config.getItemGoals()) {
			goalCounts.put(goal.itemId(), 0);
		}
		for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
			if (stack.isEmpty()) {
				continue;
			}
			for (MiningSessionTrackedItem entry : trackedItems) {
				if (entry.matches(stack)) {
					counts.merge(entry, stack.getCount(), Integer::sum);
				}
			}
			String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
			if (goalCounts.containsKey(itemId)) {
				goalCounts.merge(itemId, stack.getCount(), Integer::sum);
			}
		}
		setInventoryCounts(Map.copyOf(counts));
		setGoalInventoryCounts(Map.copyOf(goalCounts));
		Map<String, Integer> trackedCounts = new HashMap<>();
		for (MiningSessionTrackedItem entry : trackedItems) {
			trackedCounts.put(entry.identityKey(), counts.getOrDefault(entry, 0));
		}
		captureSessionGains(trackedCounts);
	}

	public void recordPickup(ItemStack stack, int amount, MiningSessionHudConfig config) {
		if (stack == null || stack.isEmpty() || amount <= 0
			|| session.status() != MiningSessionStatus.RUNNING) {
			return;
		}
		boolean changed = false;
		for (MiningSessionTrackedItem entry : config.getTrackedInventoryEntries()) {
			if (entry.matches(stack)) {
				sessionTrackedGains.merge(entry.identityKey(), amount, Integer::sum);
				observedTrackedCounts = withAddedCount(observedTrackedCounts, entry.identityKey(), amount);
				changed = true;
			}
		}
		if (changed) {
			liveDataRevision++;
		}
	}

	private static Map<String, Integer> withAddedCount(Map<String, Integer> source, String key, int amount) {
		Map<String, Integer> updated = new HashMap<>(source);
		updated.merge(key, amount, Integer::sum);
		return Map.copyOf(updated);
	}

	private void captureSessionGains(Map<String, Integer> trackedCounts) {
		boolean changed = false;
		if (session.status() == MiningSessionStatus.RUNNING && !inventoryBaselinePending) {
			changed |= addPositiveDeltas(observedTrackedCounts, trackedCounts, sessionTrackedGains);
		}
		observedTrackedCounts = Map.copyOf(trackedCounts);
		if (session.status() == MiningSessionStatus.RUNNING) {
			inventoryBaselinePending = false;
		}
		if (changed) {
			liveDataRevision++;
		}
	}

	static boolean addPositiveDeltas(Map<String, Integer> previous, Map<String, Integer> current,
									 Map<String, Integer> gains) {
		boolean changed = false;
		for (Map.Entry<String, Integer> entry : current.entrySet()) {
			int before = previous.getOrDefault(entry.getKey(), entry.getValue());
			int delta = entry.getValue() - before;
			if (delta > 0) {
				gains.merge(entry.getKey(), delta, Integer::sum);
				changed = true;
			}
		}
		return changed;
	}

	private void clearSessionInventoryProgress() {
		sessionTrackedGains.clear();
		inventoryBaselinePending = true;
		liveDataRevision++;
	}

	private void setInventoryCounts(Map<MiningSessionTrackedItem, Integer> counts) {
		if (!inventoryCounts.equals(counts)) {
			inventoryCounts = counts;
			liveDataRevision++;
		}
	}

	private RenderSnapshot snapshot(Minecraft minecraft, MiningSessionHudConfig config, boolean preview) {
		long dataRevision = preview ? 0L : liveDataRevision;
		SnapshotKey key = snapshotKey(minecraft, config, dataRevision);
		RenderSnapshot cached = preview ? previewSnapshot : liveSnapshot;
		if (cached != null && cached.key().equals(key)) {
			return cached;
		}
		List<Row> blockRows = preview ? previewRows(config) : liveRows(config);
		List<Row> itemRows = inventoryRows(config, preview);
		List<Row> goalRows = goalRows(config, preview);
		SessionTotals totals = sessionTotals(config, preview);
		RenderSnapshot rebuilt = new RenderSnapshot(key,
			unscaledSize(minecraft, config, blockRows, itemRows, goalRows, totals),
			blockRows, itemRows, goalRows, totals);
		if (preview) {
			previewSnapshot = rebuilt;
		} else {
			liveSnapshot = rebuilt;
		}
		return rebuilt;
	}

	private static SnapshotKey snapshotKey(Minecraft minecraft, MiningSessionHudConfig config, long dataRevision) {
		List<TrackedItemKey> trackedItems = config.getTrackedInventoryEntries().stream()
			.map(entry -> new TrackedItemKey(entry.identityKey(), entry.unitValue(), entry.displayName()))
			.toList();
		return new SnapshotKey(minecraft.font, config.getRowStyle(), config.getMaxVisibleBlocks(),
			config.getRowSpacing(), config.getInventoryOrder(), trackedItems, config.getMaxVisibleGoals(),
			config.isShowStatisticsFooter(), config.isShowFooterBlocks(), config.isShowFooterItems(),
			config.isShowFooterValue(), config.getGoalMode(),
			config.getItemGoals().stream().map(goal -> goal.itemId() + "=" + goal.targetAmount()).toList(),
			config.getValueGoal(), dataRevision);
	}

	private void setGoalInventoryCounts(Map<String, Integer> counts) {
		if (!goalInventoryCounts.equals(counts)) {
			goalInventoryCounts = counts;
			liveDataRevision++;
		}
	}

	private HudSize unscaledSize(Minecraft minecraft, MiningSessionHudConfig config,
							 List<Row> blockRows, List<Row> itemRows, List<Row> goalRows,
							 SessionTotals totals) {
		int contentWidth = Math.max(minecraft.font.width(TranslationKey.MINING_SESSION_TITLE.component()) + 38,
			minecraft.font.width(formatTime(PREVIEW_SECONDS)) + TEXT_LEFT);
		for (Row row : blockRows) {
			int countWidth = minecraft.font.width(row.count());
			int rowWidth = TEXT_LEFT + countWidth + RIGHT_PADDING;
			if (config.getRowStyle() == MiningSessionRowStyle.ICON_NAME_AMOUNT) {
				rowWidth += minecraft.font.width(row.name()) + 8;
			}
			contentWidth = Math.max(contentWidth, rowWidth);
		}
		for (Row row : itemRows) {
			int countWidth = minecraft.font.width(row.count());
			int rowWidth = TEXT_LEFT + countWidth + RIGHT_PADDING;
			if (config.getRowStyle() == MiningSessionRowStyle.ICON_NAME_AMOUNT) {
				rowWidth += minecraft.font.width(row.name()) + 8;
			}
			contentWidth = Math.max(contentWidth, rowWidth);
		}
		for (Row row : goalRows) {
			contentWidth = Math.max(contentWidth, TEXT_LEFT + minecraft.font.width(row.name())
				+ minecraft.font.width(row.count()) + RIGHT_PADDING + 8);
		}
		if (config.getGoalMode() == MiningSessionGoalMode.VALUE && config.getValueGoal().signum() > 0) {
			contentWidth = Math.max(contentWidth, minecraft.font.width(valueGoalText(totals.inventoryValue(),
				config.getValueGoal())) + RIGHT_PADDING * 2);
		}
		if (showStatisticsFooter(config)) {
			contentWidth = Math.max(contentWidth, minecraft.font.width(statisticsText(config, totals))
				+ RIGHT_PADDING * 2);
		}
		boolean valueGoal = config.getGoalMode() == MiningSessionGoalMode.VALUE
			&& config.getValueGoal().signum() > 0;
		int visibleGoalRows = Math.min(goalRows.size(), config.getMaxVisibleGoals());
		return MiningSessionLayout.measure(config.getRowStyle(), contentWidth, blockRows.size(), itemRows.size(),
			visibleGoalRows, valueGoal, showStatisticsFooter(config), config.getRowSpacing());
	}

	private void renderPanel(GuiGraphicsExtractor graphics, Minecraft minecraft, MiningSessionHudConfig config,
							 HudBounds bounds, RenderSnapshot snapshot,
							 MiningSessionStatus status, long elapsedSeconds, double resolvedScale) {
		HudSize size = snapshot.size();
		List<Row> blockRows = snapshot.blockRows();
		List<Row> itemRows = snapshot.itemRows();
		List<Row> goalRows = snapshot.goalRows();
		var matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			float scale = (float) resolvedScale;
			matrices.translate(bounds.x(), bounds.y());
			matrices.scale(scale, scale);
			drawRoundedBackground(graphics, 0, 0, size.width(), size.height(), config.getBackgroundColor());

			graphics.item(new ItemStack(Items.IRON_PICKAXE), ICON_LEFT, 5);
			graphics.text(minecraft.font, TranslationKey.MINING_SESSION_TITLE.component(), TEXT_LEFT, 9,
				config.getTextColor());
			int statusColor = switch (status) {
				case IDLE -> 0xFF777777;
				case RUNNING -> config.getAccentColor();
				case PAUSED -> 0xFFFFCC55;
			};
			graphics.fill(size.width() - 11, 9, size.width() - 6, 14, statusColor);
			graphics.text(minecraft.font, Component.literal(formatTime(elapsedSeconds)), TEXT_LEFT,
				MiningSessionLayout.PANEL_PADDING + MiningSessionLayout.HEADER_HEIGHT + 2,
				status == MiningSessionStatus.RUNNING ? config.getAccentColor() : config.getTextColor());

			int y = MiningSessionLayout.PANEL_PADDING + MiningSessionLayout.HEADER_HEIGHT
				+ MiningSessionLayout.TIMER_HEIGHT;
			graphics.fill(MiningSessionLayout.PANEL_PADDING, y, size.width() - MiningSessionLayout.PANEL_PADDING,
				y + 1, 0x45FFFFFF);
			y += MiningSessionLayout.DIVIDER_HEIGHT;
			y = renderRows(graphics, minecraft, config, size.width(), blockRows, y);

			if (!itemRows.isEmpty()) {
				graphics.fill(MiningSessionLayout.PANEL_PADDING, y,
					size.width() - MiningSessionLayout.PANEL_PADDING, y + 1, 0x45FFFFFF);
				y++;
				graphics.text(minecraft.font, TranslationKey.MINING_SESSION_INVENTORY.component(),
					MiningSessionLayout.PANEL_PADDING, y + 2, withOpacity(config.getTextColor(), 0.7F));
				y += MiningSessionLayout.SECTION_HEIGHT;
				y = renderRows(graphics, minecraft, config, size.width(), itemRows, y);
			}

			boolean valueGoal = config.getGoalMode() == MiningSessionGoalMode.VALUE
				&& config.getValueGoal().signum() > 0;
			if (!goalRows.isEmpty() || valueGoal) {
				graphics.fill(MiningSessionLayout.PANEL_PADDING, y,
					size.width() - MiningSessionLayout.PANEL_PADDING, y + 1, 0x45FFFFFF);
				y++;
				graphics.text(minecraft.font, TranslationKey.MINING_SESSION_GOAL.component(),
					MiningSessionLayout.PANEL_PADDING, y + 2, withOpacity(config.getTextColor(), 0.7F));
				y += MiningSessionLayout.SECTION_HEIGHT;
				if (valueGoal) {
					y = renderValueGoal(graphics, minecraft, config, size.width(), y,
						snapshot.totals().inventoryValue(), config.getValueGoal());
				} else {
					y = renderAnimatedGoalRows(graphics, minecraft, config, size.width(), goalRows, y);
				}
			}

			if (showStatisticsFooter(config)) {
				graphics.fill(MiningSessionLayout.PANEL_PADDING, y,
					size.width() - MiningSessionLayout.PANEL_PADDING, y + 1, 0x45FFFFFF);
				y++;
				Component footer = statisticsText(config, snapshot.totals());
				graphics.centeredText(minecraft.font, footer, size.width() / 2, y + 5,
					withOpacity(config.getTextColor(), 0.75F));
			}
		} finally {
			matrices.popMatrix();
		}
	}

	private int renderAnimatedGoalRows(GuiGraphicsExtractor graphics, Minecraft minecraft,
									MiningSessionHudConfig config, int width, List<Row> rows, int y) {
		int visibleRows = Math.min(rows.size(), config.getMaxVisibleGoals());
		if (rows.size() <= visibleRows) {
			return renderRows(graphics, minecraft, config, width, rows, y);
		}
		int step = MiningSessionLayout.ROW_HEIGHT + config.getRowSpacing();
		double offset = animatedGoalOffset(rows.size(), visibleRows, System.nanoTime());
		int firstRow = Math.min((int) Math.floor(offset), rows.size() - visibleRows);
		float fractionalOffset = (float) (offset - firstRow);
		int lastRow = Math.min(rows.size(), firstRow + visibleRows + 1);
		int clipHeight = visibleRows * MiningSessionLayout.ROW_HEIGHT
			+ (visibleRows - 1) * config.getRowSpacing();
		graphics.enableScissor(MiningSessionLayout.PANEL_PADDING, y,
			width - MiningSessionLayout.PANEL_PADDING, y + clipHeight);
		var matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			matrices.translate(0, -fractionalOffset * step);
			renderRows(graphics, minecraft, config, width, rows.subList(firstRow, lastRow), y);
		} finally {
			matrices.popMatrix();
			graphics.disableScissor();
		}
		return y + clipHeight;
	}

	static double animatedGoalOffset(int totalRows, int visibleRows, long nowNanos) {
		int maximumOffset = Math.max(0, totalRows - visibleRows);
		if (maximumOffset == 0) {
			return 0.0D;
		}
		long cycleNanos = GOAL_SCROLL_NANOS_PER_ROW * maximumOffset * 2L;
		double phase = Math.floorMod(nowNanos, cycleNanos) / (double) GOAL_SCROLL_NANOS_PER_ROW;
		return phase <= maximumOffset ? phase : maximumOffset * 2.0D - phase;
	}

	private int renderValueGoal(GuiGraphicsExtractor graphics, Minecraft minecraft, MiningSessionHudConfig config,
								int width, int y, BigDecimal current, BigDecimal target) {
		Component text = valueGoalText(current, target);
		graphics.centeredText(minecraft.font, text, width / 2, y + 1, config.getTextColor());
		int barX = MiningSessionLayout.PANEL_PADDING;
		int barY = y + 13;
		int barWidth = width - MiningSessionLayout.PANEL_PADDING * 2;
		graphics.fill(barX, barY, barX + barWidth, barY + 6, 0x66000000);
		double progress = target.signum() == 0 ? 0.0D
			: Math.clamp(current.divide(target, 4, java.math.RoundingMode.HALF_UP).doubleValue(), 0.0D, 1.0D);
		graphics.fill(barX + 1, barY + 1,
			barX + 1 + (int) Math.round((barWidth - 2) * progress), barY + 5, config.getAccentColor());
		return y + MiningSessionLayout.VALUE_GOAL_HEIGHT;
	}

	private int renderRows(GuiGraphicsExtractor graphics, Minecraft minecraft, MiningSessionHudConfig config,
						   int width, List<Row> rows, int startY) {
		int y = startY;
		for (Row row : rows) {
			graphics.item(row.icon(), ICON_LEFT, y + 2);
			int countX = width - RIGHT_PADDING - minecraft.font.width(row.count());
			int textY = row.totalValueText() == null ? y + 6 : y + 1;
			if (config.getRowStyle() == MiningSessionRowStyle.ICON_NAME_AMOUNT) {
				int maximumNameWidth = Math.max(0, countX - TEXT_LEFT - 6);
				Component name = fitText(minecraft, row.name(), maximumNameWidth);
				graphics.text(minecraft.font, name, TEXT_LEFT, textY, config.getTextColor());
			}
			graphics.text(minecraft.font, row.count(), countX, textY, config.getTextColor());
			if (row.totalValueText() != null) {
				drawRightAlignedFittedText(graphics, minecraft,
					row.totalValueText(), width - RIGHT_PADDING, y + 10,
					width - RIGHT_PADDING - TEXT_LEFT, config.getAccentColor());
			}
			y += MiningSessionLayout.ROW_HEIGHT + config.getRowSpacing();
		}
		return rows.isEmpty() ? startY : y - config.getRowSpacing();
	}

	private List<Row> liveRows(MiningSessionHudConfig config) {
		return session.minedBlocks().entrySet().stream()
			.sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
				.thenComparing(Map.Entry::getKey))
			.limit(config.getMaxVisibleBlocks())
			.map(entry -> blockRow(entry.getKey(), entry.getValue()))
			.toList();
	}

	private List<Row> previewRows(MiningSessionHudConfig config) {
		return List.of(
			blockRow("minecraft:deepslate", 1_284),
			blockRow("minecraft:iron_ore", 63),
			blockRow("minecraft:gold_ore", 21),
			blockRow("minecraft:diamond_ore", 7)
		).subList(0, Math.min(4, config.getMaxVisibleBlocks()));
	}

	private List<Row> inventoryRows(MiningSessionHudConfig config, boolean preview) {
		List<MiningSessionTrackedItem> entries = new ArrayList<>();
		Map<MiningSessionTrackedItem, Integer> amounts = new HashMap<>();
		Map<MiningSessionTrackedItem, ItemStack> icons = new HashMap<>();
		Map<MiningSessionTrackedItem, Component> names = new HashMap<>();
		int previewIndex = 0;
		for (MiningSessionTrackedItem entry : config.getTrackedInventoryEntries()) {
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.itemId()));
			if (item == null || item == Items.AIR) {
				continue;
			}
			int amount = preview ? Math.max(1, previewIndex++ * 16 + 7)
				: inventoryCounts.getOrDefault(entry, 0);
			ItemStack stack = new ItemStack(item);
			entry.applyAppearance(stack);
			Component name = entry.displayName() == null
				? stack.getHoverName() : Component.literal(entry.displayName());
			entries.add(entry);
			amounts.put(entry, amount);
			icons.put(entry, stack);
			names.put(entry, name);
		}
		sortInventoryEntries(config.getInventoryOrder(), entries,
			entry -> amounts.getOrDefault(entry, 0), entry -> names.get(entry).getString());

		List<Row> rows = new ArrayList<>();
		for (MiningSessionTrackedItem entry : entries) {
			int amount = amounts.getOrDefault(entry, 0);
			BigDecimal unitValue = entry.unitValue();
			BigDecimal totalValue = unitValue.signum() == 0 ? null : calculateValue(unitValue, amount);
			rows.add(row(icons.get(entry), names.get(entry), amount, totalValue));
		}
		return rows;
	}

	private List<Row> goalRows(MiningSessionHudConfig config, boolean preview) {
		if (config.getGoalMode() != MiningSessionGoalMode.ITEMS) {
			return List.of();
		}
		List<Row> rows = new ArrayList<>();
		for (MiningSessionItemGoal goal : config.getItemGoals()) {
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(goal.itemId()));
			if (item == null || item == Items.AIR) {
				continue;
			}
			int progress = preview ? Math.max(1, goal.targetAmount() * 2 / 3)
				: goalInventoryCounts.getOrDefault(goal.itemId(), 0);
			rows.add(new Row(new ItemStack(item), new ItemStack(item).getHoverName(),
				Component.literal(formatCount(progress) + " / " + formatCount(goal.targetAmount())), null));
		}
		return rows;
	}

	private SessionTotals sessionTotals(MiningSessionHudConfig config, boolean preview) {
		if (preview) {
			BigDecimal previewValue = config.getValueGoal().signum() > 0
				? config.getValueGoal().multiply(new BigDecimal("0.75")) : new BigDecimal("1124133");
			return new SessionTotals(125, 245, previewValue, previewValue);
		}
		int blocks = session.minedBlocks().values().stream().mapToInt(Integer::intValue).sum();
		int items = sessionTrackedGains.values().stream().mapToInt(Integer::intValue).sum();
		BigDecimal value = BigDecimal.ZERO;
		BigDecimal inventoryValue = BigDecimal.ZERO;
		for (MiningSessionTrackedItem entry : config.getTrackedInventoryEntries()) {
			value = value.add(calculateValue(entry.unitValue(),
				sessionTrackedGains.getOrDefault(entry.identityKey(), 0)));
			inventoryValue = inventoryValue.add(calculateValue(entry.unitValue(),
				inventoryCounts.getOrDefault(entry, 0)));
		}
		return new SessionTotals(blocks, items, value, inventoryValue);
	}

	private static boolean showStatisticsFooter(MiningSessionHudConfig config) {
		return config.isShowStatisticsFooter()
			&& (config.isShowFooterBlocks() || config.isShowFooterItems() || config.isShowFooterValue());
	}

	private static Component statisticsText(MiningSessionHudConfig config, SessionTotals totals) {
		List<Component> parts = new ArrayList<>();
		if (config.isShowFooterBlocks()) {
			parts.add(TranslationKey.MINING_SESSION_STAT_BLOCKS.component(formatCount(totals.blocks())));
		}
		if (config.isShowFooterItems()) {
			parts.add(TranslationKey.MINING_SESSION_STAT_ITEMS.component(formatCount(totals.items())));
		}
		if (config.isShowFooterValue()) {
			parts.add(TranslationKey.MINING_SESSION_STAT_VALUE.component(formatValue(totals.value())));
		}
		Component result = Component.empty();
		for (int index = 0; index < parts.size(); index++) {
			if (index > 0) {
				result = result.copy().append(" • ");
			}
			result = result.copy().append(parts.get(index));
		}
		return result;
	}

	private static Component valueGoalText(BigDecimal current, BigDecimal target) {
		return Component.literal(formatValue(current) + " / " + formatValue(target));
	}

	static void sortInventoryEntries(MiningSessionInventoryOrder order, List<MiningSessionTrackedItem> entries,
								 ToIntFunction<MiningSessionTrackedItem> amount,
								 Function<MiningSessionTrackedItem, String> name) {
		MiningSessionInventoryOrder safeOrder = order == null ? MiningSessionInventoryOrder.CUSTOM : order;
		Comparator<MiningSessionTrackedItem> amountDescending = Comparator
			.comparingInt(amount).reversed();
		Comparator<MiningSessionTrackedItem> comparator = switch (safeOrder) {
			case CUSTOM -> null;
			case AMOUNT -> amountDescending;
			case CUSTOM_VALUE -> entries.stream().noneMatch(entry -> entry.unitValue().signum() > 0)
				? amountDescending
				: Comparator.comparing(MiningSessionTrackedItem::unitValue).reversed();
			case TOTAL_VALUE -> Comparator.comparing(
				(MiningSessionTrackedItem entry) -> entry.unitValue()
					.multiply(BigDecimal.valueOf(amount.applyAsInt(entry)))).reversed();
			case ALPHABETICAL -> Comparator.comparing(name, String.CASE_INSENSITIVE_ORDER)
				.thenComparing(name);
		};
		if (comparator != null) {
			entries.sort(comparator);
		}
	}

	private static Row blockRow(String id, int amount) {
		Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
		if (block == null) {
			return row(new ItemStack(Items.BARRIER), Component.literal(id), amount, null);
		}
		ItemStack icon = new ItemStack(block.asItem());
		if (icon.isEmpty()) {
			icon = new ItemStack(Items.BARRIER);
		}
		return row(icon, block.getName(), amount, null);
	}

	private static Row row(ItemStack icon, Component name, int amount, BigDecimal totalValue) {
		return new Row(icon, name, Component.literal(formatCount(amount)),
			totalValue == null ? null : Component.literal(formatValue(totalValue)));
	}

	private long elapsedSeconds(long nowNanos) {
		return session.elapsedNanos(nowNanos) / 1_000_000_000L;
	}

	static String formatTime(long totalSeconds) {
		long safeSeconds = Math.max(0L, totalSeconds);
		long hours = safeSeconds / 3_600L;
		long minutes = safeSeconds % 3_600L / 60L;
		long seconds = safeSeconds % 60L;
		return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
	}

	private static String formatCount(int amount) {
		return String.format(Locale.ROOT, "%,d", Math.max(0, amount));
	}

	static BigDecimal calculateValue(BigDecimal unitValue, int amount) {
		return unitValue.multiply(BigDecimal.valueOf(Math.max(0, amount)));
	}

	static String formatValue(BigDecimal value) {
		DecimalFormat format = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
		format.setRoundingMode(java.math.RoundingMode.HALF_UP);
		return format.format(value == null ? BigDecimal.ZERO : value);
	}

	private static Component fitText(Minecraft minecraft, Component text, int maximumWidth) {
		if (maximumWidth <= 0 || minecraft.font.width(text) <= maximumWidth) {
			return text;
		}
		String ellipsis = "…";
		int available = Math.max(0, maximumWidth - minecraft.font.width(ellipsis));
		return Component.literal(minecraft.font.plainSubstrByWidth(text.getString(), available) + ellipsis);
	}

	private static void drawRightAlignedFittedText(GuiGraphicsExtractor graphics, Minecraft minecraft,
										   Component text, int rightX, int y, int maximumWidth, int color) {
		int textWidth = minecraft.font.width(text);
		if (textWidth <= maximumWidth) {
			graphics.text(minecraft.font, text, rightX - textWidth, y, color);
			return;
		}
		float scale = (float) maximumWidth / textWidth;
		var matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			matrices.translate(rightX, y);
			matrices.scale(scale, scale);
			graphics.text(minecraft.font, text, -textWidth, 0, color);
		} finally {
			matrices.popMatrix();
		}
	}

	private static void drawRoundedBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
									  int color) {
		graphics.fill(x + 2, y, x + width - 2, y + height, color);
		graphics.fill(x, y + 2, x + width, y + height - 2, color);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color);
	}

	private static int withOpacity(int color, float opacity) {
		int alpha = Math.round((color >>> 24) * Math.clamp(opacity, 0.0F, 1.0F));
		return color & 0x00FFFFFF | alpha << 24;
	}

	private static HudSize scaledSize(HudSize size, double scale) {
		return new HudSize((int) Math.ceil(size.width() * scale), (int) Math.ceil(size.height() * scale));
	}

	private record Row(ItemStack icon, Component name, Component count, Component totalValueText) {
	}

	private record RenderSnapshot(SnapshotKey key, HudSize size, List<Row> blockRows, List<Row> itemRows,
							  List<Row> goalRows, SessionTotals totals) {
		private RenderSnapshot {
			blockRows = List.copyOf(blockRows);
			itemRows = List.copyOf(itemRows);
			goalRows = List.copyOf(goalRows);
		}
	}

	private static double fittedScale(Minecraft minecraft, MiningSessionHudConfig config, HudSize size) {
		return fitScale(config.getUiScale(), size,
			minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
	}

	static double fitScale(double requestedScale, HudSize size, int screenWidth, int screenHeight) {
		if (size.width() <= 0 || size.height() <= 0) {
			return requestedScale;
		}
		double widthScale = Math.max(1, screenWidth) / (double) size.width();
		double heightScale = Math.max(1, screenHeight) / (double) size.height();
		return Math.min(requestedScale, Math.min(widthScale, heightScale));
	}

	private record SnapshotKey(Object font, MiningSessionRowStyle rowStyle, int maxVisibleBlocks, int rowSpacing,
							   MiningSessionInventoryOrder inventoryOrder,
							   List<TrackedItemKey> trackedItems, int maxVisibleGoals, boolean statisticsFooter,
							   boolean footerBlocks, boolean footerItems, boolean footerValue,
							   MiningSessionGoalMode goalMode, List<String> itemGoals,
							   BigDecimal valueGoal, long dataRevision) {
	}

	private record TrackedItemKey(String identity, BigDecimal unitValue, String displayName) {
	}

	private record SessionTotals(int blocks, int items, BigDecimal value, BigDecimal inventoryValue) {
	}
}
