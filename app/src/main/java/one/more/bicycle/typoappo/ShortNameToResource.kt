package one.more.bicycle.typoappo

import android.content.SharedPreferences

data class CurrencyData(
    val shortName: String,
    val resource: Int,
    var toDollarCoef: Float
)

val exchanges = listOf(
    CurrencyData("CAD", R.string.ex_cad, 1.2734204793f),
    CurrencyData("HKD", R.string.ex_hkd, 7.8499245852f),
    CurrencyData("ISK", R.string.ex_isk, 102.5641025641f),
    CurrencyData("PHP", R.string.ex_php, 52.4141109435f),
    CurrencyData("DKK", R.string.ex_dkk, 6.242500419f),
    CurrencyData("HUF", R.string.ex_huf, 263.9265962795f),
    CurrencyData("CZK", R.string.ex_czk, 21.3725490196f),
    CurrencyData("GBP", R.string.ex_gbp, 0.7373889727f),
    CurrencyData("RON", R.string.ex_ron, 3.8834422658f),
    CurrencyData("SEK", R.string.ex_sek, 8.6002178649f),
    CurrencyData("IDR", R.string.ex_idr, 13970.7474442769f),
    CurrencyData("INR", R.string.ex_inr, 67.3370202782f),
    CurrencyData("BRL", R.string.ex_brl, 3.5600804424f),
    CurrencyData("RUB", R.string.ex_rub, 61.6477291771f),
    CurrencyData("HRK", R.string.ex_hrk, 6.1918049271f),
    CurrencyData("JPY", R.string.ex_jpy, 109.30115636f),
    CurrencyData("THB", R.string.ex_thb, 31.8652589241f),
    CurrencyData("CHF", R.string.ex_chf, 0.9999162058f),
    CurrencyData("EUR", R.string.ex_eur, 0.8379420144f),
    CurrencyData("MYR", R.string.ex_myr, 3.9980727334f),
    CurrencyData("BGN", R.string.ex_bgn, 1.6388469918f),
    CurrencyData("TRY", R.string.ex_try, 4.2755991285f),
    CurrencyData("CNY", R.string.ex_cny, 6.3324953913f),
    CurrencyData("NOK", R.string.ex_nok, 7.9960616725f),
    CurrencyData("NZD", R.string.ex_nzd, 1.4337187867f),
    CurrencyData("ZAR", R.string.ex_zar, 12.2771074242f),
    CurrencyData("USD", R.string.ex_usd, 1.0f),
    CurrencyData("MXN", R.string.ex_mxn, 19.2555723144f),
    CurrencyData("SGD", R.string.ex_sgd, 1.3337523043f),
    CurrencyData("AUD", R.string.ex_aud, 1.324032177f),
    CurrencyData("ILS", R.string.ex_ils, 3.5662812133f),
    CurrencyData("KRW", R.string.ex_krw, 1066.7588402883f),
    CurrencyData("PLN", R.string.ex_pln, 3.5682922742f)
)

fun updateExchangeData(exchangeData: ExchangeData, prefsEditor: SharedPreferences.Editor) {
    exchangeData.rates.forEach { shortName, toDollarCoef ->
        val currencyData = exchanges.find { it.shortName == shortName } ?: return@forEach
        currencyData.toDollarCoef = toDollarCoef
        prefsEditor.putFloat(currencyData.shortName, toDollarCoef)
    }
    prefsEditor.apply()
}

fun loadExchangeData(prefs: SharedPreferences) {
    exchanges.forEach {
        if (prefs.contains(it.shortName))
            it.toDollarCoef = prefs.getFloat(it.shortName, Float.NaN)
    }
}
