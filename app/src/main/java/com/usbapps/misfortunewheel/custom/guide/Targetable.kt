package com.usbapps.misfortunewheel.custom.guide

import android.graphics.Path
import android.graphics.RectF

/**
 * Rozhraní pro cílový prvek, který může být zvýrazněn v průvodci.
 */
interface Targetable {
    /**
     * Vrací cestu průvodce pro cíl.
     *
     * @return Cesta průvodce pro cíl.
     */
    fun guidePath(): Path?

    /**
     * Vrací ohraničující obdélník pro cíl.
     *
     * @return Ohraničující obdélník pro cíl.
     */
    fun boundingRect(): RectF?
}