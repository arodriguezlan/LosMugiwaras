import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import losmugiwaras.a12firebaseaccess.R
import com.google.firebase.storage.FirebaseStorage
import losmugiwaras.a12firebaseaccess.entities.cls_Category

class CategoryAdapter(
    context: Context, dataModalArrayList: ArrayList<cls_Category?>?
) : ArrayAdapter<cls_Category?>(context, 0, dataModalArrayList!!) {

    var imgs = FirebaseStorage.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listitemView = convertView
        if (listitemView == null) {
            listitemView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
        }

        val dataModal: cls_Category? = getItem(position)

        val categoryID = listitemView!!.findViewById<TextView>(R.id.IdCategory)
        val categoryName = listitemView!!.findViewById<TextView>(R.id.NameCategory)
        val description = listitemView.findViewById<TextView>(R.id.DescriptionCategory)

        val imageCategory = listitemView.findViewById<ImageView>(R.id.imgCategory)

        if (dataModal != null) {
            categoryID.text = dataModal.CategoryID.toString()
            categoryName.text = dataModal.CategoryName
            description.text = dataModal.Description
            Glide.with(context).load(dataModal.urlImage).into(imageCategory)
        }

        // Acción al hacer clic en un ítem de la lista
        listitemView.setOnClickListener {
            // Cerrar sesión de Firebase
            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            // Redirigir al LoginActivity


            // Mostrar un mensaje al cerrar sesión
            Toast.makeText(context, "Sesión cerrada. Redirigiendo al login...", Toast.LENGTH_SHORT).show()

            // Si es necesario, finalizar la actividad actual para que el usuario no regrese a ella
            if (context is Activity) {
                (context as Activity).finish()
            }
        }

        return listitemView
    }
}



