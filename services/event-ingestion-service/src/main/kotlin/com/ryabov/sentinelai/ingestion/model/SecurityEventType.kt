package com.ryabov.sentinelai.ingestion.model

enum class SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    API_REQUEST,
    FILE_DOWNLOAD,
    PERMISSION_CHANGE,
    DEVICE_LOGIN,
    TOKEN_CREATED,
    PRIVILEGE_ESCALATION,
    DATA_EXPORT,
    ADMIN_ACTION
}
