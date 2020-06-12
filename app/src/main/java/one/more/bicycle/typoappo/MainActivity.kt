package one.more.bicycle.typoappo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val STATES_PREFS = "States preferences"
    private val EXCHANGES_PREFS = "Exchanges preferences"

    private val LAST_SELECTED_INPUT_CURRENCY = "Last selected input currency"
    private val LAST_SELECTED_OUTPUT_CURRENCY = "Last selected output currency"
    private val LAST_ENTERED_INPUT_VALUE = "Last entered input value"
    private val LAST_ENTERED_OUTPUT_VALUE = "Last entered output value"

    private val INPUT_TEXT_VIEW_ID = "inputText"
    private val OUTPUT_TEXT_VIEW_ID = "outputText"

    private lateinit var inputCurrencySpinner: Spinner
    private lateinit var inputTextView: TextView
    private lateinit var outputCurrencySpinner: Spinner
    private lateinit var outputTextView: TextView
    private lateinit var updateCurrency: Button
    private lateinit var currencyUpdating: ProgressBar

    private var lastFocused: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputCurrencySpinner =  findViewById(R.id.inputCurrencySpinner)
        inputTextView =         findViewById(R.id.inputTextView)
        outputCurrencySpinner = findViewById(R.id.outputCurrencySpinner)
        outputTextView =        findViewById(R.id.outputTextView)
        updateCurrency =        findViewById(R.id.updateCurrency)
        currencyUpdating =      findViewById(R.id.currencyUpdating)

        listOf(inputCurrencySpinner, outputCurrencySpinner).forEach {
            val adapter = ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,
                exchanges.map { getString(it.resource) }
            )
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            it.onItemSelectedListener = this
            it.adapter = adapter
        }

        loadExchangeData(getSharedPreferences(EXCHANGES_PREFS, Context.MODE_PRIVATE))
        loadPreviousState()

        if (inputCurrencySpinner.selectedItemPosition == AdapterView.INVALID_POSITION)
            inputCurrencySpinner.setSelection(exchanges.indexOfFirst { it.shortName == "RUB" })
        if (outputCurrencySpinner.selectedItemPosition == AdapterView.INVALID_POSITION)
            outputCurrencySpinner.setSelection(exchanges.indexOfFirst { it.shortName == "USD" })

        inputTextView.addTextChangedListener {
            if (inputTextView.isFocused) {
                lastFocused = inputTextView
                convertFromInput()
            }
        }

        outputTextView.addTextChangedListener {
            if (outputTextView.isFocused) {
                lastFocused = outputTextView
                convertFromOutput()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        saveState()
    }

    private fun saveState() {
        getSharedPreferences(STATES_PREFS, Context.MODE_PRIVATE).edit().run {
            putInt(LAST_SELECTED_INPUT_CURRENCY, inputCurrencySpinner.selectedItemPosition)
            putInt(LAST_SELECTED_OUTPUT_CURRENCY, outputCurrencySpinner.selectedItemPosition)
            putString(LAST_ENTERED_INPUT_VALUE, inputTextView.text?.toString())
            putString(LAST_ENTERED_OUTPUT_VALUE, outputTextView.text?.toString())
            apply()
        }
    }

    private fun loadPreviousState() {
        getSharedPreferences(STATES_PREFS, Context.MODE_PRIVATE).run {
            inputCurrencySpinner.setSelection(getInt(
                LAST_SELECTED_INPUT_CURRENCY, AdapterView.INVALID_POSITION))
            outputCurrencySpinner.setSelection(getInt(
                LAST_SELECTED_OUTPUT_CURRENCY, AdapterView.INVALID_POSITION))
            inputTextView.text = getString(LAST_ENTERED_INPUT_VALUE, null)
            outputTextView.text = getString(LAST_ENTERED_OUTPUT_VALUE, null)
        }
    }

    private fun convertFromInput() {
        convertValue(inputTextView.text?.toString(),
            fromPosition = inputCurrencySpinner.selectedItemPosition,
            toPosition = outputCurrencySpinner.selectedItemPosition)?.let {
            outputTextView.text = it
        }
    }

    private fun convertFromOutput() {
        convertValue(outputTextView.text?.toString(),
            fromPosition = outputCurrencySpinner.selectedItemPosition,
            toPosition = inputCurrencySpinner.selectedItemPosition)?.let {
            inputTextView.text = it
        }
    }

    private fun convertValue(value: String?, fromPosition: Int, toPosition: Int): String? =
        value?.toDoubleOrNull()?.let {
            val from2Dollar = exchanges[fromPosition].toDollarCoef
            val dollar2To = exchanges[toPosition].toDollarCoef
            val result = (it / from2Dollar) * dollar2To
            "%.2f".format(result)
        }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent) {
            inputCurrencySpinner -> convertFromOutput()
            outputCurrencySpinner -> convertFromInput()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    @Suppress("UNUSED_PARAMETER")
    fun updateExchanges(view: View) {
        currencyUpdating.isVisible = true
        updateCurrency.isEnabled = false

        GlobalScope.launch {
            try {
                val gson = Gson()
                val exchangeData = URL("https://api.exchangeratesapi.io/latest?base=USD")
                    .openConnection()
                    .getInputStream()
                    .bufferedReader()
                    .use { gson.fromJson(it, ExchangeData::class.java) }
                this@MainActivity.runOnUiThread {
                    currencyUpdating.isVisible = false
                    updateCurrency.isEnabled = true

                    toastDate(exchangeData.date)

                    val prefsEditor = getSharedPreferences(EXCHANGES_PREFS,
                        Context.MODE_PRIVATE).edit()
                    updateExchangeData(exchangeData, prefsEditor)
                    updateEnteredValues()
                }
            } catch (e: Exception) {
                this@MainActivity.runOnUiThread {
                    currencyUpdating.isVisible = false
                    updateCurrency.isEnabled = true

                    Toast.makeText(this@MainActivity,
                        R.string.currency_updating_failed,
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun toastDate(dateStr: String) {
        runCatching {
            val splitted = dateStr.split('-')
            val month = Month.of(splitted[1].toInt())
            val day = splitted[2].toInt()

            Toast.makeText(
                this@MainActivity, getString(
                    R.string.updated_from,
                    day, month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                ), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateEnteredValues() {
        when (lastFocused) {
            inputTextView -> convertFromInput()
            outputTextView -> convertFromOutput()
        }
    }

}
