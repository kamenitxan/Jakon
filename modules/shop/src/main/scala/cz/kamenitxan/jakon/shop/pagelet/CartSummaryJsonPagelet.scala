package cz.kamenitxan.jakon.shop.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractJsonPagelet, Get, JsonPagelet}
import cz.kamenitxan.jakon.shop.service.CartService
import io.javalin.http.Context

import java.math.{BigDecimal, RoundingMode}
import java.sql.Connection

/**
 * JSON API endpoint returning the current cart summary for use by client-side JavaScript.
 *
 * The cart is looked up by the {@code cart_token} cookie without creating a new cart.
 * Returns zero values when the cookie is missing or no cart exists, so the endpoint is
 * safe to call on every page load including statically-generated pages.
 */
@JsonPagelet(path = "/api/cart")
class CartSummaryJsonPagelet extends AbstractJsonPagelet {

	private val CART_COOKIE = "cart_token"

	/**
	 * Returns a summary of the current cart contents.
	 *
	 * @return [[CartSummaryResponse]] with totalQuantity, itemCount, subtotal and subtotalFormatted
	 */
	@Get(path = "/summary")
	def summary(ctx: Context)(implicit conn: Connection): CartSummaryResponse = {
		val tokenOpt = Option(ctx.cookie(CART_COOKIE)).filter(_.nonEmpty)
		tokenOpt.flatMap(CartService.findCartByToken) match {
			case None =>
				CartSummaryResponse(0, 0, BigDecimal.ZERO, "0")
			case Some(cart) =>
				val items = CartService.getItems(cart.id)
				val totalQuantity = items.map(_.quantity).sum
				val itemCount = items.size
				val subtotal = CartService.getTotalPrice(items)
				val subtotalFormatted = formatPrice(subtotal)
				CartSummaryResponse(totalQuantity, itemCount, subtotal, subtotalFormatted)
		}
	}

	private def formatPrice(amount: BigDecimal): String = {
		s"${amount.setScale(2, RoundingMode.HALF_UP)} Kč"
	}
}

/**
 * @param totalQuantity total number of units across all items (sum of quantities)
 * @param itemCount     number of distinct product lines in the cart
 * @param subtotal      subtotal as a numeric value
 * @param subtotalFormatted subtotal formatted as a localised string (e.g. "1 490,00 Kč")
 */
case class CartSummaryResponse(
	totalQuantity: Int,
	itemCount: Int,
	subtotal: BigDecimal,
	subtotalFormatted: String
)
