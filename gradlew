#!/bin/sh
# Gradle wrapper startup script
DIRNAME="$(dirname "$0")"
CLASSPATH="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"
JAVA_CMD="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk/bin}/java"
if ! command -v "$JAVA_CMD" >/dev/null 2>&1; then
  JAVA_CMD="java"
fi
exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
