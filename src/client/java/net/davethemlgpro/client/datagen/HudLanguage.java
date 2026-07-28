package net.davethemlgpro.client.datagen;

import net.davethemlgpro.client.translation.TranslationKey;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static net.davethemlgpro.client.translation.TranslationKey.*;

public enum HudLanguage {
	EN(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "Open HUD Layout Editor"),
		entry(MODULE_ARMOR, "Armor HUD"),
		entry(EDITOR_TITLE, "HUD Layout Editor"),
		entry(EDITOR_INSTRUCTIONS, "Drag modules to reposition them."),
		entry(EDITOR_RESET, "Reset"),
		entry(EDITOR_SAVE_FAILED, "Changes could not be saved.")
	), "en_us"),
	DE(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "HUD-Layout-Editor öffnen"),
		entry(MODULE_ARMOR, "Rüstungs-HUD"),
		entry(EDITOR_TITLE, "HUD-Layout-Editor"),
		entry(EDITOR_INSTRUCTIONS, "Ziehe Module, um sie neu zu positionieren."),
		entry(EDITOR_RESET, "Zurücksetzen"),
		entry(EDITOR_SAVE_FAILED, "Änderungen konnten nicht gespeichert werden.")
	), "de_de", "de_at"),
	ES(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "Abrir el editor de diseños del HUD"),
		entry(MODULE_ARMOR, "HUD de armadura"),
		entry(EDITOR_TITLE, "Editor de diseño de HUD"),
		entry(EDITOR_INSTRUCTIONS, "Arrastra los módulos para cambiar su posición."),
		entry(EDITOR_RESET, "Restablecer"),
		entry(EDITOR_SAVE_FAILED, "No se han podido guardar los cambios.")
	), "es_mx", "es_es"),
	FR_FR(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "Ouvrir l'éditeur de mise en page HUD"),
		entry(MODULE_ARMOR, "Affichage tête haute de l'armure"),
		entry(EDITOR_TITLE, "Éditeur de mise en page HUD"),
		entry(EDITOR_INSTRUCTIONS, "Faites glisser les modules pour les repositionner."),
		entry(EDITOR_RESET, "Réinitialiser"),
		entry(EDITOR_SAVE_FAILED, "Les modifications n'ont pas pu être enregistrées.")
	), "fr_fr"),
	FR_CA(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "Ouvrir l'éditeur de disposition HUD"),
		entry(MODULE_ARMOR, "Interface d'affichage de l'armure"),
		entry(EDITOR_TITLE, "Éditeur de mise en page HUD"),
		entry(EDITOR_INSTRUCTIONS, "Faites glisser les modules pour les repositionner."),
		entry(EDITOR_RESET, "Réinitialiser"),
		entry(EDITOR_SAVE_FAILED, "Les modifications n'ont pas pu être enregistrées.")
	), "fr_ca");

	static {
		Set<String> locales = new HashSet<>();
		for (HudLanguage language : values()) {
			if (language.locales.isEmpty()) {
				throw new IllegalStateException(language.name() + " has no locales");
			}
			for (String locale : language.locales) {
				if (!locales.add(locale)) {
					throw new IllegalStateException("Duplicate language locale: " + locale);
				}
			}
		}
		EN.requireComplete();
	}

	private final Map<TranslationKey, String> translations;
	private final List<String> locales;

	HudLanguage(Map<TranslationKey, String> translations, String... locales) {
		this.translations = translations;
		this.locales = List.of(locales);
	}

	public List<String> getLocales() {
		return locales;
	}

	public Map<TranslationKey, String> getTranslations() {
		return translations;
	}

	@SafeVarargs
	private static Map<TranslationKey, String> translations(Map.Entry<TranslationKey, String>... entries) {
		EnumMap<TranslationKey, String> values = new EnumMap<>(TranslationKey.class);
		for (Map.Entry<TranslationKey, String> entry : entries) {
			if (values.put(entry.getKey(), entry.getValue()) != null) {
				throw new IllegalStateException("Duplicate translation: " + entry.getKey().key());
			}
		}
		return Collections.unmodifiableMap(values);
	}

	private void requireComplete() {
		for (TranslationKey translation : TranslationKey.values()) {
			if (!translations.containsKey(translation)) {
				throw new IllegalStateException(locales.getFirst() + " is missing translation " + translation.key());
			}
		}
	}
}
