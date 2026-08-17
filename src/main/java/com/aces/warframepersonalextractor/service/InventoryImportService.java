package com.aces.warframepersonalextractor.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class InventoryImportService {

    private static final String HELPER_NAME =
            "warframe-api-helper.exe";

    private static final String INVENTORY_FILE_NAME =
            "inventory.json";

    private static final String DATA_FILE_NAME =
            "lastData.dat";

    private static final long TIMEOUT_MS = 30_000;

    public String importInventory() {

        Path projectRoot =
                Paths.get("").toAbsolutePath();

        Path helperPath =
                projectRoot.resolve(HELPER_NAME);

        Path inventoryPath =
                projectRoot.resolve(INVENTORY_FILE_NAME);

        Path dataPath =
                projectRoot.resolve(DATA_FILE_NAME);

        if (!Files.exists(helperPath)) {
            throw new IllegalStateException(
                    "warframe-api-helper.exe was not found."
            );
        }

        // Delete old files first.
        // This guarantees anything created after this point is fresh.
        deleteIfExists(inventoryPath);
        deleteIfExists(dataPath);

        Process process = null;

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            helperPath.toString()
                    );

            processBuilder.directory(
                    projectRoot.toFile()
            );

            processBuilder.inheritIO();

            process = processBuilder.start();

            waitForInventoryFile(
                    inventoryPath,
                    process
            );

            String inventoryJson =
                    Files.readString(inventoryPath);

            /*
             * The helper has completed its useful work,
             * even though it refuses to exit.
             */
            if (process.isAlive()) {
                process.destroy();

                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (process.isAlive()) {
                    process.destroyForcibly();
                }
            }

            deleteIfExists(inventoryPath);
            deleteIfExists(dataPath);

            return inventoryJson;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to run Warframe inventory helper.",
                    e
            );

        } finally {

            // Never leave helper hanging around.
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private void waitForInventoryFile(
            Path inventoryPath,
            Process process
    ) {

        long startTime =
                System.currentTimeMillis();

        while (
                System.currentTimeMillis() - startTime
                        < TIMEOUT_MS
        ) {

            if (Files.exists(inventoryPath)) {

                try {

                    if (Files.size(inventoryPath) > 0) {
                        return;
                    }

                } catch (IOException ignored) {

                }
            }

            /*
             * If helper actually crashes before creating
             * inventory.json, don't wait the full timeout.
             */
            if (!process.isAlive()) {
                throw new IllegalStateException(
                        "Inventory helper closed before creating inventory.json. " +
                                "Make sure Warframe is running and fully loaded."
                );
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Inventory import was interrupted.",
                        e
                );
            }
        }

        throw new IllegalStateException(
                "Timed out waiting for inventory.json. " +
                        "Make sure Warframe is running and fully loaded."
        );
    }

    private void deleteIfExists(Path path) {

        try {
            Files.deleteIfExists(path);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to delete file: "
                            + path.getFileName(),
                    e
            );
        }
    }
}