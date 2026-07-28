package net.davethemlgpro.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public final class AnotherHudLanguageProvider extends FabricLanguageProvider {
	private final HudLanguage language;

	public AnotherHudLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup,
									  HudLanguage language, String locale) {
		super(output, locale, registryLookup);
		this.language = language;
	}

	@Override
	public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
		language.getTranslations().forEach((translation, value) ->
			translationBuilder.add(translation.key(), value));
	}
}
