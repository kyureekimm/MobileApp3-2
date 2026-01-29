package ddwu.com.mobile.naversearchtest.ui
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ddwu.com.mobile.naversearchtest.data.network.Book
import ddwu.com.mobile.naversearchtest.databinding.ListItemBinding

/*사용하는 DTO에 맞게 수정 필요*/
class BookAdapter : RecyclerView.Adapter<BookAdapter.ItemHolder>(){

    var books: List<Book>? = null

    override fun getItemCount(): Int = books?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.itemBinding.tvItem.text = (books?.get(position) as Book).toString()
    }

    class ItemHolder(val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

}