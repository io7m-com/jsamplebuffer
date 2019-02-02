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

package com.io7m.jsamplebuffer.xmedia;

import com.io7m.jintegers.Signed24;
import com.io7m.jsamplebuffer.api.SampleBufferProviderType;
import com.io7m.jsamplebuffer.api.SampleBufferType;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Functions to create buffers from audio streams.
 */

public final class SampleBufferXMedia
{
  private SampleBufferXMedia()
  {

  }

  /**
   * Read all of the given stream into a sample buffer.
   *
   * @param stream  The stream
   * @param buffers A provider of buffers
   *
   * @return A sample buffer
   *
   * @throws IOException On I/O errors
   */

  public static SampleBufferType sampleBufferOfStream(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(buffers, "buffers");

    final var format = stream.getFormat();
    switch (format.getSampleSizeInBits()) {
      case 8:
        return sampleBufferOfStream8(stream, buffers);
      case 16:
        return sampleBufferOfStream16(stream, buffers);
      case 24:
        return sampleBufferOfStream24(stream, buffers);
      case 32:
        return sampleBufferOfStream32(stream, buffers);
      default: {
        throw new UnsupportedOperationException(
          "Only 8, 16, 24, and 32-bit samples are supported");
      }
    }
  }

  private static SampleBufferType sampleBufferOfStream32(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream32Signed(stream, buffers);
    }
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream32Unsigned(stream, buffers);
    }
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_FLOAT)) {
      return sampleBufferOfStream32Float(stream, buffers);
    }

    throw new UnsupportedEncodingException();
  }

  private static SampleBufferType sampleBufferOfStream32Unsigned(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 4;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = input_buffer.getInt(offset);
        input[channel_index] = unsignedIntToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream32Float(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 4;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = input_buffer.getFloat(offset);
        input[channel_index] = (double) read;
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream32Signed(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 4;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final int read = input_buffer.getInt(offset);
        input[channel_index] = signedIntToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream24Signed(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 3;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = Signed24.unpackFromBuffer(input_buffer, offset);
        input[channel_index] = signedInt24ToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream24Unsigned(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 3;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = Signed24.unpackFromBuffer(input_buffer, offset) & 0xffffff;
        input[channel_index] = unsignedInt24ToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream24(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream24Signed(stream, buffers);
    }
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream24Unsigned(stream, buffers);
    }
    throw new UnsupportedEncodingException();
  }

  private static SampleBufferType sampleBufferOfStream16(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream16Signed(stream, buffers);
    }
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream16Unsigned(stream, buffers);
    }
    throw new UnsupportedEncodingException();
  }

  private static SampleBufferType sampleBufferOfStream8(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream8Signed(stream, buffers);
    }
    if (Objects.equals(format.getEncoding(), AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream8Unsigned(stream, buffers);
    }
    throw new UnsupportedEncodingException();
  }

  private static SampleBufferType sampleBufferOfStream8Unsigned(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 1;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = input_buffer.get(offset);
        input[channel_index] = unsignedByteToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream8Signed(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 1;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = input_buffer.get(offset);
        input[channel_index] = signedByteToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream16Unsigned(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 2;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = input_buffer.getShort(offset);
        input[channel_index] = unsignedShortToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream16Signed(
    final AudioInputStream stream,
    final SampleBufferProviderType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var input_buffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(byteOrderOf(format));

    final var sample_size = 2;
    final var channels = format.getChannels();
    final var frame_size = channels * sample_size;
    final var frame_count = input_buffer.capacity() / frame_size;

    final var output_buffer =
      buffers.createBuffer(channels, (long) frame_count, (double) format.getSampleRate());
    final var input = new double[channels];

    for (var frame_index = 0; frame_index < frame_count; ++frame_index) {
      for (var channel_index = 0; channel_index < channels; ++channel_index) {
        final var offset = (frame_index * frame_size) + (channel_index * sample_size);
        final var read = input_buffer.getShort(offset);
        input[channel_index] = signedShortToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static double unsignedInt24ToSignedDouble(
    final int input)
  {
    final var di = (double) input;
    final var d = di / StrictMath.pow(2.0, 24.0);
    final var r = (d * 2.0) - 1.0;
    assertNormalized(r);
    return r;
  }

  private static double signedInt24ToSignedDouble(
    final int input)
  {
    final var di = (double) input;
    final var da = Math.abs(di);
    final var d = da / StrictMath.pow(2.0, 23.0);
    final var s = (d * 2.0) - 1.0;
    final var r = s * (double) Math.signum(input);
    assertNormalized(r);
    return r;
  }

  private static double unsignedIntToSignedDouble(
    final int input)
  {
    final var di = (double) input;
    final var d = di / StrictMath.pow(2.0, 32.0);
    final var r = (d * 2.0) - 1.0;
    assertNormalized(r);
    return r;
  }

  private static double signedIntToSignedDouble(
    final int input)
  {
    final var di = (double) input;
    final var da = Math.abs(di);
    final var d = da / StrictMath.pow(2.0, 31.0);
    final var s = (d * 2.0) - 1.0;
    final var r = s * (double) Math.signum(input);
    assertNormalized(r);
    return r;
  }

  private static double signedShortToSignedDouble(
    final short input)
  {
    final var di = (double) input;
    final var da = Math.abs(di);
    final var d = da / StrictMath.pow(2.0, 15.0);
    final var s = (d * 2.0) - 1.0;
    final var r = s * (double) Math.signum(input);
    assertNormalized(r);
    return r;
  }

  private static double unsignedShortToSignedDouble(
    final short input)
  {
    final var i = Short.toUnsignedInt(input);
    final var d = ((double) i) / StrictMath.pow(2.0, 16.0);
    final var r = (d * 2.0) - 1.0;
    assertNormalized(r);
    return r;
  }

  private static double unsignedByteToSignedDouble(
    final byte input)
  {
    final var i = Byte.toUnsignedInt(input);
    final var d = ((double) i) / StrictMath.pow(2.0, 8.0);
    final var r = (d * 2.0) - 1.0;
    assertNormalized(r);
    return r;
  }

  private static void assertNormalized(final double r)
  {
    assert r >= -1.0 : r + " >= -1.0";
    assert r <= 1.0 : r + " <= 1.0";
  }

  private static ByteOrder byteOrderOf(final AudioFormat format)
  {
    return format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  }

  private static double signedByteToSignedDouble(
    final byte input)
  {
    final var di = (double) input;
    final var da = Math.abs(di);
    final var d = da / StrictMath.pow(2.0, 7.0);
    final var s = (d * 2.0) - 1.0;
    final var r = s * (double) Math.signum(input);
    assertNormalized(r);
    return r;
  }
}
