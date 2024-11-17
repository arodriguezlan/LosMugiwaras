package losmugiwaras.a12firebaseaccess.comprar

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.android.gms.analytics.ecommerce.Product
import losmugiwaras.a12firebaseaccess.comprar.elementos.OrderDetail
import losmugiwaras.a12firebaseaccess.comprar.elementos.Products


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "purchase_database.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_PRODUCTS = "products"
        private const val TABLE_ORDERDETAILS = "orderdetails"

        // Productos
        private const val COLUMN_PRODUCT_ID = "id"
        private const val COLUMN_PRODUCT_NAME = "name"
        private const val COLUMN_PRODUCT_PRICE = "price"
        private const val COLUMN_PRODUCT_QUANTITY = "quantity"

        // Detalles de la orden
        private const val COLUMN_ORDERDETAIL_ID = "id"
        private const val COLUMN_ORDERDETAIL_PRODUCT_ID = "product_id"
        private const val COLUMN_ORDERDETAIL_DISCOUNT = "discount"
        private const val COLUMN_ORDERDETAIL_QUANTITY = "quantity"
        private const val COLUMN_ORDERDETAIL_SUBTOTAL = "subtotal"
    }

    // Crear la tabla de productos y detalles de la orden
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PRODUCTS_TABLE = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_PRODUCT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PRODUCT_NAME TEXT,
                $COLUMN_PRODUCT_PRICE REAL,
                $COLUMN_PRODUCT_QUANTITY INTEGER
            )
        """.trimIndent()

        val CREATE_ORDERDETAILS_TABLE = """
            CREATE TABLE $TABLE_ORDERDETAILS (
                $COLUMN_ORDERDETAIL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ORDERDETAIL_PRODUCT_ID INTEGER,
                $COLUMN_ORDERDETAIL_DISCOUNT REAL,
                $COLUMN_ORDERDETAIL_QUANTITY INTEGER,
                $COLUMN_ORDERDETAIL_SUBTOTAL REAL,
                FOREIGN KEY ($COLUMN_ORDERDETAIL_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PRODUCT_ID)
            )
        """.trimIndent()

        try {
            db.execSQL(CREATE_PRODUCTS_TABLE)
            db.execSQL(CREATE_ORDERDETAILS_TABLE)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al crear las tablas: ${e.message}")
        }
    }

    // Actualizar la base de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERDETAILS")
            onCreate(db)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al actualizar la base de datos: ${e.message}")
        }
    }

    // Insertar un producto
    fun addProduct(product: Products): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_NAME, product.ProductName)
            put(COLUMN_PRODUCT_PRICE, product.UnitPrice)
            put(COLUMN_PRODUCT_QUANTITY, product.ProductID)
        }

        var result: Long = -1
        try {
            result = db.insert(TABLE_PRODUCTS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al insertar el producto: ${e.message}")
        } finally {
            db.close()
        }
        return result
    }

    // Insertar detalles de la orden
    fun addOrderDetail(orderDetail: OrderDetail): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ORDERDETAIL_PRODUCT_ID, orderDetail.productId)
            put(COLUMN_ORDERDETAIL_DISCOUNT, orderDetail.discount)
            put(COLUMN_ORDERDETAIL_QUANTITY, orderDetail.quantity)
            put(COLUMN_ORDERDETAIL_SUBTOTAL, orderDetail.subtotal)
        }

        var result: Long = -1
        try {
            result = db.insert(TABLE_ORDERDETAILS, null, values)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al insertar los detalles de la orden: ${e.message}")
        } finally {
            db.close()
        }
        return result
    }

    // Obtener todos los productos almacenados
    @SuppressLint("Range")
    fun getAllProducts(): MutableList<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null)

        try {
            if (cursor.moveToFirst()) {
                do {
                    val ProductID = cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_ID))
                    val ProductName = cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_NAME))
                    val UnitPrice = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRODUCT_PRICE))
                    val quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_QUANTITY))

                    products.add(Products(ProductID, ProductName, UnitPrice, quantity))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener productos: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }
        return products
    }

    // Obtener los detalles de la orden
    @SuppressLint("Range")
    fun getOrderDetails(): List<OrderDetail> {
        val orderDetails = mutableListOf<OrderDetail>()
        val db = readableDatabase
        val cursor = db.query(TABLE_ORDERDETAILS, null, null, null, null, null, null)

        try {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDERDETAIL_ID))
                    val productId = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDERDETAIL_PRODUCT_ID))
                    val discount = cursor.getDouble(cursor.getColumnIndex(COLUMN_ORDERDETAIL_DISCOUNT))
                    val quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_ORDERDETAIL_QUANTITY))
                    val subtotal = cursor.getDouble(cursor.getColumnIndex(COLUMN_ORDERDETAIL_SUBTOTAL))

                    orderDetails.add(OrderDetail(id, productId, discount, quantity, subtotal))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener los detalles de la orden: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }
        return orderDetails
    }
}

private fun <E> MutableList<E>.add(element: Products) {

}
