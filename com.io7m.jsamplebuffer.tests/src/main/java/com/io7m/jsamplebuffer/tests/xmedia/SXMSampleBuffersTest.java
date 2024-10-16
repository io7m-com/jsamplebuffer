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

package com.io7m.jsamplebuffer.tests.xmedia;

import com.io7m.jintegers.Signed24;
import com.io7m.jintegers.Signed64;
import com.io7m.jintegers.Unsigned16;
import com.io7m.jintegers.Unsigned32;
import com.io7m.jintegers.Unsigned8;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.tests.SBTestDirectories;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.vanilla.SampleBufferFloat;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static javax.sound.sampled.AudioFormat.Encoding.ALAW;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_FLOAT;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.ULAW;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SXMSampleBuffersTest
{
  private static final Logger LOGGER =
    LoggerFactory.getLogger(SXMSampleBuffersTest.class);

  private Path directory;

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
      SXMSampleBuffersTest.class.getResource("/com/io7m/jsamplebuffer/tests/" + name);

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
    return SampleBufferFloat.createWithHeapBuffer(
      channels,
      frames,
      sample_rate);
  }

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = SBTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    SBTestDirectories.deleteDirectory(this.directory);
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

  @TestFactory
  public List<DynamicTest> testRoundTripMono()
    throws Exception
  {
    final var files =
      Files.lines(resource("monos.txt").toPath())
        .filter(name -> !"sine_mono_8u_be.au".equals(name))
        .collect(Collectors.toList());

    Assertions.assertTrue(files.size() > 20);

    return files.stream()
      .map(file -> {
        return DynamicTest.dynamicTest(file, () -> {
          this.roundTripFile(file);
        });
      })
      .collect(Collectors.toList());
  }

  @TestFactory
  public List<DynamicTest> testRoundTripMonoFile()
    throws Exception
  {
    final var files =
      Files.lines(resource("monos.txt").toPath())
        .filter(name -> !"sine_mono_8u_be.au".equals(name))
        .collect(Collectors.toList());

    Assertions.assertTrue(files.size() > 20);

    return files.stream()
      .map(file -> {
        return DynamicTest.dynamicTest(file, () -> {
          this.roundTripFileConvenience(file);
        });
      })
      .collect(Collectors.toList());
  }

  @TestFactory
  public List<DynamicTest> testRoundTripStereo()
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
      .map(file -> {
        return DynamicTest.dynamicTest(file, () -> {
          this.roundTripFile(file);
        });
      })
      .collect(Collectors.toList());
  }

  @TestFactory
  public List<DynamicTest> testRoundTripStereoFile()
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
      .map(file -> {
        return DynamicTest.dynamicTest(file, () -> {
          this.roundTripFileConvenience(file);
        });
      })
      .collect(Collectors.toList());
  }

  @TestFactory
  public List<DynamicTest> testOpenFiles()
    throws Exception
  {
    final var files =
      Stream.of(
        "sine_mono.wav",
        "sine_stereo.wav"
      );

    return files
      .map(file -> DynamicTest.dynamicTest(file, () -> this.openFile(file)))
      .collect(Collectors.toList());
  }

  private void openFile(
    final String fileName)
    throws IOException, UnsupportedAudioFileException
  {
    final var file =
      resource(fileName);
    final var buffer =
      SXMSampleBuffers.readSampleBufferFromFile(
        file.toPath(),
        SampleBufferDouble::createWithHeapBuffer
      );
  }

  @Test
  public void testUnsupported0()
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits())).thenReturn(
      Integer.valueOf(1));

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);

    Assertions.assertThrows(UnsupportedAudioFileException.class, () -> {
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);
    });
  }

  @TestFactory
  public Stream<DynamicTest> testUnsupportedALAW()
  {
    return IntStream.of(8, 16, 24, 32)
      .mapToObj(size -> {
        return DynamicTest.dynamicTest("testUnsupportedALAW" + size, () -> {
          final var format = Mockito.mock(AudioFormat.class);
          Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
            .thenReturn(Integer.valueOf(size));
          Mockito.when(format.getEncoding())
            .thenReturn(ALAW);

          final var stream = Mockito.mock(AudioInputStream.class);
          Mockito.when(stream.getFormat()).thenReturn(format);

          Assertions.assertThrows(
            UnsupportedAudioFileException.class,
            () -> SXMSampleBuffers.readSampleBufferFromStream(
              stream, SXMSampleBuffersTest::createBuffer));
        });
      });
  }

  @TestFactory
  public Stream<DynamicTest> testUnsupportedULAW()
  {
    return IntStream.of(8, 16, 24, 32)
      .mapToObj(size -> {
        return DynamicTest.dynamicTest("testUnsupportedULAW" + size, () -> {
          final var format = Mockito.mock(AudioFormat.class);
          Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
            .thenReturn(Integer.valueOf(size));
          Mockito.when(format.getEncoding())
            .thenReturn(ULAW);

          final var stream = Mockito.mock(AudioInputStream.class);
          Mockito.when(stream.getFormat()).thenReturn(format);

          Assertions.assertThrows(
            UnsupportedAudioFileException.class,
            () -> SXMSampleBuffers.readSampleBufferFromStream(
              stream, SXMSampleBuffersTest::createBuffer));
        });
      });
  }

  @Test
  public void testStreamUnsigned8()
    throws Exception
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
      .thenReturn(Integer.valueOf(8));
    Mockito.when(format.getEncoding())
      .thenReturn(PCM_UNSIGNED);
    Mockito.when(Boolean.valueOf(format.isBigEndian()))
      .thenReturn(Boolean.TRUE);
    Mockito.when(Integer.valueOf(format.getChannels()))
      .thenReturn(Integer.valueOf(1));

    final var data = new byte[3];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

    Unsigned8.packToBuffer(0, buffer, 0);
    Unsigned8.packToBuffer(0x7f, buffer, 1);
    Unsigned8.packToBuffer(0xff, buffer, 2);

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);
    Mockito.when(stream.readAllBytes()).thenReturn(data);

    final var sample =
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);

    checkNormalizedMono("testStreamUnsigned8", sample, 3L);

    assertEquals(-1.0, sample.frameGetExact(0L), 0.01);
    assertEquals(0.0, sample.frameGetExact(1L), 0.01);
    assertEquals(1.0, sample.frameGetExact(2L), 0.01);
  }

  @Test
  public void testStreamUnsigned16()
    throws Exception
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
      .thenReturn(Integer.valueOf(16));
    Mockito.when(format.getEncoding())
      .thenReturn(PCM_UNSIGNED);
    Mockito.when(Boolean.valueOf(format.isBigEndian()))
      .thenReturn(Boolean.TRUE);
    Mockito.when(Integer.valueOf(format.getChannels()))
      .thenReturn(Integer.valueOf(1));

    final var data = new byte[3 * 2];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

    Unsigned16.packToBuffer(0, buffer, 0);
    Unsigned16.packToBuffer(0x7fff, buffer, 2);
    Unsigned16.packToBuffer(0xffff, buffer, 4);

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);
    Mockito.when(stream.readAllBytes()).thenReturn(data);

    final var sample =
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);

    checkNormalizedMono("testStreamUnsigned16", sample, 3L);

    assertEquals(-1.0, sample.frameGetExact(0L), 0.0001);
    assertEquals(0.0, sample.frameGetExact(1L), 0.0001);
    assertEquals(1.0, sample.frameGetExact(2L), 0.0001);
  }

  @Test
  public void testStreamUnsigned24()
    throws Exception
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
      .thenReturn(Integer.valueOf(24));
    Mockito.when(format.getEncoding())
      .thenReturn(PCM_UNSIGNED);
    Mockito.when(Boolean.valueOf(format.isBigEndian()))
      .thenReturn(Boolean.TRUE);
    Mockito.when(Integer.valueOf(format.getChannels()))
      .thenReturn(Integer.valueOf(1));

    final var data = new byte[3 * 3];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

    Signed24.packToBuffer(0, buffer, 0);
    Signed24.packToBuffer(0x7fffff, buffer, 3);
    Signed24.packToBuffer(0xffffff, buffer, 6);

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);
    Mockito.when(stream.readAllBytes()).thenReturn(data);

    final var sample =
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);

    checkNormalizedMono("testStreamUnsigned24", sample, 3L);

    assertEquals(-1.0, sample.frameGetExact(0L), 0.000001);
    assertEquals(0.0, sample.frameGetExact(1L), 0.000001);
    assertEquals(1.0, sample.frameGetExact(2L), 0.000001);
  }

  @Test
  public void testStreamUnsigned32()
    throws Exception
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
      .thenReturn(Integer.valueOf(32));
    Mockito.when(format.getEncoding())
      .thenReturn(PCM_UNSIGNED);
    Mockito.when(Boolean.valueOf(format.isBigEndian()))
      .thenReturn(Boolean.TRUE);
    Mockito.when(Integer.valueOf(format.getChannels()))
      .thenReturn(Integer.valueOf(1));

    final var data = new byte[3 * 4];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

    Unsigned32.packToBuffer(0L, buffer, 0);
    Unsigned32.packToBuffer(0x7fffffffL, buffer, 4);
    Unsigned32.packToBuffer(0xffffffffL, buffer, 8);

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);
    Mockito.when(stream.readAllBytes()).thenReturn(data);

    final var sample =
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);

    checkNormalizedMono("testStreamUnsigned32", sample, 3L);

    assertEquals(-1.0, sample.frameGetExact(0L), 0.000001);
    assertEquals(0.0, sample.frameGetExact(1L), 0.000001);
    assertEquals(1.0, sample.frameGetExact(2L), 0.000001);
  }

  @Test
  public void testStreamUnsigned64()
    throws Exception
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
      .thenReturn(Integer.valueOf(64));
    Mockito.when(format.getEncoding())
      .thenReturn(PCM_UNSIGNED);
    Mockito.when(Boolean.valueOf(format.isBigEndian()))
      .thenReturn(Boolean.TRUE);
    Mockito.when(Integer.valueOf(format.getChannels()))
      .thenReturn(Integer.valueOf(1));

    final var data = new byte[3 * 8];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

    Signed64.packToBuffer(0L, buffer, 0);
    Signed64.packToBuffer(0x7fffffff_ffffffffL, buffer, 8);
    Signed64.packToBuffer(0xffffffff_ffffffffL, buffer, 16);

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);
    Mockito.when(stream.readAllBytes()).thenReturn(data);

    final var sample =
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);

    checkNormalizedMono("testStreamUnsigned64", sample, 3L);

    assertEquals(-1.0, sample.frameGetExact(0L), 0.000001);
    assertEquals(0.0, sample.frameGetExact(1L), 0.000001);
    assertEquals(1.0, sample.frameGetExact(2L), 0.000001);
  }

  @Test
  public void testStreamSigned64()
    throws Exception
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
      .thenReturn(Integer.valueOf(64));
    Mockito.when(format.getEncoding())
      .thenReturn(PCM_SIGNED);
    Mockito.when(Boolean.valueOf(format.isBigEndian()))
      .thenReturn(Boolean.TRUE);
    Mockito.when(Integer.valueOf(format.getChannels()))
      .thenReturn(Integer.valueOf(1));

    final var data = new byte[3 * 8];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

    Signed64.packToBuffer(Long.MIN_VALUE, buffer, 0);
    Signed64.packToBuffer(0L, buffer, 8);
    Signed64.packToBuffer(Long.MAX_VALUE, buffer, 16);

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);
    Mockito.when(stream.readAllBytes()).thenReturn(data);

    final var sample =
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);

    checkNormalizedMono("testStreamSigned64", sample, 3L);

    assertEquals(-1.0, sample.frameGetExact(0L), 0.000001);
    assertEquals(0.0, sample.frameGetExact(1L), 0.000001);
    assertEquals(1.0, sample.frameGetExact(2L), 0.000001);
  }

  @Test
  public void testStreamFloat64()
    throws Exception
  {
    final var format = Mockito.mock(AudioFormat.class);
    Mockito.when(Integer.valueOf(format.getSampleSizeInBits()))
      .thenReturn(Integer.valueOf(64));
    Mockito.when(format.getEncoding())
      .thenReturn(PCM_FLOAT);
    Mockito.when(Boolean.valueOf(format.isBigEndian()))
      .thenReturn(Boolean.TRUE);
    Mockito.when(Integer.valueOf(format.getChannels()))
      .thenReturn(Integer.valueOf(1));

    final var data = new byte[3 * 8];
    final var buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

    final var doubleBuffer = buffer.asDoubleBuffer();
    doubleBuffer.put(0, -1.0);
    doubleBuffer.put(1, 0.0);
    doubleBuffer.put(2, 1.0);

    final var stream = Mockito.mock(AudioInputStream.class);
    Mockito.when(stream.getFormat()).thenReturn(format);
    Mockito.when(stream.readAllBytes()).thenReturn(data);

    final var sample =
      SXMSampleBuffers.readSampleBufferFromStream(
        stream,
        SXMSampleBuffersTest::createBuffer);

    checkNormalizedMono("testStreamSigned64", sample, 3L);

    assertEquals(-1.0, sample.frameGetExact(0L), 0.000001);
    assertEquals(0.0, sample.frameGetExact(1L), 0.000001);
    assertEquals(1.0, sample.frameGetExact(2L), 0.000001);
  }

  private void roundTripFile(
    final String file)
  {
    LOGGER.debug("{}: running", file);

    try (final var stream = AudioSystem.getAudioInputStream(resource(file))) {
      LOGGER.debug("{}: format: {}", file, stream.getFormat());
      final var expectedBuffer =
        SXMSampleBuffers.readSampleBufferFromStream(
          stream,
          SXMSampleBuffersTest::createBuffer
        );

      assertEquals(1200L, expectedBuffer.frames());

      try (final var sample_stream =
             SXMSampleBuffers.createStreamFromSampleBuffer(expectedBuffer)) {
        final var received_buffer =
          SXMSampleBuffers.readSampleBufferFromStream(
            sample_stream,
            SXMSampleBuffersTest::createBuffer);

        assertEquals(
          expectedBuffer.frames(),
          received_buffer.frames());
        assertEquals(
          expectedBuffer.channels(),
          received_buffer.channels());
        assertEquals(
          expectedBuffer.sampleRate(),
          received_buffer.sampleRate());

        final var expected_frame = new double[expectedBuffer.channels()];
        final var received_frame = new double[expectedBuffer.channels()];

        for (var frame_index = 0L; frame_index < expectedBuffer.frames(); ++frame_index) {
          expectedBuffer.frameGetExact(frame_index, expected_frame);
          received_buffer.frameGetExact(frame_index, received_frame);

          final var currentIndex = frame_index;
          Assertions.assertArrayEquals(
            expected_frame,
            received_frame,
            () -> new StringBuilder(128)
              .append("Frame ")
              .append(currentIndex)
              .append(" mismatch")
              .toString());
        }
      }

    } catch (final UnsupportedAudioFileException | IOException e) {
      LOGGER.info("Ignoring unsupported audio", e);
    } finally {
      LOGGER.debug("{}: finished", file);
    }
  }

  private void roundTripFileConvenience(
    final String file)
    throws IOException
  {
    LOGGER.debug("{}: running", file);

    try (final var stream = AudioSystem.getAudioInputStream(resource(file))) {
      LOGGER.debug("{}: format: {}", file, stream.getFormat());
      final var expectedBuffer =
        SXMSampleBuffers.readSampleBufferFromStream(
          stream,
          SXMSampleBuffersTest::createBuffer
        );

      assertEquals(1200L, expectedBuffer.frames());

      SXMSampleBuffers.writeSampleBufferToFile(
        expectedBuffer,
        this.directory.resolve("out.wav")
      );

      final var receivedBuffer =
        SXMSampleBuffers.readSampleBufferFromFile(
          this.directory.resolve("out.wav"),
          SXMSampleBuffersTest::createBuffer
        );

      assertEquals(
        expectedBuffer.frames(),
        receivedBuffer.frames());
      assertEquals(
        expectedBuffer.channels(),
        receivedBuffer.channels());
      assertEquals(
        expectedBuffer.sampleRate(),
        receivedBuffer.sampleRate());

      final var expected_frame = new double[expectedBuffer.channels()];
      final var received_frame = new double[expectedBuffer.channels()];

      for (var frame_index = 0L; frame_index < expectedBuffer.frames(); ++frame_index) {
        expectedBuffer.frameGetExact(frame_index, expected_frame);
        receivedBuffer.frameGetExact(frame_index, received_frame);

        final var currentIndex = frame_index;
        Assertions.assertArrayEquals(
          expected_frame,
          received_frame,
          () -> new StringBuilder(128)
            .append("Frame ")
            .append(currentIndex)
            .append(" mismatch")
            .toString());
      }

    } catch (final UnsupportedAudioFileException e) {
      LOGGER.info("Ignoring unsupported audio", e);
    } finally {
      LOGGER.debug("{}: finished", file);
    }
  }

  private static void runForMonoFile(final String file)
    throws IOException
  {
    LOGGER.debug("{}: running", file);

    try (final var stream = AudioSystem.getAudioInputStream(resource(file))) {
      LOGGER.debug("{}: format: {}", file, stream.getFormat());
      final var buffer =
        SXMSampleBuffers.readSampleBufferFromStream(
          stream,
          SXMSampleBuffersTest::createBuffer);
      final var count = 1200L;
      assertEquals(count, buffer.frames());
      assertEquals(1, buffer.channels());
      assertEquals(48000.0, buffer.sampleRate());
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
        SXMSampleBuffers.readSampleBufferFromStream(
          stream,
          SXMSampleBuffersTest::createBuffer);
      final var count = 1200L;
      assertEquals(count, buffer.frames());
      assertEquals(2, buffer.channels());
      assertEquals(48000.0, buffer.sampleRate());
      checkNormalizedStereo(file, buffer, count);
    } catch (final UnsupportedAudioFileException e) {
      LOGGER.info("Ignoring unsupported audio", e);
    } finally {
      LOGGER.debug("{}: finished", file);
    }
  }

}
