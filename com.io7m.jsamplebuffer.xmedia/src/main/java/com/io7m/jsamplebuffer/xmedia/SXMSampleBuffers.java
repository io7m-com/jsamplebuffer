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

package com.io7m.jsamplebuffer.xmedia;

import com.io7m.jintegers.Signed24;
import com.io7m.jintegers.Unsigned32;
import com.io7m.jsamplebuffer.api.SampleBufferFactoryType;
import com.io7m.jsamplebuffer.api.SampleBufferReadableType;
import com.io7m.jsamplebuffer.api.SampleBufferType;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Functions to create buffers from audio streams.
 */

public final class SXMSampleBuffers
{
  private SXMSampleBuffers()
  {

  }

  /**
   * Read all the given stream into a sample buffer.
   *
   * @param stream  The stream
   * @param buffers A provider of buffers
   *
   * @return A sample buffer
   *
   * @throws IOException On I/O errors
   * @throws UnsupportedAudioFileException If the audio stream refers to an audio format that cannot be processed
   */

  public static SampleBufferType sampleBufferOfStream(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException, UnsupportedAudioFileException
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
        throw new UnsupportedAudioFileException(
          "Only 8, 16, 24, and 32-bit samples are supported");
      }
    }
  }

  /**
   * Produce a stream from the given sample buffer.
   *
   * @param sample The sample buffer
   *
   * @return A stream
   */

  public static AudioInputStream streamOfSampleBuffer(
    final SampleBufferReadableType sample)
  {
    final var format =
      new AudioFormat(
        AudioFormat.Encoding.PCM_FLOAT,
        (float) sample.sampleRate(),
        32,
        sample.channels(),
        sample.channels() * 4,
        (float) sample.sampleRate(),
        bigEndian()
      );

    final var frame_size = Math.multiplyExact((long) sample.channels(), 4L);
    final var data_size = Math.toIntExact(Math.multiplyExact(sample.frames(), frame_size));
    final var data = new byte[data_size];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());

    final var frame = new double[sample.channels()];
    for (var frame_index = 0L; frame_index < sample.frames(); ++frame_index) {
      sample.frameGetExact(frame_index, frame);

      final var frame_base = Math.multiplyExact(frame_index, frame_size);
      for (var channel = 0; channel < frame.length; ++channel) {
        final var offset = Math.addExact(frame_base, Math.multiplyExact((long) channel, 4L));
        buffer.putFloat(Math.toIntExact(offset), (float) frame[channel]);
      }
    }

    return new AudioInputStream(new ByteArrayInputStream(data), format, sample.frames());
  }

  private static boolean bigEndian()
  {
    return Objects.equals(ByteOrder.nativeOrder(), ByteOrder.BIG_ENDIAN);
  }

  private static SampleBufferType sampleBufferOfStream32(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException, UnsupportedAudioFileException
  {
    final var format = stream.getFormat();
    final var encoding = format.getEncoding();
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream32Signed(stream, buffers);
    }
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream32Unsigned(stream, buffers);
    }
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_FLOAT)) {
      return sampleBufferOfStream32Float(stream, buffers);
    }

    throw new UnsupportedAudioFileException("Unsupported encoding: " + encoding);
  }

  private static SampleBufferType sampleBufferOfStream32Unsigned(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
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
        final var read = Unsigned32.unpackFromBuffer(input_buffer, offset);
        input[channel_index] = unsignedIntToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream32Float(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
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
    final SampleBufferFactoryType buffers)
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
        input[channel_index] = signedIntToSignedDouble(read);
      }
      output_buffer.frameSetExact((long) frame_index, input);
    }
    return output_buffer;
  }

  private static SampleBufferType sampleBufferOfStream24Signed(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
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
    final SampleBufferFactoryType buffers)
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
    final SampleBufferFactoryType buffers)
    throws IOException, UnsupportedAudioFileException
  {
    final var format = stream.getFormat();
    final var encoding = format.getEncoding();
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream24Signed(stream, buffers);
    }
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream24Unsigned(stream, buffers);
    }
    throw new UnsupportedAudioFileException("Unsupported encoding: " + encoding);
  }

  private static SampleBufferType sampleBufferOfStream16(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException, UnsupportedAudioFileException
  {
    final var format = stream.getFormat();
    final var encoding = format.getEncoding();
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream16Signed(stream, buffers);
    }
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream16Unsigned(stream, buffers);
    }
    throw new UnsupportedAudioFileException("Unsupported encoding: " + encoding);
  }

  private static SampleBufferType sampleBufferOfStream8(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException, UnsupportedAudioFileException
  {
    final var format = stream.getFormat();
    final var encoding = format.getEncoding();
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_SIGNED)) {
      return sampleBufferOfStream8Signed(stream, buffers);
    }
    if (Objects.equals(encoding, AudioFormat.Encoding.PCM_UNSIGNED)) {
      return sampleBufferOfStream8Unsigned(stream, buffers);
    }
    throw new UnsupportedAudioFileException("Unsupported encoding: " + encoding);
  }

  private static SampleBufferType sampleBufferOfStream8Unsigned(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
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
    final SampleBufferFactoryType buffers)
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
    final SampleBufferFactoryType buffers)
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
    final SampleBufferFactoryType buffers)
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

  private static double unsignedByteToSignedDouble(
    final byte input)
  {
    final var input_real = (double) (Byte.toUnsignedInt(input));
    final var input_max = StrictMath.pow(2.0, 8.0);
    final var input_min = 0.0;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static double unsignedShortToSignedDouble(
    final short input)
  {
    final var input_real = (double) (Short.toUnsignedInt(input));
    final var input_max = StrictMath.pow(2.0, 16.0);
    final var input_min = 0.0;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static double unsignedInt24ToSignedDouble(
    final int input)
  {
    final var input_real = (double) input;
    final var input_max = StrictMath.pow(2.0, 24.0);
    final var input_min = 0.0;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static double unsignedIntToSignedDouble(
    final long input)
  {
    final var input_real = (double) input;
    final var input_max = StrictMath.pow(2.0, 32.0);
    final var input_min = 0.0;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static double signedByteToSignedDouble(
    final byte input)
  {
    final var input_real = (double) input;
    final var input_max = StrictMath.pow(2.0, 7.0);
    final var input_min = -input_max;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static double signedIntToSignedDouble(
    final int input)
  {
    final var input_real = (double) input;
    final var input_max = StrictMath.pow(2.0, 31.0);
    final var input_min = -input_max;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static double signedInt24ToSignedDouble(
    final int input)
  {
    final var input_real = (double) input;
    final var input_max = StrictMath.pow(2.0, 23.0);
    final var input_min = -input_max;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static double signedShortToSignedDouble(
    final short input)
  {
    final var input_real = (double) input;
    final var input_max = StrictMath.pow(2.0, 15.0);
    final var input_min = -input_max;
    return mapRangeToNormal(input_real, input_min, input_max);
  }

  private static void assertNormalized(final double r)
  {
    assert r >= -1.0 : r + " >= -1.0";
    assert r <= 1.0 : r + " <= 1.0";
  }

  private static double mapRangeToNormal(
    final double input_real,
    final double input_min,
    final double input_max)
  {
    assert input_real >= input_min;
    assert input_real <= input_max;

    final var input_range = input_max - input_min;

    final var output_max = 1.0;
    final var output_min = -1.0;
    final var output_range = output_max - output_min;

    final var output_value = (((input_real - input_min) * output_range) / input_range) + output_min;
    assertNormalized(output_value);
    return output_value;
  }

  private static ByteOrder byteOrderOf(final AudioFormat format)
  {
    return format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  }
}
