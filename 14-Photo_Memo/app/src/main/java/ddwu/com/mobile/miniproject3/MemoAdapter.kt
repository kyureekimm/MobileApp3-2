package ddwu.com.mobile.miniproject3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
import ddwu.com.mobile.miniproject3.data.Memo
import ddwu.com.mobile.miniproject3.databinding.MemoItemBinding
import java.io.File


class MemoAdapter() : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    var memos: List<Memo>? = null

    override fun getItemCount() = memos?.size ?: 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MemoViewHolder {
        val memoBinding = MemoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(memoBinding)
    }

    override fun onBindViewHolder(
        holder: MemoViewHolder,
        position: Int
    ) {

        holder.memoItemBinding.tvTitle.text = memos?.get(position)?.title ?: ""

        Glide.with(holder.memoItemBinding.clItem.context)
            .load(File(memos?.get(position)?.imagePath ?: ""))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .thumbnail(0.1f)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(holder.memoItemBinding.ivMemo)

        holder.memoItemBinding.clItem.setOnClickListener{   // 항목 클릭 이벤트 처리
            clickListener?.onItemClick(it, position)
        }
    }

    class MemoViewHolder(val memoItemBinding: MemoItemBinding) : RecyclerView.ViewHolder(memoItemBinding.root)

    /*adapter 항목 클릭 이벤트 처리를 위한 인터페이스 및 필요함수 정의*/
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    var clickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

}
