package com.peugether.demo.common.response

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
) {
    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

        fun of(code: String, message: String): ErrorResponse =
            ErrorResponse(
                code = code,
                message = message,
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "+09:00",
            )
    }
}
