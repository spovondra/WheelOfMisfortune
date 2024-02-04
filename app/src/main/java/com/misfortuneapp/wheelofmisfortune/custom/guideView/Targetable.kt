package com.misfortuneapp.wheelofmisfortune.custom.guideView

import android.graphics.Path
import android.graphics.RectF


interface Targetable {
    fun guidePath(): Path?

    fun boundingRect(): RectF?
}