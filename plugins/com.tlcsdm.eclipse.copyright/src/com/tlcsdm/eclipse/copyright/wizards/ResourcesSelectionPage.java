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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PatternFilter;

import com.tlcsdm.eclipse.copyright.Messages;
import com.tlcsdm.eclipse.copyright.controls.CheckboxFilteredTree;
import com.tlcsdm.eclipse.copyright.model.CopyrightSelectionItem;
import com.tlcsdm.eclipse.copyright.model.CopyrightSettings;

public class ResourcesSelectionPage extends WizardPage {
  public static final String DEFAULT_PAGE_NAME = "resourcesSelectionPage"; //$NON-NLS-1$

  protected CheckboxTreeViewer viewer;
  protected Label selectionReport;
  protected int checkedCount = 0;
  protected int totalCount = 0;

  ResourcesSelectionPage() {
    super(DEFAULT_PAGE_NAME);
    setTitle(Messages.ResourcesSelectionPage_title);
    setDescription(Messages.ResourcesSelectionPage_description);
  }

  public void createControl(Composite parent) {
    Font font = parent.getFont();

    Composite top = new Composite(parent, SWT.NONE);
    top.setLayout(new GridLayout(1, false));
    top.setFont(font);

    PatternFilter filter = new PatternFilter() {
      @Override
      public boolean isElementSelectable(Object element) {
        if ( element instanceof CopyrightSelectionItem ) {
          return ((CopyrightSelectionItem) element).getResource() instanceof IFile;
        }
        return false;
      }

      @Override
      protected boolean isLeafMatch(Viewer viewer, Object element) {
        if ( element instanceof CopyrightSelectionItem
        		&& ((CopyrightSelectionItem) element).getResource() instanceof IFile ) {
          return super.isLeafMatch(viewer, element);
        }
        return false;
      }
    };
    CheckboxFilteredTree filteredTree = new CheckboxFilteredTree(top,
    		SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);

    viewer = filteredTree.getViewer();
    viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
    viewer.setContentProvider(new SelectionContentProvider());
    viewer.setLabelProvider(new SelectionLabelProvider());
    viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        CopyrightSelectionItem element = (CopyrightSelectionItem) event.getElement();
        if ( element.getResource() instanceof IFile ) {
          checkedCount += event.getChecked() ? 1 : -1;
          element.setSelected(event.getChecked() ? 2 : 0);
        } else {
          updateCount(element, event.getChecked());
        }
        setSubtreeChecked(element, false);
        viewer.setGrayed(event.getElement(), false);
        viewer.setSubtreeChecked(event.getElement(), event.getChecked());
        updateParentState(element.getParent());
        validatePage();
      }
    });
    viewer.setCheckStateProvider(new ICheckStateProvider() {
      public boolean isChecked(Object element) {
        return ((CopyrightSelectionItem) element).getSelected() > 0;
      }

      public boolean isGrayed(Object element) {
        return ((CopyrightSelectionItem) element).getSelected() == 1;
      }
    });

    selectionReport = new Label(top, SWT.NONE);
    selectionReport.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ApplyCopyrightWizard.CONTEXT_ID);
    setPageComplete(false);
    setControl(top);
  }

  public void getSelection(CopyrightSettings settings) {
    ArrayList<IFile> selection = new ArrayList<IFile>();
    for (Object o : viewer.getCheckedElements()) {
      CopyrightSelectionItem item = (CopyrightSelectionItem) o;
      if ( item.getResource() instanceof IFile ) {
        selection.add((IFile) item.getResource());
      }
    }
    settings.setFiles(selection.toArray(new IFile[selection.size()]));
  }

  private void setChecked(TreeItem[] items) {
    for (TreeItem item : items) {
      item.setChecked(true);
      Object data = item.getData();
      if ( data != null ) {
        CopyrightSelectionItem csi = (CopyrightSelectionItem) data;
        if ( csi.getResource() instanceof IFile ) {
          checkedCount++;
        }
      }
      setChecked(item.getItems());
    }
  }

  public void setSelection(CopyrightSelectionItem[] selection) {
    checkedCount = 0;
    viewer.setInput(new CopyrightSelectionInput(selection));
    viewer.getTree().setRedraw(false);
    viewer.expandAll();
    setChecked(viewer.getTree().getItems());
    viewer.collapseAll();
    viewer.getTree().setRedraw(true);
    totalCount = checkedCount;
    validatePage();
  }

  private void setSubtreeChecked(CopyrightSelectionItem element, boolean grayed) {
    CopyrightSelectionItem[] children = element.getChildren();
    if ( children != null ) {
      for (CopyrightSelectionItem child : children) {
        viewer.setGrayChecked(child, grayed);
        setSubtreeChecked(child, grayed);
      }
    }
  }

  private void updateCount(CopyrightSelectionItem element, boolean checked) {
    element.setSelected(checked ? 2 : 0);
    if ( element.getResource() instanceof IFile ) {
      boolean isChecked = viewer.getChecked(element);
      if ( isChecked && ! checked ) {
        checkedCount --;
      } else if ( (! isChecked) && checked ) {
        checkedCount ++;
      }
    } else {
      for (CopyrightSelectionItem child : element.getChildren()) {
        updateCount(child, checked);
      }
    }
  }

  private void updateParentState(CopyrightSelectionItem parent) {
    if ( parent == null ) return;

    CopyrightSelectionItem[] children = parent.getChildren();
    int checked = 0;
    int grayed = 0;
    for (CopyrightSelectionItem child : children) {
      if ( viewer.getChecked(child) ) checked++;
      if ( viewer.getGrayed(child) ) grayed++;
    }
    int selected = (grayed > 0 || (checked < children.length && checked > 0))
    		? 1 : (checked + grayed > 0 ? 2 : 0);
    viewer.setChecked(parent, selected > 0);
    viewer.setGrayed(parent, selected == 1);
    parent.setSelected(selected);

    updateParentState(parent.getParent());
  }

  protected void validatePage() {
    selectionReport.setText(totalCount > 0
    		? NLS.bind(Messages.ResourcesSelectionPage_selectedFileInfo, checkedCount, totalCount)
    				: Messages.ResourcesSelectionPage_noResourcesInfo);
    setPageComplete(checkedCount > 0);
  }
}
