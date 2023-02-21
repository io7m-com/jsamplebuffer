package com.io7m.jsamplebuffer.tests.xmedia;

import com.io7m.jsamplebuffer.vanilla.SampleBufferFloat;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import java.nio.file.Paths;

public final class SampleConversion
{
  private SampleConversion()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: input.wav output.wav");
    }

    final var input = Paths.get(args[0]);
    final var output = Paths.get(args[1]);

    try (var input_stream = AudioSystem.getAudioInputStream(input.toFile())) {
      final var sample =
        SXMSampleBuffers.sampleBufferOfStream(input_stream, SampleBufferFloat::createWithHeapBuffer);

      try (var sample_stream = SXMSampleBuffers.streamOfSampleBuffer(sample)) {
        final var sample_format =
          sample_stream.getFormat();

        final var target_format =
          new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sample_format.getSampleRate(),
            16,
            sample_format.getChannels(),
            2,
            sample_format.getFrameRate(),
            sample_format.isBigEndian());

        try (var convert_stream = AudioSystem.getAudioInputStream(target_format, sample_stream)) {
          AudioSystem.write(convert_stream, AudioFileFormat.Type.WAVE, output.toFile());
        }
      }
    }
  }
}
