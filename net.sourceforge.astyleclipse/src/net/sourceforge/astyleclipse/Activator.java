/*
 * Source code is copied from CDT contribute indent.1.0.2.zip as example
 */
package net.sourceforge.astyleclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.astyleclipse";
	public static final String CDT_FORMAT_RESOURCE_ID = "org.eclipse.cdt.indent.FormatterPluginResources";

	// The shared instance
	private static Activator plugin;
	private ResourceBundle resourceBundle;
	/**
	 * The constructor
	 */
	public Activator() {
		// System.out.println("Hello, I am called");
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle(CDT_FORMAT_RESOURCE_ID);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		// System.out.println("Hello, I am started");
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	
	public static String getResourceString(String key) {
		ResourceBundle bundle = Activator.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
