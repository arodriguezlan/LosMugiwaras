package losmugiwaras.a12firebaseaccess.comprar.elementos

data class OrderDetail(
    val id: Int,
    val productId: Int,
    val discount: Double,
    val quantity: Int,
    val subtotal: Double
)