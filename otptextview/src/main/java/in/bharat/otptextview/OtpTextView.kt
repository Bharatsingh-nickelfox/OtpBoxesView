package `in`.bharat.otptextview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.size
import java.util.*
import java.util.regex.Pattern

class OtpTextView : FrameLayout {

    private var itemViews: MutableList<ItemView>? = null
    private var otpChildEditText: OTPChildEditText? = null
    var otpListener: OTPListener? = null
    //var attributeSet: AttributeSet? = null

    var length: Int = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var space = 0
    private var spaceTop = 0
    private var spaceLeft = 0
    private var spaceRight = 0
    private var spaceBottom = 0

    private val filter: InputFilter
        get() = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (!Pattern.compile(
                        PATTERN
                    )
                        .matcher(source[i].toString())
                        .matches()
                ) {
                    return@InputFilter ""
                }
            }
            null
        }

    val otp: String?
        get() = otpChildEditText?.text?.toString()

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        //attributeSet = attrs
        val styles = context.obtainStyledAttributes(attrs, R.styleable.OtpTextView)
        styleEditTexts(styles, attrs)
        styles.recycle()
    }

    private fun styleEditTexts(styles: TypedArray, attrs: AttributeSet?) {
        length = styles.getInt(R.styleable.OtpTextView_length, DEFAULT_LENGTH)
        generateViews(styles, attrs)
    }

    private fun generateViews(styles: TypedArray, attrs: AttributeSet?) {
        if (length > 0) {
            val otp = styles.getString(R.styleable.OtpTextView_otp)
            viewWidth = styles.getDimension(
                R.styleable.OtpTextView_width,
                Utils.getPixels(context, DEFAULT_WIDTH).toFloat()
            ).toInt()
            viewHeight = styles.getDimension(
                R.styleable.OtpTextView_height,
                Utils.getPixels(context, DEFAULT_HEIGHT).toFloat()
            ).toInt()
            space = styles.getDimension(
                R.styleable.OtpTextView_box_margin,
                Utils.getPixels(context, DEFAULT_SPACE).toFloat()
            ).toInt()
            spaceLeft = styles.getDimension(
                R.styleable.OtpTextView_box_margin_left,
                Utils.getPixels(context, DEFAULT_SPACE_LEFT).toFloat()
            ).toInt()
            spaceRight = styles.getDimension(
                R.styleable.OtpTextView_box_margin_right,
                Utils.getPixels(context, DEFAULT_SPACE_RIGHT).toFloat()
            ).toInt()
            spaceTop = styles.getDimension(
                R.styleable.OtpTextView_box_margin_top,
                Utils.getPixels(context, DEFAULT_SPACE_TOP).toFloat()
            ).toInt()
            spaceBottom = styles.getDimension(
                R.styleable.OtpTextView_box_margin_bottom,
                Utils.getPixels(context, DEFAULT_SPACE_BOTTOM).toFloat()
            ).toInt()

            //These colors are not used
            calculateBoxViews(length, attrs, 0, 0, 0)

            if (otp != null) {
                setOTP(otp)
            } else {
                setOTP("")
            }
        } else {
            throw IllegalStateException("Please specify the length of the otp view")
        }
    }

    fun calculateBoxViews(
        customLength: Int,
        attrs: AttributeSet?,
        boxBackground: Int,
        successBackground: Int,
        errorBackground: Int
    ) {
        //removeAllViews()
        removeAllViewsInLayout()
        itemViews = ArrayList()
        val params = LinearLayout.LayoutParams(viewWidth, viewHeight)
        if (space > 0) {
            params.setMargins(space, space, space, space)
        } else {
            params.setMargins(spaceLeft, spaceTop, spaceRight, spaceBottom)
        }

        val editTextLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        editTextLayoutParams.gravity = Gravity.CENTER
        otpChildEditText = OTPChildEditText(context)
        otpChildEditText?.filters = arrayOf(filter, InputFilter.LengthFilter(customLength))
        setTextWatcher(otpChildEditText)
        addView(otpChildEditText, editTextLayoutParams)


        val linearLayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val linearLayout = LinearLayout(context)

        addView(linearLayout, linearLayoutParams)

        for (i in 0 until customLength) {
            val itemView = ItemView(context, attrs)
            if (attrs == null) {
                itemView.setBackground(boxBackground, successBackground, errorBackground)
            }
            itemView.setViewState(ItemView.INACTIVE)
            linearLayout.addView(itemView, i, params)
            itemViews?.add(itemView)
        }
        //resetState()
        setOTP("")
    }

    private fun setTextWatcher(otpChildEditText: OTPChildEditText?) {
        otpChildEditText?.addTextChangedListener(object : TextWatcher {
            /**
             * @param s
             * @param start
             * @param count
             * @param after
             */
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            /**
             * @param s
             * @param start
             * @param before
             * @param count
             */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                otpListener?.let { otpListener ->
                    if (s.length == length)
                        otpListener.onInteractionListener(true)
                    else
                        otpListener.onInteractionListener(false)
                }
                setOTP(s)
                setFocus(s.length)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun setFocus(length: Int) {
        itemViews?.let { itemViews ->
            for (i in itemViews.indices) {
                when {
                    i == length -> {
                        itemViews[i].setViewState(ItemView.ACTIVE)
                    }
                    i < length -> {
                        itemViews[i].setViewState(ItemView.FILLED)
                    }
                    else -> {
                        itemViews[i].setViewState(ItemView.INACTIVE)
                    }
                }
            }
            if (length == itemViews.size) {
                itemViews[itemViews.size - 1].setViewState(ItemView.FILLED)
            }
        }
    }

    fun setOTP(s: CharSequence) {
        itemViews?.let { itemViews ->
            for (i in itemViews.indices) {
                if (i < s.length) {
                    itemViews[i].setText(s[i].toString())
                } else {
                    itemViews[i].setText("")
                }
            }
        }
    }

    /**
     * This function is kept for Future reference
     */
    fun requestFocusOTP() {
        otpChildEditText?.requestFocus()
    }

    fun showError() {
        itemViews?.let { itemViews ->
            for (itemView in itemViews) {
                itemView.setViewState(ItemView.ERROR)
            }
        }
    }

    fun hideText() {
        itemViews?.let { itemViews ->
            for (itemView in itemViews) {
                itemView.hideText()
            }
        }
    }

    fun showText() {
        itemViews?.let { itemViews ->
            for (itemView in itemViews) {
                itemView.showText()
            }
        }
    }

    /**
     * This function is kept for Future reference
     */
    fun setViewHeight(w: Int, h: Int, margin: Int) {
        itemViews?.let { itemViews ->
            itemViews.forEachIndexed { index, itemView ->
                val params =
                    LinearLayout.LayoutParams((w - (margin * 1.61)).toInt(), h - (margin * 2))
                when (index) {
                    0 -> params.setMargins(0, margin, margin, margin)
                    itemView.size - 1 -> params.setMargins(margin, margin, 0, margin)
                    else -> params.setMargins(margin, margin, margin, margin)
                }

                //params.setMargins(margin, margin, margin, margin)
                itemView.layoutParams = params
            }
        }
    }


    /**
     * This function is kept for Future reference
     */
    fun resetState() {
        otp?.let {
            setFocus(it.length)
        }
    }


    /**
     * This function is kept for Future reference
     */
    fun showSuccess() {
        itemViews?.let { itemViews ->
            for (itemView in itemViews) {
                itemView.setViewState(ItemView.SUCCESS)
            }
        }
    }

    fun setOTP(otp: String) {
        otpChildEditText?.setText(otp)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(l: OnTouchListener) {
        super.setOnTouchListener(l)
        otpChildEditText?.setOnTouchListener(l)
    }

    companion object {

        private const val DEFAULT_LENGTH = 4
        private const val DEFAULT_HEIGHT = 48
        private const val DEFAULT_WIDTH = 48
        private const val DEFAULT_SPACE = -1
        private const val DEFAULT_SPACE_LEFT = 4
        private const val DEFAULT_SPACE_RIGHT = 4
        private const val DEFAULT_SPACE_TOP = 4
        private const val DEFAULT_SPACE_BOTTOM = 4

        private const val PATTERN = "[1234567890]*"
    }
}
