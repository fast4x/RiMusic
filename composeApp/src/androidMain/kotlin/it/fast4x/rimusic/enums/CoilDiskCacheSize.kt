package it.fast4x.rimusic.enums

enum class CoilDiskCacheMaxSize {
    `32MB`,
    `64MB`,
    `128MB`,
    `256MB`,
    `512MB`,
    `1GB`,
    `2GB`,
    `4GB`,
    `8GB`,
    Custom;

    val bytes: Long
        get() = when (this) {
            `32MB` -> 32
            `64MB` -> 64
            `128MB` -> 128
            `256MB` -> 256
            `512MB` -> 512
            `1GB` -> 1024
            `2GB` -> 2048
            `4GB` -> 4096
            `8GB` -> 8192
            Custom -> 1000000
        } * 1000 * 1000L
}
