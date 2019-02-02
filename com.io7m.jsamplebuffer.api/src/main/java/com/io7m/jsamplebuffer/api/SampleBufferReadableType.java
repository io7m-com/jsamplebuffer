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

package com.io7m.jsamplebuffer.api;

import com.io7m.jranges.RangeCheckException;

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
   * @throws RangeCheckException      If {@code index >= frames()}
   */

  void frameGetExact(
    long index,
    double[] output)
    throws RangeCheckException;

  /**
   * Get the value of frame {@code index}.
   *
   * @param index The frame index
   *
   * @return The value in the frame
   *
   * @throws IllegalArgumentException If {@code 1 != channels()}
   * @throws RangeCheckException      If {@code index >= frames()}
   */

  double frameGetExact(
    long index)
    throws RangeCheckException;
}
