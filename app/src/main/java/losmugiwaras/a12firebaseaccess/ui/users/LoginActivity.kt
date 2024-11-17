package losmugiwaras.a12firebaseaccess.ui.users

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import losmugiwaras.a12firebaseaccess.MainActivity
import losmugiwaras.a12firebaseaccess.R
import java.util.Date

class LoginActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()

    private lateinit var btnAutenticar: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var txtRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialización de vistas
        btnAutenticar = findViewById(R.id.btnAutenticar)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtContra)
        txtRegister = findViewById(R.id.txtRegister)

        // Acción para ir al registro de usuario
        txtRegister.setOnClickListener {
            goToSignup()
        }

        // Acción de autenticación
        btnAutenticar.setOnClickListener {
            if (txtEmail.text.isNotEmpty() && txtContra.text.isNotEmpty()) {
                authenticateUser(txtEmail.text.toString(), txtContra.text.toString())
            } else {
                showAlert("Error", "El correo electrónico y la contraseña no pueden estar vacíos")
            }
        }
    }

    private fun authenticateUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid.orEmpty()
                    if (uid.isNotEmpty()) {
                        fetchCustomerID(uid)
                    } else {
                        showToast("No se pudo obtener el UID del usuario.")
                    }

                    // Actualizar el último acceso
                    updateLastAccess(uid)

                    // Registrar los datos de login en SharedPreferences para persistirlos
                    val prefe = this.getSharedPreferences("appData", Context.MODE_PRIVATE)
                    val editor = prefe.edit()
                    editor.putString("email", email)
                    editor.putString("contra", password)
                    editor.apply()

                    // Navegar a la pantalla principal
                    navigateToMain()

                } else {
                    showAlert("Error", "Error al autenticar el usuario: ${it.exception?.localizedMessage}")
                }
            }
    }

    private fun fetchCustomerID(uid: String) {
        db.collection("datosUsuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val customerID = document.getString("CustomerID").orEmpty()
                    if (customerID.isNotEmpty()) {
                        fetchOrdersByCustomerID(uid, customerID)
                    } else {
                        showToast("No se encontró un CustomerID para este usuario.")
                    }
                } else {
                    showToast("No se encontraron datos asociados a este usuario.")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al recuperar los datos del usuario: ${e.message}")
            }
    }

    private fun fetchOrdersByCustomerID(uid: String, customerID: String) {
        db.collection("Orders")
            .whereEqualTo("CustomerID", customerID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val ordersList = mutableListOf<Map<String, Any>>()
                    for (document in querySnapshot.documents) {
                        val orderData = mapOf(
                            "orderId" to document.id,
                            "ShipName" to document.getString("ShipName").orEmpty(),
                            "ShipAddress" to document.getString("ShipAddress").orEmpty(),
                            "ShipCity" to document.getString("ShipCity").orEmpty(),
                            "ShipCountry" to document.getString("ShipCountry").orEmpty()
                        )
                        ordersList.add(orderData)
                    }
                    checkIfOrdersExist(uid, ordersList)
                } else {
                    showToast("No se encontraron órdenes para el CustomerID: $customerID.")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al recuperar las órdenes: ${e.message}")
            }
    }

    private fun checkIfOrdersExist(uid: String, ordersList: List<Map<String, Any>>) {
        val userRef = db.collection("datosUsuarios").document(uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val existingOrders = document.get("orders") as? List<Map<String, Any>>?
                    if (existingOrders.isNullOrEmpty()) {
                        updateUserWithOrders(uid, ordersList)
                    } else {
                        showToast("Las órdenes ya están registradas para este usuario.")
                    }
                } else {
                    showToast("No se encontraron datos para el usuario.")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al verificar las órdenes: ${e.message}")
            }
    }

    private fun updateUserWithOrders(uid: String, ordersList: List<Map<String, Any>>) {
        val userRef = db.collection("datosUsuarios").document(uid)
        userRef.update("orders", ordersList)
            .addOnSuccessListener {
                showToast("Órdenes actualizadas correctamente.")
                navigateToMain()
            }
            .addOnFailureListener { e ->
                showToast("Error al actualizar las órdenes: ${e.message}")
            }
    }

    private fun updateLastAccess(uid: String) {
        val dt: Date = Date()
        val user = hashMapOf("ultAcceso" to dt.toString())

        db.collection("datosUsuarios")
            .whereEqualTo("idemp", uid)
            .get()
            .addOnSuccessListener { documentReference ->
                documentReference.forEach { document ->
                    db.collection("datosUsuarios").document(document.id)
                        .update(user as Map<String, Any>)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar los datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMain() {
        Log.d("LoginActivity", "Navegando a MainActivity...")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Finaliza la actividad de login para no volver atrás
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAlert(titu: String, mssg: String) {
        val diagMessage = AlertDialog.Builder(this)
        diagMessage.setTitle(titu)
        diagMessage.setMessage(mssg)
        diagMessage.setPositiveButton("Aceptar", null)

        val diagVentana: AlertDialog = diagMessage.create()
        diagVentana.show()
    }

    private fun goToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivityForResult(intent, 1)  // 1 es un valor de resultado para identificar esta acción
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}