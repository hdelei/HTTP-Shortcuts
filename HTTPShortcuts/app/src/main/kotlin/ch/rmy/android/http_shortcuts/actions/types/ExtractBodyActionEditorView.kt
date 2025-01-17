package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import kotterknife.bindView

class ExtractBodyActionEditorView(
    context: Context,
    private val action: ExtractBodyAction,
    private val variablePlaceholderProvider: VariablePlaceholderProvider
) : BaseActionEditorView(context, R.layout.action_editor_extract_body) {

    private val extractionOption: LabelledSpinner by bindView(R.id.input_extraction_option)
    private val substringOptions: View by bindView(R.id.container_substring_options)
    private val substringStart: EditText by bindView(R.id.input_substring_start_index)
    private val substringEnd: EditText by bindView(R.id.input_substring_end_index)
    private val jsonOptions: View by bindView(R.id.container_parse_json_options)
    private val jsonPath: EditText by bindView(R.id.input_json_path)

    private val targetVariableView: TextView by bindView(R.id.target_variable)
    private val variableButton: VariableButton by bindView(R.id.variable_button_target_variable)

    private var selectedVariableId: String = action.variableId
        set(value) {
            field = value
            updateViews()
        }

    init {
        extractionOption.setItemsFromPairs(EXTRACTION_OPTIONS.map {
            it.first to context.getString(it.second)
        })
        extractionOption.selectedItem = action.extractionType
        extractionOption.selectionChanges
            .subscribe {
                updateViews()
            }
            .attachTo(destroyer)

        substringStart.setText(action.substringStart.toString())
        substringEnd.setText(action.substringEnd.toString())

        jsonPath.setText(action.jsonPath)

        targetVariableView.text = action.variableId
        targetVariableView.setOnClickListener {
            variableButton.performClick()
        }
        variableButton.variablePlaceholderProvider = variablePlaceholderProvider

        variableButton.variableSource
            .subscribe {
                selectedVariableId = it.variableId
            }
            .attachTo(destroyer)
        updateViews()
    }

    private fun updateViews() {
        val variablePlaceholder = variablePlaceholderProvider.findPlaceholderById(selectedVariableId)
        if (variablePlaceholder == null) {
            targetVariableView.setText(R.string.action_type_target_variable_no_variable_selected)
        } else {
            targetVariableView.text = variablePlaceholder.variableKey
        }
        val selectedOption = extractionOption.selectedItem
        substringOptions.visible = selectedOption == ExtractBodyAction.EXTRACTION_OPTION_SUBSTRING
        jsonOptions.visible = selectedOption == ExtractBodyAction.EXTRACTION_OPTION_PARSE_JSON
    }

    override fun compile(): Boolean {
        if (selectedVariableId.isEmpty()) {
            return false
        }
        action.extractionType = extractionOption.selectedItem
        action.variableId = selectedVariableId

        action.substringStart = substringStart.text.toString().toIntOrNull() ?: 0
        action.substringEnd = substringEnd.text.toString().toIntOrNull() ?: 0

        action.jsonPath = jsonPath.text.toString()

        return true
    }

    companion object {

        private val EXTRACTION_OPTIONS = listOf(
            ExtractBodyAction.EXTRACTION_OPTION_FULL_BODY to R.string.action_type_extract_body_description_option_full_body,
            ExtractBodyAction.EXTRACTION_OPTION_SUBSTRING to R.string.action_type_extract_body_description_option_substring,
            ExtractBodyAction.EXTRACTION_OPTION_PARSE_JSON to R.string.action_type_extract_body_description_option_json
        )

    }

}