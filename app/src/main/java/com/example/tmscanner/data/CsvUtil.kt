package com.example.tmscanner.data

import java.io.File

object CsvUtil {

    fun create(list: List<String>, file: File) {
        file.printWriter(Charsets.UTF_8).use { out ->

            list.forEach { item ->
                val safe = item.replace("\"", "\"\"")
                out.println("\"$safe\"")
            }
        }
    }
}