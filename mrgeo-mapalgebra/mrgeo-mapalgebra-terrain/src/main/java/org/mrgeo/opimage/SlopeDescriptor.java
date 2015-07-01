/*
 * Copyright 2009-2015 DigitalGlobe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mrgeo.opimage;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

/**
 * @author jason.surratt
 *
 */
public class SlopeDescriptor extends OperationDescriptorImpl implements
    RenderedImageFactory
{
  public static final String OPERATION_NAME = SlopeDescriptor.class.getName();
  private static final long serialVersionUID = 1L;

  public static RenderedOp create(RenderedImage slope, RenderingHints hints)
  {
    ParameterBlock paramBlock = (new ParameterBlock()).addSource(slope);
    return JAI.create(OPERATION_NAME, paramBlock, hints);
  }

  public static RenderedOp create(RenderedImage slope, String units, RenderingHints hints)
  {
    ParameterBlock paramBlock = (new ParameterBlock()).addSource(slope);
    paramBlock.add(units);
    return JAI.create(OPERATION_NAME, paramBlock, hints);
  }

  public SlopeDescriptor()
  {
    // I realize this formatting is horrendous, but Java won't let me assign
    // variables before
    // calling super.
    super(new String[][] { { "GlobalName", OPERATION_NAME }, { "LocalName", OPERATION_NAME },
        { "Vendor", "com.spadac" }, { "Description", "Max slope values for an elevation surface" },
        { "DocURL", "http://www.spadac.com/" }, { "Version", "1.0" } },
        new String[] { RenderedRegistryMode.MODE_NAME },
        1,
        new String[] { "units"},
        new Class[] { String.class },
        new Object[] { "deg" }, null);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.image.renderable.RenderedImageFactory#create(java.awt.image.renderable
   * .ParameterBlock, java.awt.RenderingHints)
   */
  @Override
  public RenderedImage create(ParameterBlock paramBlock, RenderingHints hints)
  {
    RenderedImage normal = HornNormalDescriptor.create(paramBlock.getRenderedSource(0), null);

    if (paramBlock.getNumParameters() > 0)
    {
      return SlopeOpImage.create(normal, (String) paramBlock.getObjectParameter(0), hints);
    }
    return SlopeOpImage.create(normal, hints);

  }

}
