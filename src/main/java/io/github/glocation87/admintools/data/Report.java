package io.github.glocation87.admintools.data;

import java.util.UUID;

public record Report(UUID id, UUID reporter, String reporterName, UUID target, String targetName, String reason, long time) {
}
