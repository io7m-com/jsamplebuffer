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

package com.io7m.jsamplebuffer.tests.xmedia;

import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferFloat;
import com.io7m.jsamplebuffer.xmedia.SampleBufferXMedia;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SampleBufferXMediaTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger(SampleBufferXMediaTest.class);

  private static void checkNormalizedMono(
    final String test,
    final SampleBufferType buffer,
    final long count)
  {
    var big = -Double.MAX_VALUE;
    var small = Double.MAX_VALUE;
    var nearly_one = false;
    var nearly_m_one = false;
    var over_one = false;
    var under_m_one = false;

    for (var index = 0; index < count; ++index) {
      final var sample = buffer.frameGetExact(index);
      over_one = over_one || sample > 1.0;
      nearly_one = nearly_one || sample > 0.9;
      under_m_one = under_m_one || sample < -1.0;
      nearly_m_one = nearly_m_one || sample < -0.9;
      big = Math.max(big, sample);
      small = Math.min(small, sample);
    }

    if (over_one || under_m_one) {
      final var separator = System.lineSeparator();
      Assertions.fail(
        new StringBuilder(128)
          .append(test)
          .append(": ")
          .append("One or more sample values outside of accepted range")
          .append(separator)
          .append("  Expected Highest: ")
          .append(1.0)
          .append(separator)
          .append("  Received Highest: ")
          .append(big)
          .append(separator)
          .append("  Expected Lowest:  ")
          .append(-1.0)
          .append(separator)
          .append("  Received Lowest:  ")
          .append(small)
          .append(separator)
          .toString());
    }

    if (!nearly_m_one || !nearly_one) {
      final var separator = System.lineSeparator();
      Assertions.fail(
        new StringBuilder(128)
          .append(test)
          .append(": ")
          .append("Sample value range was too narrow")
          .append(separator)
          .append("  Expected Highest: ")
          .append(1.0)
          .append(separator)
          .append("  Received Highest: ")
          .append(big)
          .append(separator)
          .append("  Expected Lowest:  ")
          .append(-1.0)
          .append(separator)
          .append("  Received Lowest:  ")
          .append(small)
          .append(separator)
          .toString());
    }
  }

  private static void checkNormalizedStereo(
    final String test,
    final SampleBufferType buffer,
    final long count)
  {
    var big = -Double.MAX_VALUE;
    var small = Double.MAX_VALUE;
    var nearly_one = false;
    var nearly_m_one = false;
    var over_one = false;
    var under_m_one = false;

    final var sample = new double[2];
    for (var index = 0; index < count; ++index) {
      buffer.frameGetExact(index, sample);
      over_one = over_one || sample[0] > 1.0 || sample[1] > 1.0;
      nearly_one = nearly_one || sample[0] > 0.9 || sample[1] > 0.9;
      under_m_one = under_m_one || sample[0] < -1.0 || sample[1] < -1.0;
      nearly_m_one = nearly_m_one || sample[0] < -0.9 || sample[1] < -0.9;
      big = Math.max(Math.max(big, sample[0]), sample[1]);
      small = Math.min(Math.min(small, sample[0]), sample[1]);
    }

    if (over_one || under_m_one) {
      final var separator = System.lineSeparator();
      Assertions.fail(
        new StringBuilder(128)
          .append(test)
          .append(": ")
          .append("One or more sample values outside of accepted range")
          .append(separator)
          .append("  Expected Highest: ")
          .append(1.0)
          .append(separator)
          .append("  Received Highest: ")
          .append(big)
          .append(separator)
          .append("  Expected Lowest:  ")
          .append(-1.0)
          .append(separator)
          .append("  Received Lowest:  ")
          .append(small)
          .append(separator)
          .toString());
    }

    if (!nearly_m_one || !nearly_one) {
      final var separator = System.lineSeparator();
      Assertions.fail(
        new StringBuilder(128)
          .append(test)
          .append(": ")
          .append("Sample value range was too narrow")
          .append(separator)
          .append("  Expected Highest: ")
          .append(1.0)
          .append(separator)
          .append("  Received Highest: ")
          .append(big)
          .append(separator)
          .append("  Expected Lowest:  ")
          .append(-1.0)
          .append(separator)
          .append("  Received Lowest:  ")
          .append(small)
          .append(separator)
          .toString());
    }
  }

  private static File resource(
    final String name)
    throws IOException
  {
    final var path =
      Files.createTempFile("samplebuffer-xmedia-" + name, ".wav");
    final var resource =
      SampleBufferXMediaTest.class.getResource("/com/io7m/jsamplebuffer/tests/" + name);

    try (final var output = Files.newOutputStream(path)) {
      try (final var input = resource.openStream()) {
        input.transferTo(output);
      }
    }

    return path.toFile();
  }

  private static SampleBufferType createBuffer(
    final int channels,
    final long frames,
    final double sample_rate)
  {
    return SampleBufferFloat.createWithHeapBuffer(channels, frames, sample_rate);
  }

  @TestFactory
  public List<DynamicTest> testAllFormatsMono()
    throws Exception
  {
    final var files =
      Files.lines(resource("monos.txt").toPath())
        .filter(name -> !"sine_mono_8u_be.au".equals(name))
        .collect(Collectors.toList());

    Assertions.assertTrue(files.size() > 20);

    return files.stream()
      .map(file -> DynamicTest.dynamicTest(file, () -> runForMonoFile(file)))
      .collect(Collectors.toList());
  }

  @TestFactory
  public List<DynamicTest> testAllFormatsStereo()
    throws Exception
  {
    final var files =
      Files.lines(resource("stereos.txt").toPath())
        .filter(name -> !"sine_stereo_8u_be.au".equals(name))
        .filter(name -> !"sine_stereo_32fp_le.aiff".equals(name))
        .filter(name -> !"sine_stereo_32s_le.aiff".equals(name))
        .filter(name -> !"sine_stereo_32u_le.aiff".equals(name))
        .collect(Collectors.toList());

    Assertions.assertTrue(files.size() > 20);

    return files.stream()
      .map(file -> DynamicTest.dynamicTest(file, () -> runForStereoFile(file)))
      .collect(Collectors.toList());
  }

  private static void runForMonoFile(final String file)
    throws IOException
  {
    LOGGER.debug("{}: running", file);

    try (final var stream = AudioSystem.getAudioInputStream(resource(file))) {
      LOGGER.debug("{}: format: {}", file, stream.getFormat());
      final var buffer =
        SampleBufferXMedia.sampleBufferOfStream(stream, SampleBufferXMediaTest::createBuffer);
      final var count = 1200L;
      Assertions.assertEquals(count, buffer.frames());
      Assertions.assertEquals(1, buffer.channels());
      Assertions.assertEquals(48000.0, buffer.sampleRate());
      checkNormalizedMono(file, buffer, count);
    } catch (final UnsupportedAudioFileException e) {
      LOGGER.info("Ignoring unsupported audio", e);
    } finally {
      LOGGER.debug("{}: finished", file);
    }
  }

  private static void runForStereoFile(final String file)
    throws IOException
  {
    LOGGER.debug("{}: running", file);

    try (final var stream = AudioSystem.getAudioInputStream(resource(file))) {
      LOGGER.debug("{}: format: {}", file, stream.getFormat());
      final var buffer =
        SampleBufferXMedia.sampleBufferOfStream(stream, SampleBufferXMediaTest::createBuffer);
      final var count = 1200L;
      Assertions.assertEquals(count, buffer.frames());
      Assertions.assertEquals(2, buffer.channels());
      Assertions.assertEquals(48000.0, buffer.sampleRate());
      checkNormalizedStereo(file, buffer, count);
    } catch (final UnsupportedAudioFileException e) {
      LOGGER.info("Ignoring unsupported audio", e);
    } finally {
      LOGGER.debug("{}: finished", file);
    }
  }

}
