jsamplebuffer
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.jsamplebuffer/com.io7m.jsamplebuffer.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.jsamplebuffer%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.jsamplebuffer/com.io7m.jsamplebuffer?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/jsamplebuffer/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/jsamplebuffer.svg?style=flat-square)](https://codecov.io/gh/io7m-com/jsamplebuffer)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.jsamplebuffer](./src/site/resources/jsamplebuffer.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jsamplebuffer/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/jsamplebuffer/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jsamplebuffer/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/jsamplebuffer/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jsamplebuffer/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/jsamplebuffer/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jsamplebuffer/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/jsamplebuffer/actions?query=workflow%3Amain.windows.temurin.lts)|

## jsamplebuffer

The `jsamplebuffer` package implements a set of types and
functions for manipulating buffers of audio data.

## Features

* Functions for safely reading and writing sample data.
* Convenience functions for constructing sample buffers from `javax.sound` streams.
* [OSGi-ready](https://www.osgi.org/)
* [JPMS-ready](https://en.wikipedia.org/wiki/Java_Platform_Module_System)
* ISC license.
* High-coverage automated test suite.

## Usage

### Creating Buffers

Use `SampleBufferDouble` and `SampleBufferFloat` to create buffers of audio
where samples are stored as `double` or `float` values, respectively:

```
int channels      = 2;
long frames       = 100L;
double sampleRate = 44100.0;

final var buffer =
  SampleBufferDouble.createWithHeapBuffer(
    channels,
    frames,
    sampleRate
  );

assert 2 == buffer.channels();
assert 100L == buffer.frames();
assert 200L == buffer.samples();
```

Additional methods are provided for creating buffers from `ByteBuffer` values,
and from direct memory.

### Reading Buffers

Use the `frameGetExact` method to read a single frame from a buffer:

```
double samples[] = new double[2];

buffer.frameGetExact(0L, samples);

// samples[0] now contains the sample value for the first channel
// samples[1] now contains the sample value for the second channel
```

### Writing Buffers

Use the `frameSetExact` method to write a single frame to a buffer:

```
double samples[] = new double[2];
samples[0] = 0.5;
samples[1] = 0.75;

buffer.frameSetExact(0L, samples);
```

Additional methods are provided that are specialized to mono and stereo
buffers. Additional methods are provided that allow for filling all channels
of a frame with a single sample value.

### Converting Buffers

Implementations of the `SampleBufferRateConverterType` can be used to
convert buffers between different sampling rates. Currently, one
implementation is provided, `SXMSampleBufferRateConverters`, that uses the
JVM's `javax.sound` conversion machinery. The following code converts a
given buffer to a sample rate of `22050hz`:

```
SampleBufferType srcBuffer;

SXMSampleBufferRateConverters converters;

converters.createConverter()
  .convert(
    SampleBufferDouble::createWithHeapBuffer,
    srcBuffer,
    22050.0
  );
```

### Parsing And Serializing Buffers

The `SXMSampleBuffers` class contains numerous convenience methods for
reading/writing audio buffers from/to audio files using `javax.sound`.

```
Path inputFile;
Path outputFile;

final var buffer =
  SXMSampleBuffers.readSampleBufferFromFile(
    inputFile,
    SampleBufferDouble::createWithHeapBuffer
  );

SXMSampleBuffers.writeSampleBufferToFile(
  buffer,
  outputFile
);
```

