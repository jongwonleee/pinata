package capstone.aiimageeditor.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import capstone.aiimageeditor.R
import capstone.aiimageeditor.customviews.RoundedImageView
import com.bumptech.glide.Glide
import com.itaewonproject.adapter.BaseViewHolder

class AdapterImageList(val context: Context, var images: MutableList<String>) : RecyclerView.Adapter<AdapterImageList.ViewHolder>() {
    private lateinit var listener: OnItemClickListener

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size+1
    }
    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private var img: RoundedImageView = itemView.findViewById(R.id.imageView) as RoundedImageView

        override fun bind(pos: Int) {
            img.setOnClickListener { listener.onClick(pos) }
            if(pos==0) img.setImageResource(R.drawable.ic_add)
            else
                Glide.with(itemView)
                    .load(Uri.parse(images[pos-1]))
                    .into(img)
        }
    }
}
