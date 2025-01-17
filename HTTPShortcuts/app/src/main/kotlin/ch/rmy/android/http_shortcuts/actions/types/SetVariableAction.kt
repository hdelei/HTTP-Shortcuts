package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable

class SetVariableAction(
    actionType: SetVariableActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var newValue: String
        get() = internalData[KEY_NEW_VALUE] ?: ""
        set(value) {
            internalData[KEY_NEW_VALUE] = value
        }

    var variableId: String
        get() = internalData[KEY_VARIABLE_ID] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_ID] = value
        }

    override fun getDescription(context: Context): CharSequence =
        context.getString(R.string.action_type_set_variable_description, Variables.toRawPlaceholder(variableId), newValue)

    override fun perform(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val value = Variables.rawPlaceholdersToResolvedValues(newValue, variableValues)
        variableValues[variableId] = value
        return Commons.setVariableValue(variableId, value)
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
        SetVariableActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_NEW_VALUE = "newValue"
        private const val KEY_VARIABLE_ID = "variableId"

    }

}