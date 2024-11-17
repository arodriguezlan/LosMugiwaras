package losmugiwaras.a12firebaseaccess.comprar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import losmugiwaras.a12firebaseaccess.R
import losmugiwaras.a12firebaseaccess.comprar.elementos.Products

class ProductAdapter(
    private val context: Context,
    private val productList: MutableList<Products>,
    private val onClick: (Products) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(), Filterable {

    private var productListFiltered = productList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productListFiltered[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        return productListFiltered.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    productList
                } else {
                    val query = constraint.toString().lowercase()
                    productList.filter {
                        it.ProductName.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                productListFiltered = results?.values as MutableList<Products>
                notifyDataSetChanged()
            }
        }
    }

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val productNameTextView: TextView = view.findViewById(R.id.productName)
        private val productPriceTextView: TextView = view.findViewById(R.id.productPrice)
        private val productStockTextView: TextView = view.findViewById(R.id.productStock)

        fun bind(product: Products) {
            productNameTextView.text = product.ProductName
            productPriceTextView.text = "Precio: \$${product.UnitPrice}"
            productStockTextView.text = "En stock: ${product.UnitsInStock}"
            itemView.setOnClickListener {
                onClick(product)
            }
        }
    }
}
