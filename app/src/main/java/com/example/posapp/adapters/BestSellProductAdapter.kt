package com.example.posapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.databinding.BestSellProductItemBinding
import com.example.posapp.models.OrderDetail
import java.text.NumberFormat
import java.util.Locale

class BestSellProductAdapter(
    private val context: Context,
    private var productList: List<OrderDetail>
) : RecyclerView.Adapter<BestSellProductAdapter.BestSellProductViewHolder>() {

    private val databases : Databases = Databases(context)

    class BestSellProductViewHolder(private val binding: BestSellProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val productImg = binding.bestSellProductImg
        val productName = binding.bestSellProductName
        val revenue = binding.bestSellRevenue
        val countQuantity = binding.bestSellQuantity
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BestSellProductAdapter.BestSellProductViewHolder {
        val binding = BestSellProductItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return BestSellProductViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BestSellProductAdapter.BestSellProductViewHolder,
        position: Int
    ) {
        val item = productList[position]

        val getProduct = databases.getProductByID(item.productId)
        val bitmap = BitmapFactory.decodeFile(getProduct.imageResId)
        holder.productImg.setImageBitmap(bitmap)

        holder.productName.text = getProduct.name
        holder.revenue.text = "Sales revenue: " + formatCurrency(item.subTotal)
        holder.countQuantity.text = "Sold: " + item.quantity
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    // Hàm định dạng giá tiền
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }
}