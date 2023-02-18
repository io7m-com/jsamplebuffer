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
 * <p>A readable and writable sample buffer.</p>
 *
 * <p>All methods that take sample/frame indices as arguments throw {@code
 * com.io7m.jranges.RangeCheckException} exceptions if the indices are out of
 * range.</p>
 */

public interface SampleBufferType extends SampleBufferReadableType
{
  /**
   * Set the value of frame {@code index}. If the underlying buffer contains
   * more than one channel, then the given value is inserted into all channels
   * in the frame.
   *
   * @param index The frame index
   * @param value The input
   */

  void frameSetAll(
    long index,
    double value);

  /**
   * Set the value of frame {@code index}.
   *
   * @param index The frame index
   * @param c0    The input for channel 0
   *
   * @throws IllegalArgumentException If {@code channels() != 1}
   */

  void frameSetExact(
    long index,
    double c0)
    throws IllegalArgumentException;

  /**
   * Set the value of frame {@code index}.
   *
   * @param index The frame index
   * @param c0    The input for channel 0
   * @param c1    The input for channel 1
   *
   * @throws IllegalArgumentException If {@code channels() != 2}
   */

  void frameSetExact(
    long index,
    double c0,
    double c1)
    throws IllegalArgumentException;

  /**
   * Set the value of frame {@code index}. The input value is assumed to contain
   * one sample for each of the channels in the frame.
   *
   * @param index The frame index
   * @param value The input
   *
   * @throws IllegalArgumentException If {@code value.length != channels()}
   */

  void frameSetExact(
    long index,
    double[] value)
    throws IllegalArgumentException;
}
