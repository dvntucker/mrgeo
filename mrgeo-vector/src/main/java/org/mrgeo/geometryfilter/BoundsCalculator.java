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

package org.mrgeo.geometryfilter;

import org.mrgeo.geometry.Geometry;
import org.mrgeo.utils.Bounds;

import java.util.Iterator;


/**
 * @author jason.surratt
 * 
 */
public class BoundsCalculator
{
  public static Bounds calculateBounds(Iterator<? extends Geometry> it)
  {
    Bounds result = new Bounds();

    while (it.hasNext())
    {
      result.expand(it.next().getBounds());
    }

    return result;
  }
}
