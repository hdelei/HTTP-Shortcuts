package ch.rmy.android.http_shortcuts.data

import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.io.Closeable
import java.util.*

class Controller : Destroyable, Closeable {

    private val realm: Realm = RealmFactory.getInstance().createRealm()

    override fun destroy() {
        if (!realm.isClosed) {
            realm.close()
        }
    }

    override fun close() = destroy()

    fun getShortcuts() = Repository.getShortcuts(realm)

    fun getVariables(): RealmList<Variable> = getBase().variables

    fun getShortcutById(id: String) = Repository.getShortcutById(realm, id)

    fun getShortcutByName(shortcutName: String): Shortcut? = Repository.getShortcutByName(realm, shortcutName)

    fun getShortcutsPendingExecution(): RealmResults<PendingExecution> = Repository.getShortcutsPendingExecution(realm)

    fun getShortcutPendingExecution(shortcutId: String) = Repository.getShortcutPendingExecution(realm, shortcutId)

    fun exportBase() = getBase().detachFromRealm()

    private fun getBase() = Repository.getBase(realm)!!

    fun importBaseSynchronously(base: Base) {
        val oldBase = getBase()
        realm.executeTransaction { realm ->
            if (oldBase.categories.singleOrNull()?.shortcuts?.isEmpty() == true) {
                oldBase.categories.clear()
            }

            val persistedCategories = realm.copyToRealmOrUpdate(base.categories)
            oldBase.categories.removeAll(persistedCategories)
            oldBase.categories.addAll(persistedCategories)

            val persistedVariables = realm.copyToRealmOrUpdate(base.variables)
            oldBase.variables.removeAll(persistedVariables)
            oldBase.variables.addAll(persistedVariables)
        }
    }

    fun renameShortcut(shortcutId: String, newName: String) =
        Transactions.commit { realm ->
            Repository.getShortcutById(realm, shortcutId)?.name = newName
        }

    fun createPendingExecution(
        shortcutId: String,
        resolvedVariables: Map<String, String>,
        tryNumber: Int = 0,
        waitUntil: Date? = null,
        requiresNetwork: Boolean
    ) =
        Transactions.commit { realm ->
            val alreadyPending = Repository.getShortcutPendingExecution(realm, shortcutId) != null
            if (!alreadyPending) {
                realm.copyToRealm(PendingExecution.createNew(shortcutId, resolvedVariables, tryNumber, waitUntil, requiresNetwork))
            }
        }

    fun removePendingExecution(shortcutId: String) =
        Transactions.commit { realm ->
            Repository.getShortcutPendingExecution(realm, shortcutId)?.deleteFromRealm()
        }

}
