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

package com.io7m.jsamplebuffer.tests.vanilla;

import com.io7m.jranges.RangeCheckException;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.tests.api.SampleBufferContract;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

public final class SampleBufferDoubleTest extends SampleBufferContract
{
  @Test
  public void testChannelCountWrong()
  {
    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class,
        () -> SampleBufferDouble.createWithByteBuffer(
          -1,
          100L,
          44100.0,
          count -> ByteBuffer.allocate((int) count)));

    Assertions.assertTrue(
      ex.getMessage().contains("Channels"),
      "Channel range check failed");
  }

  @Test
  public void testFrameCountWrong()
  {
    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class,
        () -> SampleBufferDouble.createWithByteBuffer(
          2,
          -1L,
          44100.0,
          count -> ByteBuffer.allocate((int) count)));

    Assertions.assertTrue(
      ex.getMessage().contains("Frames"),
      "Frame range check failed");
  }

  @Test
  public void testBufferMisallocation()
  {
    final var ex =
      Assertions.assertThrows(
        IllegalStateException.class,
        () -> SampleBufferDouble.createWithByteBuffer(
          2,
          100L,
          44100.0,
          count -> ByteBuffer.allocate(10)));

    Assertions.assertTrue(
      ex.getMessage().contains("10 octets"),
      "Misallocation detected");
  }

  @Test
  public void testChannelCountWrongHeap()
  {
    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class,
        () -> SampleBufferDouble.createWithHeapBuffer(
          -1,
          100L,
          44100.0));

    Assertions.assertTrue(
      ex.getMessage().contains("Channels"),
      "Channel range check failed");
  }

  @Test
  public void testFrameCountWrongHeap()
  {
    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class,
        () -> SampleBufferDouble.createWithHeapBuffer(
          2,
          -1L, 44100.0));

    Assertions.assertTrue(
      ex.getMessage().contains("Frames"),
      "Frame range check failed");
  }

  @Test
  public void testChannelCountWrongDirect()
  {
    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class,
        () -> SampleBufferDouble.createWithDirectBuffer(
          -1,
          100L, 44100.0));

    Assertions.assertTrue(
      ex.getMessage().contains("Channels"),
      "Channel range check failed");
  }

  @Test
  public void testFrameCountWrongDirect()
  {
    final var ex =
      Assertions.assertThrows(
        RangeCheckException.class,
        () -> SampleBufferDouble.createWithDirectBuffer(
          2,
          -1L, 44100.0));

    Assertions.assertTrue(
      ex.getMessage().contains("Frames"),
      "Frame range check failed");
  }

  @Override
  protected SampleBufferType createBuffer(
    final int channels,
    final long frames)
  {
    return SampleBufferDouble.createWithHeapBuffer(channels, frames, 44100.0);
  }

  @Test
  public void testCreateHeapMonoSimple()
  {
    final var buffer = SampleBufferDouble.createWithHeapBuffer(1, 100L, 44100.0);
    Assertions.assertEquals(1, buffer.channels());
    Assertions.assertEquals(100L, buffer.frames());
    Assertions.assertEquals(100L, buffer.samples());
  }

  @Test
  public void testCreateHeapStereoSimple()
  {
    final var buffer = SampleBufferDouble.createWithHeapBuffer(2, 100L, 44100.0);
    Assertions.assertEquals(2, buffer.channels());
    Assertions.assertEquals(100L, buffer.frames());
    Assertions.assertEquals(200L, buffer.samples());
  }

  @Test
  public void testCreateDirectMonoSimple()
  {
    final var buffer = SampleBufferDouble.createWithDirectBuffer(1, 100L, 44100.0);
    Assertions.assertEquals(1, buffer.channels());
    Assertions.assertEquals(100L, buffer.frames());
    Assertions.assertEquals(100L, buffer.samples());
  }

  @Test
  public void testCreateDirectStereoSimple()
  {
    final var buffer = SampleBufferDouble.createWithDirectBuffer(2, 100L, 44100.0);
    Assertions.assertEquals(2, buffer.channels());
    Assertions.assertEquals(100L, buffer.frames());
    Assertions.assertEquals(200L, buffer.samples());
  }
}
