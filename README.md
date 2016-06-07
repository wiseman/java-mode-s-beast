# java-mode-s-beast

[![Build Status](https://travis-ci.org/wiseman/java-mode-s-beast.svg?branch=master)](https://travis-ci.org/wiseman/java-mode-s-beast)[![Coverage Status](https://coveralls.io/repos/github/wiseman/java-mode-s-beast/badge.svg?branch=master)](https://coveralls.io/github/wiseman/java-mode-s-beast?branch=master)

This is a Java library that decodes Mode-S Beast messages containing
Mode S/ADS-B information.

The decoder is primarily a Java port of the C# code in
[Virtual Radar Server](http://www.virtualradarserver.co.uk/).

## Development

To run unit tests & check code style:

```
gradle check
```

To publish to OSSRH/Central Repository:

```
gradle uploadArchives
```
