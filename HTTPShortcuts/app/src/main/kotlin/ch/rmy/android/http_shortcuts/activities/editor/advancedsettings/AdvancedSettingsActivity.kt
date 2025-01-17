package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeChecked
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.http_shortcuts.views.PanelButton
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class AdvancedSettingsActivity : BaseActivity() {

    private val viewModel: AdvancedSettingsViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }

    private val waitForConnectionCheckBox: CheckBox by bindView(R.id.input_wait_for_connection)
    private val followRedirectsCheckBox: CheckBox by bindView(R.id.input_follow_redirects)
    private val acceptCertificatesCheckBox: CheckBox by bindView(R.id.input_accept_certificates)
    private val timeoutView: PanelButton by bindView(R.id.input_timeout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_settings)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        waitForConnectionCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setWaitForConnection(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        followRedirectsCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setFollowRedirects(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)
        acceptCertificatesCheckBox
            .observeChecked()
            .concatMapCompletable { isChecked ->
                viewModel.setAcceptAllCertificates(isChecked)
            }
            .subscribe()
            .attachTo(destroyer)

        timeoutView.setOnClickListener {
            showTimeoutDialog()
        }
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })
    }

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        waitForConnectionCheckBox.isChecked = shortcut.isWaitForNetwork
        followRedirectsCheckBox.isChecked = shortcut.followRedirects
        acceptCertificatesCheckBox.isChecked = shortcut.acceptAllCertificates
        timeoutView.subtitle = viewModel.getTimeoutSubtitle(shortcut)
    }

    private fun showTimeoutDialog() {
        // TODO: Move this out into its own class
        val shortcut = shortcutData.value ?: return
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)

        val slider = view.findViewById<SeekBar>(R.id.slider)
        val label = view.findViewById<TextView>(R.id.slider_value)

        slider.max = TIMEOUT_MAX - TIMEOUT_MIN

        slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                label.text = viewModel.getTimeoutText(progressToTimeout(progress))
            }
        })
        label.text = viewModel.getTimeoutText(shortcut.timeout)
        slider.progress = timeoutToProgress(shortcut.timeout)

        MaterialDialog.Builder(context)
            .title(R.string.label_timeout)
            .customView(view, true)
            .positiveText(R.string.dialog_ok)
            .onPositive { _, _ ->
                viewModel.setTimeout(progressToTimeout(slider.progress))
                    .subscribe()
                    .attachTo(destroyer)
            }
            .showIfPossible()
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, AdvancedSettingsActivity::class.java)

    companion object {

        private const val TIMEOUT_MIN = 1

        private const val TIMEOUT_MAX = 10 * 60

        private fun timeoutToProgress(timeout: Int) = (timeout / 1000) - TIMEOUT_MIN

        private fun progressToTimeout(progress: Int) = (progress + TIMEOUT_MIN) * 1000

    }

}