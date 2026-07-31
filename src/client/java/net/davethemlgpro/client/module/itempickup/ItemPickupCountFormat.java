package net.davethemlgpro.client.module.itempickup;

public enum ItemPickupCountFormat {
	PLUS("+"),
	MULTIPLY("x");

	private final String prefix;

	ItemPickupCountFormat(String prefix) {
		this.prefix = prefix;
	}

	public String format(int amount) {
		return prefix + amount;
	}
}
