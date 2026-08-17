package net.davethemlgpro.client.module.containersearch;

final class ContainerSearchQueryState {
	private String query = "";

	String get() {
		return query;
	}

	void set(String query) {
		this.query = query == null ? "" : query;
	}

	void clear() {
		query = "";
	}
}
