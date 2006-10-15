package net.sourceforge.astyleclipse.preferences;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import net.sourceforge.astyleclipse.Activator;
import net.sourceforge.astyleclipse.AstyleLog;
import net.sourceforge.astyleclipse.astyle.ASFormatter;
import net.sourceforge.astyleclipse.astyle.AStyle;
import org.eclipse.jface.resource.JFaceResources;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class AstylePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	private static final String ASTYLE_OPTIONS_DELIMITER = " ";

	private static final String ASTYLE_STYLE_KR = "kr";

	private static final String ASTYLE_STYLE_LINUX = "linux";

	private static final String ASTYLE_STYLE_GNU = "gnu";

	private static final String ASTYLE_STYLE_ANSI = "ansi";

	private static final String ASTYLE_STYLE_NONE = "none";

	RadioGroupFieldEditor styleField;

	BooleanFieldEditor enableOptionFileField;

	StringFieldEditor optionsField;

	FileFieldEditor optionFileField;

	Document previewdocument;
	FontFieldEditor fontEditor;
	TextViewer textViewer;

	String selectedStyle = ASTYLE_STYLE_NONE;

	String previewCode = "";

	public AstylePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Setting for code formatting (engine is astyle 1.14.1 )");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {

		Composite composite = getFieldEditorParent();
		styleField = new RadioGroupFieldEditor(
				PreferenceConstants.STYLE_CHOICE, " style ", 1, new String[][] {
						{ "&ansi : ANSI style ", ASTYLE_STYLE_ANSI },
						{ "&gnu : GNU style", ASTYLE_STYLE_GNU },
						// FIXME, how to use & here Kernighan & Ritchie
						// FIXME, why the shortcut for keyboard disappear ?
						{ "&kr : Kernighan Ritchie style", ASTYLE_STYLE_KR },
						{ "&linux : Linux style", ASTYLE_STYLE_LINUX },
						{ "&no : no style", ASTYLE_STYLE_NONE },
				// { "&java : Java style", "java" },
				}, composite);
		addField(styleField);

		optionsField = new StringFieldEditor(PreferenceConstants.OTHER_OPTIONS,
				"Other &options (space is delimiter):", composite);
		addField(optionsField);

		// optionsField.getLabelControl(composite).setEnabled(false);
		// optionsField.getTextControl(composite).setEnabled(false);

		enableOptionFileField = new BooleanFieldEditor(
				PreferenceConstants.ENABLE_OPTION_FILE,
				"Enable astyle option file", composite);
		addField(enableOptionFileField);
		optionFileField = new FileFieldEditor(PreferenceConstants.OPTION_FILE,
				"Astyle option filename:", composite);
		optionFileField.setEnabled(enableOptionFileField.getBooleanValue(),
				composite);

		addField(optionFileField);

		// TODO : shall i inheritance from FieldEditor for
		// TextView so the PreferenceStore can be handled easily ?
		Label label = new Label(composite, SWT.NONE);
		label.setText("Preview:");

		textViewer = new TextViewer(composite, SWT.MULTI
				| SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY);
		previewCode = getPreferenceStore().getString(
				PreferenceConstants.PREVIEW_SOURCE);
		previewdocument = new Document(previewCode);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalSpan = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;

		textViewer.getTextWidget().setLayoutData(gd);
		// use JFaceresource to handle default font
		// Mac os is
		//org.eclipse.jface.textfont.0=Monaco-regular-11
		// Windows 2000 is
		// org.eclipse.jface.textfont.0=Courier New-regular-10
		textViewer.getTextWidget()
				.setFont(JFaceResources.getTextFont());
		textViewer.setDocument(previewdocument);
		// FIXME is it ok to use performOk
		performOk();
	}

	// When file option is enabled, the file valid shall be check
	// if not, file valid shall not be checked.
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		// AstyleLog.logInfo("value is changed event");
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			// AstyleLog.logInfo("it is value change");
			if (event.getSource() == enableOptionFileField) {
				// enable/disable the optionFileField
				optionFileField.setEnabled(enableOptionFileField
						.getBooleanValue(), getFieldEditorParent());
				checkState();
			} else if (event.getSource() == styleField) {
				selectedStyle = (String) event.getNewValue();
				checkState();
			} else if (event.getSource() == optionsField) {
				// check only when delimiter is entered
				if (event.getNewValue().toString().endsWith(
						ASTYLE_OPTIONS_DELIMITER)) {
					checkState();
				}
			}

		} else if (event.getProperty().equals(FieldEditor.IS_VALID)) {
			if (event.getSource() == optionFileField) {
				checkState();
			}
		}
	}

	protected void checkState() {
		super.checkState();
		if (updatePreview(false)) {
			setErrorMessage(null);
			setValid(true);
		} else {
			setValid(false);
		}
	}

	@Override
	// comes here as well if Apply button is pressed
	public boolean performOk() {
		boolean updateReferenceStore = true;
		boolean ok = updatePreview(updateReferenceStore);
		if (!ok)
			return false;
		return super.performOk();
	}

	/**
	 * @param updateReferenceStore
	 * @return
	 */
	private boolean updatePreview(boolean updateReferenceStore) {
		ASFormatter formatter = new ASFormatter();
		// AstyleLog.logInfo(selectedStyle);
		if (!selectedStyle.equalsIgnoreCase(ASTYLE_STYLE_NONE)) {
			// style: style=ansi
			if (!AStyle.parseOptions(formatter, "style=" + selectedStyle))
				return false;
		}
		if (!AStyle.parseOptions(formatter, optionsField.getStringValue())) {
			setErrorMessage("Option error");
			return false;
		}
		if (enableOptionFileField.getBooleanValue()) {
			if (!AStyle.parseOptionFile(formatter, optionFileField
					.getStringValue())) {
				setErrorMessage("Option file error");
				return false;
			}
		}
		String formattedString = AStyle.formatString(formatter,
				getPreferenceStore().getString(
						PreferenceConstants.PREVIEW_SOURCE));
		// AstyleLog.logInfo(formattedString);
		previewdocument.set(formattedString);
		// update data if press Ok/Apply
		if (updateReferenceStore) {
			getPreferenceStore().setDefault(PreferenceConstants.PREVIEW_SOURCE,
					formattedString);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}