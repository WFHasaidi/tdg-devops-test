add_library(calculator
    calculator.cpp
    calculator.h
)
add_library(calculator::calculator ALIAS calculator)

target_include_directories(calculator
    PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
        $<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>
)

install(TARGETS calculator
    EXPORT calculatorTargets
    ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
    INCLUDES DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
)

install(FILES calculator.h
    DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
)
