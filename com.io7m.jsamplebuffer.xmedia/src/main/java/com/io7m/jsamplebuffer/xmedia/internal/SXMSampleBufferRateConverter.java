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


package com.io7m.jsamplebuffer.xmedia.internal;

import com.io7m.jsamplebuffer.api.SampleBufferException;
import com.io7m.jsamplebuffer.api.SampleBufferFactoryType;
import com.io7m.jsamplebuffer.api.SampleBufferRateConverterType;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * An implementation of a sample rate converter that uses {@code javax.sound}
 * to perform the resampling.
 */

public final class SXMSampleBufferRateConverter
  implements SampleBufferRateConverterType
{
  /**
   * An implementation of a sample rate converter that uses {@code javax.sound}
   * to perform the resampling.
   */

  public SXMSampleBufferRateConverter()
  {

  }

  @Override
  public SampleBufferType convert(
    final SampleBufferFactoryType sampleBuffers,
    final SampleBufferType buffer,
    final double sampleRate)
    throws SampleBufferException
  {
    try (var srcStream = SXMSampleBuffers.createStreamFromSampleBuffer(buffer)) {
      final var srcFormat = srcStream.getFormat();
      final var dstFormat = new AudioFormat(
        srcFormat.getEncoding(),
        (float) sampleRate,
        srcFormat.getSampleSizeInBits(),
        srcFormat.getChannels(),
        srcFormat.getFrameSize(),
        (float) (sampleRate / (double) srcFormat.getChannels()),
        srcFormat.isBigEndian(),
        srcFormat.properties()
      );

      try (var dstStream = AudioSystem.getAudioInputStream(
        dstFormat,
        srcStream)) {
        return SXMSampleBuffers.readSampleBufferFromStream(
          dstStream,
          sampleBuffers);
      }
    } catch (IOException | UnsupportedAudioFileException e) {
      throw new SampleBufferException(e.getMessage(), e);
    }
  }
}
