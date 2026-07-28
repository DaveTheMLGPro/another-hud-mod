package net.davethemlgpro.client;

import net.davethemlgpro.client.datagen.AnotherHudLanguageProvider;
import net.davethemlgpro.client.datagen.HudLanguage;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AnotherHUDModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		for (HudLanguage language : HudLanguage.values()) {
			for (String locale : language.getLocales()) {
				pack.addProvider((output, registryLookup) ->
					new AnotherHudLanguageProvider(output, registryLookup, language, locale));
			}
		}
	}
}
