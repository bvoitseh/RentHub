package com.bohakka.renthub.event;

import java.util.List;

/** Published when photos are deleted in the database; their stored files are removed after the commit (FR-4a). */
public record StoredObjectsRemoved(List<String> keys) {
}
