package capstone.aiimageeditor.customviews

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import capstone.aiimageeditor.R

class RoundedImageView: androidx.appcompat.widget.AppCompatImageView {
    private var imageRadius =0f

    private var isCircular = false


    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr){
        initView(context,attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?):super(context, attrs){
        initView(context,attrs)

    }

    private fun initView(context: Context?, attrs: AttributeSet?){
        val a = context!!.obtainStyledAttributes(attrs,
            R.styleable.RoundedImageView
        )
        imageRadius = a.getDimension(R.styleable.RoundedImageView_imageRadius,0f)
        isCircular = a.getBoolean(R.styleable.RoundedImageView_isCircular,false)
        setRounds()
    }
    private fun setRounds(){
        val drawable:Drawable
        if(isCircular){
            drawable = ShapeDrawable(OvalShape())
        }else
        {
            drawable = GradientDrawable()
            drawable.setColor(Color.TRANSPARENT)
            drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            drawable.shape = GradientDrawable.RECTANGLE
            drawable.cornerRadius = imageRadius
        }
        background = drawable
        clipToOutline = true
    }
}