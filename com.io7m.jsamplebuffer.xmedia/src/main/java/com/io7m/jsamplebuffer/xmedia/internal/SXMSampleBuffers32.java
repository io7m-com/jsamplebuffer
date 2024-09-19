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

import com.io7m.jintegers.Unsigned32;
import com.io7m.jsamplebuffer.api.SampleBufferFactoryType;
import com.io7m.jsamplebuffer.api.SampleBufferType;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Functions to construct sample buffers.
 */

public final class SXMSampleBuffers32
{
  private SXMSampleBuffers32()
  {

  }

  /**
   * Create a sample buffer from a stream.
   *
   * @param stream  The stream
   * @param buffers The buffer factory
   *
   * @return A sample buffer
   *
   * @throws IOException                   On I/O errors
   * @throws UnsupportedAudioFileException On unsupported audio
   */

  public static SampleBufferType sampleBufferOfStream32(
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

    throw new UnsupportedAudioFileException(
      "Unsupported encoding: %s".formatted(encoding)
    );
  }

  private static SampleBufferType sampleBufferOfStream32Unsigned(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var inputBuffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(SXMSampleBuffersInfo.byteOrderOf(format));

    final var sampleSize = 4;
    final var channels = format.getChannels();
    final var frameSize = channels * sampleSize;
    final var frameCount = inputBuffer.capacity() / frameSize;

    final var outputBuffer =
      buffers.createBuffer(
        channels,
        frameCount,
        format.getSampleRate());
    final var input = new double[channels];

    for (var frameIndex = 0; frameIndex < frameCount; ++frameIndex) {
      for (var channelIndex = 0; channelIndex < channels; ++channelIndex) {
        final var offset = (frameIndex * frameSize) + (channelIndex * sampleSize);
        final var read = Unsigned32.unpackFromBuffer(inputBuffer, offset);
        input[channelIndex] = unsignedIntToSignedDouble(read);
      }
      outputBuffer.frameSetExact(frameIndex, input);
    }
    return outputBuffer;
  }

  private static SampleBufferType sampleBufferOfStream32Float(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var inputBuffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(SXMSampleBuffersInfo.byteOrderOf(format));

    final var sampleSize = 4;
    final var channels = format.getChannels();
    final var frameSize = channels * sampleSize;
    final var frameCount = inputBuffer.capacity() / frameSize;

    final var outputBuffer =
      buffers.createBuffer(
        channels,
        frameCount,
        format.getSampleRate());

    final var input = new double[channels];
    for (var frameIndex = 0; frameIndex < frameCount; ++frameIndex) {
      for (var channelIndex = 0; channelIndex < channels; ++channelIndex) {
        final var offset = (frameIndex * frameSize) + (channelIndex * sampleSize);
        final var read = inputBuffer.getFloat(offset);
        input[channelIndex] = read;
      }
      outputBuffer.frameSetExact(frameIndex, input);
    }
    return outputBuffer;
  }

  private static SampleBufferType sampleBufferOfStream32Signed(
    final AudioInputStream stream,
    final SampleBufferFactoryType buffers)
    throws IOException
  {
    final var format = stream.getFormat();
    final var inputBuffer =
      ByteBuffer.wrap(stream.readAllBytes())
        .order(SXMSampleBuffersInfo.byteOrderOf(format));

    final var sampleSize = 4;
    final var channels = format.getChannels();
    final var frameSize = channels * sampleSize;
    final var frameCount = inputBuffer.capacity() / frameSize;

    final var outputBuffer =
      buffers.createBuffer(
        channels,
        frameCount,
        format.getSampleRate());
    final var input = new double[channels];

    for (var frameIndex = 0; frameIndex < frameCount; ++frameIndex) {
      for (var channelIndex = 0; channelIndex < channels; ++channelIndex) {
        final var offset = (frameIndex * frameSize) + (channelIndex * sampleSize);
        final var read = inputBuffer.getInt(offset);
        input[channelIndex] = signedIntToSignedDouble(read);
      }
      outputBuffer.frameSetExact(frameIndex, input);
    }
    return outputBuffer;
  }

  private static double unsignedIntToSignedDouble(
    final long input)
  {
    final var inputReal = (double) input;
    final var inputMax = StrictMath.pow(2.0, 32.0);
    final var inputMin = 0.0;
    return SXMSampleBuffersInfo.mapRangeToNormal(inputReal, inputMin, inputMax);
  }

  private static double signedIntToSignedDouble(
    final int input)
  {
    final var inputReal = (double) input;
    final var inputMax = StrictMath.pow(2.0, 31.0);
    final var inputMin = -inputMax;
    return SXMSampleBuffersInfo.mapRangeToNormal(inputReal, inputMin, inputMax);
  }
}