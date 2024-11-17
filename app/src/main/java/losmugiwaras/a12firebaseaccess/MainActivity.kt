package losmugiwaras.a12firebaseaccess

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import losmugiwaras.a12firebaseaccess.entities.cls_Category
import CategoryAdapter
import losmugiwaras.a12firebaseaccess.comprar.CatalogoActivity
import losmugiwaras.a12firebaseaccess.ui.users.LoginActivity

const val valorIntentLogin = 1

class MainActivity : AppCompatActivity() {
    var auth = FirebaseAuth.getInstance()
    var email: String? = null
    var contra: String? = null
    var db = FirebaseFirestore.getInstance()
    var TAG = "losmugiwarasTestingApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Intentar obtener el token del usuario del local storage, sino llamar a la ventana de registro
        val prefe = getSharedPreferences("appData", MODE_PRIVATE)
        email = prefe.getString("email", "")
        contra = prefe.getString("contra", "")

        if (email.toString().trim { it <= ' ' }.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, valorIntentLogin)
        } else {
            val uid: String = auth.uid.toString()
            if (uid == "null") {
                auth.signInWithEmailAndPassword(email.toString(), contra.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Autenticación correcta", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            obtenerDatos()
        }

        // Configurar el botón para redirigir a CatalogActivity
        val btnComprar: Button = findViewById(R.id.comprar)
        btnComprar.setOnClickListener {
            val catalogIntent = Intent(this, CatalogoActivity::class.java)
            startActivity(catalogIntent)
        }
    }

    private fun obtenerDatos() {
        // Método para obtener datos
        val coleccion: ArrayList<cls_Category?> = ArrayList()
        val listaView: ListView = findViewById(R.id.lstCategories)
        db.collection("Categories").orderBy("CategoryID")
            .get()
            .addOnCompleteListener { docc ->
                if (docc.isSuccessful) {
                    for (document in docc.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        val datos = cls_Category(
                            document.data["CategoryID"].toString().toInt(),
                            document.data["CategoryName"].toString(),
                            document.data["Description"].toString(),
                            document.data["urlImage"].toString()
                        )
                        coleccion.add(datos)
                    }
                    val adapter = CategoryAdapter(this, coleccion)
                    listaView.adapter = adapter
                } else {
                    Log.w(TAG, "Error obteniendo documentos.", docc.exception)
                }
            }
    }
}
