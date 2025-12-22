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

import com.io7m.jsamplebuffer.api.SampleBufferFactoryType;
import com.io7m.jsamplebuffer.api.SampleBufferReadableType;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.xmedia.internal.SXMSampleBuffers16;
import com.io7m.jsamplebuffer.xmedia.internal.SXMSampleBuffers24;
import com.io7m.jsamplebuffer.xmedia.internal.SXMSampleBuffers32;
import com.io7m.jsamplebuffer.xmedia.internal.SXMSampleBuffers64;
import com.io7m.jsamplebuffer.xmedia.internal.SXMSampleBuffers8;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
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
   * Write the given sample to the given file in WAVE format.
   *
   * @param buffer The sample buffer
   * @param file   The output file
   *
   * @throws IOException On errors
   */

  public static void writeSampleBufferToFile(
    final SampleBufferReadableType buffer,
    final Path file)
    throws IOException
  {
    writeSampleBufferToFile(buffer, AudioFileFormat.Type.WAVE, file);
  }

  /**
   * Write the given sample to the given file.
   *
   * @param buffer The sample buffer
   * @param type   The file format
   * @param file   The output file
   *
   * @throws IOException On errors
   */

  public static void writeSampleBufferToFile(
    final SampleBufferReadableType buffer,
    final AudioFileFormat.Type type,
    final Path file)
    throws IOException
  {
    try (var stream = Files.newOutputStream(file)) {
      AudioSystem.write(
        createStreamFromSampleBuffer(buffer),
        type,
        stream
      );
    }
  }

  /**
   * Read the given file into a sample buffer.
   *
   * @param file    The file
   * @param buffers A provider of buffers
   *
   * @return A sample buffer
   *
   * @throws IOException                   On I/O errors
   * @throws UnsupportedAudioFileException If the file refers to an audio format
   *                                       that cannot be processed
   */

  public static SampleBufferType readSampleBufferFromFile(
    final Path file,
    final SampleBufferFactoryType buffers)
    throws IOException, UnsupportedAudioFileException
  {
    try (var stream = Files.newInputStream(file)) {
      try (var buffered = new BufferedInputStream(stream)) {
        try (var audioStream = AudioSystem.getAudioInputStream(buffered)) {
          return readSampleBufferFromStream(audioStream, buffers);
        }
      }
    }
  }

  /**
   * Read the given stream into a sample buffer.
   *
   * @param stream  The stream
   * @param buffers A provider of buffers
   *
   * @return A sample buffer
   *
   * @throws IOException                   On I/O errors
   * @throws UnsupportedAudioFileException If the audio stream refers to an
   *                                       audio format that cannot be
   *                                       processed
   */

  public static SampleBufferType readSampleBufferFromStream(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException, UnsupportedAudioFileException
  {
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(buffers, "buffers");

    final var format = stream.getFormat();
    switch (format.getSampleSizeInBits()) {
      case 8:
        return SXMSampleBuffers8.sampleBufferOfStream8(stream, buffers);
      case 16:
        return SXMSampleBuffers16.sampleBufferOfStream16(stream, buffers);
      case 24:
        return SXMSampleBuffers24.sampleBufferOfStream24(stream, buffers);
      case 32:
        return SXMSampleBuffers32.sampleBufferOfStream32(stream, buffers);
      case 64:
        return SXMSampleBuffers64.sampleBufferOfStream64(stream, buffers);
      default: {
        throw new UnsupportedAudioFileException(
          "Only 8, 16, 24, 32, and 64-bit samples are supported");
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

  public static AudioInputStream createStreamFromSampleBuffer(
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

    final var frameSize =
      Math.multiplyExact((long) sample.channels(), 4L);
    final var dataSize =
      Math.toIntExact(Math.multiplyExact(sample.frames(), frameSize));

    final var data =
      new byte[dataSize];
    final var buffer =
      ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());

    final var frame = new double[sample.channels()];
    for (var frameIndex = 0L; frameIndex < sample.frames(); ++frameIndex) {
      sample.frameGetExact(frameIndex, frame);

      final var frameBase = Math.multiplyExact(frameIndex, frameSize);
      for (var channel = 0; channel < frame.length; ++channel) {
        final var offset =
          Math.addExact(frameBase, Math.multiplyExact((long) channel, 4L));
        buffer.putFloat(Math.toIntExact(offset), (float) frame[channel]);
      }
    }

    return new AudioInputStream(
      new ByteArrayInputStream(data),
      format,
      sample.frames()
    );
  }

  private static boolean bigEndian()
  {
    return Objects.equals(ByteOrder.nativeOrder(), ByteOrder.BIG_ENDIAN);
  }
}
