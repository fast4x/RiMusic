package it.fast4x.rimusic.utils

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

class CaptureCrash (private val LOG_PATH: String) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Save crash log to a file
        saveCrashLog(throwable)

        // Terminate the app or perform any other necessary action
        android.os.Process.killProcess(android.os.Process.myPid());
        exitProcess(1)
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {

            val logFile = File(
                LOG_PATH,
                "RiMusic_crash_log.txt"
            )
            if (!logFile.exists()) {
                logFile.createNewFile()
            }

            FileWriter(logFile, true).use { writer ->
                writer.append("------------------------------------------------- \n")
                writer.append("--- Crash Event ${LocalDateTime.now()} \n")
                writer.append("------------------------------------------------- \n")
                printFullStackTrace(throwable,PrintWriter(writer))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun printFullStackTrace(throwable: Throwable, printWriter: PrintWriter) {
        //printWriter.println()
        printWriter.print("FullStackTrace: \n")
        printWriter.print(throwable.toString()+"\n")
        throwable.stackTrace.forEach { element ->
            printWriter.print("\t $element \n")
        }
        val cause = throwable.cause
        if (cause != null) {
            printWriter.print("Caused by:\t")
            printFullStackTrace(cause, printWriter)
        }
        printWriter.print("\n")
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}