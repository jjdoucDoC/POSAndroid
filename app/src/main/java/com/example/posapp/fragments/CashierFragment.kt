package com.example.posapp.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.posapp.Databases
import com.example.posapp.adapters.ProductGridAdapter
import com.example.posapp.adapters.ProductListAdapter
import com.example.posapp.models.Products
import com.example.posapp.R
import com.example.posapp.activities.OrderActivity
import com.example.posapp.adapters.CartAdapter
import com.example.posapp.databinding.FragmentCashierBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.util.Locale

class CashierFragment : Fragment() {

    private var isGridView = true
    private lateinit var productList: List<Products>    // Item Product
    private lateinit var filteredList: List<Products>   // Item Product Search
    private lateinit var cartList: MutableMap<Products, Int>    // Item Cart with Key(Product) - Value(Int)
    private lateinit var databases: Databases

    private val REQUEST_CODE_PLACE_ORDER = 1    // request code for clear cart

    // Declare binding
    private var _binding: FragmentCashierBinding? = null    // lưu trữ binding và giải phóng khi view bị hủy
    private val binding get() = _binding!!      // đảm bảo luôn sử dụng binding không null (!!)

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
        filteredList = productList

        // Default Cart container is hide
        binding.cartContainer.visibility = View.GONE

        updateView()
        setUpSearchProduct()

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

        binding.addOrderBtn.setOnClickListener {
            val intent = Intent(requireContext(), OrderActivity::class.java)
            intent.putExtra("cartList", HashMap(cartList))
            startActivityForResult(intent, REQUEST_CODE_PLACE_ORDER)
        }
    }

    // Switch between GridView and ListView Product function
    private fun updateView() {
        val recyclerView = binding.productListContainer
        if (isGridView) {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.adapter = ProductGridAdapter(requireContext(), filteredList) { product, view ->
                addToCartAnimation(view)
                addToCart(product)
            }
        } else {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ProductListAdapter(requireContext(), filteredList) { product, view ->
                addToCartAnimation(view)
                addToCart(product)
            }
        }
    }

    // Add to cart handle event
    private fun addToCart (product: Products) {
        // Check if product is exists in cart
        if (cartList.containsKey(product)) {
            cartList[product] = cartList[product]!! + 1 // If is exists, increase quantity of product by 1
        } else {
            cartList[product] = 1 // If not exists, set default quantity to 1
        }
        updateCart()
    }

    // Update Cart Function
    @SuppressLint("SetTextI18n")
    private fun updateCart (bottomSheetDialog: BottomSheetDialog? = null) {
        val totalItems = cartList.values.sum()
        val totalPrice = cartList.entries.sumOf {
            it.key.price * it.value
        }

        binding.countProductOrder.text = "$totalItems items"
        binding.totalPriceOrder.text = "Total: ${formatCurrency(totalPrice)}"

        // Update BottomSheetDialog height
        bottomSheetDialog?.let {
            val scrollView = it.findViewById<NestedScrollView>(R.id.scrollView)
            val maxItemCount = 3
            val itemHeight = resources.getDimensionPixelSize(R.dimen.cart_item_height)
            val maxHeight = maxItemCount * itemHeight

            scrollView?.layoutParams?.height =
                if (cartList.size <= maxItemCount) ViewGroup.LayoutParams.WRAP_CONTENT else maxHeight
            scrollView?.requestLayout()
        }

        // Show Cart container when cart is not empty
        binding.cartContainer.visibility =
            if (cartList.isNotEmpty()) View.VISIBLE else View.GONE
    }

    // Format Currency Function
    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "đ ${formatter.format(amount)}"
    }

    // Show bottom cart sheet
    @SuppressLint("NotifyDataSetChanged", "InflateParams")
    private fun showBottomCartSheet() {
        // Create a BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_cart, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up RecyclerView to show the list of cart items
        val cartRecyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.cart_list)
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())  // Set layout for list
        cartRecyclerView.adapter = CartAdapter(requireContext(), cartList, { product, quantity ->
            // Callback to update the quantity
            if (quantity == 0) {
                // Remove product if quantity = 0
                cartList.remove(product)
                cartRecyclerView.adapter?.notifyDataSetChanged()    // Refresh RecyclerView
                if (cartList.isEmpty()) {
                    bottomSheetDialog.dismiss()
                    binding.cartContainer.visibility = View.GONE
                }
                updateCart(bottomSheetDialog)
            } else {
                // Update cart with new quantiy
                cartList[product] = quantity
                updateCart(bottomSheetDialog)
            }
        })

        // Clear Sheet Cart
        val clearSheetCartButton = bottomSheetView.findViewById<ImageView>(R.id.clear_sheet_cart_btn)
        clearSheetCartButton.setOnClickListener {
            showClearSheetDialog(
                "Warning",
                "Are you sure you want to delete this cart?",
                bottomSheetDialog,
                cartRecyclerView
            )
        }

        updateCart(bottomSheetDialog)
        bottomSheetDialog.show()
    }

    // Show Alert Dialog to clear the cart
    private fun showClearSheetDialog(
        title: String,
        message: String,
        bottomSheetDialog: BottomSheetDialog,
        cartRecyclerView: RecyclerView
    ) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.alert_dialog_delete, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
        val yesButton = dialogView.findViewById<Button>(R.id.yes_button)
        val noButton = dialogView.findViewById<Button>(R.id.no_button)

        dialogTitle.text = title
        dialogMessage.text = message

        val customDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Handle the Yes button click
        yesButton.setOnClickListener {
            cartList.clear()
            cartRecyclerView.adapter?.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
            updateCart(bottomSheetDialog)
            customDialog.dismiss()  // Close the dialog after action
        }

        // Handle the No button click
        noButton.setOnClickListener {
            customDialog.dismiss()  // Close the dialog if No is clicked
        }

        customDialog.show()  // Show the dialog
    }

    // Add to cart animation function
    private fun addToCartAnimation(startView: View) {
        startView.post {
            val startLoc = IntArray(2)
            startView.getLocationOnScreen(startLoc)

            val finalLoc = IntArray(2)
            binding.cartIcon.getLocationOnScreen(finalLoc)

            val startX = startLoc[0] + startView.width - 80
            val startY = startLoc[1] + startView.height - 250
            val finalX = finalLoc[0] + binding.cartIcon.width / 2
            val finalY = finalLoc[1] + binding.cartIcon.height / 2

            val dotView = ImageView(requireContext())
            dotView.setImageResource(R.drawable.dot)
            val params = RelativeLayout.LayoutParams(65, 65)
            binding.root.addView(dotView, params)

            val anim = ValueAnimator.ofFloat(0f, 1f)
            anim.addUpdateListener {
                val fraction = it.animatedFraction

                val currentX = startX + fraction * (finalX - startX)
                val currentY = startY + fraction * (finalY - startY) - (570 * (1 - 4 * (fraction - 0.5f) * (fraction - 0.5f)))

                dotView.translationX = currentX
                dotView.translationY = currentY
            }

            anim.duration = 660

            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.root.removeView(dotView)
                }
            })

            anim.start()
        }
    }

    // Search Product function
    private fun setUpSearchProduct() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase(Locale.getDefault())

                filteredList = databases.getProduct().filter {
                    it.name.lowercase(Locale.getDefault()).contains(query) ||
                            it.id.toString().contains(query)
                }
                updateView()

                if(query.isNotEmpty()) {
                    binding.searchBtn.setImageResource(R.drawable.baseline_close_24)
                    binding.searchBtn.setOnClickListener {
                        binding.searchInput.setText("")
                    }
                } else {
                    binding.searchBtn.setImageResource(R.drawable.search_ic)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Giải phóng binding khi fragment bị hủy
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PLACE_ORDER && resultCode == Activity.RESULT_OK) {
            val clearCart = data?.getBooleanExtra("clearCart", false) ?: false
            if (clearCart) {
                cartList.clear()
                updateCart()
            }
        }
    }
}