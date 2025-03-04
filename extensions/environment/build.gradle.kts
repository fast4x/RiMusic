
plugins {
    kotlin("jvm")
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.kotlin.serialization)
}

sourceSets.all {
    java.srcDir("src/$name/kotlin")
}

dependencies {
    implementation(projects.ktorClientBrotli)
    implementation(projects.piped)
    implementation(projects.invidious)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.serialization)
    implementation(libs.logging.interceptor)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.xml)
    implementation(libs.ktor.serialization.kotlinx.protobuf)
    implementation(libs.newpipe.extractor)
    implementation(libs.ksoup.html)
    implementation(libs.ksoup.entities)
    implementation(libs.okhttp.dnsoverhttps)
    

    //testImplementation(libs.junit)
}
