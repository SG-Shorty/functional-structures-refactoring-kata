import Models._
import cats.implicits._

object App {

  def applyDiscount(cartId: CartId, storage: Storage[Cart]): Unit = {
    val cart: Option[Cart] = loadCart(cartId)
    val rule: Option[DiscountRule] = cart.flatMap(c => lookupCustomerDiscountRule(c.customerId))
    val discount: Option[Double] = rule.ap(cart)
    val updatedCart: Option[Cart] = cart.flatMap(c => discount.map(d => updateAmount(c, d)))
    updatedCart.map(uc => save(uc, storage))
  }

  def loadCart(id: CartId): Option[Cart] =
    if (id.value.contains("gold")) Some(Cart(id, CustomerId("gold-customer"), 100))
    else if (id.value.contains("normal")) Some(Cart(id, CustomerId("normal-customer"), 100))
    else None

  def lookupCustomerDiscountRule(id: CustomerId): Option[DiscountRule] =
    if (id.value.contains("gold")) Some(DiscountRule(half))
    else None

  def half(cart: Cart): Double =
    cart.amount / 2

  def updateAmount(cart: Cart, discount: Double): Cart =
    cart.copy(id = cart.id, customerId = cart.customerId, amount = cart.amount - discount)

  def save(cart: Cart, storage: Storage[Cart]): Unit =
    storage.flush(cart)
}
