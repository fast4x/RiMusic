package it.fast4x.environment.models

sealed class MediaType {
    data object Song : MediaType()

    data object Video : MediaType()
}