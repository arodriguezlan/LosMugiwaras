package losmugiwaras.a12firebaseaccess.comprar.elementos

data class Products(
    val ProductID: Int = 0,
    val ProductName: String = "",
    val UnitPrice: Double = 0.0,
    val QuantityPerUnit: Int? = null,
    val UnitsInStock: Int? = null,
    val UnitsOnOrder: Int? = null,
    val ReorderLevel: Int? = null,
    val Discontinued: Int? = null,
    val SupplierID: Int? = null,
    val CategoryID: Int? = null,
)

