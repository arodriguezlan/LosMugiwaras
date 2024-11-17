package losmugiwaras.a12firebaseaccess.comprar

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import losmugiwaras.a12firebaseaccess.R
import losmugiwaras.a12firebaseaccess.comprar.elementos.Products

class CatalogoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var productAdapter: ProductAdapter
    private var productList: MutableList<Products> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(this, productList) { product ->
            // Acción al hacer clic en un producto
            val intent = Intent(this, Compra::class.java)
            intent.putExtra("ProductID", product.ProductID)
            intent.putExtra("ProductName", product.ProductName)
            intent.putExtra("UnitPrice", product.UnitPrice)
            startActivity(intent)
        }
        recyclerView.adapter = productAdapter

        // Configurar la búsqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productAdapter.filter.filter(newText)
                return true
            }
        })

        // Inicializar Firestore
        val db = FirebaseFirestore.getInstance()

        // Recuperar productos desde Firestore
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()  // Limpiar la lista antes de agregar nuevos datos
                for (document in result) {
                    val product = document.toObject(Products::class.java)
                    productList.add(product)
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los productos", Toast.LENGTH_SHORT).show()
            }
    }
}
