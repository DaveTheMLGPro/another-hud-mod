package net.davethemlgpro.client.module.armor;

final class ArmorHudSlotLayout {
	private boolean visible;
	private int itemX;
	private int itemY;
	private int itemSize;
	private int textX;
	private int textY;
	private int textWidth;
	private int textHeight;
	private String durabilityText = "";

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getItemX() {
		return itemX;
	}

	public void setItemX(int itemX) {
		this.itemX = itemX;
	}

	public int getItemY() {
		return itemY;
	}

	public void setItemY(int itemY) {
		this.itemY = itemY;
	}

	public int getItemSize() {
		return itemSize;
	}

	public void setItemSize(int itemSize) {
		this.itemSize = itemSize;
	}

	public int getTextX() {
		return textX;
	}

	public void setTextX(int textX) {
		this.textX = textX;
	}

	public int getTextY() {
		return textY;
	}

	public void setTextY(int textY) {
		this.textY = textY;
	}

	public int getTextWidth() {
		return textWidth;
	}

	public void setTextWidth(int textWidth) {
		this.textWidth = textWidth;
	}

	public int getTextHeight() {
		return textHeight;
	}

	public void setTextHeight(int textHeight) {
		this.textHeight = textHeight;
	}

	public String getDurabilityText() {
		return durabilityText;
	}

	public void setDurabilityText(String durabilityText) {
		this.durabilityText = durabilityText;
	}

	public void setEntry(int itemX, int itemY, int itemSize, int textX, int textY,
	              int textWidth, int textHeight, String durabilityText) {
		visible = true;
		this.itemX = itemX;
		this.itemY = itemY;
		this.itemSize = itemSize;
		this.textX = textX;
		this.textY = textY;
		this.textWidth = textWidth;
		this.textHeight = textHeight;
		this.durabilityText = durabilityText;
	}

	public void hide() {
		visible = false;
		durabilityText = "";
	}
}
