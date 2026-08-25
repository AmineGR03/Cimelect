package com.cimelect.enums;

import java.util.List;

public enum OperationStatus {
    CREEE,
    EN_TRANSIT,
    DEDOUANEMENT,
    RECUE,
    PREPARATION,
    EXPEDIEE,
    LIVREE,
    CLOTUREE;

    public static List<OperationStatus> importCycle() {
        return List.of(CREEE, EN_TRANSIT, DEDOUANEMENT, RECUE, CLOTUREE);
    }

    public static List<OperationStatus> exportCycle() {
        return List.of(CREEE, PREPARATION, EXPEDIEE, LIVREE, CLOTUREE);
    }
}
