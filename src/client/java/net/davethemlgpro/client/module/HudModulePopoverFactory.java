package net.davethemlgpro.client.module;

import net.davethemlgpro.client.screen.popover.HudPopoverTab;

import java.util.List;

@FunctionalInterface
public interface HudModulePopoverFactory<C extends HudModuleConfig<C>> {
	List<HudPopoverTab> create(C config);
}
