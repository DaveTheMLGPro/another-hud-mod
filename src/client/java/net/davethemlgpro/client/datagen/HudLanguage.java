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
		entry(EDITOR_INSTRUCTIONS, "Drag to move. Shift-click a HUD for quick settings."),
		entry(EDITOR_RESET, "Reset"),
		entry(EDITOR_SAVE_FAILED, "Changes could not be saved."),
		entry(POPOVER_NO_SETTINGS, "No quick settings available."),
		entry(SETTINGS_ARMOR_ORIENTATION, "Orientation"),
		entry(SETTINGS_ARMOR_ORIENTATION_DESCRIPTION, "Arranges armor slots horizontally or vertically."),
		entry(SETTINGS_ARMOR_SCALE, "Scale"),
		entry(SETTINGS_ARMOR_SCALE_DESCRIPTION, "Changes the size of the entire Armor HUD."),
		entry(SETTINGS_ARMOR_SPACING, "Slot Spacing"),
		entry(SETTINGS_ARMOR_SPACING_DESCRIPTION, "Controls the space between armor slots."),
		entry(SETTINGS_ARMOR_SLOT_STYLE, "Slot Style"),
		entry(SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION, "Selects the background style used for armor slots."),
		entry(SETTINGS_ARMOR_SHOW_EMPTY, "Show Empty Slots"),
		entry(SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION, "Shows slots even when no armor is equipped."),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE, "Center Visible Slots"),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION,
			"Centers equipped armor slots when empty slots are hidden."),
		entry(SETTINGS_VALUE_HORIZONTAL, "Horizontal"),
		entry(SETTINGS_VALUE_VERTICAL, "Vertical"),
		entry(SETTINGS_VALUE_CLEAR, "No Background"),
		entry(SETTINGS_VALUE_INVENTORY, "Inventory"),
		entry(SETTINGS_VALUE_HOTBAR, "Hotbar")
	), "en_us"),
	DE(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "HUD-Layout-Editor öffnen"),
		entry(MODULE_ARMOR, "Rüstungs-HUD"),
		entry(EDITOR_TITLE, "HUD-Layout-Editor"),
		entry(EDITOR_INSTRUCTIONS, "Ziehen zum Verschieben. Umschalt-Klick: Schnelleinstellungen."),
		entry(EDITOR_RESET, "Zurücksetzen"),
		entry(EDITOR_SAVE_FAILED, "Änderungen konnten nicht gespeichert werden."),
		entry(POPOVER_NO_SETTINGS, "Keine Schnelleinstellungen verfügbar."),
		entry(SETTINGS_ARMOR_ORIENTATION, "Ausrichtung"),
		entry(SETTINGS_ARMOR_ORIENTATION_DESCRIPTION,
			"Ordnet die Rüstungsslots horizontal oder vertikal an."),
		entry(SETTINGS_ARMOR_SCALE, "Skalierung"),
		entry(SETTINGS_ARMOR_SCALE_DESCRIPTION, "Ändert die Größe des gesamten Rüstungs-HUDs."),
		entry(SETTINGS_ARMOR_SPACING, "Slot-Abstand"),
		entry(SETTINGS_ARMOR_SPACING_DESCRIPTION, "Legt den Abstand zwischen Rüstungsslots fest."),
		entry(SETTINGS_ARMOR_SLOT_STYLE, "Slot-Stil"),
		entry(SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION, "Legt den Hintergrundstil der Rüstungsslots fest."),
		entry(SETTINGS_ARMOR_SHOW_EMPTY, "Leere Slots anzeigen"),
		entry(SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION,
			"Zeigt Slots auch an, wenn keine Rüstung ausgerüstet ist."),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE, "Sichtbare Slots zentrieren"),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION,
			"Zentriert ausgerüstete Rüstungsslots, wenn leere Slots ausgeblendet sind."),
		entry(SETTINGS_VALUE_HORIZONTAL, "Horizontal"),
		entry(SETTINGS_VALUE_VERTICAL, "Vertikal"),
		entry(SETTINGS_VALUE_CLEAR, "Ohne Hintergrund"),
		entry(SETTINGS_VALUE_INVENTORY, "Inventar"),
		entry(SETTINGS_VALUE_HOTBAR, "Schnellleiste")
	), "de_de", "de_at"),
	ES(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "Abrir el editor de diseños del HUD"),
		entry(MODULE_ARMOR, "HUD de armadura"),
		entry(EDITOR_TITLE, "Editor de diseño de HUD"),
		entry(EDITOR_INSTRUCTIONS, "Arrastra para mover. Mayús-clic: ajustes rápidos."),
		entry(EDITOR_RESET, "Restablecer"),
		entry(EDITOR_SAVE_FAILED, "No se han podido guardar los cambios."),
		entry(POPOVER_NO_SETTINGS, "No hay ajustes rápidos disponibles."),
		entry(SETTINGS_ARMOR_ORIENTATION, "Orientación"),
		entry(SETTINGS_ARMOR_ORIENTATION_DESCRIPTION,
			"Organiza las ranuras de armadura horizontal o verticalmente."),
		entry(SETTINGS_ARMOR_SCALE, "Escala"),
		entry(SETTINGS_ARMOR_SCALE_DESCRIPTION, "Cambia el tamaño de todo el HUD de armadura."),
		entry(SETTINGS_ARMOR_SPACING, "Espaciado de ranuras"),
		entry(SETTINGS_ARMOR_SPACING_DESCRIPTION, "Controla el espacio entre las ranuras de armadura."),
		entry(SETTINGS_ARMOR_SLOT_STYLE, "Estilo de ranura"),
		entry(SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION,
			"Selecciona el estilo de fondo de las ranuras de armadura."),
		entry(SETTINGS_ARMOR_SHOW_EMPTY, "Mostrar ranuras vacías"),
		entry(SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION,
			"Muestra las ranuras incluso cuando no hay armadura equipada."),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE, "Centrar ranuras visibles"),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION,
			"Centra las ranuras equipadas cuando las ranuras vacías están ocultas."),
		entry(SETTINGS_VALUE_HORIZONTAL, "Horizontal"),
		entry(SETTINGS_VALUE_VERTICAL, "Vertical"),
		entry(SETTINGS_VALUE_CLEAR, "Sin fondo"),
		entry(SETTINGS_VALUE_INVENTORY, "Inventario"),
		entry(SETTINGS_VALUE_HOTBAR, "Barra rápida")
	), "es_mx", "es_es"),
	FR_FR(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "Ouvrir l'éditeur de mise en page HUD"),
		entry(MODULE_ARMOR, "Affichage tête haute de l'armure"),
		entry(EDITOR_TITLE, "Éditeur de mise en page HUD"),
		entry(EDITOR_INSTRUCTIONS, "Glisser pour déplacer. Maj-clic : réglages rapides."),
		entry(EDITOR_RESET, "Réinitialiser"),
		entry(EDITOR_SAVE_FAILED, "Les modifications n'ont pas pu être enregistrées."),
		entry(POPOVER_NO_SETTINGS, "Aucun réglage rapide disponible."),
		entry(SETTINGS_ARMOR_ORIENTATION, "Orientation"),
		entry(SETTINGS_ARMOR_ORIENTATION_DESCRIPTION,
			"Dispose les emplacements d'armure horizontalement ou verticalement."),
		entry(SETTINGS_ARMOR_SCALE, "Échelle"),
		entry(SETTINGS_ARMOR_SCALE_DESCRIPTION, "Modifie la taille de tout l'affichage de l'armure."),
		entry(SETTINGS_ARMOR_SPACING, "Espacement des emplacements"),
		entry(SETTINGS_ARMOR_SPACING_DESCRIPTION, "Contrôle l'espace entre les emplacements d'armure."),
		entry(SETTINGS_ARMOR_SLOT_STYLE, "Style des emplacements"),
		entry(SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION,
			"Sélectionne le style d'arrière-plan des emplacements d'armure."),
		entry(SETTINGS_ARMOR_SHOW_EMPTY, "Afficher les emplacements vides"),
		entry(SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION,
			"Affiche les emplacements même lorsqu'aucune armure n'est équipée."),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE, "Centrer les emplacements visibles"),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION,
			"Centre les emplacements équipés lorsque les emplacements vides sont masqués."),
		entry(SETTINGS_VALUE_HORIZONTAL, "Horizontal"),
		entry(SETTINGS_VALUE_VERTICAL, "Vertical"),
		entry(SETTINGS_VALUE_CLEAR, "Sans arrière-plan"),
		entry(SETTINGS_VALUE_INVENTORY, "Inventaire"),
		entry(SETTINGS_VALUE_HOTBAR, "Barre rapide")
	), "fr_fr"),
	FR_CA(translations(
		entry(KEY_CATEGORY_GENERAL, "Another HUD Mod"),
		entry(KEY_OPEN_LAYOUT_EDITOR, "Ouvrir l'éditeur de disposition HUD"),
		entry(MODULE_ARMOR, "Interface d'affichage de l'armure"),
		entry(EDITOR_TITLE, "Éditeur de mise en page HUD"),
		entry(EDITOR_INSTRUCTIONS, "Glisser pour déplacer. Maj-clic : réglages rapides."),
		entry(EDITOR_RESET, "Réinitialiser"),
		entry(EDITOR_SAVE_FAILED, "Les modifications n'ont pas pu être enregistrées."),
		entry(POPOVER_NO_SETTINGS, "Aucun réglage rapide disponible."),
		entry(SETTINGS_ARMOR_ORIENTATION, "Orientation"),
		entry(SETTINGS_ARMOR_ORIENTATION_DESCRIPTION,
			"Dispose les emplacements d'armure horizontalement ou verticalement."),
		entry(SETTINGS_ARMOR_SCALE, "Échelle"),
		entry(SETTINGS_ARMOR_SCALE_DESCRIPTION, "Modifie la taille de toute l'interface d'armure."),
		entry(SETTINGS_ARMOR_SPACING, "Espacement des emplacements"),
		entry(SETTINGS_ARMOR_SPACING_DESCRIPTION, "Contrôle l'espace entre les emplacements d'armure."),
		entry(SETTINGS_ARMOR_SLOT_STYLE, "Style des emplacements"),
		entry(SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION,
			"Sélectionne le style d'arrière-plan des emplacements d'armure."),
		entry(SETTINGS_ARMOR_SHOW_EMPTY, "Afficher les emplacements vides"),
		entry(SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION,
			"Affiche les emplacements même lorsqu'aucune armure n'est équipée."),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE, "Centrer les emplacements visibles"),
		entry(SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION,
			"Centre les emplacements équipés lorsque les emplacements vides sont masqués."),
		entry(SETTINGS_VALUE_HORIZONTAL, "Horizontal"),
		entry(SETTINGS_VALUE_VERTICAL, "Vertical"),
		entry(SETTINGS_VALUE_CLEAR, "Sans arrière-plan"),
		entry(SETTINGS_VALUE_INVENTORY, "Inventaire"),
		entry(SETTINGS_VALUE_HOTBAR, "Barre rapide")
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
