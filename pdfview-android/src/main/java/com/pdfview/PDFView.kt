package com.pdfview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.pdfview.pdf.R
import com.pdfview.subsamplincscaleimageview.ImageSource
import com.pdfview.subsamplincscaleimageview.SubsamplingScaleImageView
import java.io.File

class PDFView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SubsamplingScaleImageView(context, attrs) {

    private var mfile: File? = null
    private var mScale: Float = 8f
    private var pageSpacing: Int = 0

    @ColorInt
    private var spacingColor: Int = 0

    init {
        setMinimumTileDpi(120)
        setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)

        attrs.let {
            val a = context.obtainStyledAttributes(it, R.styleable.PDFView)
            this.pageSpacing = a.getDimensionPixelSize(R.styleable.PDFView_pageSpacing, 0)
            this.spacingColor = a.getColor(R.styleable.PDFView_pageSpacingColor, Color.WHITE)
            a.recycle()
        }
    }

    fun fromAsset(assetFileName: String): PDFView {
        mfile = FileUtils.fileFromAsset(context, assetFileName)
        return this
    }

    fun fromFile(file: File): PDFView {
        mfile = file
        return this
    }

    fun fromFile(filePath: String): PDFView {
        mfile = File(filePath)
        return this
    }

    fun scale(scale: Float): PDFView {
        mScale = scale
        return this
    }

    fun show() {
        val source = ImageSource.uri(mfile!!.path)
        setRegionDecoderFactory {
            PDFRegionDecoder(
                    view = this,
                    file = mfile!!,
                    scale = mScale,
                    pageSpacing = pageSpacing,
                    backgroundColorPdf = spacingColor
            )
        }
        setImage(source)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.recycle()
    }
}