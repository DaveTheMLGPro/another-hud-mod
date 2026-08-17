package net.davethemlgpro.client.module.containersearch;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ContainerSearchMatcher {
	private ContainerSearchMatcher() {
	}

	public static boolean matches(ItemStack stack, String query) {
		return matches(stack, query, false);
	}

	public static boolean matches(ItemStack stack, String query, boolean exactMatch) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		List<QueryTerm> terms = terms(query);
		if (terms.isEmpty()) {
			return false;
		}

		Identifier identifier = BuiltInRegistries.ITEM.getKey(stack.getItem());
		String itemId = normalize(identifier.toString());
		String itemPath = normalize(identifier.getPath().replace('_', ' '));
		String itemName = normalize(stack.getHoverName().getString());

		List<String> textTerms = terms.stream()
			.filter(term -> term.type() == TermType.TEXT)
			.map(QueryTerm::value)
			.toList();
		if (exactMatch && !textTerms.isEmpty()) {
			String text = String.join(" ", textTerms);
			if (!itemName.equals(text) && !itemId.equals(text) && !itemPath.equals(text)) {
				return false;
			}
		}

		for (QueryTerm term : terms) {
			if (term.value().isEmpty()) {
				return false;
			}
			boolean matches = switch (term.type()) {
				case TEXT -> exactMatch || itemName.contains(term.value())
					|| itemId.contains(term.value()) || itemPath.contains(term.value());
				case ITEM_ID -> exactMatch
					? itemId.equals(term.value()) || itemPath.equals(term.value().replace('_', ' '))
					: itemId.contains(term.value()) || itemPath.contains(term.value().replace('_', ' '));
				case NAMESPACE -> identifier.getNamespace().equals(term.value());
				case TAG -> matchesTag(stack, term.value());
			};
			if (!matches) {
				return false;
			}
		}
		return true;
	}

	public static MatchSummary summarize(Iterable<ItemStack> stacks, String query) {
		return summarize(stacks, query, false);
	}

	public static MatchSummary summarize(Iterable<ItemStack> stacks, String query, boolean exactMatch) {
		int matchingStacks = 0;
		long matchingItems = 0;
		for (ItemStack stack : stacks) {
			if (matches(stack, query, exactMatch)) {
				matchingStacks++;
				matchingItems += stack.getCount();
			}
		}
		return new MatchSummary(matchingStacks, matchingItems);
	}

	public static List<String> tokens(String query) {
		String value = query == null ? "" : query.trim();
		if (value.isEmpty()) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		StringBuilder token = new StringBuilder();
		boolean quoted = false;
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			if (character == '"') {
				quoted = !quoted;
			} else if (Character.isWhitespace(character) && !quoted) {
				addToken(result, token);
			} else {
				token.append(character);
			}
		}
		addToken(result, token);
		return List.copyOf(result);
	}

	private static List<QueryTerm> terms(String query) {
		return tokens(query).stream().map(token -> {
			String normalized = normalize(token);
			if (normalized.startsWith("@")) {
				return new QueryTerm(TermType.NAMESPACE, normalized.substring(1));
			}
			if (normalized.startsWith("#")) {
				return new QueryTerm(TermType.TAG, normalized.substring(1));
			}
			if (normalized.startsWith("id:")) {
				return new QueryTerm(TermType.ITEM_ID, normalized.substring(3));
			}
			return new QueryTerm(TermType.TEXT, normalized);
		}).toList();
	}

	private static boolean matchesTag(ItemStack stack, String value) {
		Identifier id = Identifier.tryParse(value);
		if (id == null) {
			return false;
		}
		TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
		return stack.is(tag);
	}

	private static void addToken(List<String> result, StringBuilder token) {
		if (!token.isEmpty()) {
			result.add(token.toString());
			token.setLength(0);
		}
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private enum TermType {
		TEXT,
		ITEM_ID,
		NAMESPACE,
		TAG
	}

	private record QueryTerm(TermType type, String value) {
	}

	public record MatchSummary(int stacks, long items) {
	}
}
