/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.jsamplebuffer.vanilla;

import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeCheckException;
import com.io7m.jranges.RangeInclusiveI;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jsamplebuffer.api.SampleBufferType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.LongFunction;

/**
 * A sample buffer using {@code float} elements.
 */

public final class SampleBufferFloat implements SampleBufferType
{
  private static final RangeInclusiveI VALID_CHANNELS =
    RangeInclusiveI.of(1, Integer.MAX_VALUE);
  private static final RangeInclusiveL VALID_FRAMES =
    RangeInclusiveL.of(1L, Long.MAX_VALUE);

  private static final long SAMPLE_SIZE = 4L;

  private final int channels;
  private final long frames;
  private final ByteBuffer buffer;
  private final long frame_size;
  private final RangeInclusiveL frame_range;

  private SampleBufferFloat(
    final int in_channels,
    final long in_frames,
    final ByteBuffer in_buffer)
  {
    this.channels =
      RangeCheck.checkIncludedInInteger(
        in_channels,
        "Channels",
        VALID_CHANNELS,
        "Valid channel count");
    this.frames =
      RangeCheck.checkIncludedInLong(
        in_frames,
        "Frames",
        VALID_FRAMES,
        "Valid frame count");

    this.frame_range =
      RangeInclusiveL.of(0L, in_frames - 1L);

    this.buffer = Objects.requireNonNull(in_buffer, "buffer");
    this.frame_size = Math.multiplyExact((long) this.channels, SAMPLE_SIZE);
  }

  /**
   * Create a sample buffer.
   *
   * @param channels The number of channels per frame
   * @param frames   The number of frames in the buffer
   * @param create   A function that allocates a byte buffer for the samples
   *
   * @return A new sample buffer
   */

  public static SampleBufferType createWithByteBuffer(
    final int channels,
    final long frames,
    final LongFunction<ByteBuffer> create)
  {
    Objects.requireNonNull(create, "create");

    RangeCheck.checkIncludedInInteger(
      channels,
      "Channels",
      VALID_CHANNELS,
      "Valid channel count");
    RangeCheck.checkIncludedInLong(
      frames,
      "Frames",
      VALID_FRAMES,
      "Valid frame count");

    final var per_frame = Math.multiplyExact(SAMPLE_SIZE, channels);
    final var bytes = Math.multiplyExact(per_frame, frames);
    final var buffer = create.apply(bytes);
    if ((long) buffer.capacity() != bytes) {
      final var separator = System.lineSeparator();
      throw new IllegalStateException(
        new StringBuilder(128)
          .append("Buffer size incorrect.")
          .append(separator)
          .append("  Expected: ")
          .append(Long.toUnsignedString(bytes))
          .append(" octets")
          .append(separator)
          .append("  Received: ")
          .append(buffer.capacity())
          .append(" octets")
          .toString());
    }

    return new SampleBufferFloat(channels, frames, buffer);
  }

  /**
   * Create a sample buffer. The underlying buffer will be allocated using direct memory.
   *
   * @param channels The number of channels per frame
   * @param frames   The number of frames in the buffer
   *
   * @return A new sample buffer
   */

  public static SampleBufferType createWithDirectBuffer(
    final int channels,
    final long frames)
  {
    return createWithByteBuffer(
      channels,
      frames, bytes ->
        ByteBuffer.allocateDirect(Math.toIntExact(bytes))
          .order(ByteOrder.nativeOrder()));
  }

  /**
   * Create a sample buffer. The underlying buffer will be heap-allocated.
   *
   * @param channels The number of channels per frame
   * @param frames   The number of frames in the buffer
   *
   * @return A new sample buffer
   */

  public static SampleBufferType createWithHeapBuffer(
    final int channels,
    final long frames)
  {
    return createWithByteBuffer(
      channels,
      frames, bytes ->
        ByteBuffer.allocate(Math.toIntExact(bytes))
          .order(ByteOrder.nativeOrder()));
  }

  @Override
  public void frameSetAll(
    final long index,
    final double value)
    throws IllegalArgumentException
  {
    RangeCheck.checkIncludedInLong(
      index, "Frame index", this.frame_range, "Frame range");

    final var base = Math.multiplyExact(this.frame_size, index);
    for (var channel_index = 0; channel_index < this.channels; ++channel_index) {
      final var offset = base + (Math.multiplyExact(SAMPLE_SIZE, channel_index));
      this.buffer.putFloat(Math.toIntExact(offset), (float) value);
    }
  }

  @Override
  public void frameSetExact(
    final long index,
    final double c0,
    final double c1)
    throws IllegalArgumentException
  {
    this.checkChannelCount(2);

    RangeCheck.checkIncludedInLong(
      index, "Frame index", this.frame_range, "Frame range");

    final var base = Math.multiplyExact(this.frame_size, index);
    this.buffer.putFloat(Math.toIntExact(base), (float) c0);
    this.buffer.putFloat(Math.toIntExact(base + SAMPLE_SIZE), (float) c1);
  }

  @Override
  public void frameSetExact(
    final long index,
    final double c0)
    throws IllegalArgumentException
  {
    this.checkChannelCount(1);

    RangeCheck.checkIncludedInLong(
      index, "Frame index", this.frame_range, "Frame range");

    final var base = Math.multiplyExact(this.frame_size, index);
    this.buffer.putFloat(Math.toIntExact(base), (float) c0);
  }

  @Override
  public void frameSetExact(
    final long index,
    final double[] value)
    throws IllegalArgumentException
  {
    Objects.requireNonNull(value, "value");

    this.checkChannelCount(value.length);

    RangeCheck.checkIncludedInLong(
      index, "Frame index", this.frame_range, "Frame range");

    final var base = Math.multiplyExact(this.frame_size, index);
    for (var channel_index = 0; channel_index < this.channels; ++channel_index) {
      final var offset = base + (Math.multiplyExact(SAMPLE_SIZE, channel_index));
      this.buffer.putFloat(Math.toIntExact(offset), (float) value[channel_index]);
    }
  }

  private void checkChannelCount(
    final int length)
  {
    if (this.channels != length) {
      final var separator = System.lineSeparator();
      throw new IllegalArgumentException(
        new StringBuilder("Incorrect channel count.")
          .append(separator)
          .append("  Expected: ")
          .append(this.channels)
          .append(separator)
          .append("  Received: ")
          .append(length)
          .append(separator)
          .toString());
    }
  }

  @Override
  public int channels()
  {
    return this.channels;
  }

  @Override
  public long frames()
  {
    return this.frames;
  }

  @Override
  public void frameGetExact(
    final long index,
    final double[] output)
    throws IllegalArgumentException
  {
    Objects.requireNonNull(output, "output");

    this.checkChannelCount(output.length);

    RangeCheck.checkIncludedInLong(
      index, "Frame index", this.frame_range, "Frame range");

    final var base = Math.multiplyExact(this.frame_size, index);
    for (var channel_index = 0; channel_index < this.channels; ++channel_index) {
      final var offset = base + (Math.multiplyExact(SAMPLE_SIZE, channel_index));
      output[channel_index] = (double) this.buffer.getFloat(Math.toIntExact(offset));
    }
  }

  @Override
  public double frameGetExact(final long index)
    throws RangeCheckException
  {
    this.checkChannelCount(1);

    RangeCheck.checkIncludedInLong(
      index, "Frame index", this.frame_range, "Frame range");

    final var base = Math.multiplyExact(this.frame_size, index);
    return (double) this.buffer.getFloat(Math.toIntExact(base));
  }
}
