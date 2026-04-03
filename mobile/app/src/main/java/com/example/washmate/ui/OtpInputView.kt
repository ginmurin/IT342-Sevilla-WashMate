package com.example.washmate.ui

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.washmate.R

class OtpInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val otpFields = mutableListOf<EditText>()
    private var otpChangedListener: ((Boolean) -> Unit)? = null
    private val otpLength = 6

    init {
        orientation = HORIZONTAL
        gravity = android.view.Gravity.CENTER

        // Create 6 OTP input fields
        repeat(otpLength) { index ->
            val editText = EditText(context).apply {
                layoutParams = LayoutParams(70, 90).apply {
                    setMargins(10, 0, 10, 0)
                }
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(InputFilter.LengthFilter(1))
                maxLines = 1
                setBackgroundResource(R.drawable.otp_input_bg)
                textSize = 28f
                setTextColor(0xFF000000.toInt()) // Black text
                setHintTextColor(0xFF999999.toInt()) // Gray hint
                gravity = android.view.Gravity.CENTER
                imeOptions = EditorInfo.IME_ACTION_NEXT
                setPadding(0, 0, 0, 0)

                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                        if (text.isEmpty() && index > 0) {
                            otpFields[index - 1].requestFocus()
                            otpFields[index - 1].text.clear()
                        }
                    }
                    false
                }

                addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s?.length == 1 && index < otpLength - 1) {
                            otpFields[index + 1].requestFocus()
                        }
                        checkOtpComplete()
                    }

                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }

            otpFields.add(editText)
            addView(editText)
        }
    }

    fun setOtpChangedListener(listener: (Boolean) -> Unit) {
        otpChangedListener = listener
    }

    fun getOtp(): String {
        return otpFields.joinToString("") { it.text.toString() }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        otpFields.forEach { it.isEnabled = enabled }
    }

    private fun checkOtpComplete() {
        val isComplete = otpFields.all { it.text.length == 1 }
        otpChangedListener?.invoke(isComplete)
    }

    fun clearOtp() {
        otpFields.forEach { it.text.clear() }
    }

    fun focusFirstField() {
        otpFields.firstOrNull()?.requestFocus()
    }
}
