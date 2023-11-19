package de.speedboat.plugins.coreview.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.util.containers.stream
import de.speedboat.plugins.coreview.services.CoReviewService
import javax.swing.Icon

class SeverityIconRenderer(private val suggestion: CoReviewService.SuggestionInformation) : GutterIconRenderer() {

    override fun equals(other: Any?): Boolean {
        return other is SeverityIconRenderer && suggestion == other.suggestion;
    }

    override fun hashCode(): Int {
        return suggestion.hashCode()
    }

    override fun getTooltipText(): String {
        val severity = Severity.getSeverity(suggestion.suggestion.severity)
        return severity.title
    }

    override fun getIcon(): Icon {
        val severity = Severity.getSeverity(suggestion.suggestion.severity)
        return severity.icon
    }

    enum class Severity(val title: String, private val minVal: Float, private val maxVal: Float, val icon: Icon) {
        INFO("Info", 0.0f, 0.4f, AllIcons.General.Information),
        WARNING("Warning", 0.4f, 0.7f, AllIcons.General.Warning),
        SEVERE("Severe Warning", 0.7f, 1.0f, AllIcons.General.Error);

        companion object {
            fun getSeverity(severity: Float): Severity = Severity.values().stream()
                    .filter { it.minVal <= severity && it.maxVal >= severity }
                    .findFirst()
                    .orElse(INFO)
        }
    }
}