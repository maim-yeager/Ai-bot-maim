# Convenience makefile
.PHONY: help setup build ci

help:
	@echo "make setup  - bootstrap Android SDK (if desired)"
	@echo "make build  - build debug APK locally (runs setup if SDK missing)"
	@echo "make ci     - run CI build (locally uses Gradle)"

setup:
	bash ./scripts/bootstrap-android-sdk.sh

build:
	bash ./scripts/run-build.sh

ci:
	./gradlew assembleDebug --no-daemon
