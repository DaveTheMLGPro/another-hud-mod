package net.davethemlgpro.client.module.containersearch;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContainerSearchMatcherTest {
	private static Item grayStainedGlass;

	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		grayStainedGlass = Items.STAINED_GLASS.gray();
		for (var item : List.of(grayStainedGlass, Items.GLASS, Items.IRON_INGOT)) {
			BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getKey(item)).orElseThrow()
				.bindComponents(DataComponentMap.EMPTY);
		}
	}

	@Test
	void matchesLocalizedNameAndNamespacedItemIdTerms() {
		ItemStack grayGlass = new ItemStack(grayStainedGlass, 32);

		assertTrue(ContainerSearchMatcher.matches(grayGlass, "gray stained glass"));
		assertTrue(ContainerSearchMatcher.matches(grayGlass, "minecraft:gray_stained_glass"));
		assertTrue(ContainerSearchMatcher.matches(grayGlass, "glass gray"));
		assertFalse(ContainerSearchMatcher.matches(grayGlass, "white stained glass"));
		assertFalse(ContainerSearchMatcher.matches(ItemStack.EMPTY, "glass"));
		assertFalse(ContainerSearchMatcher.matches(grayGlass, "  "));
	}

	@Test
	void supportsQuotedPhrasesNamespacesAndIdFilters() {
		ItemStack grayGlass = new ItemStack(grayStainedGlass, 32);

		assertTrue(ContainerSearchMatcher.matches(grayGlass, "\"gray stained\" glass"));
		assertTrue(ContainerSearchMatcher.matches(grayGlass, "@minecraft glass"));
		assertFalse(ContainerSearchMatcher.matches(grayGlass, "@example glass"));
		assertTrue(ContainerSearchMatcher.matches(grayGlass, "id:gray_stained_glass"));
		assertFalse(ContainerSearchMatcher.matches(grayGlass, "id:iron_ingot"));
		assertEquals(List.of("gray stained", "@minecraft", "id:glass"),
			ContainerSearchMatcher.tokens("\"gray stained\" @minecraft id:glass"));
	}

	@Test
	void exactModeRequiresTheWholeNameOrItemId() {
		ItemStack grayGlass = new ItemStack(grayStainedGlass, 32);

		assertTrue(ContainerSearchMatcher.matches(grayGlass, "gray stained glass", true));
		assertTrue(ContainerSearchMatcher.matches(grayGlass, "id:gray_stained_glass", true));
		assertFalse(ContainerSearchMatcher.matches(grayGlass, "gray glass", true));
		assertFalse(ContainerSearchMatcher.matches(grayGlass, "id:stained_glass", true));
	}

	@Test
	void totalsMatchingStacksWithoutIncludingOtherItems() {
		ContainerSearchMatcher.MatchSummary summary = ContainerSearchMatcher.summarize(List.of(
			new ItemStack(grayStainedGlass, 64),
			new ItemStack(Items.GLASS, 48),
			new ItemStack(grayStainedGlass, 64),
			new ItemStack(Items.IRON_INGOT, 12),
			new ItemStack(grayStainedGlass, 19)
		), "gray stained glass");

		assertEquals(3, summary.stacks());
		assertEquals(147, summary.items());
	}
}
