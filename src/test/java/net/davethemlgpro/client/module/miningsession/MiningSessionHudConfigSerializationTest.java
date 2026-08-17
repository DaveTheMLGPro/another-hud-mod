package net.davethemlgpro.client.module.miningsession;

import com.google.gson.Gson;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningSessionHudConfigSerializationTest {
	private final Gson gson = new Gson();

	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		for (String itemId : List.of("minecraft:paper", "minecraft:jungle_leaves", "minecraft:oak_log")) {
			BuiltInRegistries.ITEM.get(Identifier.parse(itemId)).orElseThrow()
				.bindComponents(DataComponentMap.EMPTY);
		}
	}

	@Test
	void roundTripsSettingsAndLayout() {
		MiningSessionHudConfig original = new MiningSessionHudConfig();
		original.setVisible(false);
		original.getLayout().setAnchor(HudAnchor.BOTTOM_RIGHT);
		original.getLayout().setOffset(-12, -24);
		original.setRowStyle(MiningSessionRowStyle.ICON_AMOUNT);
		original.setMaxVisibleBlocks(9);
		original.setUiScale(1.35D);
		original.setRowSpacing(4);
		original.setAutoStartOnBlockBreak(true);
		original.setOresOnly(false);
		original.setInventoryOrder(MiningSessionInventoryOrder.TOTAL_VALUE);
		original.setShowStatisticsFooter(true);
		original.setShowFooterItems(false);
		original.setShowFooterValue(true);
		original.setGoalMode(MiningSessionGoalMode.ITEMS);
		original.setMaxVisibleGoals(6);
		original.putItemGoal("minecraft:diamond", 64);
		original.putItemGoal("redstone", 128);
		original.setValueGoal(new BigDecimal("250000.50"));
		original.addTrackedInventoryItem("diamond");
		original.addTrackedInventoryItem("minecraft:iron_ingot");
		original.setTrackedInventoryItemValue("minecraft:diamond", new BigDecimal("10.25"));
		original.setBackgroundColor(0xAA112233);
		original.setTextColor(0xFFABCDEF);
		original.setAccentColor(0xFF33CC77);

		String serialized = gson.toJson(original);
		MiningSessionHudConfig restored = gson.fromJson(serialized, MiningSessionHudConfig.class);
		restored.validate();

		assertTrue(serialized.contains("\"enabled\""));
		assertFalse(restored.visible());
		assertEquals(HudAnchor.BOTTOM_RIGHT, restored.getLayout().getAnchor());
		assertEquals(-12, restored.getLayout().getOffsetX());
		assertEquals(-24, restored.getLayout().getOffsetY());
		assertEquals(MiningSessionRowStyle.ICON_AMOUNT, restored.getRowStyle());
		assertEquals(9, restored.getMaxVisibleBlocks());
		assertEquals(1.35D, restored.getUiScale());
		assertEquals(4, restored.getRowSpacing());
		assertTrue(restored.isAutoStartOnBlockBreak());
		assertFalse(restored.isOresOnly());
		assertEquals(MiningSessionInventoryOrder.TOTAL_VALUE, restored.getInventoryOrder());
		assertTrue(restored.isShowStatisticsFooter());
		assertTrue(restored.isShowFooterBlocks());
		assertFalse(restored.isShowFooterItems());
		assertTrue(restored.isShowFooterValue());
		assertEquals(MiningSessionGoalMode.ITEMS, restored.getGoalMode());
		assertEquals(6, restored.getMaxVisibleGoals());
		assertEquals(List.of("minecraft:diamond", "minecraft:redstone"), restored.getItemGoalIds());
		assertEquals(List.of(64, 128), restored.getItemGoals().stream()
			.map(MiningSessionItemGoal::targetAmount).toList());
		assertEquals(0, new BigDecimal("250000.5").compareTo(restored.getValueGoal()));
		assertEquals(java.util.List.of("minecraft:diamond", "minecraft:iron_ingot"),
			restored.getTrackedInventoryItems());
		assertEquals(new BigDecimal("10.25"), restored.getTrackedInventoryItemValue("minecraft:diamond"));
		assertEquals(BigDecimal.ZERO, restored.getTrackedInventoryItemValue("minecraft:iron_ingot"));
		assertEquals(0xAA112233, restored.getBackgroundColor());
		assertEquals(0xFFABCDEF, restored.getTextColor());
		assertEquals(0xFF33CC77, restored.getAccentColor());
	}

	@Test
	void oresOnlyDefaultsToEnabled() {
		MiningSessionHudConfig defaults = new MiningSessionHudConfig();
		assertTrue(defaults.isOresOnly());
		assertFalse(defaults.isShowFooterValue());
		assertEquals(3, defaults.getMaxVisibleGoals());
		MiningSessionHudConfig restored = gson.fromJson("{}", MiningSessionHudConfig.class);
		restored.validate();
		assertTrue(restored.isOresOnly());
		assertEquals(MiningSessionInventoryOrder.CUSTOM, restored.getInventoryOrder());
	}

	@Test
	void malformedValuesAreRepaired() {
		MiningSessionHudConfig restored = gson.fromJson("""
			{
			  "layout": null,
			  "rowStyle": null,
			  "maxVisibleBlocks": 99,
			  "uiScale": -4,
			  "rowSpacing": 99,
			  "goalMode": null,
			  "maxVisibleGoals": 99,
			  "valueGoal": -50,
			  "itemGoals": [
			    {"itemId": "diamond", "targetAmount": 0},
			    {"itemId": "minecraft:diamond", "targetAmount": 12},
			    {"itemId": "bad id", "targetAmount": 4}
			  ],
			  "trackedInventoryEntries": [
			    {"itemId": " DIAMOND "},
			    {"itemId": "minecraft:diamond"},
			    {"itemId": "bad id"},
			    null
			  ]
			}
			""", MiningSessionHudConfig.class);
		restored.validate();

		assertEquals(HudAnchor.TOP_LEFT, restored.getLayout().getAnchor());
		assertEquals(MiningSessionRowStyle.ICON_NAME_AMOUNT, restored.getRowStyle());
		assertEquals(MiningSessionHudConfig.MAX_VISIBLE_BLOCKS, restored.getMaxVisibleBlocks());
		assertEquals(MiningSessionHudConfig.MIN_UI_SCALE, restored.getUiScale());
		assertEquals(MiningSessionHudConfig.MAX_ROW_SPACING, restored.getRowSpacing());
		assertEquals(java.util.List.of("minecraft:diamond"), restored.getTrackedInventoryItems());
		assertEquals(MiningSessionGoalMode.NONE, restored.getGoalMode());
		assertEquals(MiningSessionHudConfig.MAX_VISIBLE_GOALS, restored.getMaxVisibleGoals());
		assertEquals(List.of("minecraft:diamond"), restored.getItemGoalIds());
		assertEquals(1, restored.getItemGoals().getFirst().targetAmount());
		assertEquals(BigDecimal.ZERO, restored.getValueGoal());
	}

	@Test
	void copyOwnsIndependentLayoutAndTrackedItemList() {
		MiningSessionHudConfig original = new MiningSessionHudConfig();
		original.addTrackedInventoryItem("minecraft:sand");
		original.setTrackedInventoryItemValue("minecraft:sand", new BigDecimal("10"));
		original.putItemGoal("minecraft:diamond", 64);
		MiningSessionHudConfig copy = original.copy();
		copy.getLayout().setOffset(40, 50);
		copy.addTrackedInventoryItem("minecraft:diamond");
		copy.setTrackedInventoryItemValue("minecraft:sand", new BigDecimal("12"));
		copy.putItemGoal("minecraft:diamond", 32);
		copy.putItemGoal("minecraft:gold_ingot", 12);

		assertNotSame(original.getLayout(), copy.getLayout());
		assertEquals(8, original.getLayout().getOffsetX());
		assertEquals(java.util.List.of("minecraft:sand"), original.getTrackedInventoryItems());
		assertEquals(0, new BigDecimal("10").compareTo(
			original.getTrackedInventoryItemValue("minecraft:sand")));
		assertEquals(List.of("minecraft:diamond"), original.getItemGoalIds());
		assertEquals(64, original.getItemGoals().getFirst().targetAmount());
	}

	@Test
	void manualItemIdsAcceptBareAndNamespacedForms() {
		MiningSessionHudConfig config = new MiningSessionHudConfig();

		assertTrue(config.putTrackedInventoryItem("diamond", new BigDecimal("10")));
		assertTrue(config.putTrackedInventoryItem("minecraft:diamond", new BigDecimal("12")));

		assertEquals(List.of("minecraft:diamond"), config.getTrackedInventoryItems());
		assertEquals(0, new BigDecimal("12").compareTo(
			config.getTrackedInventoryItemValue("minecraft:diamond")));
	}

	@Test
	void trackedItemOrderCanBeChangedAndRoundTrips() {
		MiningSessionHudConfig config = new MiningSessionHudConfig();
		config.addTrackedInventoryItem("minecraft:diamond");
		config.addTrackedInventoryItem("minecraft:iron_ingot");
		config.addTrackedInventoryItem("minecraft:gold_ingot");

		assertTrue(config.moveTrackedInventoryItem(2, 0));
		assertEquals(List.of("minecraft:gold_ingot", "minecraft:diamond", "minecraft:iron_ingot"),
			config.getTrackedInventoryItems());

		MiningSessionHudConfig restored = gson.fromJson(gson.toJson(config), MiningSessionHudConfig.class);
		restored.validate();
		assertEquals(config.getTrackedInventoryItems(), restored.getTrackedInventoryItems());
	}

	@Test
	void itemGoalsAllowFifteenEntriesAndRejectTheSixteenth() {
		MiningSessionHudConfig config = new MiningSessionHudConfig();
		for (int index = 0; index < MiningSessionHudConfig.MAX_ITEM_GOALS; index++) {
			assertTrue(config.putItemGoal("example:goal_" + index, index + 1));
		}

		assertEquals(15, config.getItemGoals().size());
		assertFalse(config.putItemGoal("example:goal_15", 16));
		assertTrue(config.putItemGoal("example:goal_0", 99));
		assertEquals(99, config.getItemGoals().getFirst().targetAmount());
	}

	@Test
	void inventoryOrderModesSortRowsAndCustomValueFallsBackToAmount() {
		MiningSessionHudConfig config = new MiningSessionHudConfig();
		config.putTrackedInventoryItem("minecraft:diamond", new BigDecimal("5"));
		config.putTrackedInventoryItem("minecraft:iron_ingot", new BigDecimal("10"));
		config.putTrackedInventoryItem("minecraft:gold_ingot", BigDecimal.ZERO);
		var entries = config.getTrackedInventoryEntries();
		java.util.Map<String, Integer> amounts = java.util.Map.of(
			"minecraft:diamond", 3,
			"minecraft:iron_ingot", 1,
			"minecraft:gold_ingot", 20);

		assertSortedIds(MiningSessionInventoryOrder.CUSTOM, entries, amounts,
			List.of("minecraft:diamond", "minecraft:iron_ingot", "minecraft:gold_ingot"));
		assertSortedIds(MiningSessionInventoryOrder.AMOUNT, entries, amounts,
			List.of("minecraft:gold_ingot", "minecraft:diamond", "minecraft:iron_ingot"));
		assertSortedIds(MiningSessionInventoryOrder.CUSTOM_VALUE, entries, amounts,
			List.of("minecraft:iron_ingot", "minecraft:diamond", "minecraft:gold_ingot"));
		assertSortedIds(MiningSessionInventoryOrder.TOTAL_VALUE, entries, amounts,
			List.of("minecraft:diamond", "minecraft:iron_ingot", "minecraft:gold_ingot"));
		assertSortedIds(MiningSessionInventoryOrder.ALPHABETICAL, entries, amounts,
			List.of("minecraft:diamond", "minecraft:gold_ingot", "minecraft:iron_ingot"));

		entries.forEach(entry -> entry.setUnitValue(BigDecimal.ZERO));
		assertSortedIds(MiningSessionInventoryOrder.CUSTOM_VALUE, entries, amounts,
			List.of("minecraft:gold_ingot", "minecraft:diamond", "minecraft:iron_ingot"));
	}

	private static void assertSortedIds(MiningSessionInventoryOrder order,
								List<MiningSessionTrackedItem> source, java.util.Map<String, Integer> amounts,
								List<String> expected) {
		List<MiningSessionTrackedItem> sorted = new java.util.ArrayList<>(source);
		MiningSessionHudModule.sortInventoryEntries(order, sorted,
			entry -> amounts.get(entry.itemId()), MiningSessionTrackedItem::itemId);
		assertEquals(expected, sorted.stream().map(MiningSessionTrackedItem::itemId).toList());
	}

	@Test
	void customItemModelAppearanceRoundTripsAndRemainsOptional() {
		MiningSessionHudConfig restored = gson.fromJson("""
			{
			  "trackedInventoryEntries": [
			    {
			      "itemId": "minecraft:paper",
			      "appearance": {
			        "itemModel": "example:items/mining_token",
			        "floats": [12.5],
			        "flags": [true],
			        "strings": ["rare"],
			        "colors": [16755200]
			      }
			    },
			    {"itemId": "minecraft:sand"}
			  ]
			}
			""", MiningSessionHudConfig.class);
		restored.validate();

		MiningSessionItemAppearance appearance =
			restored.getTrackedInventoryEntries().get(0).appearance();
		assertEquals("example:items/mining_token", appearance.itemModel());
		assertEquals(java.util.List.of(12.5F), appearance.floats());
		assertEquals(null, restored.getTrackedInventoryEntries().get(1).appearance());

		MiningSessionHudConfig copy = restored.copy();
		String serialized = gson.toJson(copy);
		assertTrue(serialized.contains("example:items/mining_token"));
		assertTrue(serialized.contains("\"trackedInventoryEntries\""));
	}

	@Test
	void heldItemCapturesModelValueAndDisplayName() {
		MiningSessionHudConfig config = new MiningSessionHudConfig();
		ItemStack held = new ItemStack(Items.PAPER);
		held.set(DataComponents.ITEM_MODEL, Identifier.parse("example:mining_token"));
		held.set(DataComponents.CUSTOM_MODEL_DATA,
			new CustomModelData(List.of(7.5F), List.of(true), List.of("rare"), List.of(0x55AAFF)));
		held.set(DataComponents.CUSTOM_NAME, Component.literal("Rare Mining Token"));

		assertTrue(config.putHeldTrackedInventoryItem(held, new BigDecimal("25.50")));

		MiningSessionTrackedItem tracked = config.getTrackedInventoryEntries().getFirst();
		assertEquals("minecraft:paper", tracked.itemId());
		assertEquals("Rare Mining Token", tracked.displayName());
		assertEquals(0, new BigDecimal("25.5").compareTo(tracked.unitValue()));
		assertEquals("example:mining_token", tracked.appearance().itemModel());
		assertEquals(List.of(7.5F), tracked.appearance().floats());
		assertTrue(tracked.matches(held));
	}

	@Test
	void trackedItemsUseCustomNameThenModelThenItemIdAsIdentity() {
		MiningSessionHudConfig config = new MiningSessionHudConfig();

		ItemStack john = new ItemStack(Items.JUNGLE_LEAVES);
		john.set(DataComponents.CUSTOM_NAME, Component.literal("John"));
		ItemStack claus = new ItemStack(Items.JUNGLE_LEAVES);
		claus.set(DataComponents.CUSTOM_NAME, Component.literal("Claus"));
		assertTrue(config.putHeldTrackedInventoryItem(john, BigDecimal.ZERO));
		assertTrue(config.putHeldTrackedInventoryItem(claus, BigDecimal.ZERO));
		assertTrue(config.putTrackedInventoryItem("minecraft:jungle_leaves", BigDecimal.ZERO));

		for (int index = 1; index <= 3; index++) {
			ItemStack modeledPaper = new ItemStack(Items.PAPER);
			modeledPaper.set(DataComponents.CUSTOM_NAME, Component.literal("diamond"));
			modeledPaper.set(DataComponents.ITEM_MODEL, Identifier.parse("example:paper_" + index));
			assertTrue(config.putHeldTrackedInventoryItem(modeledPaper, BigDecimal.ZERO));
		}

		for (int index = 0; index < 5; index++) {
			assertTrue(config.putHeldTrackedInventoryItem(new ItemStack(Items.OAK_LOG), BigDecimal.ZERO));
		}

		assertEquals(7, config.getTrackedInventoryEntries().size());
		assertEquals(java.util.Arrays.asList("John", "Claus", null, "diamond", "diamond", "diamond", null),
			config.getTrackedInventoryEntries().stream()
				.map(MiningSessionTrackedItem::displayName).toList());
		assertTrue(config.getTrackedInventoryEntries().get(0).matches(john));
		assertFalse(config.getTrackedInventoryEntries().get(0).matches(claus));
		assertTrue(config.getTrackedInventoryEntries().get(6).matches(new ItemStack(Items.OAK_LOG)));
	}

	@Test
	void sameBaseItemWithDifferentModelsRemainsAsSeparateEntries() {
		MiningSessionHudConfig restored = gson.fromJson("""
			{
			  "trackedInventoryEntries": [
			    {
			      "itemId": "minecraft:paper",
			      "unitValue": 10,
			      "displayName": "Copper Mining Token",
			      "appearance": {"itemModel": "example:first", "floats": [1]}
			    },
			    {
			      "itemId": "minecraft:paper",
			      "unitValue": 20,
			      "appearance": {"itemModel": "example:second", "floats": [2]}
			    }
			  ]
			}
			""", MiningSessionHudConfig.class);
		restored.validate();

		assertEquals(java.util.List.of("minecraft:paper", "minecraft:paper"),
			restored.getTrackedInventoryItems());
		assertEquals(2, restored.getTrackedInventoryEntries().size());
		assertEquals("Copper Mining Token", restored.getTrackedInventoryEntries().get(0).displayName());
		assertEquals(null, restored.getTrackedInventoryEntries().get(1).displayName());
		assertFalse(restored.getTrackedInventoryEntries().get(0).identityKey()
			.equals(restored.getTrackedInventoryEntries().get(1).identityKey()));
		assertTrue(gson.toJson(restored).contains("Copper Mining Token"));
	}
}
