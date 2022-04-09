/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.jsamplebuffer.tests.api;

import com.io7m.jranges.RangeCheckException;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class SampleBufferContract
{
  protected abstract SampleBufferType createBuffer(
    int channels,
    long frames);

  @Test
  public final void testCreateEmpty()
  {
    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class,
        () -> this.createBuffer(1, 0L));

    Assertions.assertTrue(
      ex.getMessage().contains("Frames"),
      "Frame range check failed");
  }

  @Test
  public final void testCreateMonoSimple()
  {
    final var buffer = this.createBuffer(1, 100L);
    Assertions.assertEquals(1, buffer.channels());
    Assertions.assertEquals(100L, buffer.frames());
    Assertions.assertEquals(44100.0, buffer.sampleRate());
  }

  @Test
  public final void testCreateStereoSimple()
  {
    final var buffer = this.createBuffer(2, 100L);
    Assertions.assertEquals(2, buffer.channels());
    Assertions.assertEquals(100L, buffer.frames());
    Assertions.assertEquals(44100.0, buffer.sampleRate());
  }

  @Test
  public final void testFrameSetGetExactC1()
  {
    final var buffer = this.createBuffer(1, 100L);

    for (var index = 0; index < 100; ++index) {
      buffer.frameSetExact(index, index);
      final var value = buffer.frameGetExact(index);
      Assertions.assertEquals(index, value, 0.00001);
    }
  }

  @Test
  public final void testFrameSetAllGetExactC1_1()
  {
    final var buffer = this.createBuffer(1, 100L);

    for (var index = 0; index < 100; ++index) {
      buffer.frameSetAll(index, index);
      final var output = new double[1];
      buffer.frameGetExact(index, output);
      Assertions.assertEquals(index, output[0], 0.00001);
    }
  }

  @Test
  public final void testFrameSetAllGetExactC1_2()
  {
    final var buffer = this.createBuffer(1, 100L);

    for (var index = 0; index < 100; ++index) {
      buffer.frameSetAll(index, index);
      final var output = buffer.frameGetExact(index);
      Assertions.assertEquals(index, output, 0.00001);
    }
  }

  @Test
  public final void testFrameSetGetExactC1_Wrong0()
  {
    final var buffer = this.createBuffer(2, 100L);

    final var ex =
      Assertions.assertThrows(
        IllegalArgumentException.class, () -> buffer.frameSetExact(0L, 0.0));

    Assertions.assertTrue(
      ex.getMessage().contains("channel"),
      "Incorrect channel count detected");
  }

  @Test
  public final void testFrameSetExactC1_Range()
  {
    final var buffer = this.createBuffer(1, 100L);

    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class, () -> buffer.frameSetExact(100L, 0.0));

    Assertions.assertTrue(
      ex.getMessage().contains("range"),
      "Incorrect range detected");
  }

  @Test
  public final void testFrameSetAllC1_Range()
  {
    final var buffer = this.createBuffer(1, 100L);

    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class, () -> buffer.frameSetAll(100L, 0.0));

    Assertions.assertTrue(
      ex.getMessage().contains("range"),
      "Incorrect range detected");
  }

  @Test
  public final void testFrameGetExactC1_Range()
  {
    final var buffer = this.createBuffer(1, 100L);

    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class, () -> buffer.frameGetExact(100L));

    Assertions.assertTrue(
      ex.getMessage().contains("range"),
      "Incorrect range detected");
  }

  @Test
  public final void testFrameSetGetExactC2_0()
  {
    final var buffer = this.createBuffer(2, 100L);

    for (var index = 0; index < 100; ++index) {
      buffer.frameSetExact(index, index, (index * 2.0));
      final var output = new double[2];
      buffer.frameGetExact(index, output);
      Assertions.assertEquals(index, output[0], 0.00001);
      Assertions.assertEquals(index * 2.0, output[1], 0.00001);
    }
  }

  @Test
  public final void testFrameSetGetExactC2_1()
  {
    final var buffer = this.createBuffer(2, 100L);

    for (var index = 0; index < 100; ++index) {
      final var input = new double[2];
      input[0] = index;
      input[1] = index * 2.0;
      buffer.frameSetExact(index, input);
      final var output = new double[2];
      buffer.frameGetExact(index, output);
      Assertions.assertEquals(index, output[0], 0.00001);
      Assertions.assertEquals(index * 2.0, output[1], 0.00001);
    }
  }

  @Test
  public final void testFrameSetAllGetExactC2_1()
  {
    final var buffer = this.createBuffer(2, 100L);

    for (var index = 0; index < 100; ++index) {
      buffer.frameSetAll(index, index);
      final var output = new double[2];
      buffer.frameGetExact(index, output);
      Assertions.assertEquals(index, output[0], 0.00001);
      Assertions.assertEquals(index, output[1], 0.00001);
    }
  }

  @Test
  public final void testFrameSetExactC2_Wrong0()
  {
    final var buffer = this.createBuffer(1, 100L);

    final var ex =
      Assertions.assertThrows(
        IllegalArgumentException.class, () -> buffer.frameSetExact(0L, 0.0, 1.0));

    Assertions.assertTrue(
      ex.getMessage().contains("channel"),
      "Incorrect channel count detected");
  }

  @Test
  public final void testFrameSetExactC2_Range()
  {
    final var buffer = this.createBuffer(2, 100L);

    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class, () -> buffer.frameSetExact(100L, 0.0, 1.0));

    Assertions.assertTrue(
      ex.getMessage().contains("range"),
      "Incorrect range detected");
  }

  @Test
  public final void testFrameGetExactC2_Range()
  {
    final var buffer = this.createBuffer(2, 100L);

    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class, () -> buffer.frameGetExact(100L, new double[2]));

    Assertions.assertTrue(
      ex.getMessage().contains("range"),
      "Incorrect range detected");
  }
}
