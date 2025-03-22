package com.pdfview

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.annotation.ColorInt
import com.pdfview.subsamplincscaleimageview.SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
import com.pdfview.subsamplincscaleimageview.decoder.ImageRegionDecoder
import java.io.File

internal class PDFRegionDecoder(private val view: PDFView,
                                private val file: File,
                                private val scale: Float,
                                private val pageSpacing: Int = 0,
                                @ColorInt private val backgroundColorPdf: Int = Color.WHITE) : ImageRegionDecoder {

    private lateinit var descriptor: ParcelFileDescriptor
    private lateinit var renderer: PdfRenderer
    private var pageWidth = 0
    private var pageHeight = 0

    @Throws(Exception::class)
    override fun init(context: Context, uri: Uri): Point {
        // Open the PDF file descriptor
        descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        // Create a PdfRenderer instance
        renderer = PdfRenderer(descriptor)
        // Open the first page to get dimensions
        val page = renderer.openPage(0)
        pageWidth = (page.width * scale).toInt()
        pageHeight = (page.height * scale).toInt()
        // Adjust view settings based on the number of pages
        if (renderer.pageCount > 15) {
            view.setHasBaseLayerTiles(false)
        } else if (renderer.pageCount == 1) {
            view.setMinimumScaleType(SCALE_TYPE_CENTER_INSIDE)
        }
        page.close()
        // Return the total dimensions including spacing
        return Point(pageWidth, pageHeight * renderer.pageCount + pageSpacing * (renderer.pageCount - 1))
    }

    override fun decodeRegion(rect: Rect, sampleSize: Int): Bitmap {
        // Calculate the start and end pages to render
        val numPageAtStart = Math.floor(rect.top.toDouble() / (pageHeight + pageSpacing)).toInt()
        val numPageAtEnd = Math.ceil(rect.bottom.toDouble() / (pageHeight + pageSpacing)).toInt() - 1
        // Create a bitmap to draw the region
        val bitmap = Bitmap.createBitmap(rect.width() / sampleSize, rect.height() / sampleSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Fill the canvas with the background color
        canvas.drawColor(backgroundColorPdf)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        // Render each page in the specified region
        for ((iteration, pageIndex) in (numPageAtStart..numPageAtEnd).withIndex()) {
            synchronized(renderer) {
                // Open the page at the current index
                val page = renderer.openPage(pageIndex)
                // Create a matrix for scaling and translating the page content
                val matrix = Matrix()
                matrix.setScale(scale / sampleSize, scale / sampleSize)
                // Calculate the translation values
                val dx = (-rect.left / sampleSize).toFloat()
                val dy = -((rect.top - (pageHeight + pageSpacing) * numPageAtStart) / sampleSize).toFloat() + ((pageHeight + pageSpacing).toFloat() / sampleSize) * iteration
                // Apply the translation to the matrix
                matrix.postTranslate(dx, dy)
                // Render the page onto the bitmap using the matrix
                page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                // Close the page to release resources
                page.close()
            }
        }
        return bitmap
    }

    override fun isReady(): Boolean {
        // Check if the decoder is ready
        return pageWidth > 0 && pageHeight > 0
    }

    override fun recycle() {
        // Close the renderer and descriptor
        renderer.close()
        descriptor.close()
        pageWidth = 0
        pageHeight = 0
    }
}