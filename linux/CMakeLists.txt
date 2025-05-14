cmake_minimum_required(VERSION 3.10)
project(rimusic LANGUAGES CXX)

set(BINARY_NAME "rimusic")
set(APPLICATION_ID "com.github.sarathiq.RiMusic")
set(VERSION "1.0.0")

# Configure build options.
get_property(IS_MULTICONFIG GLOBAL PROPERTY GENERATOR_IS_MULTI_CONFIG)
if(IS_MULTICONFIG)
  set(CMAKE_CONFIGURATION_TYPES "Debug;Profile;Release"
    CACHE STRING "" FORCE)
else()
  if(NOT CMAKE_BUILD_TYPE AND NOT CMAKE_CONFIGURATION_TYPES)
    set(CMAKE_BUILD_TYPE "Debug" CACHE
      STRING "Flutter build mode" FORCE)
    set_property(CACHE CMAKE_BUILD_TYPE PROPERTY STRINGS
      "Debug" "Profile" "Release")
  endif()
endif()

set(CMAKE_EXE_LINKER_FLAGS_PROFILE "${CMAKE_EXE_LINKER_FLAGS_RELEASE}")
set(CMAKE_SHARED_LINKER_FLAGS_PROFILE "${CMAKE_SHARED_LINKER_FLAGS_RELEASE}")
set(CMAKE_C_FLAGS_PROFILE "${CMAKE_C_FLAGS_RELEASE}")
set(CMAKE_CXX_FLAGS_PROFILE "${CMAKE_CXX_FLAGS_RELEASE}")

# Use colored output
set(CMAKE_COLOR_MAKEFILE ON)

# Enable Hot Reload by default.
if(NOT CMAKE_BUILD_TYPE MATCHES "Debug")
  set(FLUTTER_BUNDLE_INSTALL_DIR "${CMAKE_INSTALL_PREFIX}/bin")
endif()

# Linux-specific dependencies
find_package(PkgConfig REQUIRED)
pkg_check_modules(GTK REQUIRED IMPORTED_TARGET gtk+-3.0)
pkg_check_modules(GLIB REQUIRED IMPORTED_TARGET glib-2.0)
pkg_check_modules(GIO REQUIRED IMPORTED_TARGET gio-2.0)

add_definitions(-DAPPLICATION_ID="${APPLICATION_ID}")

# Application build
set(FLUTTER_MANAGED_DIR "${CMAKE_CURRENT_SOURCE_DIR}/flutter")
add_subdirectory(${FLUTTER_MANAGED_DIR})

# Generated plugin build rules
set(GENERATED_PLUGINS_DIR "${PROJECT_BINARY_DIR}/generated_plugins")
add_subdirectory(${GENERATED_PLUGINS_DIR} plugins)

# Application source
add_executable(${BINARY_NAME}
  "main.cc"
  "my_application.cc"
  "${FLUTTER_MANAGED_DIR}/generated_plugin_registrant.cc"
)

apply_standard_settings(${BINARY_NAME})
target_link_libraries(${BINARY_NAME} PRIVATE flutter)
target_link_libraries(${BINARY_NAME} PRIVATE PkgConfig::GTK)
target_link_libraries(${BINARY_NAME} PRIVATE PkgConfig::GLIB)
target_link_libraries(${BINARY_NAME} PRIVATE PkgConfig::GIO)

# Install
install(TARGETS ${BINARY_NAME} RUNTIME DESTINATION "${CMAKE_INSTALL_PREFIX}/bin")
install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/rimusic.desktop" DESTINATION "${CMAKE_INSTALL_PREFIX}/share/applications")
install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/com.github.sarathiq.RiMusic.appdata.xml" DESTINATION "${CMAKE_INSTALL_PREFIX}/share/metainfo")