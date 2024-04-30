/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.jsamplebuffer.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SBTestDirectories
{
  private static final Logger LOGGER =
    LoggerFactory.getLogger(SBTestDirectories.class);

  private SBTestDirectories()
  {

  }

  public static Path createBaseDirectory()
    throws IOException
  {
    final var property = System.getProperty("java.io.tmpdir");
    if (property.isBlank() || property.isEmpty()) {
      throw new IllegalStateException();
    }

    final var path = Path.of(property).resolve("jsamplebuffer");
    Files.createDirectories(path);
    return path;
  }

  public static Path createTempDirectory()
    throws IOException
  {
    final var path = createBaseDirectory();
    final var temp = path.resolve(UUID.randomUUID().toString());
    Files.createDirectories(temp);
    return temp;
  }

  public static void deleteDirectory(
    final Path directory)
    throws IOException
  {
    try (var walk = Files.walk(directory)) {
      walk.sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    }
  }

  public static Path resourceOf(
    final Class<?> clazz,
    final Path output,
    final String name)
    throws IOException
  {
    final var internal = String.format("/com/io7m/jsamplebuffer/tests/%s", name);
    final var url = clazz.getResource(internal);
    if (url == null) {
      throw new NoSuchFileException(internal);
    }

    final var target = output.resolve(name);
    LOGGER.debug("copy {} {}", name, target);

    try (var stream = url.openStream()) {
      Files.copy(stream, target);
    }
    return target;
  }

  public static InputStream resourceStreamOf(
    final Class<?> clazz,
    final Path output,
    final String name)
    throws IOException
  {
    return Files.newInputStream(resourceOf(clazz, output, name));
  }

  public static byte[] resourceBytesOf(
    final Class<?> clazz,
    final Path output,
    final String name)
    throws IOException
  {
    try (var stream = resourceStreamOf(clazz, output, name)) {
      return stream.readAllBytes();
    }
  }

  public static String resourceStringOf(
    final Class<?> clazz,
    final Path output,
    final String name)
    throws IOException
  {
    return new String(resourceBytesOf(clazz, output, name), UTF_8);
  }
}
