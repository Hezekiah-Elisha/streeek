package com.bizilabs.streeek.lib.presentation

import android.app.Application
import cafe.adriel.voyager.core.registry.ScreenRegistry
import com.bizilabs.streeek.feature.authentication.featureAuthentication
import com.bizilabs.streeek.feature.landing.featureLanding
import com.bizilabs.streeek.feature.profile.featureProfile
import com.bizilabs.streeek.feature.setup.featureSetup
import com.bizilabs.streeek.feature.tabs.featureTabs
import com.bizilabs.streeek.feature.team.screenTeam
import timber.log.Timber

abstract class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
        initNavigation()
    }

    private fun initTimber(){
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    private fun initNavigation(){
        ScreenRegistry {
            featureLanding()
            featureAuthentication()
            featureSetup()
            featureTabs()
            featureProfile()
            screenTeam()
        }
    }

}