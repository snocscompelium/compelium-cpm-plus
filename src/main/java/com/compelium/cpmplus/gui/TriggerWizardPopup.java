package com.compelium.cpmplus.gui;

import java.util.ArrayList;
import java.util.List;

import com.compelium.cpmplus.physics.TriggerSettings;
import com.compelium.cpmplus.physics.TriggerType;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.Checkbox;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimationProperties;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.gui.popup.AnimationSettingsPopup;

/**
 * Edit &gt; Tools &gt; "Gatilhos do CPM+": picks a movement trigger and its settings and writes
 * the matching animation name, so nobody has to type it by hand.
 */
public class TriggerWizardPopup extends PopupPanel {
	private static final int WIDTH = 270;

	private final EditorGui eg;
	private final DropDownBox<Option> triggerBox;
	private final Label description, preview;
	private final Spinner peso, mola, forca, limite, limiar;
	private final Checkbox toggleBox;

	public TriggerWizardPopup(EditorGui eg) {
		super(eg.getGui());
		this.eg = eg;
		setBounds(new Box(0, 0, WIDTH, 300));
		FlowLayout layout = new FlowLayout(this, 4, 1);

		addElement(new Label(gui, gui.i18nFormat("cpmplus.wizard.trigger")).setBounds(new Box(5, 0, WIDTH - 10, 10)));

		List<Option> options = new ArrayList<>();
		for (TriggerType t : TriggerType.VALUES) {
			options.add(new Option(t, gui.i18nFormat(t.langKey())));
		}
		triggerBox = new DropDownBox<>(eg, options);
		triggerBox.setBounds(new Box(5, 0, WIDTH - 10, 20));
		addElement(triggerBox);

		description = new Label(gui, "");
		description.setBounds(new Box(5, 0, WIDTH - 10, 10));
		addElement(description);

		peso = addSetting("peso", TriggerSettings.DEFAULT_PESO);
		mola = addSetting("mola", TriggerSettings.DEFAULT_MOLA);
		forca = addSetting("forca", TriggerSettings.DEFAULT_FORCA);
		limite = addSetting("limite", TriggerSettings.DEFAULT_LIMITE);
		limiar = addSetting("limiar", TriggerSettings.DEFAULT_LIMIAR);

		toggleBox = new Checkbox(gui, gui.i18nFormat("cpmplus.wizard.toggle"));
		toggleBox.setBounds(new Box(5, 0, WIDTH - 10, 20));
		toggleBox.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.toggle")));
		toggleBox.setAction(() -> {
			toggleBox.setSelected(!toggleBox.isSelected());
			refresh();
		});
		addElement(toggleBox);

		preview = new Label(gui, "");
		preview.setBounds(new Box(5, 0, WIDTH - 10, 10));
		addElement(preview);

		Panel buttons = new Panel(gui);
		buttons.setBounds(new Box(0, 0, WIDTH, 20));
		addElement(buttons);

		Button create = new Button(gui, gui.i18nFormat("cpmplus.wizard.create"), this::createNew);
		create.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.create")));
		create.setBounds(new Box(0, 0, 80, 20));
		buttons.addElement(create);

		Editor editor = eg.getEditor();
		Button apply = new Button(gui, gui.i18nFormat("cpmplus.wizard.apply"), this::applyToSelected);
		apply.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.apply")));
		apply.setBounds(new Box(0, 0, 80, 20));
		apply.setEnabled(editor.selectedAnim != null);
		buttons.addElement(apply);

		Button copy = new Button(gui, gui.i18nFormat("cpmplus.wizard.copy"), () -> {
			gui.setClipboardText(currentName());
			close();
		});
		copy.setBounds(new Box(0, 0, 80, 20));
		buttons.addElement(copy);

		FlowLayout buttonLayout = new FlowLayout(buttons, 5, 0);
		buttonLayout.setSize(WIDTH);

		Button fix = new Button(gui, gui.i18nFormat("cpmplus.wizard.fix"), this::fixAll);
		fix.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.fix")));
		fix.setBounds(new Box(5, 0, WIDTH - 10, 20));
		addElement(fix);

		triggerBox.setAction(this::refresh);
		loadFromSelected(editor, options);
		refresh();
		layout.run();
	}

	private Spinner addSetting(String key, float def) {
		Panel row = new Panel(gui);
		row.setBounds(new Box(0, 0, WIDTH, 20));
		Label lbl = new Label(gui, gui.i18nFormat("cpmplus.wizard." + key));
		lbl.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip." + key)));
		lbl.setBounds(new Box(5, 5, 110, 10));
		row.addElement(lbl);
		Spinner sp = new Spinner(gui);
		sp.setDp(1);
		sp.setValue(def);
		sp.setBounds(new Box(120, 0, 70, 20));
		sp.addChangeListener(() -> {
			if (sp.getValue() < 0) sp.setValue(0);
			if (sp.getValue() > 10) sp.setValue(10);
			refresh();
		});
		row.addElement(sp);
		Label range = new Label(gui, "0 - 10");
		range.setBounds(new Box(196, 5, 60, 10));
		row.addElement(range);
		addElement(row);
		return sp;
	}

	private void loadFromSelected(Editor editor, List<Option> options) {
		EditorAnim anim = editor.selectedAnim;
		if (anim == null || anim.displayName == null) return;
		TriggerSettings st = TriggerSettings.parse(WizardUtil.baseName(anim.displayName));
		if (st == null) return;
		for (Option o : options) {
			if (o.type == st.type) triggerBox.setSelected(o);
		}
		peso.setValue(st.peso);
		mola.setValue(st.mola);
		forca.setValue(st.forca);
		limite.setValue(st.limite);
		limiar.setValue(st.limiar);
		toggleBox.setSelected(anim.type == AnimationType.LAYER);
	}

	private TriggerType selectedType() {
		Option o = triggerBox.getSelected();
		return o != null ? o.type : TriggerType.QUEDA;
	}

	private String currentName() {
		return TriggerSettings.nameFor(selectedType(), peso.getValue(), mola.getValue(), forca.getValue(), limite.getValue(), limiar.getValue());
	}

	private void refresh() {
		TriggerType t = selectedType();
		description.setText(gui.i18nFormat(t.langKey() + ".desc"));
		limiar.setEnabled(toggleBox.isSelected());
		preview.setText(gui.i18nFormat("cpmplus.wizard.name", currentName()));
	}

	private void createNew() {
		Editor editor = eg.getEditor();
		boolean toggle = toggleBox.isSelected();
		AnimationType type = toggle ? AnimationType.LAYER : AnimationType.VALUE_LAYER;
		close();
		editor.addNewAnim(new AnimationProperties(null, currentName(), type, true, false, InterpolatorType.LINEAR_SINGLE, true, false, false, false));
		eg.openPopup(new AnimationSettingsPopup(gui, editor, true));
	}

	private void applyToSelected() {
		Editor editor = eg.getEditor();
		EditorAnim anim = editor.selectedAnim;
		close();
		if (anim == null) return;
		String name = currentName() + WizardUtil.suffix(anim.displayName);
		InterpolatorType fixed = WizardUtil.aligned(anim.intType);
		editor.action("edit", "action.cpm.anim").
		updateValueOp(anim, anim.displayName, name, (a, b) -> a.displayName = b).
		updateValueOp(anim, anim.intType, fixed, (a, b) -> a.intType = b).
		onAction(anim, EditorAnim::clearCache).
		execute();
		editor.updateGui();
	}

	private void fixAll() {
		int changed = WizardUtil.fixAlignment(eg.getEditor());
		close();
		gui.displayMessagePopup(gui.i18nFormat("cpmplus.button.wizard"), gui.i18nFormat(changed == 0 ? "cpmplus.message.fix.none" : "cpmplus.message.fix.done", changed));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("cpmplus.button.wizard");
	}

	private static final class Option {
		final TriggerType type;
		final String label;

		Option(TriggerType type, String label) {
			this.type = type;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
