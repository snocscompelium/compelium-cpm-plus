package com.compelium.cpmplus.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.compelium.cpmplus.effects.EmitterSettings;
import com.compelium.cpmplus.effects.ParticlePresets;
import com.compelium.cpmplus.physics.TriggerType;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.TextField;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.util.ItemSlot;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.animation.interpolator.InterpolatorType;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.actions.ActionBuilder;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimationProperties;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.model.render.ItemRenderer;

/**
 * Edit &gt; Tools &gt; "Partículas do CPM+": creates an invisible marker point on the selected
 * part and the animation that tells the mod which particles come out of it.
 *
 * The marker is one of CPM's own item holder points, so CPM already reports its position
 * every frame and never draws it. The animation only carries the settings in its name and
 * a tiny, never-played offset on the marker that links the two together.
 */
public class EmitterWizardPopup extends PopupPanel {
	private static final int WIDTH = 280;
	private static final String CUSTOM = "";

	private final EditorGui eg;
	private final DropDownBox<NamedValue<String>> particleBox;
	private final TextField idField, colorField;
	private final Spinner qtd, vel, esp, tam;
	private final DropDownBox<NamedValue<TriggerType>> gateBox;
	private final Label preview, help;

	public EmitterWizardPopup(EditorGui eg) {
		super(eg.getGui());
		this.eg = eg;
		setBounds(new Box(0, 0, WIDTH, 340));
		FlowLayout layout = new FlowLayout(this, 4, 1);

		addElement(new Label(gui, gui.i18nFormat("cpmplus.emitter.particle")).setBounds(new Box(5, 0, WIDTH - 10, 10)));
		List<NamedValue<String>> particles = new ArrayList<>();
		for (String id : ParticlePresets.IDS) {
			particles.add(new NamedValue<>(id, gui.i18nFormat(ParticlePresets.langKey(id))));
		}
		particles.add(new NamedValue<>(CUSTOM, gui.i18nFormat("cpmplus.emitter.custom")));
		particleBox = new DropDownBox<>(eg, particles);
		particleBox.setBounds(new Box(5, 0, WIDTH - 10, 20));
		addElement(particleBox);

		Panel idRow = new Panel(gui);
		idRow.setBounds(new Box(0, 0, WIDTH, 20));
		Label idLabel = new Label(gui, gui.i18nFormat("cpmplus.emitter.id"));
		idLabel.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.emitter.id")));
		idLabel.setBounds(new Box(5, 5, 110, 10));
		idRow.addElement(idLabel);
		idField = new TextField(gui);
		idField.setBounds(new Box(120, 0, WIDTH - 125, 20));
		idField.setText(EmitterSettings.DEFAULT_PARTICLE);
		idField.setEventListener(this::refresh);
		idRow.addElement(idField);
		addElement(idRow);

		qtd = addSetting("qtd", EmitterSettings.DEFAULT_QTD, 0, EmitterSettings.MAX_QTD, 0);
		vel = addSetting("vel", EmitterSettings.DEFAULT_VEL, 0, 10, 1);
		esp = addSetting("esp", EmitterSettings.DEFAULT_ESP, 0, 10, 1);
		tam = addSetting("tam", EmitterSettings.DEFAULT_TAM, 0.1f, 4, 1);

		Panel colorRow = new Panel(gui);
		colorRow.setBounds(new Box(0, 0, WIDTH, 20));
		Label colorLabel = new Label(gui, gui.i18nFormat("cpmplus.emitter.cor"));
		colorLabel.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.emitter.cor")));
		colorLabel.setBounds(new Box(5, 5, 110, 10));
		colorRow.addElement(colorLabel);
		colorField = new TextField(gui);
		colorField.setBounds(new Box(120, 0, 70, 20));
		colorField.setText("ffffff");
		colorField.setEventListener(this::refresh);
		colorRow.addElement(colorField);
		addElement(colorRow);

		addElement(new Label(gui, gui.i18nFormat("cpmplus.emitter.gate")).setBounds(new Box(5, 0, WIDTH - 10, 10)));
		List<NamedValue<TriggerType>> gates = new ArrayList<>();
		gates.add(new NamedValue<>(null, gui.i18nFormat("cpmplus.emitter.gate.always")));
		for (TriggerType t : TriggerType.VALUES) {
			gates.add(new NamedValue<>(t, gui.i18nFormat(t.langKey())));
		}
		gateBox = new DropDownBox<>(eg, gates);
		gateBox.setBounds(new Box(5, 0, WIDTH - 10, 20));
		gateBox.setAction(this::refresh);
		addElement(gateBox);

		help = new Label(gui, "");
		help.setBounds(new Box(5, 0, WIDTH - 10, 10));
		addElement(help);

		preview = new Label(gui, "");
		preview.setBounds(new Box(5, 0, WIDTH - 10, 10));
		addElement(preview);

		Panel buttons = new Panel(gui);
		buttons.setBounds(new Box(0, 0, WIDTH, 20));
		addElement(buttons);

		Button create = new Button(gui, gui.i18nFormat("cpmplus.emitter.create"), this::create);
		create.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.emitter.create")));
		create.setBounds(new Box(0, 0, 85, 20));
		buttons.addElement(create);

		Editor editor = eg.getEditor();
		Button apply = new Button(gui, gui.i18nFormat("cpmplus.wizard.apply"), this::applyToSelected);
		apply.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.emitter.apply")));
		apply.setBounds(new Box(0, 0, 85, 20));
		apply.setEnabled(editor.selectedAnim != null && EmitterSettings.parse(WizardUtil.baseName(editor.selectedAnim.displayName)) != null);
		buttons.addElement(apply);

		Button copy = new Button(gui, gui.i18nFormat("cpmplus.wizard.copy"), () -> {
			gui.setClipboardText(current().toName());
			close();
		});
		copy.setBounds(new Box(0, 0, 85, 20));
		buttons.addElement(copy);

		FlowLayout buttonLayout = new FlowLayout(buttons, 5, 0);
		buttonLayout.setSize(WIDTH);

		particleBox.setAction(() -> {
			NamedValue<String> sel = particleBox.getSelected();
			if (sel != null && !CUSTOM.equals(sel.value)) idField.setText(sel.value);
			refresh();
		});

		loadFromSelected(editor, particles, gates);
		refresh();
		layout.run();
	}

	private Spinner addSetting(String key, float def, float min, float max, int dp) {
		Panel row = new Panel(gui);
		row.setBounds(new Box(0, 0, WIDTH, 20));
		Label lbl = new Label(gui, gui.i18nFormat("cpmplus.emitter." + key));
		lbl.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.emitter." + key)));
		lbl.setBounds(new Box(5, 5, 110, 10));
		row.addElement(lbl);
		Spinner sp = new Spinner(gui);
		sp.setDp(dp);
		sp.setValue(def);
		sp.setBounds(new Box(120, 0, 70, 20));
		sp.addChangeListener(() -> {
			if (sp.getValue() < min) sp.setValue(min);
			if (sp.getValue() > max) sp.setValue(max);
			refresh();
		});
		row.addElement(sp);
		Label range = new Label(gui, format(min) + " - " + format(max));
		range.setBounds(new Box(196, 5, 80, 10));
		row.addElement(range);
		addElement(row);
		return sp;
	}

	private static String format(float v) {
		return Math.abs(v - Math.round(v)) < 0.001f ? Integer.toString(Math.round(v)) : String.format(Locale.ROOT, "%.1f", v);
	}

	private void loadFromSelected(Editor editor, List<NamedValue<String>> particles, List<NamedValue<TriggerType>> gates) {
		EditorAnim anim = editor.selectedAnim;
		EmitterSettings st = anim == null ? null : EmitterSettings.parse(WizardUtil.baseName(anim.displayName));
		String id = st != null ? st.particle : EmitterSettings.DEFAULT_PARTICLE;
		String bare = id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
		NamedValue<String> match = particles.get(particles.size() - 1);
		for (NamedValue<String> p : particles) {
			if (p.value.equals(bare)) match = p;
		}
		particleBox.setSelected(match);
		idField.setText(id);
		if (st == null) return;
		qtd.setValue(st.qtd);
		vel.setValue(st.vel);
		esp.setValue(st.esp);
		tam.setValue(st.tam);
		colorField.setText(String.format(Locale.ROOT, "%06x", st.cor));
		for (NamedValue<TriggerType> g : gates) {
			if (g.value == st.gatilho) gateBox.setSelected(g);
		}
	}

	private EmitterSettings current() {
		NamedValue<TriggerType> g = gateBox.getSelected();
		int cor = parseColor(colorField.getText());
		return EmitterSettings.of(idField.getText(), qtd.getValue(), vel.getValue(), esp.getValue(), cor, tam.getValue(), g != null ? g.value : null);
	}

	private static int parseColor(String s) {
		String h = s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
		if (h.startsWith("#")) h = h.substring(1);
		if (h.length() == 3) h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
		try {
			return h.length() == 6 ? Integer.parseInt(h, 16) : EmitterSettings.DEFAULT_COR;
		} catch (NumberFormatException e) {
			return EmitterSettings.DEFAULT_COR;
		}
	}

	private void refresh() {
		EmitterSettings st = current();
		boolean dust = st.usesColor();
		colorField.setEnabled(dust);
		tam.setEnabled(dust);
		help.setText(gui.i18nFormat(st.isBurst() ? "cpmplus.emitter.help.burst" : "cpmplus.emitter.help.rate"));
		preview.setText(gui.i18nFormat("cpmplus.wizard.name", st.toName()));
	}

	private void create() {
		Editor editor = eg.getEditor();
		TreeElement sel = editor.selectedElement;
		if (!(sel instanceof ModelElement)) {
			close();
			gui.displayMessagePopup(gui.i18nFormat("cpmplus.button.emitter"), gui.i18nFormat("cpmplus.message.emitter.select"));
			return;
		}
		ModelElement picked = (ModelElement) sel;
		EmitterSettings st = current();
		close();

		ModelElement marker;
		if (picked.itemRenderer != null) {
			// An existing point (an earlier marker or one of CPM's item holders): add another emitter to it
			marker = picked;
		} else {
			marker = new ModelElement(editor);
			marker.itemRenderer = new ItemRenderer(ItemSlot.API_SLOT, 0);
			marker.name = gui.i18nFormat("cpmplus.emitter.marker", gui.i18nFormat(ParticlePresets.langKey(bareId(st.particle))));
			marker.size = new Vec3f(0, 0, 0);
			marker.parent = picked;
			ActionBuilder ab = editor.action("add", "action.cpm.itemHold");
			ab.addToList(picked.children, marker);
			ab.execute();
		}

		editor.addNewAnim(new AnimationProperties(null, st.toName(), AnimationType.LAYER, true, false, InterpolatorType.LINEAR_SINGLE, true, false, false, false));
		EditorAnim anim = editor.selectedAnim;
		if (anim != null) {
			anim.addFrame(false);
			AnimFrame frame = anim.getSelectedFrame();
			// A tiny offset that is never played: it only links the marker to this emitter
			if (frame != null) frame.setPos(marker, new Vec3f(0, 0.02f, 0));
		}
		editor.selectedAnim = null;
		editor.selectedElement = marker;
		editor.updateGui();
		gui.displayMessagePopup(gui.i18nFormat("cpmplus.button.emitter"), gui.i18nFormat(marker == picked ? "cpmplus.message.emitter.added" : "cpmplus.message.emitter.created"));
	}

	private static String bareId(String id) {
		String b = id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
		for (String p : ParticlePresets.IDS) {
			if (p.equals(b)) return b;
		}
		return "custom";
	}

	private void applyToSelected() {
		Editor editor = eg.getEditor();
		EditorAnim anim = editor.selectedAnim;
		close();
		if (anim == null) return;
		String name = current().toName() + WizardUtil.suffix(anim.displayName);
		editor.action("edit", "action.cpm.anim").
		updateValueOp(anim, anim.displayName, name, (a, b) -> a.displayName = b).
		execute();
		editor.updateGui();
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("cpmplus.button.emitter");
	}

	private static final class NamedValue<T> {
		final T value;
		final String label;

		NamedValue(T value, String label) {
			this.value = value;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
