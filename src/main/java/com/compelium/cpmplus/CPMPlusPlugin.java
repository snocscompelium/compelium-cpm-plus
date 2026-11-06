package com.compelium.cpmplus;

import com.compelium.cpmplus.gui.BendWizardPopup;
import com.compelium.cpmplus.gui.EmitterWizardPopup;
import com.compelium.cpmplus.gui.TriggerWizardPopup;
import com.tom.cpm.api.CPMPlugin;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;
import com.tom.cpm.shared.util.Log;

/**
 * Entry point CPM calls when it starts. Adds the assistants to the editor's
 * Edit &gt; Tools menu.
 */
@CPMPlugin
public class CPMPlusPlugin implements ICPMPlugin {
	public static final String MOD_ID = "cpmplus";

	@Override
	public void initClient(IClientAPI api) {
		api.registerEditorGenerator("cpmplus.button.wizard", "cpmplus.tooltip.wizard", eg -> eg.openPopup(new TriggerWizardPopup(eg)));
		api.registerEditorGenerator("cpmplus.button.emitter", "cpmplus.tooltip.emitter", eg -> eg.openPopup(new EmitterWizardPopup(eg)));
		api.registerEditorGenerator("cpmplus.button.bend", "cpmplus.tooltip.bend", eg -> eg.openPopup(new BendWizardPopup(eg)));
		Log.info("[Compelium's CPM+] Loaded");
	}

	@Override
	public void initCommon(ICommonAPI api) {
	}

	@Override
	public String getOwnerModId() {
		return MOD_ID;
	}
}
