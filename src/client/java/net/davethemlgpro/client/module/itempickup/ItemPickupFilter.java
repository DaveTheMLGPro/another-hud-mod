package net.davethemlgpro.client.module.itempickup;

import java.util.Collection;

final class ItemPickupFilter {
	private ItemPickupFilter() {
	}

	static boolean allows(ItemPickupFilterMode mode, Collection<String> itemIds, String itemId) {
		boolean listed = itemIds.contains(itemId);
		return switch (mode) {
			case SHOW_ALL -> true;
			case HIDE_LISTED -> !listed;
			case ONLY_LISTED -> listed;
		};
	}
}
