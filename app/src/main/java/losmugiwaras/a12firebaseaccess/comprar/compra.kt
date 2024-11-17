package losmugiwaras.a12firebaseaccess.comprar

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import losmugiwaras.a12firebaseaccess.R

class Compra : AppCompatActivity() {

    private lateinit var productIdTextView: TextView
    private lateinit var productNameTextView: TextView
    private lateinit var productPriceTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compra)

        // Inicializar vistas
        productIdTextView = findViewById(R.id.ProductIdTextView)
        productNameTextView = findViewById(R.id.productNameTextView)
        productPriceTextView = findViewById(R.id.productPriceTextView)

        // Obtener datos del Intent
        val productId = intent.getStringExtra("ProductID") ?: "ID no disponible"
        val productName = intent.getStringExtra("ProductName") ?: "Nombre no disponible"
        val productPrice = intent.getDoubleExtra("UnitPrice", 0.0)

        // Mostrar datos en la interfaz
        productIdTextView.text = productId
        productNameTextView.text = productName
        productPriceTextView.text = String.format("$%.2f", productPrice)

        // Acciones adicionales como botones de confirmación o cancelación
        // Implementa la lógica necesaria aquí si corresponde
    }
}
