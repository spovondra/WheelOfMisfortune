package com.usbapps.misfortunewheel.custom.guide

/**
 * Enumerace definující různé způsoby zavírání průvodce.
 */
enum class DismissType {
    /**
     * Zavírání průvodce stiskem mimo průvodce.
     */
    Outside,

    /**
     * Zavírání průvodce stiskem kdekoliv na obrazovce.
     */
    Anywhere,

    /**
     * Zavírání průvodce stiskem na cílový pohled.
     */
    TargetView,

    /**
     * Zavírání průvodce stiskem na vlastní pohled průvodce.
     */
    SelfView,

    /**
     * Zavírání průvodce stiskem mimo cílový pohled a zprávu průvodce.
     */
    OutsideTargetAndMessage
}

/**
 * Enumerace definující různé způsoby zarovnání průvodce.
 */
enum class Gravity {
    /**
     * Automatické zarovnání průvodce.
     */
    Auto,

    /**
     * Zarovnání průvodce na střed.
     */
    Center
}

/**
 * Enumerace definující různé typy ukazatelů průvodce.
 */
enum class PointerType {
    /**
     * Kruhový ukazatel průvodce.
     */
    Circle,

    /**
     * Šipkový ukazatel průvodce.
     */
    Arrow,

    /**
     * Žádný ukazatel průvodce.
     */
    None
}
