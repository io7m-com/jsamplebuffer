/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.jsamplebuffer.tests.xmedia;

import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.vanilla.SampleBufferFloat;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBufferRateConverters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SXMSampleBufferRateConvertersTest
{
  private SXMSampleBufferRateConverters converters;

  @BeforeEach
  public void setup()
  {
    this.converters =
      new SXMSampleBufferRateConverters();
  }

  /**
   * Test that a range of common conversion rates are supported.
   *
   * @return A stream of tests
   */

  @TestFactory
  public Stream<DynamicTest> testConversionsDouble()
  {
    final var rates =
      DoubleStream.of(
          8000.0,
          11025.0,
          16000.0,
          22050.0,
          44100.0,
          48000.0,
          88200.0,
          96000.0,
          176400.0,
          192000.0,
          352800.0,
          384000.0
        )
        .boxed()
        .toList();

    final var channels =
      List.of(1, 2);

    return rates.stream()
      .flatMap(srcRate -> {
        return rates.stream().flatMap(dstRate -> {
          return channels.stream()
            .map(c -> {
              return this.runConversionDouble(srcRate, dstRate, c);
            });
        });
      });
  }

  /**
   * Test that a range of common conversion rates are supported.
   *
   * @return A stream of tests
   */

  @TestFactory
  public Stream<DynamicTest> testConversionsFloat()
  {
    final var rates =
      DoubleStream.of(
          8000.0,
          11025.0,
          16000.0,
          22050.0,
          44100.0,
          48000.0,
          88200.0,
          96000.0,
          176400.0,
          192000.0,
          352800.0,
          384000.0
        )
        .boxed()
        .toList();

    final var channels =
      List.of(1, 2);

    return rates.stream()
      .flatMap(srcRate -> {
        return rates.stream().flatMap(dstRate -> {
          return channels.stream()
            .map(c -> {
              return this.runConversionFloat(srcRate, dstRate, c);
            });
        });
      });
  }

  private DynamicTest runConversionDouble(
    final Double srcRate,
    final Double dstRate,
    final Integer channels)
  {
    return DynamicTest.dynamicTest(
      "testConvert_%d_%f_to_%f".formatted(channels, srcRate, dstRate),
      () -> {
        final var srcBuffer =
          SampleBufferDouble.createWithHeapBuffer(
            channels.intValue(),
            1000L,
            srcRate.doubleValue()
          );

        for (int index = 0; index < 1000; ++index) {
          srcBuffer.frameSetAll(index, StrictMath.sin(index));
        }

        final var dstBuffer =
          this.converters.createConverter()
            .convert(
              SampleBufferDouble::createWithHeapBuffer,
              srcBuffer,
              dstRate.doubleValue()
            );

        assertEquals(dstRate, dstBuffer.sampleRate());
        assertEquals(channels, dstBuffer.channels());
      });
  }

  private DynamicTest runConversionFloat(
    final Double srcRate,
    final Double dstRate,
    final Integer channels)
  {
    return DynamicTest.dynamicTest(
      "testConvert_%d_%f_to_%f".formatted(channels, srcRate, dstRate),
      () -> {
        final var srcBuffer =
          SampleBufferFloat.createWithHeapBuffer(
            channels.intValue(),
            1000L,
            srcRate.doubleValue()
          );

        for (int index = 0; index < 1000; ++index) {
          srcBuffer.frameSetAll(index, StrictMath.sin(index));
        }

        final var dstBuffer =
          this.converters.createConverter()
            .convert(
              SampleBufferFloat::createWithHeapBuffer,
              srcBuffer,
              dstRate.doubleValue()
            );

        assertEquals(dstRate, dstBuffer.sampleRate());
        assertEquals(channels, dstBuffer.channels());
      });
  }
}
