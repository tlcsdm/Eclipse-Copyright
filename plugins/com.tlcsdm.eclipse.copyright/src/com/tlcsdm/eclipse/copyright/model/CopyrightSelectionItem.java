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
package com.tlcsdm.eclipse.copyright.model;

import org.eclipse.core.resources.IResource;

public class CopyrightSelectionItem {
  protected IResource resource;
  protected CopyrightSelectionItem parent;
  protected CopyrightSelectionItem[] children;
  protected int selected = 0;

  public CopyrightSelectionItem(IResource resource, CopyrightSelectionItem[] children) {
    this.resource  = resource;
    this.children = children;
    this.selected = 2;
    if ( children != null ) {
      for (CopyrightSelectionItem child : children) {
        child.parent = this;
      }
    }
  }

  public IResource getResource() {
    return resource;
  }

  public CopyrightSelectionItem[] getChildren() {
    return children;
  }

  public CopyrightSelectionItem getParent() {
    return parent;
  }

  public int getSelected() {
    return selected;
  }

  public void setSelected(int selected) {
    this.selected = selected;
  }

  @Override
  public String toString() {
    return resource.getName() + "," + selected;
  }
}
