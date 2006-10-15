package net.sourceforge.astyleclipse.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import net.sourceforge.astyleclipse.Activator;
import net.sourceforge.astyleclipse.AstyleLog;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.STYLE_CHOICE, "ansi");
		store.setDefault(PreferenceConstants.OTHER_OPTIONS,"");
		String env = System.getProperty("user.home");
		String defaultFile = ".astylerc";
		if (env != null) {
			// AstyleLog.logInfo("home is:" + env);
			defaultFile = env + File.separator + ".astylerc";
		}
		store.setDefault(PreferenceConstants.OPTION_FILE,defaultFile);
		store.setDefault(PreferenceConstants.ENABLE_OPTION_FILE, false);
		
		String PREVIEW = "namespace foospace \n" + "{ \n" + " int Foo()\n"
		+ " {\n" + " if (isBar) { \n" + " bar();\n" + " return 1;\n" + " } else \n"
		+ " return 0;\n" + " }\n" + "}\n";
		store.setDefault(PreferenceConstants.PREVIEW_SOURCE, PREVIEW);
	}

}
