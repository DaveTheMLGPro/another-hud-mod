package net.davethemlgpro.client.module.miningsession;

final class MiningSessionItemGoal {
	private String itemId;
	private int targetAmount = 1;

	MiningSessionItemGoal() {
	}

	MiningSessionItemGoal(String itemId, int targetAmount) {
		this.itemId = itemId;
		this.targetAmount = targetAmount;
		validate();
	}

	MiningSessionItemGoal copy() {
		return new MiningSessionItemGoal(itemId, targetAmount);
	}

	boolean validate() {
		itemId = MiningSessionHudConfig.normalizeItemId(itemId);
		targetAmount = Math.clamp(targetAmount, 1, MiningSessionHudConfig.MAX_GOAL_ITEM_AMOUNT);
		return itemId != null;
	}

	String itemId() {
		return itemId;
	}

	int targetAmount() {
		return targetAmount;
	}

	void setTargetAmount(int amount) {
		targetAmount = Math.clamp(amount, 1, MiningSessionHudConfig.MAX_GOAL_ITEM_AMOUNT);
	}
}
