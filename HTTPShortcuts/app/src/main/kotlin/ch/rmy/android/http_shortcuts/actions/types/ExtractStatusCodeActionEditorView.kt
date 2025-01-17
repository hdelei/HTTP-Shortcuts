package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import kotterknife.bindView

class ExtractStatusCodeActionEditorView(
    context: Context,
    private val action: ExtractStatusCodeAction,
    private val variablePlaceholderProvider: VariablePlaceholderProvider
) : BaseActionEditorView(context, R.layout.action_editor_extract_status_code) {

    private val targetVariableView: TextView by bindView(R.id.target_variable)
    private val variableButton: VariableButton by bindView(R.id.variable_button_target_variable)

    private var selectedVariableId: String = action.variableId

    init {
        targetVariableView.text = action.variableId
        targetVariableView.setOnClickListener {
            variableButton.performClick()
        }
        variableButton.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton.variableSource
            .subscribe {
                selectedVariableId = it.variableId
                updateViews()
            }
            .attachTo(destroyer)
        updateViews()
    }

    private fun updateViews() {
        val variablePlaceholder = variablePlaceholderProvider.findPlaceholderById(selectedVariableId)
        if (variablePlaceholder == null) {
            targetVariableView.setText(R.string.action_type_target_variable_no_variable_selected)
        } else {
            targetVariableView.text = selectedVariableId
        }
    }

    override fun compile(): Boolean {
        if (selectedVariableId.isEmpty()) {
            return false
        }
        action.variableId = selectedVariableId
        return true
    }

}