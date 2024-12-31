package com.example.posapp.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.adapters.ProductGridAdapter
import com.example.posapp.adapters.ProductListAdapter
import com.example.posapp.models.Products
import com.example.posapp.R
import com.example.posapp.adapters.CartAdapter
import com.example.posapp.databinding.FragmentCashierBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class CashierFragment : Fragment() {

    private var isGridView = true
    private lateinit var productList: List<Products>
    private lateinit var cartList: MutableMap<Products, Int>    // Lưu sản phẩm và số lượng
    private lateinit var databases: Databases

    // Declare binding
    private var _binding: FragmentCashierBinding? = null    // biến _binding để lưu trữ binding và giải phóng nó khi view bị hủy
    private val binding get() = _binding!!      // biến binding đảm bảo luôn sử dụng binding không null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialization binding
        _binding = FragmentCashierBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databases = Databases(requireContext())
        productList = databases.getProduct()
        cartList = mutableMapOf()

        // Default Cart container is hide
        binding.cartContainer.visibility = View.GONE

        updateView()

        // Toggle Product View Button
        binding.toggleViewButton.setOnClickListener {
            isGridView = !isGridView
            binding.toggleViewButton.setImageResource(
                if (isGridView) {
                    R.drawable.list_ic
                } else {
                    R.drawable.grid_ic
                }
            )
            updateView()
        }

        binding.cartContainer.setOnClickListener {
            showBottomCartSheet()
        }

    }

    // Switch between GridView and ListView Product function
    private fun updateView() {
        val recyclerView = binding.productListContainer
        if (isGridView) {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.adapter = ProductGridAdapter(requireContext(), productList) {
                product, view ->
                addToCartAnimation(view)
                addToCart(product)
            }
        } else {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ProductListAdapter(requireContext(), productList) {
                product, view ->
                addToCartAnimation(view)
                addToCart(product)
            }
        }
    }

    // Add to cart handle event
    private fun addToCart (product: Products) {
        if (cartList.containsKey(product)) {
            cartList[product] = cartList[product]!! + 1
        } else {
            cartList[product] = 1
        }

        cartContainerVisible()
    }

    // Show Cart container when Cart is not empty
    private fun cartContainerVisible() {
        binding.cartContainer.visibility =
            if (cartList.isNotEmpty()) View.VISIBLE else View.GONE
    }

    // Show bottom cart sheet
    @SuppressLint("NotifyDataSetChanged")
    private fun showBottomCartSheet() {
        // Create a BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_cart, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up RecyclerView to show the list of cart items
        val cartRecyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.order_cart_list)
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())  // Set layout for list

        cartRecyclerView.adapter = CartAdapter(requireContext(), cartList, {
            // Callback to update the quantity
            product, quantity ->
            if (quantity == 0) {
                // Remove product if quantity = 0
                cartList.remove(product)
                cartRecyclerView.adapter?.notifyDataSetChanged()    // Refresh RecyclerView
                if (cartList.isEmpty()) {
                    bottomSheetDialog.dismiss()
                    binding.cartContainer.visibility = View.GONE
                }
            } else {
                // Update cart with new quantiy
                cartList[product] = quantity
            }
        }, { product ->
            // Remove product from cart when clicked
            cartList.remove(product)
            cartRecyclerView.adapter?.notifyDataSetChanged()
            if (cartList.isEmpty()) {
                bottomSheetDialog.dismiss()
                binding.cartContainer.visibility = View.GONE
            }
        })

        // Set up NestedScrollView
        val scrollView = bottomSheetView.findViewById<NestedScrollView>(R.id.scrollView)
        val maxItemCount = 3
        val itemHeight = 300
        val maxHeight = maxItemCount * itemHeight

        // Adjust ScrollView height
        scrollView.layoutParams.height =
            if (cartList.size <= maxItemCount) ViewGroup.LayoutParams.WRAP_CONTENT else maxHeight

        bottomSheetDialog.show()
    }

    // Add to cart animation function
    private fun addToCartAnimation(startView: View) {
        // Get starting position
        val startLoc = IntArray(2)
        startView.getLocationOnScreen(startLoc)

        // Get final postion
        val finalLoc = IntArray(2)
        binding.cartIcon.getLocationOnScreen(finalLoc)

        val startX = startLoc[0] + startView.width - 80
        val startY = startLoc[1] + startView.height - 250
        val finalX = finalLoc[0] + binding.cartIcon.width / 2
        val finalY = finalLoc[1] + binding.cartIcon.height / 2

        // Create dot view
        val dotView = ImageView(requireContext())
        dotView.setImageResource(R.drawable.dot)
        val params = RelativeLayout.LayoutParams(65, 65)
        binding.root.addView(dotView, params)

        // Animate dot with a parabolic path
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.addUpdateListener {
            val fraction = it.animatedFraction

            // X increases linearly from startX to finalX
            val currentX = startX + fraction * (finalX - startX)
            // Y follows a parabolic curve
            val currentY = startY + fraction * (finalY - startY) - (600 * (1 - 4 * (fraction - 0.5f) * (fraction - 0.5f)))

            dotView.translationX = currentX
            dotView.translationY = currentY
        }

        anim.duration = 600

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                binding.root.removeView(dotView) // Remove dot after animation
            }
        })

        anim.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Giải phóng binding khi fragment bị hủy
        _binding = null
    }

}