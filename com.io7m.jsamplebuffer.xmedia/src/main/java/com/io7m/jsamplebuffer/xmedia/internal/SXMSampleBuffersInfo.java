/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.jsamplebuffer.xmedia.internal;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteOrder;

final class SXMSampleBuffersInfo
{
  private SXMSampleBuffersInfo()
  {

  }

  static ByteOrder byteOrderOf(
    final AudioFormat format)
  {
    return format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  }

  public static double mapRangeToNormal(
    final double inputReal,
    final double inputMin,
    final double inputMax)
  {
    assert inputReal >= inputMin;
    assert inputReal <= inputMax;

    final var inputRange = inputMax - inputMin;

    final var outputMax = 1.0;
    final var outputMin = -1.0;
    final var outputRange = outputMax - outputMin;

    final var outputValue =
      (((inputReal - inputMin) * outputRange) / inputRange) + outputMin;

    assertNormalized(outputValue);
    return outputValue;
  }

  private static void assertNormalized(
    final double r)
  {
    assert r >= -1.0 : r + " >= -1.0";
    assert r <= 1.0 : r + " <= 1.0";
  }
}
