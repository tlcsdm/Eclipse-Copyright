/*******************************************************************************
 * Copyright (c) 2008-2012 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.tlcsdm.eclipse.copyright.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import com.tlcsdm.eclipse.copyright.Constants;
import com.tlcsdm.eclipse.copyright.Messages;
import com.tlcsdm.eclipse.copyright.model.Copyright;
import com.tlcsdm.eclipse.copyright.model.CopyrightManager;
import com.tlcsdm.eclipse.copyright.model.CopyrightSettings;
import com.tlcsdm.eclipse.copyright.viewers.CopyrightContentProvider;
import com.tlcsdm.eclipse.copyright.viewers.CopyrightLabelProvider;
import com.tlcsdm.eclipse.copyright.viewers.CopyrightsComparator;
import com.tlcsdm.eclipse.copyright.viewers.CopyrightsInput;

public class CopyrightSettingsPage extends WizardPage {
  public static final String DEFAULT_PAGE_NAME = "copyrightSettingPage"; //$NON-NLS-1$
  private static final int LINES_NUMBER = 15;

  protected ComboViewer copyrightType;
  protected Text headerText;
  protected Text includePattern;
  protected Text excludePattern;
  protected Button forceApply;
  protected Button addLicenseFile;
  protected Text licenseFile;

  protected CopyrightSettings settings;
  protected Copyright selectedCopyright = null;

  CopyrightSettingsPage() {
    super(DEFAULT_PAGE_NAME);
    setTitle(Messages.CopyrightSettingsPage_title);
    setDescription(Messages.CopyrightSettingsPage_description);
  }

  public void createControl(Composite parent) {
    Font font = parent.getFont();
    FontData[] fontData = font.getFontData();
    GridData data;

    Composite top = new Composite(parent, SWT.NONE);
    top.setLayout(new GridLayout(2, false));
    top.setFont(font);

    Label l1 = new Label(top, SWT.NONE);
    l1.setText(Messages.CopyrightSettingsPage_labelTypes);
    l1.setFont(font);
    Combo combo = new Combo(top, SWT.BORDER | SWT.READ_ONLY);
    combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    combo.setFont(font);
    copyrightType = new ComboViewer(combo);
    copyrightType.setContentProvider(new CopyrightContentProvider());
    copyrightType.setLabelProvider(new CopyrightLabelProvider());
    copyrightType.setComparator(new CopyrightsComparator());
    copyrightType.setInput(new CopyrightsInput(true));

    Label l2 = new Label(top, SWT.NONE);
    l2.setText(Messages.CopyrightSettingsPage_labelHeader);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    l2.setLayoutData(data);
    l2.setFont(font);
    headerText = new Text(top, SWT.BORDER | SWT.MULTI | SWT.WRAP
                          | SWT.H_SCROLL | SWT.V_SCROLL);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    data.heightHint = (fontData.length > 0 ? fontData[0].getHeight() : 10) * LINES_NUMBER;
    headerText.setLayoutData(data);
    headerText.setFont(font);

    Label l3 = new Label(top, SWT.NONE);
    l3.setText(Messages.CopyrightSettingsPage_includePattern);
    l3.setFont(font);
    includePattern = new Text(top, SWT.BORDER);
    includePattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    includePattern.setFont(font);
    includePattern.setToolTipText(Messages.CopyrightSettingsPage_includePatternTooltip);
    Label l6 = new Label(top, SWT.NONE);
    l6.setText(Messages.CopyrightSettingsPage_excludePattern);
    l6.setFont(font);
    excludePattern = new Text(top, SWT.BORDER);
    excludePattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    excludePattern.setFont(font);
    excludePattern.setToolTipText(Messages.CopyrightSettingsPage_excludePatternTooltip);
    Label l4 = new Label(top, SWT.NONE);
    l4.setText(Messages.CopyrightSettingsPage_msgPatternsDescr);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    l4.setLayoutData(data);
    l4.setFont(font);

    forceApply = new Button(top, SWT.CHECK);
    forceApply.setText(Messages.CopyrightSettingsPage_checkboxReplaceHeaders);
    data = new GridData();
    data.horizontalSpan = 2;
    data.verticalIndent = 10;
    forceApply.setLayoutData(data);
    forceApply.setFont(font);

    addLicenseFile = new Button(top, SWT.CHECK);
    addLicenseFile.setText(Messages.CopyrightSettingsPage_checkboxAddLicense);
    data = new GridData();
    data.horizontalSpan = 2;
    addLicenseFile.setLayoutData(data);
    addLicenseFile.setFont(font);

    Label l5 = new Label(top, SWT.NONE);
    l5.setText(Messages.CopyrightSettingsPage_labelLicenseFile);
    l5.setFont(font);

    licenseFile = new Text(top, SWT.BORDER);
    licenseFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    licenseFile.setFont(font);

    createListeners();
    copyrightType.setSelection(new StructuredSelection(CopyrightManager.CUSTOM));
    PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ApplyCopyrightWizard.CONTEXT_ID);
    setPageComplete(false);
    setControl(top);
  }

  private void createListeners() {
    copyrightType.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        if ( selectedCopyright != null ) {
          selectedCopyright.setHeaderText(headerText.getText());
        }
        selectedCopyright = (Copyright) ((StructuredSelection) event.getSelection()).getFirstElement();
        settings.setCopyright(selectedCopyright);
        String header = selectedCopyright.getHeaderText();
        headerText.setText(header != null ? header : Constants.EMPTY_STRING);
        if ( selectedCopyright.getLicenseFilename().length() > 0 ) {
          addLicenseFile.setEnabled(true);
          licenseFile.setEnabled(addLicenseFile.getSelection());
          licenseFile.setText(selectedCopyright.getLicenseFilename());
        } else {
          addLicenseFile.setEnabled(false);
          addLicenseFile.setSelection(false);
          licenseFile.setEnabled(false);
          licenseFile.setText(Constants.EMPTY_STRING);
        }
        settings.setLicenseFile(addLicenseFile.getSelection()
                                ? licenseFile.getText()
                                : null);
      }
    });
    forceApply.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {}

      public void widgetSelected(SelectionEvent e) {
        settings.setForceApply(forceApply.getSelection());
      }
    });
    addLicenseFile.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {}

      public void widgetSelected(SelectionEvent e) {
        boolean selected = addLicenseFile.getSelection();
        licenseFile.setEnabled(selected);
        settings.setLicenseFile(selected ? licenseFile.getText() : null);
      }
    });

    ModifyListener listener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validatePage(e.widget);
      }
    };
    headerText.addModifyListener(listener);
    includePattern.addModifyListener(listener);
    excludePattern.addModifyListener(listener);
    licenseFile.addModifyListener(listener);
  }

  public void init(CopyrightSettings settings) {
    this.settings = settings;
  }

  protected void validatePage(Widget widget) {
    if ( widget == headerText ) {
      String header = headerText.getText().trim();
      setPageComplete(header.length() > 0);
      Copyright cp = settings.getCopyright();
      if ( cp != null ) {
        cp.setHeaderText(header);
      }
    } else if ( widget == includePattern ) {
      settings.setIncludePattern(includePattern.getText().trim());
    } else if ( widget == excludePattern ) {
      settings.setExcludePattern(excludePattern.getText().trim());
    } else if ( widget == licenseFile ) {
      settings.setLicenseFile(licenseFile.getText().trim());
    }
  }
}
