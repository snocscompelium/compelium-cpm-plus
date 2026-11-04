package com.compelium.cpmplus;

import com.compelium.cpmplus.gui.TriggerWizardPopup;
import com.tom.cpm.api.CPMPlugin;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;
import com.tom.cpm.shared.util.Log;

/**
 * Entry point CPM calls when it starts. Adds the trigger assistant to the editor's
 * Edit &gt; Tools menu.
 */
@CPMPlugin
public class CPMPlusPlugin implements ICPMPlugin {
	public static final String MOD_ID = "cpmplus";

	@Override
	public void initClient(IClientAPI api) {
		api.registerEditorGenerator("cpmplus.button.wizard", "cpmplus.tooltip.wizard", eg -> eg.openPopup(new TriggerWizardPopup(eg)));
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
