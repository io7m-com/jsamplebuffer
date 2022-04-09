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

package com.io7m.jsamplebuffer.api;

/**
 * A readable sample buffer. A sample buffer is an abstraction over a block of audio data. The
 * sample buffer pretends that the audio is interleaved (such that all of the samples for a given
 * frame are stored consecutively) and consists of double-precision floating-point samples.
 *
 * Sample buffers may store audio differently internally, and may store using a lower level of
 * precision than the double-precision API implies.
 *
 * All methods that take sample/frame indices as arguments throw
 * {@code com.io7m.jranges.RangeCheckException} exceptions if the indices are out of range.
 */

public interface SampleBufferReadableType
{
  /**
   * @return The number of channels in a frame
   */

  int channels();

  /**
   * @return The number of frames in the buffer
   */

  long frames();

  /**
   * @return The sample rate in hz
   */

  double sampleRate();

  /**
   * @return The number of samples in the buffer
   */

  default long samples()
  {
    return Math.multiplyExact((long) this.channels(), this.frames());
  }

  /**
   * Get the value of frame {@code index}.
   *
   * @param index  The frame index
   * @param output The output
   *
   * @throws IllegalArgumentException If {@code output.length != channels()}
   */

  void frameGetExact(
    long index,
    double[] output);

  /**
   * Get the value of frame {@code index}.
   *
   * @param index The frame index
   *
   * @return The value in the frame
   *
   * @throws IllegalArgumentException If {@code 1 != channels()}
   */

  double frameGetExact(
    long index);
}
