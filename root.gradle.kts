plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    "1.21.10-fabric"(1_21_10, "srg") {
        "1.21.8-fabric"(1_21_08, "srg") {
            "1.21.5-fabric"(1_21_05, "srg")
        }
    }
}

subprojects {
    afterEvaluate {
        extensions.findByName("loom")?.let {
            (it as net.fabricmc.loom.api.LoomGradleExtensionAPI).apply {
                runConfigs.configureEach {
                    runDir("../../run")
                }
            }
        }
    }
}

// Line ending preservation for cross-platform compatibility
// Helper functions for line ending management
private val relevantExtensions = listOf("java", "kt", "kts", "json")

private fun isRelevantFile(file: File) = file.isFile && relevantExtensions.contains(file.extension)

private fun detectLineSeparator(text: String): String = when {
    text.contains("\r\n") -> "\r\n"
    text.contains("\n") -> "\n"
    else -> System.lineSeparator()
}

private fun restoreLineEndings(file: File, originalEnding: String) {
    val content = file.readText()
    when {
        originalEnding == "\r\n" && !content.contains("\r\n") && content.contains("\n") ->
            file.writeText(content.replace("\n", "\r\n"))
        originalEnding == "\n" && content.contains("\r\n") ->
            file.writeText(content.replace("\r\n", "\n"))
    }
}

val storedLineEndings = mutableMapOf<String, String>()
var preprocessRan = false

subprojects {
    tasks.withType<com.replaymod.gradle.preprocess.PreprocessTask> {
        val taskEndings = mutableMapOf<String, String>()

        doFirst {
            if (storedLineEndings.isEmpty()) {
                rootProject.file("src/main").takeIf { it.exists() }?.walkTopDown()
                    ?.filter { isRelevantFile(it) }
                    ?.forEach {
                        storedLineEndings[it.absolutePath] = detectLineSeparator(it.readText())
                    }
            }
            preprocessRan = true
            outputs.files.forEach { it ->
                it.takeIf { it.exists() }?.walkTopDown()?.filter { isRelevantFile(it) }
                    ?.forEach {
                        taskEndings[it.absolutePath] = detectLineSeparator(it.readText())
                    }
            }
        }

        doLast {
            outputs.files.forEach { it ->
                it.takeIf { it.exists() }?.walkTopDown()?.filter { isRelevantFile(it) }
                    ?.forEach { f ->
                        taskEndings[f.absolutePath]?.let { orig ->
                            restoreLineEndings(f, orig)
                        }
                    }
            }
        }
    }
}

gradle.taskGraph.whenReady {
    gradle.taskGraph.allTasks.lastOrNull()?.doLast {
        if (preprocessRan && storedLineEndings.isNotEmpty()) {
            var restored = 0
            rootProject.file("src/main").takeIf { it.exists() }?.walkTopDown()
                ?.filter { isRelevantFile(it) }
                ?.forEach { f ->
                    storedLineEndings[f.absolutePath]?.let { orig ->
                        val contentBefore = f.readText()
                        restoreLineEndings(f, orig)
                        val contentAfter = f.readText()
                        if (contentBefore != contentAfter) restored++
                    }
                }
            if (restored > 0) println("[Line Endings] Restored $restored files")
            storedLineEndings.clear()
            preprocessRan = false
        }
    }
}