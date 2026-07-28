package net.davethemlgpro.client.module;

import net.davethemlgpro.client.screen.popover.HudPopoverControl;

import java.util.List;

@FunctionalInterface
public interface HudModulePopoverFactory<C extends HudModuleConfig<C>> {
	List<HudPopoverControl> create(C config);
}
