package com.compelium.cpmplus.gui;

import java.util.ArrayList;
import java.util.List;

import com.compelium.cpmplus.bends.BendName;
import com.compelium.cpmplus.bends.BendPart;
import com.tom.cpl.gui.elements.Button;
import com.tom.cpl.gui.elements.DropDownBox;
import com.tom.cpl.gui.elements.Label;
import com.tom.cpl.gui.elements.Panel;
import com.tom.cpl.gui.elements.PopupPanel;
import com.tom.cpl.gui.elements.Spinner;
import com.tom.cpl.gui.elements.Tooltip;
import com.tom.cpl.gui.util.FlowLayout;
import com.tom.cpl.math.Box;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.animation.AnimationType;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.anim.AnimFrame;
import com.tom.cpm.shared.editor.anim.AnimationProperties;
import com.tom.cpm.shared.editor.anim.EditorAnim;
import com.tom.cpm.shared.editor.elements.ElementType;
import com.tom.cpm.shared.editor.elements.ModelElement;
import com.tom.cpm.shared.editor.gui.EditorGui;
import com.tom.cpm.shared.editor.tree.TreeElement;
import com.tom.cpm.shared.model.PlayerModelParts;

/**
 * Edit &gt; Tools &gt; "Dobras do CPM+": marks the selected piece so it bends with Emotecraft's emotes.
 *
 * The mark is a hidden layer animation named like {@code bend_left_arm} that uses the piece.
 * The mod never plays it: it only tells which pieces turn at the elbow, knee or waist.
 * The small turn the assistant puts in it is a preview for the editor's test controls.
 */
public class BendWizardPopup extends PopupPanel {
	private static final int WIDTH = 270;
	private static final int MAX_PERCENT = 400;

	private final EditorGui eg;
	private final DropDownBox<PartOption> partBox;
	private final Spinner percent;
	private final Label pivot, preview;
	private final ModelElement piece;

	public BendWizardPopup(EditorGui eg) {
		super(eg.getGui());
		this.eg = eg;
		setBounds(new Box(0, 0, WIDTH, 200));
		FlowLayout layout = new FlowLayout(this, 4, 1);
		Editor editor = eg.getEditor();
		piece = selectedPiece(editor);

		addElement(new Label(gui, gui.i18nFormat("cpmplus.bend.intro")).setBounds(new Box(5, 0, WIDTH - 10, 10)));
		addElement(new Label(gui, gui.i18nFormat("cpmplus.bend.part")).setBounds(new Box(5, 0, WIDTH - 10, 10)));

		List<PartOption> parts = new ArrayList<>();
		for (BendPart p : BendPart.VALUES) {
			parts.add(new PartOption(p, gui.i18nFormat("cpmplus.bend.part." + p.palBone)));
		}
		partBox = new DropDownBox<>(eg, parts);
		partBox.setBounds(new Box(5, 0, WIDTH - 10, 20));
		partBox.setAction(this::refresh);
		addElement(partBox);

		Panel row = new Panel(gui);
		row.setBounds(new Box(0, 0, WIDTH, 20));
		Label lbl = new Label(gui, gui.i18nFormat("cpmplus.bend.percent"));
		lbl.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.bend.percent")));
		lbl.setBounds(new Box(5, 5, 110, 10));
		row.addElement(lbl);
		percent = new Spinner(gui);
		percent.setDp(0);
		percent.setValue(100);
		percent.setBounds(new Box(120, 0, 70, 20));
		percent.addChangeListener(() -> {
			if (percent.getValue() < 1) percent.setValue(1);
			if (percent.getValue() > MAX_PERCENT) percent.setValue(MAX_PERCENT);
			refresh();
		});
		row.addElement(percent);
		Label range = new Label(gui, "1 - " + MAX_PERCENT);
		range.setBounds(new Box(196, 5, 70, 10));
		row.addElement(range);
		addElement(row);

		pivot = new Label(gui, "");
		pivot.setBounds(new Box(5, 0, WIDTH - 10, 10));
		addElement(pivot);

		preview = new Label(gui, "");
		preview.setBounds(new Box(5, 0, WIDTH - 10, 10));
		addElement(preview);

		if (piece == null) {
			addElement(new Label(gui, gui.i18nFormat("cpmplus.bend.select")).setBounds(new Box(5, 0, WIDTH - 10, 10)));
		}

		Panel buttons = new Panel(gui);
		buttons.setBounds(new Box(0, 0, WIDTH, 20));
		addElement(buttons);

		Button mark = new Button(gui, gui.i18nFormat("cpmplus.bend.mark"), this::mark);
		mark.setTooltip(new Tooltip(eg, gui.i18nFormat("cpmplus.tooltip.bend.mark")));
		mark.setBounds(new Box(0, 0, 100, 20));
		mark.setEnabled(piece != null);
		buttons.addElement(mark);

		Button copy = new Button(gui, gui.i18nFormat("cpmplus.wizard.copy"), () -> {
			gui.setClipboardText(currentName());
			close();
		});
		copy.setBounds(new Box(0, 0, 100, 20));
		buttons.addElement(copy);

		FlowLayout buttonLayout = new FlowLayout(buttons, 5, 0);
		buttonLayout.setSize(WIDTH);

		BendPart guess = guessPart(piece);
		if (guess != null) {
			for (PartOption o : parts) {
				if (o.part == guess) partBox.setSelected(o);
			}
		}
		refresh();
		layout.run();
	}

	/** The selected piece, or null when nothing (or a whole vanilla body part) is selected. */
	private static ModelElement selectedPiece(Editor editor) {
		TreeElement sel = editor.selectedElement;
		if (!(sel instanceof ModelElement)) return null;
		ModelElement e = (ModelElement) sel;
		return e.type == ElementType.ROOT_PART ? null : e;
	}

	/** The body part the piece sits in, so the right option is already selected. */
	private static BendPart guessPart(ModelElement e) {
		while (e != null && e.type != ElementType.ROOT_PART) e = e.parent;
		if (e == null || !(e.typeData instanceof PlayerModelParts)) return null;
		switch ((PlayerModelParts) e.typeData) {
		case LEFT_ARM: return BendPart.LEFT_ARM;
		case RIGHT_ARM: return BendPart.RIGHT_ARM;
		case LEFT_LEG: return BendPart.LEFT_LEG;
		case RIGHT_LEG: return BendPart.RIGHT_LEG;
		case BODY: return BendPart.TORSO;
		default: return null;
		}
	}

	private BendPart selectedPart() {
		PartOption o = partBox.getSelected();
		return o != null ? o.part : BendPart.LEFT_ARM;
	}

	private int currentPercent() {
		int p = Math.round(percent.getValue());
		return p < 1 ? 1 : Math.min(p, MAX_PERCENT);
	}

	private String currentName() {
		return BendName.nameFor(selectedPart(), currentPercent());
	}

	private void refresh() {
		BendPart p = selectedPart();
		String key = p.isTorso() ? "torso" : (p == BendPart.LEFT_LEG || p == BendPart.RIGHT_LEG ? "leg" : "arm");
		pivot.setText(gui.i18nFormat("cpmplus.bend.pivot." + key));
		preview.setText(gui.i18nFormat("cpmplus.wizard.name", currentName()));
	}

	/** A small turn in the bending direction, so the editor's test controls show the bend. */
	private static Vec3f previewTurn(BendPart p) {
		if (p.isTorso()) return new Vec3f(30, 0, 0);
		if (p == BendPart.LEFT_LEG || p == BendPart.RIGHT_LEG) return new Vec3f(90, 0, 0);
		return new Vec3f(-90, 0, 0);
	}

	private void mark() {
		Editor editor = eg.getEditor();
		BendPart part = selectedPart();
		int pct = currentPercent();
		String name = currentName();
		close();
		if (piece == null) return;

		EditorAnim anim = null;
		for (EditorAnim a : editor.animations) {
			BendName bn = BendName.parse(WizardUtil.baseName(a.displayName));
			if (bn != null && bn.part == part && Math.abs(bn.factor - pct / 100f) < 0.0001f) {
				anim = a;
				break;
			}
		}
		boolean created = anim == null;
		if (created) {
			// A hidden layer: never shown in the gesture menu, never played by the mod
			editor.addNewAnim(new AnimationProperties(null, name, AnimationType.LAYER, true, false, WizardUtil.DEFAULT, false, false, false, true));
			anim = editor.selectedAnim;
		}
		String msg;
		if (anim == null) return;
		List<AnimFrame> frames = anim.getFrames();
		AnimFrame frame = frames.isEmpty() ? null : frames.get(0);
		if (frame == null) {
			anim.addFrame(false);
			frame = anim.getSelectedFrame();
		}
		if (frame != null && frame.getComponents().containsKey(piece)) {
			msg = "cpmplus.message.bend.already";
		} else {
			if (frame != null) {
				frame.setRot(piece, previewTurn(part));
				anim.clearCache();
			}
			msg = created ? "cpmplus.message.bend.created" : "cpmplus.message.bend.added";
		}
		// Leave the animation unselected so the next edits change the model, not the mark
		editor.selectedAnim = null;
		editor.selectedElement = piece;
		editor.updateGui();
		gui.displayMessagePopup(gui.i18nFormat("cpmplus.button.bend"), gui.i18nFormat(msg, WizardUtil.baseName(anim.displayName)));
	}

	@Override
	public String getTitle() {
		return gui.i18nFormat("cpmplus.button.bend");
	}

	private static final class PartOption {
		final BendPart part;
		final String label;

		PartOption(BendPart part, String label) {
			this.part = part;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
