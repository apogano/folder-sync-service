package com.documentshub.foldersync.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ChecksumUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void sameContentProducesSameChecksum() throws IOException {
        Path fileA = tempDir.resolve("a.txt");
        Path fileB = tempDir.resolve("b.txt");
        Files.writeString(fileA, "identical content");
        Files.writeString(fileB, "identical content");

        assertEquals(ChecksumUtil.sha256(fileA), ChecksumUtil.sha256(fileB));
    }

    @Test
    void differentContentProducesDifferentChecksum() throws IOException {
        Path fileA = tempDir.resolve("a.txt");
        Path fileB = tempDir.resolve("b.txt");
        Files.writeString(fileA, "content one");
        Files.writeString(fileB, "content two");

        assertNotEquals(ChecksumUtil.sha256(fileA), ChecksumUtil.sha256(fileB));
    }

    @Test
    void checksumMatchesKnownSha256Value() throws IOException {
        Path file = tempDir.resolve("known.txt");
        Files.writeString(file, "hello world");

        // Independently verifiable: `echo -n "hello world" | sha256sum`
        assertEquals(
                "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
                ChecksumUtil.sha256(file)
       );
    }
}