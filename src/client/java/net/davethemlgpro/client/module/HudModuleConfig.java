package net.davethemlgpro.client.module;

import net.davethemlgpro.client.hud.layout.ModuleLayout;

public interface HudModuleConfig<C extends HudModuleConfig<C>> {
	boolean visible();
	void setVisible(boolean visible);
	ModuleLayout getLayout();
	C copy();
	void copyFrom(C source);
	void validate();
}
