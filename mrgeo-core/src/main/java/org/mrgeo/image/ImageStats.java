/*
 * Copyright 2009-2016 DigitalGlobe, Inc.
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
 *
 */

package org.mrgeo.image;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.mrgeo.data.adhoc.AdHocDataProvider;
import org.mrgeo.data.raster.MrGeoRaster;
import org.mrgeo.data.raster.RasterWritable.RasterWritableException;
import org.mrgeo.utils.FloatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public class ImageStats implements Cloneable, Serializable
{
private static final long serialVersionUID = 1L;

public double min, max, mean, sum;
public long count;

@SuppressWarnings("unused")
private static Logger log = LoggerFactory.getLogger(ImageStats.class);

// tile id that is not valid for TMS and represents stats output from an OpChain mapper
public static final long STATS_TILE_ID = -13;

// count of image statistics measures, currently min, max, sum & count
public static final int STATS_COUNT = 4;

// TODO: Add minx, maxx, miny, maxy
public ImageStats()
{
}

public ImageStats(final double min, final double max)
{
  this.min = min;
  this.max = max;
}

public ImageStats(final double min, final double max, final double sum, final long count)
{
  this.min = min;
  this.max = max;
  this.sum = sum;
  this.count = count;
  this.mean = sum / count;
}

/**
 * Aggregates statistics output from multiple tasks
 *
 * @param listOfStats
 *          the stats arrays to aggregate
 * @return an array of ImageStats objects
 */
static public ImageStats[] aggregateStats(final List<ImageStats[]> listOfStats)
{
  ImageStats[] stats;
  if (listOfStats.isEmpty())
  {
    stats = initializeStatsArray(0);
  }
  else
  {
    // aggregate stats from each mapper or reducer
    final int bands = listOfStats.get(0).length;
    stats = initializeStatsArray(bands);
    for (final ImageStats[] s : listOfStats)
    {
      for (int i = 0; i < s.length; i++)
      {
        stats[i].min = Math.min(stats[i].min, s[i].min);
        stats[i].max = Math.max(stats[i].max, s[i].max);
        stats[i].sum += s[i].sum;
        stats[i].count += s[i].count;
      }
    }
    // update metadata
    for (int i = 0; i < bands; i++)
    {
      stats[i].mean = stats[i].sum / stats[i].count;
    }

  }
  return stats;
}

/**
 * Computes pixel value statistics: min, max, sum, count, & mean for a Raster and returns an array
 * of ImageStats objects, one for each band in the image.
 *
 * @param raster
 *          the raster to compute stats for
 * @param nodata
 *          the value to ignore
 *
 */
static public void computeAndUpdateStats(final ImageStats[] tileStats, final MrGeoRaster raster,
    final double[] nodata)
{
  double sample;
  for (int y = 0; y < raster.height(); y++)
  {
    for (int x = 0; x < raster.width(); x++)
    {
      for (int b = 0; b < raster.bands(); b++)
      {
        sample = raster.getPixelDouble(x, y, b);

        // throw out NaN samples
        if (FloatUtils.isNotNodata(sample, nodata[b]))
        {
          tileStats[b].min = Math.min(sample, tileStats[b].min);
          tileStats[b].max = Math.max(sample, tileStats[b].max);
          tileStats[b].sum += sample;
          tileStats[b].count++;
          tileStats[b].mean = tileStats[b].sum / tileStats[b].count;
        }
      }
    }
  }

}

/**
 * Computes pixel value statistics: min, max, sum, count, & mean for a Raster and returns an array
 * of ImageStats objects, one for each band in the image.
 *
 * @param raster
 *          the raster to compute stats for
 * @param nodata
 *          the value to ignore
 * @return an array of ImageStats objects
 */
static public ImageStats[] computeStats(final MrGeoRaster raster, final double[] nodata)
    throws RasterWritableException
{
  final ImageStats[] tileStats = initializeStatsArray(raster.bands());
  computeAndUpdateStats(tileStats, raster, nodata);
  return tileStats;
}

/**
 * Initializes an array of ImageStats objects, one for each band in the image, and initializes the
 * properties min, max, sum, count & mean to appropriate values.
 *
 * @param bands
 *          the number of bands in the image
 * @return an array of ImageStats objects
 */
static public ImageStats[] initializeStatsArray(final int bands)
{
  final ImageStats[] stats = new ImageStats[bands];
  for (int i = 0; i < bands; i++)
  {
    stats[i] = new ImageStats(Double.MAX_VALUE, -Double.MAX_VALUE);
    stats[i].sum = 0;
    stats[i].count = 0;
    stats[i].mean = 0;
  }
  return stats;
}



static public void writeStats(final AdHocDataProvider provider, final ImageStats[] stats)
    throws IOException
{
  OutputStream stream = null;

  try
  {
    stream = provider.add();

    final ObjectMapper mapper = new ObjectMapper();
    try
    {
      DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
      pp.indentArraysWith(new DefaultPrettyPrinter.Lf2SpacesIndenter());

      mapper.prettyPrintingWriter(pp).writeValue(stream, stats);
    }
    catch (final NoSuchMethodError e)
    {
      // if we don't have the pretty printer, just write the json
      mapper.writeValue(stream, stats);
    }
  }
  finally
  {
    if (stream != null)
    {
      stream.close();
    }
  }
}


@Override
@SuppressFBWarnings(value = "CN_IDIOM_NO_SUPER_CALL", justification = "No super.clone() to call")
public Object clone()
{
  final ImageStats s = new ImageStats(min, max, sum, count);
  s.mean = sum / count;
  return s;
}

@Override
public boolean equals(final Object obj)
{
  if (obj == null)
  {
    return false;
  }
  if (obj == this)
  {
    return true;
  }
  if (obj.getClass() != getClass())
  {
    return false;
  }

  final ImageStats rhs = (ImageStats) obj;
  return new EqualsBuilder()
      .
      // if deriving: appendSuper(super.equals(obj)).
          append(min, rhs.min).append(max, rhs.max).append(sum, rhs.sum).append(count, rhs.count)
      .isEquals();
}

@Override
public int hashCode()
{
  return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
      // if deriving: appendSuper(super.hashCode()).
          append(min).append(max).append(sum).append(count).toHashCode();
}

}
