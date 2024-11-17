package losmugiwaras.a12firebaseaccess.ui.users

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import losmugiwaras.a12firebaseaccess.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class SignupActivity : AppCompatActivity() {
    var auth = FirebaseAuth.getInstance()
    var db = FirebaseFirestore.getInstance()

    private lateinit var txtRNombre: EditText
    private lateinit var txtREmail: EditText
    private lateinit var txtRContra: EditText
    private lateinit var txtRreContra: EditText
    private lateinit var txtContactName: EditText
    private lateinit var txtContactTitle: EditText

    private lateinit var btnRegistrarU: Button


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        // Inicialización de campos
        txtRNombre = findViewById(R.id.txtRNombre)
        txtREmail = findViewById(R.id.txtREmail)
        txtRContra = findViewById(R.id.txtRContra)
        txtRreContra = findViewById(R.id.txtRreContra)
        txtContactName = findViewById(R.id.txtContactName)
        txtContactTitle = findViewById(R.id.txtContactTitle)
        btnRegistrarU = findViewById(R.id.btnRegistrarU)

        btnRegistrarU.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = txtRNombre.text.toString()
        val email = txtREmail.text.toString()
        val contra = txtRContra.text.toString()
        val reContra = txtRreContra.text.toString()
        val contacto = txtContactName.text.toString()
        val ocupacion = txtContactTitle.text.toString()

        // Validación de campos vacíos
        if (nombre.isEmpty() || email.isEmpty() || contra.isEmpty() || reContra.isEmpty()) {
            Toast.makeText(this, "Favor de llenar todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificación de contraseñas
        if (contra != reContra) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, contra)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val dt = Date()

                    // Buscar en la colección "Customers" usando ContactName y ContactTitle
                    db.collection("Customers")
                        .whereEqualTo("ContactName", contacto)
                        .whereEqualTo("ContactTitle", ocupacion)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                // Si encontramos un cliente que coincida
                                val customerDoc = querySnapshot.documents[0]
                                val customerID = customerDoc.getString("CustomerID") ?: ""

                                // Si encontramos el CustomerID, proceder con el registro
                                if (customerID.isNotEmpty()) {
                                    val userData = hashMapOf(
                                        "idemp" to user?.uid,  // UID del usuario registrado
                                        "usuario" to nombre,
                                        "email" to email,
                                        "contacto" to contacto,
                                        "ocupacion" to ocupacion,
                                        "ultAcceso" to dt.toString(),
                                        "CustomerID" to customerID  // Vinculamos el CustomerID de Customers
                                    )

                                    // Guardar datos en la colección 'datosUsuarios'
                                    db.collection("datosUsuarios")
                                        .document(user?.uid ?: "")  // Usamos el UID como ID en la colección
                                        .set(userData)
                                        .addOnSuccessListener {
                                            // Guardar en SharedPreferences (opcional)
                                            val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)
                                            val editor = prefe.edit()
                                            editor.putString("email", email)
                                            editor.putString("contra", contra)
                                            editor.putString("contacto", contacto)
                                            editor.putString("ocupacion", ocupacion)
                                            editor.putString("CustomerID", customerID)  // Guardamos el CustomerID también
                                            editor.apply()

                                            Toast.makeText(
                                                this,
                                                "Usuario registrado correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Redirigir al LoginActivity después del registro
                                            val intent = Intent(this, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish() // Finaliza SignupActivity para evitar que el usuario regrese
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                this,
                                                "Error al registrar el usuario: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(this, "No se encontró un cliente válido para vincular", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Si no se encuentra un cliente que coincida
                                Toast.makeText(this, "No se encontró un cliente con esos datos", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al verificar clientes: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Error al crear el usuario: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
