package ddwu.com.mobile.finalproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ddwu.com.mobile.finalproject.data.MusicSpot
import ddwu.com.mobile.finalproject.databinding.SpotItemBinding // XML 이름이 item_spot이면 보통 ItemSpotBinding이지만, 현재 프로젝트 설정에 맞춰 유지합니다.

/* 데이터를 리스트 아이템 레이아웃에 연결합니다. */
class SpotAdapter : RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {

    var spots: List<MusicSpot>? = null

    override fun getItemCount(): Int = spots?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val binding = SpotItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val currentSpot = spots?.get(position)


        holder.binding.tvItmDate.text = currentSpot?.saveDate ?: ""

        holder.binding.tvItmSongTitle.text = currentSpot?.songTitle

        holder.binding.tvItmArtist.text = currentSpot?.artistName

        if (currentSpot?.memo.isNullOrBlank()) {
            holder.binding.tvItmMemo.text = ""
        } else {
            holder.binding.tvItmMemo.text = "\"${currentSpot?.memo}\""
        }


        Glide.with(holder.itemView.context)
            .load(currentSpot?.albumUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.ivItmAlbumArt)


        holder.binding.clItem.setOnClickListener {
            onItemClickListener?.onItemClick(currentSpot?.id ?: 0)
        }
    }

    class SpotViewHolder(val binding: SpotItemBinding) : RecyclerView.ViewHolder(binding.root)


    interface OnItemClickListener {
        fun onItemClick(id: Int)
    }
    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }
}