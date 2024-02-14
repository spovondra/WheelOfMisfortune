package com.usbapps.misfortunewheel.custom.guide

import android.view.View

/**
 * Rozhraní pro posluchače, který zpracovává události zavírání průvodce.
 */
interface GuideListener {
    /**
     * Voláno při zavření průvodce.
     *
     * @param view Pohled spojený s průvodcem (pokud existuje).
     */
    fun onDismiss(view: View?)
}