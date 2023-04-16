package com.cylonid.nativealpha.model

import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.WebAppSettingsActivity
import com.cylonid.nativealpha.helper.BiometricPromptHelper
import com.cylonid.nativealpha.util.Const
import com.cylonid.nativealpha.util.Utility
import java.util.*

class WebApp {
    val ID: Int
    var baseUrl: String
    var title: String
    var isActiveEntry = true
    var isOverrideGlobalSettings = true

    var isOpenUrlExternal = false
    var isAllowCookies = true
    var isAllowThirdPartyCookies = false
    var isRestorePage = false
    var isAllowJs = true
    var isRequestDesktop = false
    var isClearCache = false
    var isUseAdblock = false
    var isSendSavedataRequest = false
    var isBlockImages = false
    var isAllowHttp = false
    var isAllowLocationAccess = false
    var userAgent: String? = null
    var isUseCustomUserAgent = false
    var isAutoreload = false
    var timeAutoreload = 0
    var isForceDarkMode = false
    var isUseTimespanDarkMode = false
    var timespanDarkModeBegin: String? = "22:00"
    var timespanDarkModeEnd: String? = "06:00"
    var isIgnoreSslErrors = false
    var isShowExpertSettings = false
    var isSafeBrowsing = false
    var isBlockThirdPartyRequests = false
    var containerId: Int = Const.NO_CONTAINER
    var isUseContainer = false
    var isDrmAllowed = false
    var isShowFullscreen = false
    var isKeepAwake = false
    var isCameraPermission = false
    var isMicrophonePermission = false
    var isEnableZooming = false
    var isBiometricProtection = false
    var isAllowMediaPlaybackInBackground = false
    var isContextMenuEnabled = true

    constructor(url: String, id: Int) {
        title = url.replace("http://", "").replace("https://", "").replace("www.", "")
        baseUrl = url
        this.ID = id
        initDefaultSettings()
    }

    constructor(other: WebApp) {
        title = other.title
        ID = other.ID
        baseUrl = other.baseUrl
        isOverrideGlobalSettings = other.isOverrideGlobalSettings
        containerId = other.containerId
        isUseContainer = other.isUseContainer
        copySettings(other)
    }

    //This part of the copy ctor should be callable independently from actual object construction to copy values of the global web app template
    fun copySettings(other: WebApp) {
        isOpenUrlExternal = other.isOpenUrlExternal
        isAllowCookies = other.isAllowCookies
        isAllowThirdPartyCookies = other.isAllowThirdPartyCookies
        isRestorePage = other.isRestorePage
        isAllowJs = other.isAllowJs
        isActiveEntry = other.isActiveEntry
        isRequestDesktop = other.isRequestDesktop
        isClearCache = other.isClearCache
        isUseAdblock = other.isUseAdblock
        isSendSavedataRequest = other.isSendSavedataRequest
        isBlockImages = other.isBlockImages
        isAllowHttp = other.isAllowHttp
        isAllowLocationAccess = other.isAllowLocationAccess
        userAgent = other.userAgent
        isUseCustomUserAgent = other.isUseCustomUserAgent
        isAutoreload = other.isAutoreload
        timeAutoreload = other.timeAutoreload
        isForceDarkMode = other.isForceDarkMode
        isUseTimespanDarkMode = other.isUseTimespanDarkMode
        timespanDarkModeBegin = other.timespanDarkModeBegin
        timespanDarkModeEnd = other.timespanDarkModeEnd
        isIgnoreSslErrors = other.isIgnoreSslErrors
        isShowExpertSettings = other.isShowExpertSettings
        isSafeBrowsing = other.isSafeBrowsing
        isBlockThirdPartyRequests = other.isBlockThirdPartyRequests
        isDrmAllowed = other.isDrmAllowed
        isShowFullscreen = other.isShowFullscreen
        isKeepAwake = other.isKeepAwake
        isCameraPermission = other.isCameraPermission
        isMicrophonePermission = other.isMicrophonePermission
        isEnableZooming = other.isEnableZooming
        isBiometricProtection = other.isBiometricProtection
        isAllowMediaPlaybackInBackground = other.isAllowMediaPlaybackInBackground
        isContextMenuEnabled = other.isContextMenuEnabled
    }

    private fun initDefaultSettings() {
        if (baseUrl.contains("facebook.com")) {
            userAgent = Const.DESKTOP_USER_AGENT
            isUseCustomUserAgent = true
        }
    }

    /*
        This function is used for settings where the ctor needs to have a different setting because
        we want different behaviour for already existing and newly created Web Apps.
            */
    fun applySettingsForNewWebApp() {
        isOverrideGlobalSettings = false
    }

    fun markInactive() {
        isActiveEntry = false
        Utility.deleteShortcuts(Arrays.asList(ID))
    }


    val alphanumericBaseUrl: String
        get() = baseUrl.replace("\\P{Alnum}".toRegex(), "").replace("https", "").replace("http", "").replace("www", "")

    fun onSwitchCookiesChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val switchThirdPCookies = mSwitch.rootView.findViewById<Switch>(R.id.switch3PCookies)
        if (isChecked) switchThirdPCookies.isEnabled = true else {
            switchThirdPCookies.isEnabled = false
            switchThirdPCookies.isChecked = false
        }
    }

    private fun disableSwitchBiometricAccessChangeListener(switchBiometricAccess: Switch) {
        switchBiometricAccess.setOnCheckedChangeListener(null)
    }

    private fun enableSwitchBiometricAccessChangeListener(switchBiometricAccess: Switch,
                                                          activity: WebAppSettingsActivity) {
        switchBiometricAccess.setOnCheckedChangeListener { switch, checked ->
            onSwitchBiometricAccessChanged(
                switch,
                checked,
                activity
            )
        }
    }

    private fun setSwitchBiometricAccessSilently(newValue: Boolean,
                                                 switchBiometricAccess: Switch,
                                                 activity: WebAppSettingsActivity) {
        disableSwitchBiometricAccessChangeListener(switchBiometricAccess)
        switchBiometricAccess.isChecked = newValue
        enableSwitchBiometricAccessChangeListener(switchBiometricAccess, activity)
    }

    fun onSwitchBiometricAccessChanged(
        mSwitch: CompoundButton,
        isChecked: Boolean,
        activity: WebAppSettingsActivity
    ) {
        val switchBiometricAccess =
            mSwitch.rootView.findViewById<Switch>(R.id.switchBiometricAccess)

        // reset to value before user toggled, actual setting of value is done by prompt success callback
        setSwitchBiometricAccessSilently(!switchBiometricAccess.isChecked, switchBiometricAccess, activity)

        if (!switchBiometricAccess.isChecked) {
            BiometricPromptHelper(activity as FragmentActivity).showPrompt(
                {
                    setSwitchBiometricAccessSilently(true, switchBiometricAccess, activity)
                    isBiometricProtection = true
                },
                {}, activity.getString(R.string.bioprompt_enable_restriction)
            )
        }
        if (switchBiometricAccess.isChecked) {
            BiometricPromptHelper(activity as FragmentActivity).showPrompt({
                setSwitchBiometricAccessSilently(false, switchBiometricAccess, activity)
                isBiometricProtection = false
            }, {}, activity.getString(R.string.bioprompt_disable_restricition)
            )
        }
    }

    fun onSwitchJsChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val switchDesktopVersion = mSwitch.rootView.findViewById<Switch>(R.id.switchDesktopSite)
        val switchAdblock = mSwitch.rootView.findViewById<Switch>(R.id.switchAdblock)
        if (isChecked) {
            switchDesktopVersion.isEnabled = true
            switchAdblock.isEnabled = true
        } else {
            switchDesktopVersion.isChecked = false
            switchDesktopVersion.isEnabled = false
            switchAdblock.isChecked = false
            switchAdblock.isEnabled = false
        }
    }

    fun onSwitchForceDarkChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val switchLimit = mSwitch.rootView.findViewById<Switch>(R.id.switchTimeSpanDarkMode)
        val txtBegin = mSwitch.rootView.findViewById<EditText>(R.id.textDarkModeBegin)
        val txtEnd = mSwitch.rootView.findViewById<EditText>(R.id.textDarkModeEnd)
        if (isChecked) {
            switchLimit.isEnabled = true
            txtBegin.isEnabled = true
            txtEnd.isEnabled = true
        } else {
            switchLimit.isChecked = false
            switchLimit.isEnabled = false
            txtBegin.isEnabled = false
            txtEnd.isEnabled = false
        }
    }

    fun onSwitchTimeSpanDarkChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val lblBegin = mSwitch.rootView.findViewById<TextView>(R.id.lblDarkModeBegin)
        val lblEnd = mSwitch.rootView.findViewById<TextView>(R.id.lblDarkModeEnd)
        val txtBegin = mSwitch.rootView.findViewById<EditText>(R.id.textDarkModeBegin)
        val txtEnd = mSwitch.rootView.findViewById<EditText>(R.id.textDarkModeEnd)
        if (isChecked) {
            lblBegin.isEnabled = true
            lblEnd.isEnabled = true
            txtBegin.isEnabled = true
            txtEnd.isEnabled = true
        } else {
            lblBegin.isEnabled = false
            lblEnd.isEnabled = false
            txtBegin.isEnabled = false
            txtEnd.isEnabled = false
        }
    }

    fun onSwitchUserAgentChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val txt = mSwitch.rootView.findViewById<EditText>(R.id.textUserAgent)
        val switchDesktopVersion = mSwitch.rootView.findViewById<Switch>(R.id.switchDesktopSite)
        if (isChecked) {
            switchDesktopVersion.isChecked = false
            switchDesktopVersion.isEnabled = false
            txt.isEnabled = true
        } else {
            txt.isEnabled = false
            switchDesktopVersion.isEnabled = true
        }
    }

    fun onSwitchAutoreloadChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val text = mSwitch.rootView.findViewById<EditText>(R.id.textReloadInterval)
        val label = mSwitch.rootView.findViewById<TextView>(R.id.labelReloadInterval)
        text.isEnabled = isChecked
        label.isEnabled = isChecked
    }

    fun onSwitchExpertSettingsChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val expertSettings = mSwitch.rootView.findViewById<LinearLayout>(R.id.sectionExpertSettings)
        if (isChecked) expertSettings.visibility = View.VISIBLE else expertSettings.visibility = View.GONE
    }

    fun onSwitchSandboxChanged(mSwitch: CompoundButton?, isChecked: Boolean) {
        containerId = if (isChecked) {
            SandboxManager.getInstance().calculateNextFreeContainerId()
        } else {
            Const.NO_CONTAINER
        }
    }

    fun onSwitchOverrideGlobalSettingsChanged(mSwitch: CompoundButton, isChecked: Boolean) {
        val sectionDetailedWebAppSettings = mSwitch.rootView.findViewById<LinearLayout>(R.id.sectionWebAppDetailSettings)
        Utility.setViewAndChildrenEnabled(sectionDetailedWebAppSettings, isChecked)
    }
}