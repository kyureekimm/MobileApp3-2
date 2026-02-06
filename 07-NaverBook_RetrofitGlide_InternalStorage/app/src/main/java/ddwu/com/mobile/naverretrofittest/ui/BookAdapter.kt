package ddwu.com.mobile.naverretrofittest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ddwu.com.mobile.naverretrofittest.data.Book
import ddwu.com.mobile.naverretrofittest.databinding.ListItemBinding

class BookAdapter : RecyclerView.Adapter<BookAdapter.ItemHolder>(){

    var items: List<Book>? = null

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.itemBinding.tvItem.text = (items?.get(position) as Book).toString()
        holder.itemBinding.clItem.setOnClickListener{   // 항목 클릭 이벤트 처리
            clickListener?.onItemClick(it, position)
        }
    }

    class ItemHolder(val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)


    /*adapter 항목 클릭 이벤트 처리를 위한 인터페이스 및 필요함수 정의*/
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    var clickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

}