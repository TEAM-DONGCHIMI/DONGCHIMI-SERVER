rootProject.name = "dongchimi-server"

include(
    "common",
    "core",
    "gateway:auth",
    "gateway:logging",
    "infrastructure:db",
    "infrastructure:client",
    "infrastructure:storage",
    "infrastructure:redis",
    "infrastructure:excel",
    "api:core-api",
    "api:owner-api",
    "api:admin-api",
    "api:user-api",
    "bootstrap",
)
