package net.davethemlgpro.client.module.miningsession;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class MiningSessionItemAppearance {
	private static final int MAX_COMPONENT_VALUES = 64;
	private static final int MAX_STRING_LENGTH = 256;

	private String itemModel;
	private List<Float> floats = new ArrayList<>();
	private List<Boolean> flags = new ArrayList<>();
	private List<String> strings = new ArrayList<>();
	private List<Integer> colors = new ArrayList<>();

	MiningSessionItemAppearance() {
	}

	MiningSessionItemAppearance(String itemModel, List<Float> floats, List<Boolean> flags,
								List<String> strings, List<Integer> colors) {
		this.itemModel = itemModel;
		this.floats = new ArrayList<>(floats);
		this.flags = new ArrayList<>(flags);
		this.strings = new ArrayList<>(strings);
		this.colors = new ArrayList<>(colors);
		validate();
	}

	static MiningSessionItemAppearance capture(ItemStack stack) {
		Identifier model = stack.get(DataComponents.ITEM_MODEL);
		CustomModelData customModel = stack.get(DataComponents.CUSTOM_MODEL_DATA);
		ItemStack baseStack = new ItemStack(stack.getItem());
		Identifier baseModel = baseStack.get(DataComponents.ITEM_MODEL);
		CustomModelData baseCustomModel = baseStack.get(DataComponents.CUSTOM_MODEL_DATA);
		if (Objects.equals(model, baseModel)) {
			model = null;
		}
		if (Objects.equals(customModel, baseCustomModel)) {
			customModel = null;
		}
		return new MiningSessionItemAppearance(model == null ? null : model.toString(),
			customModel == null ? List.of() : customModel.floats(),
			customModel == null ? List.of() : customModel.flags(),
			customModel == null ? List.of() : customModel.strings(),
			customModel == null ? List.of() : customModel.colors());
	}

	static boolean hasMeaningfulAppearance(ItemStack stack) {
		return capture(stack).isMeaningful();
	}

	MiningSessionItemAppearance copy() {
		return new MiningSessionItemAppearance(itemModel, floats, flags, strings, colors);
	}

	void applyTo(ItemStack stack) {
		Identifier model = itemModel == null ? null : Identifier.tryParse(itemModel);
		if (model != null) {
			stack.set(DataComponents.ITEM_MODEL, model);
		}
		if (!floats.isEmpty() || !flags.isEmpty() || !strings.isEmpty() || !colors.isEmpty()) {
			stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
				List.copyOf(floats), List.copyOf(flags), List.copyOf(strings), List.copyOf(colors)));
		}
	}

	boolean isMeaningful() {
		return itemModel != null || !floats.isEmpty() || !flags.isEmpty() || !strings.isEmpty() || !colors.isEmpty();
	}

	boolean matches(ItemStack stack) {
		return identityKey().equals(capture(stack).identityKey());
	}

	String identityKey() {
		return String.valueOf(itemModel) + '|' + floats + '|' + flags + '|' + strings + '|' + colors;
	}

	String displayLabel() {
		StringBuilder label = new StringBuilder(itemModel == null ? "custom model" : itemModel);
		if (!floats.isEmpty() || !flags.isEmpty() || !strings.isEmpty() || !colors.isEmpty()) {
			label.append(" · CMD");
			if (!floats.isEmpty()) {
				label.append(' ').append(floats);
			} else if (!strings.isEmpty()) {
				label.append(' ').append(strings);
			}
		}
		return label.toString();
	}

	String itemModel() {
		return itemModel;
	}

	List<Float> floats() {
		return List.copyOf(floats);
	}

	void validate() {
		Identifier parsedModel = itemModel == null ? null : Identifier.tryParse(itemModel);
		itemModel = parsedModel == null ? null : parsedModel.toString();
		floats = finiteFloats(floats);
		flags = limitedNonNull(flags);
		strings = limitedStrings(strings);
		colors = limitedNonNull(colors);
	}

	private static List<Float> finiteFloats(List<Float> source) {
		List<Float> repaired = new ArrayList<>();
		if (source != null) {
			for (Float value : source) {
				if (value != null && Float.isFinite(value) && repaired.size() < MAX_COMPONENT_VALUES) {
					repaired.add(value);
				}
			}
		}
		return repaired;
	}

	private static <T> List<T> limitedNonNull(List<T> source) {
		List<T> repaired = new ArrayList<>();
		if (source != null) {
			for (T value : source) {
				if (value != null && repaired.size() < MAX_COMPONENT_VALUES) {
					repaired.add(value);
				}
			}
		}
		return repaired;
	}

	private static List<String> limitedStrings(List<String> source) {
		List<String> repaired = new ArrayList<>();
		if (source != null) {
			for (String value : source) {
				if (value != null && repaired.size() < MAX_COMPONENT_VALUES) {
					repaired.add(value.substring(0, Math.min(value.length(), MAX_STRING_LENGTH)));
				}
			}
		}
		return repaired;
	}
}
